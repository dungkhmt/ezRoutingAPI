package com.dailyopt.VRPLoad3D.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import com.dailyopt.VRPLoad3D.model.*;
import localsearch.domainspecific.packing.entities.*;
import localsearch.domainspecific.packing.models.*;
import localsearch.domainspecific.packing.algorithms.*;

public class RoutingLoad3DSolver {
	Request[] requests = null;
	GoogleMapsQuery G = new GoogleMapsQuery();
	double[][] distance;
	HashMap<Point, Request> mPoint2Request = null;
	HashMap<Request, ArrayList<Item3D>> mRequest2Item3D;
	//HashMap<Item3D, Request> mItem2Request;
	//HashMap<Integer, Item3D> mID2Item3D;
	HashMap<Integer, Item> mID2Item;
	HashMap<String, Integer> mCode2Index;

	RoutingLoad3DInput input;

	// modelling
	Model3D[] loadModels;

	VRManager mgr;
	VarRoutesVR XR;
	ConstraintSystemVR CS;
	ArcWeightsManager awm;
	IFunctionVR obj;
	int nbVehicles;
	ArrayList<Point> clientPoints;
	ArrayList<Point> startPoints;
	ArrayList<Point> endPoints;
	ArrayList<Point> allPoints;
	Point depot;
	// double lat_depot = 21.028811;
	// double lng_depot = 105.778229;

	ArrayList<GreedyConstructiveOrderLoadConstraint> containerSolvers;

	public void mapping() {
		
		nbVehicles = input.getVehicles().length;

		mPoint2Request = new HashMap<Point, Request>();

		this.nbVehicles = nbVehicles;
		clientPoints = new ArrayList<Point>();
		startPoints = new ArrayList<Point>();
		endPoints = new ArrayList<Point>();
		allPoints = new ArrayList<Point>();
		for (int i = 0; i < requests.length; i++) {
			Point p = new Point(i);
			clientPoints.add(p);
			allPoints.add(p);
			mPoint2Request.put(p, requests[i]);
		}

		int iddepot = requests.length;
		for (int k = 1; k <= nbVehicles; k++) {
			Point s = new Point(iddepot);
			Point e = new Point(iddepot);
			startPoints.add(s);
			endPoints.add(e);
			allPoints.add(s);
			allPoints.add(e);
		}

		awm = new ArcWeightsManager(allPoints);
		for (int i = 0; i < requests.length; i++) {
			Point pi = clientPoints.get(i);
			Request req = requests[i];
			for (int j = 0; j < requests.length; j++) {
				Point pj = clientPoints.get(j);
				awm.setWeight(pi, pj, distance[i][j]);
			}
			for (Point s : startPoints) {
				double d = G.getApproximateDistanceMeter(req.getLat(), req
						.getLng(), input.getDepot().getLat(), input.getDepot()
						.getLng());
				awm.setWeight(pi, s, d);
				awm.setWeight(s, pi, d);
			}
			for (Point e : endPoints) {
				double d = G.getApproximateDistanceMeter(req.getLat(), req
						.getLng(), input.getDepot().getLat(), input.getDepot()
						.getLng());
				awm.setWeight(pi, e, d);
				awm.setWeight(e, pi, d);
			}
		}

	}
	public RoutingLoad3DInput getInput(){
		return input;
	}
	public void solve() {
		mapping();
		stateModel();
		search();
	}
	public String name(){
		return "RoutingLoad3DSolver";
	}
	public void search() {
		containerSolvers = new ArrayList<GreedyConstructiveOrderLoadConstraint>();

		for (int i = 0; i < loadModels.length; i++) {
			GreedyConstructiveOrderLoadConstraint GCLC = new GreedyConstructiveOrderLoadConstraint(
					loadModels[i], this);
			GCLC.init();

			containerSolvers.add(GCLC);
		}

		HashSet<Integer> cand_requests = new HashSet<Integer>();
		for (int i = 0; i < requests.length; i++)
			cand_requests.add(i);

		int k = 1;
		boolean finished = false;
		//Point curPoint = XR.startPoint(k);
		while (!finished) {

			double minDis = Integer.MAX_VALUE;
			int sel_i = -1;
			int sel_vehicle = -1;
			Point sel_point = null;
			
			for (int i : cand_requests) {
				Point p = clientPoints.get(i);
				Request r = requests[i];
				ArrayList<Item3D> items = getItems(r);

				for (int v = 0; v < input.getVehicles().length; v++) {
					GreedyConstructiveOrderLoadConstraint GCLC = containerSolvers
							.get(v);

					Point lastPoint = XR.prev(XR.endPoint(v+1));
					if (GCLC.tryLoad(items)) {
						double d = awm.getDistance(lastPoint, p);
						if (d < minDis) {
							minDis = d;
							sel_i = i;
							sel_vehicle = v;
							sel_point = lastPoint;
						}
					} else {

					}
				}
			}
			if (sel_i == -1) {
				System.out.println(name() + "::solve, cannot register the request#######################");
				break;
			}

			// perform the move
			Point p = clientPoints.get(sel_i);
			Request r = requests[sel_i];
			ArrayList<Item3D> items = getItems(r);
			containerSolvers.get(sel_vehicle).load(items);
			mgr.performAddOnePoint(p, sel_point);

			System.out.println("addPoint " + p.ID + " after " + sel_point.ID
					+ " on vehicle " + sel_vehicle + " --> XR = " + XR.toString() + ", totalCost = "
					+ obj.getValue());
			cand_requests.remove(sel_i);
			if (cand_requests.size() == 0) {
				finished = true;
			}
			System.out
					.println("---------------------------------------------------");
		}

		for (int i = 0; i < containerSolvers.size(); i++) {
			GreedyConstructiveOrderLoadConstraint gclc = containerSolvers
					.get(i);
			System.out.println("Xe " + (i + 1) + ":");
			// gclc.printSolution();
			ArrayList<Move3D> solution = gclc.solution;
			// printSolutionLoad(solution);

			System.out.println("------------------------------------");
		}
		for (int ki = 1; ki <= XR.getNbRoutes(); ki++) {
			System.out.println("Xe " + ki + " : ");
			for (Point p = XR.next(XR.startPoint(ki)); p != XR.endPoint(ki); p = XR
					.next(p)) {
				Request r = mPoint2Request.get(p);
				System.out.println("OrderID " + r.getOrderID() + ", Address : "
						+ r.getAddr());
			}
			System.out.println("---------------------------");
		}
	}

