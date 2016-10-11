package com.kse.ezRoutingAPI.deliverygoods.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IConstraintVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.leq.Leq;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.timewindows.CEarliestArrivalTimeVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedNodeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.ConstraintViolationsVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.functions.MaxVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.EarliestArrivalTimeVR;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyCrossExchangeMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyKPointsMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOnePointMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOrOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOrOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove3Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove4Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove5Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove6Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove7Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove8Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove3Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove4Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove5Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove6Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove7Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove8Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.INeighborhoodExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.search.GenericLocalSearch;

import com.kse.ezRoutingAPI.deliverygoods.model.DeliveryRequest;
import com.kse.ezRoutingAPI.deliverygoods.model.Shipper;

class DeliveryGoodSearch extends GenericLocalSearch{
	private DeliveryRequest[] req;
	private HashMap<DeliveryRequest, Point> mReq2Delivery;
	
	public DeliveryGoodSearch(VRManager mgr, DeliveryRequest[] req, 
			HashMap<DeliveryRequest, Point> mReq2Delivery){
		super(mgr);
		this.req = req;
		this.mReq2Delivery = mReq2Delivery;
		
	}
	
	public void generateInitialSolution(){
		VarRoutesVR XR = mgr.getVarRoutesVR();
		int k = 0;
		for(int i = 0; i < req.length; i++){
			k = k+1;
			if(k > XR.getNbRoutes()) k = 1;
			Point p = XR.prev(XR.endPoint(k));
			Point delivery = mReq2Delivery.get(req[i]);
			mgr.performAddOnePoint(delivery, p);
			System.out.println(name() + "::generateInitialSolution, addPoint " + delivery.ID + ", p = " + p.ID + ", k = " + k + ", XR = " + XR.toString());
		}
	}
	
}
public class DeliveryGoodSolver {
	
	DeliveryRequest[] req;// = input.getRequests();
	Shipper[] shippers;// = input.getTrucks();

	ArrayList<Point> startPoints;// = new ArrayList<Point>();
	ArrayList<Point> endPoints;// = new ArrayList<Point>();
	ArrayList<Point> deliveryPoints;// = new ArrayList<Point>();
	ArrayList<Point> allPoints;// = new ArrayList<Point>();
	ArrayList<Point> clientPoints;// = new ArrayList<Point>();
	
	HashMap<Point, DeliveryRequest> mPoint2Request;// = new HashMap<Point, PickupDeliveryRequest>();
	HashMap<DeliveryRequest, Point> mReq2Delivery;// = new HashMap<PickupDeliveryRequest, Point>();
	HashMap<Point, Shipper> mPoint2Shipper;// = new HashMap<Point, Truck>();

	ArcWeightsManager distances;// = new ArcWeightsManager(allPoints);
	ArcWeightsManager travelTimes;// = new ArcWeightsManager(allPoints);
	NodeWeightsManager demand;// = new NodeWeightsManager(allPoints);

	HashMap<Point, Integer> earliestAllowedArrivalTime;// = new HashMap<Point, Integer>();
	HashMap<Point, Integer> serviceDuration;// = new HashMap<Point, Integer>();
	HashMap<Point, Integer> latestAllowedArrivalTime;// = new HashMap<Point, Integer>();

	// modelling
	VRManager mgr;// = new VRManager();
	VarRoutesVR XR;// = new VarRoutesVR(mgr);
	ConstraintSystemVR CS;// = new ConstraintSystemVR(mgr);
	AccumulatedWeightEdgesVR awe;// = new AccumulatedWeightEdgesVR(XR, distances);
	AccumulatedWeightNodesVR awn;// = new AccumulatedWeightNodesVR(XR, demand);
	AccumulatedWeightEdgesVR arrivalTime;// = new AccumulatedWeightEdgesVR(XR,	travelTimes);

	HashMap<Point, IFunctionVR> accDemand;// = new HashMap<Point, IFunctionVR>();
	HashMap<Point, IFunctionVR> accDistance;// = new HashMap<Point, IFunctionVR>();


	EarliestArrivalTimeVR eat;
	IFunctionVR obj;// = new TotalCostVR(XR, distances);
	LexMultiFunctions F;// = new LexMultiFunctions();

