package com.kse.ezRoutingAPI.dichung.controller;

import localsearch.domainspecific.vehiclerouting.apps.sharedaride.Constraint.CPickupDeliveryOfGoodVR;
import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IConstraintVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.Implicate;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.eq.Eq;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.leq.Leq;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.timewindows.CEarliestArrivalTimeVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedNodeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.ConstraintViolationsVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.IndexOnRoute;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.functions.RouteIndex;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.minus.Minus;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.EarliestArrivalTimeVR;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyCrossExchangeMoveExplorer;
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
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kse.ezRoutingAPI.dichung.model.GlobalSharedTaxiInput;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiInput;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiRequest;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiRoute;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiRouteElement;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiSolution;
import com.kse.ezRoutingAPI.dichung.service.DichungService;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripElement;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripInput;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripRoute;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripSolution;
import com.kse.ezRoutingAPI.dichungduongdai.service.SharedLongTripService;
import com.kse.utils.DateTimeUtils;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DichungAPIController {
	String ROOT_DIR = "C:/ezRoutingAPIRoot/";
	public String name() {
		return "DichungAPIController";
	}


	public void writeRequest(SharedTaxiInput input, SharedTaxiSolution sol){
		try{
			DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
			Date date = new Date();
			
			//System.out.println(dateFormat.format(date)); //2014/08/06 15:59:48
			String dt = dateFormat.format(date);
			String[] s  = dt.split(":"); 
			
			String dir = ROOT_DIR + "/" + DateTimeUtils.currentDate();
			File f = new File(dir);
			if(!f.exists()){
				f.mkdir();
			}
			
			//String fn = ROOT_DIR + "dichungairport-requests-" + s[0] + s[1] + s[2] + "-" + s[3] + s[4] + s[5] + ".txt";
			String fn = dir + "/dichungairport-requests-" + s[0] + s[1] + s[2] + "-" + s[3] + s[4] + s[5] + ".txt";
			System.out.println(name() + "::writeRequest to file " + fn);
			PrintWriter out = new PrintWriter(fn);
			out.println("airport: (address \t lalng)");
			out.println(input.getAirportAddress() + "\t" + input.getAirportPos());
			
			out.println("requests: (chunkName\t pickup_address \t pickup_latlng \t delivery_address \t delivery_latlng \t depart_time \t number_passengers)");
			
			for(int i= 0; i<input.getRequests().length; i++){
				SharedTaxiRequest r = input.getRequests()[i];
				out.println(r.getChungName() + "\t" + r.getPickupAddress() + "\t" + r.getPickupPos() + "\t" +
						r.getDeliveryAddress() + "\t" + r.getDeliveryPos() + "\t" + r.getDepartTime() + "\t" + r.getNumberPassengers());
			}
			
			ObjectMapper mapper = new ObjectMapper();
			String jsonsol = mapper.writeValueAsString(sol);
			String jsoninput = mapper.writeValueAsString(input);
			
			out.println("input: JSON");
			out.println(jsoninput);
			
			out.println("solution: JSON");
			out.println(jsonsol);
			
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void writeGlobalRequest(GlobalSharedTaxiInput input){
		try{
			DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
			Date date = new Date();
			
			//System.out.println(dateFormat.format(date)); //2014/08/06 15:59:48
			String dt = dateFormat.format(date);
			String[] s  = dt.split(":"); 
			
			String dir = ROOT_DIR + "/" + DateTimeUtils.currentDate();
			File f = new File(dir);
			if(!f.exists()){
				f.mkdir();
			}
			
			//String fn = ROOT_DIR + "dichungairport-requests-" + s[0] + s[1] + s[2] + "-" + s[3] + s[4] + s[5] + ".txt";
			String fn = dir + "/dichung-global-requests-" + s[0] + s[1] + s[2] + "-" + s[3] + s[4] + s[5] + ".txt";
			System.out.println(name() + "::writeRequest to file " + fn);
			PrintWriter out = new PrintWriter(fn);
			
			ObjectMapper mapper = new ObjectMapper();
			String jsoninput = mapper.writeValueAsString(input);
			
			//out.println("input: JSON");
			out.println(jsoninput);
			
	
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@RequestMapping(value = "/shared-a-ride-plan", method = RequestMethod.POST)
	public SharedTaxiSolution[] computeSharedTaxiSolution(HttpServletRequest request,
			@RequestBody GlobalSharedTaxiInput input) {
		String path = request.getServletContext().getRealPath("ezRoutingAPIROOT");
		System.out.println(name() + "::computeSharedTaxiSolution, path = " + path);
		
		writeGlobalRequest(input);
		
		// compute solution for air port requests
		SharedTaxiInput[] requestAirports = input.getAirportInput();
		SharedTaxiSolution[] sa = new SharedTaxiSolution[requestAirports.length];
		DichungService dichungService = new DichungService();
		for(int i = 0; i < requestAirports.length; i++){
			sa[i] = dichungService.computeSharedTaxiHanoiNoiBaiSolution(requestAirports[i]);
		}
		
		// compute solution for long trip requests
		SharedLongTripInput[] requestLongTrips = input.getLongtripInput();
		SharedLongTripSolution[] sl = new SharedLongTripSolution[requestLongTrips.length];
		SharedLongTripService dichungDuongdaiService = new SharedLongTripService();
		for(int i = 0; i < requestLongTrips.length; i++){
			sl[i] = dichungDuongdaiService.computeSharedLongTrip(requestLongTrips[i]);
		}
		
		// convert long trip solutions into unique format of shared a ride solution
		SharedTaxiSolution[] tmp = new SharedTaxiSolution[sl.length];
		for(int i = 0; i < sl.length; i++){
			SharedLongTripRoute[] r = sl[i].getRoutes();
			SharedTaxiRoute[] tr = new SharedTaxiRoute[r.length];
			for(int j = 0; j < r.length; j++){
				SharedLongTripElement[] re = r[j].getRouteElements();
				SharedTaxiRouteElement[] te = new SharedTaxiRouteElement[re.length];
				for(int k = 0; k < re.length; k++){
					te[k] = new SharedTaxiRouteElement(re[k].getTicketCode(),
							re[k].getPickupAddress(),
							re[k].getDeliveryAddress(),
							"-",
							re[k].getDepartTime(),
							"-",
							"-",
							"-",
							"-",
							"-"
							);
				}
				tr[j] = new SharedTaxiRoute(te, r[j].getTaxiType(), r[j].getNbPeople(), "-");
			}
			tmp[i] = new SharedTaxiSolution(-1, -1, requestLongTrips[i].getRequests().length, tr);
		}
		
		SharedTaxiSolution[] sol  = new SharedTaxiSolution[sa.length+tmp.length];
		for(int i = 0; i < sa.length; i++)
			sol[i] = sa[i];
		for(int i = 0; i < tmp.length; i++)
			sol[i + sa.length] = tmp[i];
		return sol;
	}
	
	
	@RequestMapping(value = "/shared-taxi-plan-dichung-airport", method = RequestMethod.POST)
	public SharedTaxiSolution computeSharedTaxiSolution(HttpServletRequest request,
			@RequestBody SharedTaxiInput input) {
		String path = request.getServletContext().getRealPath("ezRoutingAPIROOT");
		System.out.println(name() + "::computeSharedTaxiSolution, path = " + path);
		DichungService dichungService = new DichungService();
		SharedTaxiSolution sol = dichungService.computeSharedTaxiHanoiNoiBaiSolution(input);
		
		writeRequest(input, sol);
		
		return sol;
		/*
		HashMap<SharedTaxiRequest, Integer> mRequest2ID = new HashMap<SharedTaxiRequest, Integer>();
		SharedTaxiRequest[] requests = input.getRequests();
		int N = requests.length;
		for (int i = 0; i < requests.length; i++)
			mRequest2ID.put(requests[i], i);

		GoogleMapsQuery G = new GoogleMapsQuery();
		
		int[][] travelTimes = new int[N][N];// travelTimes[i][j] is the travel
											// time from pickup point of request
											// i to pickup point of request j
		for (int i = 0; i < requests.length; i++) {
			for (int j = 0; j < requests.length; j++) {
				int t = G.getTravelTime(requests[i].getPickupAddress(),
						requests[j].getPickupAddress(), "driving");
				travelTimes[i][j] = t;
				System.out.println(name() + "::computeSharedTaxiSolution, TT[" + i + "," + j + "] = " + travelTimes[i][j]);
				if(travelTimes[i][j] < 0){
					System.out.println(name() + "::computeSharedTaxiSolution exception from " + 
				requests[i].getPickupAddress() + " to " + requests[j].getPickupAddress());
					System.exit(-1);
				}
			}
		}
		
		
		HashMap<Point, SharedTaxiRequest> mPoint2Request = new HashMap<Point, SharedTaxiRequest>();
		
		int K = requests.length;// init nbVehicles
		ArrayList<Point> startPoints = new ArrayList<Point>();
		ArrayList<Point> endPoints = new ArrayList<Point>();
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		ArrayList<Point> allPoints = new ArrayList<Point>();

		HashMap<Point, Integer> earliestAllowedArrivalTime = new HashMap<Point, Integer>();
		HashMap<Point, Integer> serviceDuration = new HashMap<Point, Integer>();
		HashMap<Point, Integer> latestAllowedArrivalTime = new HashMap<Point, Integer>();

		int id = -1;

		long minTimePoint = Integer.MAX_VALUE;
		for(int i = 0; i < requests.length; i++){
			long t = DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
			minTimePoint = minTimePoint < t ? minTimePoint : t;
			t = DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
			minTimePoint = minTimePoint < t ? minTimePoint : t;
		}
		int dt = 10000;
		int pickupDuration = 60;// 60 seconds
		int D = 10000;// distance from start/end poitns to each client point
		
		for (int i = 0; i < K; i++) {
			id++;
			Point p = new Point(id);
			clientPoints.add(p);
			
			long et = DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
			long lt = DateTimeUtils.dateTime2Int(requests[i].getLateDeliveryDateTime());
			int converted_et = (int)(et - minTimePoint) + dt;
			int converted_lt = (int)(lt - minTimePoint) + dt;
			earliestAllowedArrivalTime.put(p, converted_et);
			latestAllowedArrivalTime.put(p,converted_lt);
			serviceDuration.put(p, pickupDuration);// pickup duration is assumed to be 1 minute
			
			allPoints.add(p);
			
			mPoint2Request.put(p, requests[i]);
		}
		
		
		for (int i = 0; i < K; i++) {
			id++;
			Point s = new Point(id);
			startPoints.add(s);
			allPoints.add(s);
			
			id++;
			Point t = new Point(id);
			endPoints.add(t);
			allPoints.add(t);
		}

		for(Point s: startPoints){
			earliestAllowedArrivalTime.put(s, 0);
			latestAllowedArrivalTime.put(s,Integer.MAX_VALUE);
			serviceDuration.put(s, 0);// pickup duration is assumed to be 0
		}
		for(Point t: endPoints){
			earliestAllowedArrivalTime.put(t, 0);
			latestAllowedArrivalTime.put(t,Integer.MAX_VALUE);
			serviceDuration.put(t, 0);// pickup duration is assumed to be 0
		}
		

		ArcWeightsManager awm = new ArcWeightsManager(allPoints);
		for (int i = 0; i < clientPoints.size(); i++) {
			for (int j = 0; j < clientPoints.size(); j++) {
				awm.setWeight(clientPoints.get(i), clientPoints.get(j),
						travelTimes[i][j]);
			}
		}
		for (Point p : clientPoints) {
			for (Point s : startPoints) {
				awm.setWeight(s, p, D);
				awm.setWeight(p, s, D);
				
			}
			for (Point t : endPoints) {
				awm.setWeight(p, t, D);
				awm.setWeight(t, p, D);
			}
		}
		for(Point s: startPoints){
			for(Point t: endPoints)
				awm.setWeight(s, t, 0);
		}
		
		int capacity = 0;
		for(int i = 0; i < input.getVehicleCapacities().length; i++)
			capacity = capacity < input.getVehicleCapacities()[i] ? input.getVehicleCapacities()[i] : capacity;
		
			
			
		NodeWeightsManager nwm = new NodeWeightsManager(allPoints);
		for(int i = 0; i < clientPoints.size(); i++)
			nwm.setWeight(clientPoints.get(i), requests[i].getNumberPassengers());
		for(Point s: startPoints)
			nwm.setWeight(s, 0);
		for(Point t: endPoints)
			nwm.setWeight(t, 0);
		
		
		// model
		VRManager mgr = new VRManager();
		VarRoutesVR XR = new VarRoutesVR(mgr);
		for(int i = 0; i < startPoints.size(); i++){
			XR.addRoute(startPoints.get(i), endPoints.get(i));
		}
		for(Point p: clientPoints)
			XR.addClientPoint(p);
		
		ConstraintSystemVR CS = new ConstraintSystemVR(mgr);
		
		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		AccumulatedWeightNodesVR awn = new AccumulatedWeightNodesVR(XR, nwm);
		
		IFunctionVR[] d =  new IFunctionVR[K];// d[i] is the distance of route i+1
		IFunctionVR[] w = new IFunctionVR[K];// w[i] is the accumulated demand on route i+1
		for(int i = 0; i < K; i++){
			Point t = XR.endPoint(i+1);// start point of route i+1
			d[i] = new AccumulatedEdgeWeightsOnPathVR(awe, t); 
			w[i] = new AccumulatedNodeWeightsOnPathVR(awn, t);
			CS.post(new Leq(w[i],capacity));
			CS.post(new Leq(d[i],input.getMaxWaitTime()));
		}		
		
		EarliestArrivalTimeVR eat = new EarliestArrivalTimeVR(XR, awm,
				earliestAllowedArrivalTime, serviceDuration);

		CEarliestArrivalTimeVR twCtrs = new CEarliestArrivalTimeVR(eat,
				latestAllowedArrivalTime);
		CS.post(twCtrs);

		IFunctionVR obj = new TotalCostVR(XR, awm);
		LexMultiFunctions F = new LexMultiFunctions();
		F.add(new ConstraintViolationsVR(CS));
		F.add(obj);
		
		
		mgr.close();

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

		GenericLocalSearch se = new GenericLocalSearch(mgr);
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.setMaxStable(50);

		se.search(5, input.getMaxTime());

		ArrayList<SharedTaxiRoute> routes = new ArrayList<SharedTaxiRoute>();
		
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			Point s = XR.startPoint(k);
			Point t = XR.endPoint(k);
			if(XR.next(s) != t){
				ArrayList<Point> points = new ArrayList<Point>();
				for(Point x = XR.next(s); x != t; x = XR.next(x)){
					points.add(x);
				}
				String[] ticketCodes = new String[points.size()];
				for(int i = 0; i < points.size(); i++){
					SharedTaxiRequest req = mPoint2Request.get(points.get(i));
					ticketCodes[i] = req.getTicketCode();
				}
				SharedTaxiRoute r = new SharedTaxiRoute(ticketCodes);
				routes.add(r);
			}
		}
		SharedTaxiRoute[] A = new SharedTaxiRoute[routes.size()];
		for(int i = 0; i < routes.size(); i++){
			A[i] = routes.get(i);
		}
		return new SharedTaxiSolution(A);
		*/
	}

}