	public ArrayList<Item3D> getItems(Request r) {
		ArrayList<Item3D> items = mRequest2Item3D.get(r);
		return items;
	}

	public void stateModel() {
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		CS = new ConstraintSystemVR(mgr);
		for (int k = 1; k <= nbVehicles; k++) {
			Point s = startPoints.get(k - 1);
			Point e = endPoints.get(k - 1);
			XR.addRoute(s, e);
		}
		for (Point p : clientPoints)
			XR.addClientPoint(p);

		obj = new TotalCostVR(XR, awm);

		mgr.close();

		int total = 0;
		for (int i = 0; i < requests.length; i++) {
			for (int j = 0; j < requests[i].getItems().length; j++) {
				total += requests[i].getItems()[j].getQuantity();
			}
		}
		// Container3D container = new Container3D(200, 400, 200);
		Item3D[] items = new Item3D[total];

		int idx = -1;
		for (int i = 0; i < requests.length; i++) {
			Request r = requests[i];
			ArrayList<Item3D> I3D = getItems(r);
			for (int j = 0; j < I3D.size(); j++) {
				idx++;
				items[idx] = I3D.get(j);
			}
		}
		loadModels = new Model3D[input.getVehicles().length];
		for (int i = 0; i < loadModels.length; i++) {
			Container3D container = new Container3D(
					input.getVehicles()[i].getWidth(),
					input.getVehicles()[i].getLength(),
					input.getVehicles()[i].getHeight());

			loadModels[i] = new Model3D(container, items);
		}

	}