	public void computeSolution(){
		// model
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		CS = new ConstraintSystemVR(mgr);

		for (int i = 0; i < startPoints.size(); i++) {
			XR.addRoute(startPoints.get(i), endPoints.get(i));
		}

		for (int i = 0; i < deliveryPoints.size(); i++) {
			XR.addClientPoint(deliveryPoints.get(i));
		}

		int K = XR.getNbRoutes();

		awe = new AccumulatedWeightEdgesVR(XR, distances);
		awn = new AccumulatedWeightNodesVR(XR, demand);
		arrivalTime = new AccumulatedWeightEdgesVR(XR,
				travelTimes);

		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			awe.setAccumulatedWeightStartPoint(k, 0);
			arrivalTime.setAccumulatedWeightStartPoint(k, 0);// start time point of vehicle k
			awn.setAccumulatedWeightStartPoint(k, 0);
		}
		accDemand = new HashMap<Point, IFunctionVR>();
		accDistance = new HashMap<Point, IFunctionVR>();
		for (Point v : allPoints) {
			IFunctionVR dv = new AccumulatedNodeWeightsOnPathVR(awn, v);
			accDemand.put(v, dv);
			IFunctionVR disv = new AccumulatedEdgeWeightsOnPathVR(awe, v);
			accDistance.put(v, disv);
		}
		IFunctionVR[] distanceOfRoute = new IFunctionVR[XR.getNbRoutes()];
		
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			Point s = XR.startPoint(k);
			Point e = XR.endPoint(k);
			Shipper sh = mPoint2Shipper.get(s);
			IFunctionVR f = accDemand.get(e);
			CS.post(new Leq(f,sh.getWeight()));
			
			
			distanceOfRoute[k-1] = accDistance.get(e);
		}

		eat = new EarliestArrivalTimeVR(XR, travelTimes,
				earliestAllowedArrivalTime, serviceDuration);

		CEarliestArrivalTimeVR twCtrs = new CEarliestArrivalTimeVR(eat,
				latestAllowedArrivalTime);
		CS.post(twCtrs);

		obj = new TotalCostVR(XR, distances);
		F = new LexMultiFunctions();
		IFunctionVR maxDistanceRoute = new MaxVR(distanceOfRoute);
		F.add(new ConstraintViolationsVR(CS));
		F.add(maxDistanceRoute);
		//F.add(obj);
		

		mgr.close();

		/*
		Point last = XR.startPoint(1);
		int I = req.length/2;
		for(int i = 0; i < I; i++){
			Point p = mReq2Pickup.get(req[i]);
			Point d = mReq2Delivery.get(req[i]);
			mgr.performAddOnePoint(p, last);
			last = p;
			mgr.performAddOnePoint(d, last);
			last = d;
		}
		last = XR.startPoint(2);
		for(int i = I; i < req.length; i++){
			Point p = mReq2Pickup.get(req[i]);
			Point d = mReq2Delivery.get(req[i]);
			mgr.performAddOnePoint(p, last);
			last = p;
			mgr.performAddOnePoint(d, last);
			last = d;
		}
		System.out.println("XR = " + XR.toString() + ", F = " + F.getValues().toString());
		//for(Point p: allPoints){
		//	System.out.println("earliestArrivalTime = " + eat.getEarliestArrivalTime(p) + ", latestAllowed = " + latestAllowedArrivalTime.get(p));
		//}
		
		
		if(true)return;
		*/
		
		/*
		 * XR.setRandom(); for(Point p: requestPoints){
		 * System.out.println("routeOfPoint " + p.ID + " = " +
		 * routeOfPoint.get(p).getValue() + ", index of point " + p.ID + " = " +
		 * indexOfPointOnRoute.get(p).getValue()); } System.out.println("XR = "
		 * + XR.toString() + ", F = " + F.getValues().toString());
		 */

		ArrayList<INeighborhoodExplorer> NE = new ArrayList<INeighborhoodExplorer>();
		
		NE.add(new GreedyOnePointMoveExplorer(XR, F));

		
		NE.add(new GreedyOrOptMove1Explorer(XR, F));
		NE.add(new GreedyOrOptMove2Explorer(XR, F));
		NE.add(new GreedyThreeOptMove1Explorer(XR, F));
		NE.add(new GreedyThreeOptMove2Explorer(XR, F));
		NE.add(new GreedyThreeOptMove3Explorer(XR, F));
		NE.add(new GreedyThreeOptMove4Explorer(XR, F));
		NE.add(new GreedyThreeOptMove5Explorer(XR, F));
		NE.add(new GreedyThreeOptMove6Explorer(XR, F));
		NE.add(new GreedyThreeOptMove7Explorer(XR, F));
		NE.add(new GreedyThreeOptMove8Explorer(XR, F));
		NE.add(new GreedyTwoOptMove1Explorer(XR, F));
		NE.add(new GreedyTwoOptMove2Explorer(XR, F));
		NE.add(new GreedyTwoOptMove3Explorer(XR, F));
		NE.add(new GreedyTwoOptMove4Explorer(XR, F));
		NE.add(new GreedyTwoOptMove5Explorer(XR, F));
		NE.add(new GreedyTwoOptMove6Explorer(XR, F));
		NE.add(new GreedyTwoOptMove7Explorer(XR, F));
		NE.add(new GreedyTwoOptMove8Explorer(XR, F));
		// NE.add(new GreedyTwoPointsMoveExplorer(XR, F));
		NE.add(new GreedyCrossExchangeMoveExplorer(XR, F));
		// NE.add(new GreedyAddOnePointMoveExplorer(XR, F));
		
		
		//HashSet<Point> mandatory = new HashSet<Point>();
		//for(Point p: clientPoints) mandatory.add(p);
		//NE.add(new GreedyKPointsMoveExplorer(XR, F, 2, mandatory));

		//GenericLocalSearch se = new GenericLocalSearch(mgr);
		DeliveryGoodSearch se = new DeliveryGoodSearch(mgr, req, mReq2Delivery);
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		
		se.setMaxStable(50);

		se.search(1000, 10);
		//se.generateInitialSolution();

		for(int k = 1; k <= XR.getNbRoutes(); k++){
			Point s=XR.startPoint(k);
			Point t=XR.endPoint(k);
			System.out.println("truck[" + k + "] early start = " + earliestAllowedArrivalTime.get(s) + ", late start = " + latestAllowedArrivalTime.get(s)
					+ ", early end = " + earliestAllowedArrivalTime.get(t) + ", late end = " + latestAllowedArrivalTime.get(t));
			
		}
	
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			System.out.println("Route[" + k + "]:");
			Point p;
			for(p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
				System.out.println("Point " + p.ID + ": earliestArrivalTime = " + eat.getEarliestArrivalTime(p) + ", latestAllowed = " + latestAllowedArrivalTime.get(p) + ", travel time = " + travelTimes.getDistance(p, XR.next(p)));
			}
			System.out.println("Point " + p.ID + ": earliestArrivalTime = " + eat.getEarliestArrivalTime(p) + ", latestAllowed = " + latestAllowedArrivalTime.get(p));
		}
		
	}
}