	public RoutingLoad3DSolution solve(RoutingLoad3DInput input) {
		this.input = input;
		requests = input.getRequests();
		int n = requests.length;
		distance = new double[n + 1][n + 1];
		mCode2Index = new HashMap<String, Integer>();

		for (int i = 0; i < n; i++) {
			Request r = requests[i];
			mCode2Index.put(r.getOrderID(), i);
		}
		mCode2Index.put(input.getDepot().getCode(), n);

		for (int i = 0; i < input.getDistances().length; i++) {
			DistanceElement de = input.getDistances()[i];
			String srcCode = de.getSrcCode();
			String destCode = de.getDestCode();
			int i1 = mCode2Index.get(srcCode);
			int i2 = mCode2Index.get(destCode);
			distance[i1][i2] = de.getDistance();
		}
		
		mRequest2Item3D = new HashMap<Request, ArrayList<Item3D>>();
		mID2Item = new HashMap<Integer, Item>();
		int itemID = 0;
		for(int i = 0; i < input.getRequests().length; i++){
			Request r = input.getRequests()[i];
			ArrayList<Item3D> item3D = new ArrayList<Item3D>();
			for(int j = 0; j < r.getItems().length; j++){
				Item I = r.getItems()[j];
				for(int k = 0; k < I.getQuantity(); k++){
					itemID++;
					Item3D I3 = new Item3D(itemID,I.getW(),I.getL(),I.getH());
					item3D.add(I3);
					mID2Item.put(itemID, I);
				}
			}
			mRequest2Item3D.put(r, item3D);
			
		}
		
		
		solve();
		
		RoutingSolution[] routes = new RoutingSolution[input.getVehicles().length];
		for(int v = 0; v < routes.length; v++){
			
			ArrayList<RoutingElement> re = new ArrayList<RoutingElement>();
			re.add(new RoutingElement("depot", "depot", input.getDepot().getLat() + "," + input.getDepot().getLng()));
			for(Point p = XR.next(XR.startPoint(v+1)); p != XR.endPoint(v+1); p = XR.next(p)){
				Request r = mPoint2Request.get(p);
				RoutingElement e = new RoutingElement(r.getOrderID(), r.getAddr(),r.getLat() + "," + r.getLng());
				re.add(e);
			}
			re.add(new RoutingElement("depot", "depot", input.getDepot().getLat() + "," + input.getDepot().getLng()));
			
			
			RoutingElement[] are = new RoutingElement[re.size()];
			for(int i = 0; i < re.size(); i++) are[i] = re.get(i);
			routes[v] = new RoutingSolution(are);
		}
		
		LoadingSolution[] loads = new LoadingSolution[input.getVehicles().length];
		for(int v = 0; v < loads.length; v++){
			Vehicle vehicle = input.getVehicles()[v];
			GreedyConstructiveOrderLoadConstraint GCLC = containerSolvers.get(v);
			ArrayList<Move3D> moves = GCLC.solution;
			LoadingElement[] le = new LoadingElement[moves.size()];
			for(int i = 0; i < moves.size(); i++){
				Move3D m = moves.get(i);
				Item I = mID2Item.get(m.getItemID());
				String description = "";
				String sw = "Sat mep trai";
				if (m.getPosition().getX_w() > 0) {
					for (int j = 0; j < i; j++) {
						Move3D mj = moves.get(j);
						if (mj.getPosition().getX_w() + mj.getW() == m
								.getPosition().getX_w()) {
							sw = "Ben trai item " + mj.getItemID();
						}
					}
				}
				String sl = "Sat vach sau";
				if (m.getPosition().getX_l() > 0) {
					for (int j = 0; j < i; j++) {
						Move3D mj = moves.get(j);
						if (mj.getPosition().getX_l() + mj.getL() == m
								.getPosition().getX_l()) {
							sl = "Truoc item " + mj.getItemID();
						}
					}
				}
				String sh = "Tren san";
				if (m.getPosition().getX_h() > 0) {
					for (int j = 0; j < i; j++) {
						Move3D mj = moves.get(j);
						if (mj.getPosition().getX_h() + mj.getH() == m
								.getPosition().getX_h()) {
							sh = "Tren item " + mj.getItemID();
						}
					}
				}

				description = sw + ", " + sl + ", " + sh;
				
				LoadingElement e = new LoadingElement(I,m.getPosition().getX_w(),m.getPosition().getX_l(),m.getPosition().getX_h(),
						description);
				le[i] = e;
			}
			LoadingSolution ld = new LoadingSolution(vehicle,le);
			loads[v] = ld;
		}
		
		RoutingLoad3DSolution sol = new RoutingLoad3DSolution(routes,loads);
		return sol;
	}
}
