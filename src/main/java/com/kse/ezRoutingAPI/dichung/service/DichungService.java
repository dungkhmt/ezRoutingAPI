package com.kse.ezRoutingAPI.dichung.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.LatLng;

import org.springframework.web.bind.annotation.RequestBody;

import com.kse.ezRoutingAPI.common.algorithms.DFS;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiInput;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiRequest;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiRoute;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiRouteElement;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiSolution;
import com.kse.utils.DateTimeUtils;

class DichungSearch extends GenericLocalSearch{
	public ArrayList<Point> clientPoints;
	
	public DichungSearch(ArrayList<Point> clientPoints, VRManager mgr, LexMultiFunctions F, ArrayList<INeighborhoodExplorer> neighborhoodExplorer){
		super(mgr,F,neighborhoodExplorer);
	}
	public DichungSearch(ArrayList<Point> clientPoints, VRManager mgr){
		super(mgr);
		this.clientPoints = clientPoints;
	}
	public void generateInitialSolution(){
		System.out.println("DichungSearch::generateInitialSolution, XR = " + XR.toString());
		int nbReq = clientPoints.size()/2;
		for(int i = 0; i < mgr.getVarRoutesVR().getNbRoutes(); i++){
			Point s = XR.startPoint(i+1);
			Point p = clientPoints.get(i);
			Point d = clientPoints.get(i + nbReq);
			System.out.println("DichungSearch::generateInitialSolution, start addOnePoint(" + p.ID + "," + s.ID + ")");
			mgr.performAddOnePoint(p,s);
			mgr.performAddOnePoint(d,p);
			System.out.println("DichungSearch::generateInitialSolution, start addOnePoint(" + p.ID + "," + s.ID + "), XR = " + XR.toString());
		}
	}
	
	public void search(int maxIter, int timeLimit){
		//generateInitialSolution();
		super.search(maxIter, timeLimit);
	}
}
public class DichungService {
	public static final double APPX = 1.5;
	public static final double EPS = 10;// 10m
	
	public String name(){
		return "DichungService";
	}
	
	public SharedTaxiSolution computeSharedTaxiSolutionCluster(SharedTaxiInput input, 
			HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng,
			HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng, 
			int speed) {
		
		HashMap<SharedTaxiRequest, Integer> mRequest2ID = new HashMap<SharedTaxiRequest, Integer>();
		SharedTaxiRequest[] requests = input.getRequests();
		
		System.out.println(name() + "::computeSharedTaxiSolutionCluster there are " + requests.length + " REQUESTS");
		for(int i = 0; i < requests.length; i++){
			System.out.println(name() + "::computeSharedTaxiSolutionCluster, request at " + requests[i].getPickupAddress());
		}
		
		//all the requests has location (lat,lng) and close together in a cluster
		
		
		int N = requests.length;
		for (int i = 0; i < requests.length; i++)
			mRequest2ID.put(requests[i], i);

		GoogleMapsQuery G = new GoogleMapsQuery();
		
		int[][] travelTimes = new int[N][N];// travelTimes[i][j] is the travel
											// time from pickup point of request
											// i to pickup point of request j
		
		HashMap<SharedTaxiRequest, Integer> shortestTravelTime = new HashMap<SharedTaxiRequest, Integer>();
		for(int i = 0; i < N;i++){
			
			int t = G.getTravelTime(requests[i].getPickupAddress(), requests[i].getDeliveryAddress(), "driving");
			
			if(t < 0){
				LatLng llp = mPickup2LatLng.get(requests[i]);
				LatLng lld = mDelivery2LatLng.get(requests[i]);
				double d = G.computeDistanceHaversine(llp.lat, llp.lng, lld.lat, lld.lng);
				t = (int)(d*1000*APPX/speed);
			}
			shortestTravelTime.put(requests[i], t);
		}
		
		
		for (int i = 0; i < requests.length; i++) {
			SharedTaxiRequest ri = requests[i];
			LatLng li= mPickup2LatLng.get(ri);
			for (int j = 0; j < requests.length; j++) {
				SharedTaxiRequest rj = requests[j];
				LatLng lj = mPickup2LatLng.get(rj);
				
				/*
				double  d = G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
				d = d*1000;
				int appxt =  (int)(d*APPX/speed);// approximate travel time
				int t = G.getTravelTime(requests[i].getPickupAddress(), requests[j].getPickupAddress(), "driving");
				travelTimes[i][j] = t;
				System.out.println(name() + "::computeSharedTaxiSolution, TT[" + i + "," + j + "] = " + travelTimes[i][j]);
				if(travelTimes[i][j] < 0){
					System.out.println(name() + "::computeSharedTaxiSolution exception from " + 
				requests[i].getPickupAddress() + " to " + requests[j].getPickupAddress() + " --> use approximate time");
					//System.exit(-1);
					travelTimes[i][j] = appxt;
				}
				*/
				
				travelTimes[i][j] = G.estimateTravelTime(li.lat, li.lng, lj.lat, lj.lng, "driving", speed, APPX);
				System.out.println(name() + "::computeSharedTaxiSolutionCluster, travelTimes[" + i + "," + j + "] = " + travelTimes[i][j]);
			}
		}
		int C = 6;//input.getVehicleCapacities()[1];
		
		boolean[][] m = new boolean[N][N];// m[i][j] = 1 if request i can be followed requests j (i -> j -> destination)
		for(int i = 0; i < N; i++){
			SharedTaxiRequest ri = requests[i];
			int ei = (int)DateTimeUtils.dateTime2Int(ri.getEarlyPickupDateTime());
			int li = (int)DateTimeUtils.dateTime2Int(ri.getLatePickupDateTime());
			for(int j = 0; j < N; j++){
				SharedTaxiRequest rj = requests[j];
				int ej = (int)DateTimeUtils.dateTime2Int(rj.getEarlyPickupDateTime());
				int lj = (int)DateTimeUtils.dateTime2Int(rj.getLatePickupDateTime());
				
				boolean ok = ri.getNumberPassengers() + rj.getNumberPassengers() <= C;
				ok = ok && (ej <= ei + travelTimes[i][j] && ei + travelTimes[i][j] <= lj && travelTimes[i][j] < input.getMaxWaitTime()||
						ej <= li + travelTimes[i][j] && li + travelTimes[i][j] <= lj && travelTimes[i][j] < input.getMaxWaitTime());
						//ei <= ej + travelTimes[j][i] && ej + travelTimes[j][i] <= li && travelTimes[j][i] < input.getMaxWaitTime() ||
						//ei <= ej + travelTimes[j][i] && ej + travelTimes[j][i] <= li && travelTimes[j][i] < input.getMaxWaitTime());
				m[i][j] = ok;
				m[j][i] = ok;
			}
		}
		
		for(int i = 0;i < N; i++){
			for(int j = 0; j < N; j++)
				System.out.println(name() + "::computeSharedTaxiSolutionCluster, m[" + i + "," + j + "] = " + m[i][j]);
		}
		
		// establish sharing
		ArrayList<Integer> first = new ArrayList<Integer>();
		ArrayList<Integer> second = new ArrayList<Integer>();
		HashSet<Integer> S = new HashSet<Integer>();
		for(int i = 0; i < N; i++) S.add(i);
		int[] inDeg = new int[N];
		int[] outDeg = new int[N];
		for(int i = 0; i < N; i++){
			inDeg[i] = 0;
			outDeg[i] = 0;
		}
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				if(m[i][j]){
					outDeg[i]++;
					inDeg[j]++;
				}
			}
		}
		while(S.size() > 0){
			// find i such that outDeg[i] is min
			int minOutDeg = Integer.MAX_VALUE;
			int min_i = -1;
			for(int i: S){
				min_i = i; break;
			}
			for(int i: S){
				if(outDeg[i] < minOutDeg && outDeg[i] > 0){
					minOutDeg = i; min_i = i;
				}
			}
			first.add(min_i);
			
			// find j such that m[i][j] = true and inDeg[j] + outDeg[j] is min
			int minInOutDeg = Integer.MAX_VALUE;
			int min_j = -1;
			for(int j: S)if(j != min_i && m[min_i][j] == true){
				if(minInOutDeg > inDeg[j] + outDeg[j]){
					minInOutDeg = inDeg[j] + outDeg[j];
					min_j = j;
				}
			}
			
			/*
			// share i and j (j follows j)
			if(min_j == -1){// select randomly
				for(int j: S)if(j != min_i){
					min_j = j; break;
				}
			}
			*/
			second.add(min_j);
			
			
			// remove min_i and min_j from S
			for(int j: S){
				if(min_i > -1){
					if(m[min_i][j] == true) inDeg[j]--;
					if(m[j][min_i] == true) outDeg[j]--;
				}
				if(min_j > -1){
					if(m[min_j][j] == true) inDeg[j]--;
					if(m[j][min_j] == true) outDeg[j]--;
				}
			}
			S.remove(min_i);
			S.remove(min_j);
			System.out.println(name() + "::computeSharedTaxiSolutionCluster"
					+ " SHARED " + min_i + " with " + min_j + ", S.sz = " + S.size());
		}
		SharedTaxiRoute[] routes = new SharedTaxiRoute[first.size()];
		for(int k = 0; k < first.size(); k++){
			int i = first.get(k);
			int j = second.get(k);
			int load = 0;
			ArrayList<SharedTaxiRouteElement> route = new ArrayList<SharedTaxiRouteElement>();
			SharedTaxiRouteElement e = new SharedTaxiRouteElement(requests[i].getTicketCode(),requests[i].getPickupAddress(),
					requests[i].getEarlyPickupDateTime(),requests[i].getLatePickupDateTime(),-1,-1);
			load += requests[i].getNumberPassengers();
			route.add(e);
			
			if(j > -1){
				e = new SharedTaxiRouteElement(requests[j].getTicketCode(),requests[j].getPickupAddress(),
						requests[j].getEarlyPickupDateTime(),requests[j].getLatePickupDateTime(),-1,-1);
				
				load += requests[j].getNumberPassengers();
				route.add(e);
			}
			SharedTaxiRouteElement[] aRoute = new SharedTaxiRouteElement[route.size()];
			for(int k1 = 0; k1 < route.size(); k1++)
				aRoute[k1]= route.get(k1);
			
			SharedTaxiRoute r = new SharedTaxiRoute(aRoute,load,"-");
			routes[k] = r;
		}
		return new SharedTaxiSolution(routes);
	}
	
	public SharedTaxiSolution computeSharedTaxiSolutionCluster0(SharedTaxiInput input, 
			HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng,
			HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng, 
			int speed) {
		
		HashMap<SharedTaxiRequest, Integer> mRequest2ID = new HashMap<SharedTaxiRequest, Integer>();
		SharedTaxiRequest[] requests = input.getRequests();
		
		System.out.println(name() + "::computeSharedTaxiSolutionCluster there are " + requests.length + " REQUESTS");
		for(int i = 0; i < requests.length; i++){
			System.out.println(name() + "::computeSharedTaxiSolutionCluster, request at " + requests[i].getPickupAddress());
		}
		
		//all the requests has location (lat,lng) and close together in a cluster
		
		
		int N = requests.length;
		for (int i = 0; i < requests.length; i++)
			mRequest2ID.put(requests[i], i);

		GoogleMapsQuery G = new GoogleMapsQuery();
		
		int[][] travelTimes = new int[N][N];// travelTimes[i][j] is the travel
											// time from pickup point of request
											// i to pickup point of request j
		
		HashMap<SharedTaxiRequest, Integer> shortestTravelTime = new HashMap<SharedTaxiRequest, Integer>();
		for(int i = 0; i < N;i++){
			
			int t = G.getTravelTime(requests[i].getPickupAddress(), requests[i].getDeliveryAddress(), "driving");
			
			if(t < 0){
				LatLng llp = mPickup2LatLng.get(requests[i]);
				LatLng lld = mDelivery2LatLng.get(requests[i]);
				double d = G.computeDistanceHaversine(llp.lat, llp.lng, lld.lat, lld.lng);
				t = (int)(d*1000*APPX/speed);
			}
			shortestTravelTime.put(requests[i], t);
		}
		
		
		for (int i = 0; i < requests.length; i++) {
			SharedTaxiRequest ri = requests[i];
			LatLng li= mPickup2LatLng.get(ri);
			for (int j = 0; j < requests.length; j++) {
				SharedTaxiRequest rj = requests[j];
				LatLng lj = mPickup2LatLng.get(rj);
				double  d = G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
				d = d*1000;
				int appxt =  (int)(d*APPX/speed);// approximate travel time
				
				int t = G.getTravelTime(requests[i].getPickupAddress(), requests[j].getPickupAddress(), "driving");
				travelTimes[i][j] = t;
				System.out.println(name() + "::computeSharedTaxiSolution, TT[" + i + "," + j + "] = " + travelTimes[i][j]);
				if(travelTimes[i][j] < 0){
					System.out.println(name() + "::computeSharedTaxiSolution exception from " + 
				requests[i].getPickupAddress() + " to " + requests[j].getPickupAddress() + " --> use approximate time");
					//System.exit(-1);
					travelTimes[i][j] = appxt;
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
			long lt = DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
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
			SharedTaxiRequest req = mPoint2Request.get(p);
			for (Point s : startPoints) {
				awm.setWeight(s, p, D);
				awm.setWeight(p, s, D);
				
			}
			for (Point t : endPoints) {
				//awm.setWeight(p, t, D);
				//awm.setWeight(t, p, D);
				awm.setWeight(p,t,shortestTravelTime.get(req));
				awm.setWeight(t,p,shortestTravelTime.get(req));
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
		HashMap<Point, IFunctionVR> mPoint2AccDis = new HashMap<Point, IFunctionVR>();
		HashMap<Point, IFunctionVR> mPoint2IdxRoute = new HashMap<Point, IFunctionVR>();
		
		for(Point p: clientPoints){
			IFunctionVR ad = new AccumulatedEdgeWeightsOnPathVR(awe, p);
			mPoint2AccDis.put(p, ad);
			
			IFunctionVR idx = new RouteIndex(XR,p);
			mPoint2IdxRoute.put(p, idx);
		}
		for(int i = 0; i < K; i++){
			Point t = XR.endPoint(i+1);// start point of route i+1
			d[i] = new AccumulatedEdgeWeightsOnPathVR(awe, t); 
			w[i] = new AccumulatedNodeWeightsOnPathVR(awn, t);
			CS.post(new Leq(w[i],capacity));
			//CS.post(new Leq(d[i],input.getMaxWaitTime()));
		}		
		
		//HashMap<Point, IFunctionVR> mPoint2TravelTime2Destination = new HashMap<Point, IFunctionVR>();
		for(Point p: clientPoints){
			SharedTaxiRequest req = mPoint2Request.get(p);
			IFunctionVR idx = mPoint2IdxRoute.get(p);
			int maxD = shortestTravelTime.get(req) + input.getMaxWaitTime();
			
			IFunctionVR accD = mPoint2AccDis.get(p);
			for(int k = 1; k <= K; k++){
				IConstraintVR c1 = new Eq(idx,k);
				IFunctionVR f = new Minus(d[k-1], accD);
				//mPoint2TravelTime2Destination.put(p, f);
				IConstraintVR c2 = new Leq(f,maxD);
				CS.post(new Implicate(c1, c2));
			}
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

		//GenericLocalSearch se = new GenericLocalSearch(mgr);
		DichungSearch se = new DichungSearch(clientPoints,mgr);
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.setMaxStable(50);

		se.search(50, input.getMaxTime());

		ArrayList<SharedTaxiRoute> routes = new ArrayList<SharedTaxiRoute>();
		
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			Point s = XR.startPoint(k);
			Point t = XR.endPoint(k);
			if(XR.next(s) != t){
				ArrayList<Point> points = new ArrayList<Point>();
				for(Point x = XR.next(s); x != t; x = XR.next(x)){
					points.add(x);
				}
				//points.add(t);
				SharedTaxiRouteElement[] e = new SharedTaxiRouteElement[points.size()];
				for(int i = 0; i < points.size(); i++){
					Point p = points.get(i);
					int arrTime = (int)eat.getEarliestArrivalTime(p);
					arrTime += minTimePoint - dt;
					String arrDT = DateTimeUtils.unixTimeStamp2DateTime(arrTime);
					SharedTaxiRequest req = mPoint2Request.get(p);
					int maxT = shortestTravelTime.get(req) + input.getMaxWaitTime();
					//IFunctionVR idRoute = mPoint2IdxRoute.get(p);
					//Point t = 
					int T = (int)eat.getEarliestArrivalTime(t) - (int)eat.getEarliestArrivalTime(p);//(int)mPoint2TravelTime2Destination.get(p).getValue();
					e[i] = new SharedTaxiRouteElement(req.getTicketCode(),req.getPickupAddress(),arrDT,req.getLatePickupDateTime(),T,maxT);
				}
				int arrTime2Destination = (int)eat.getEarliestArrivalTime(t);
				arrTime2Destination += minTimePoint - dt;
				String time2Destination = DateTimeUtils.unixTimeStamp2DateTime(arrTime2Destination);
				SharedTaxiRoute r = new SharedTaxiRoute(e,(int)w[k-1].getValue(),time2Destination);
				routes.add(r);
			}
		}
		SharedTaxiRoute[] A = new SharedTaxiRoute[routes.size()];
		for(int i = 0; i < routes.size(); i++){
			A[i] = routes.get(i);
		}
		return new SharedTaxiSolution(A);
	}
	

	public SharedTaxiSolution computeSharedTaxiSolutionCluster1(SharedTaxiInput input, 
			HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng,
			HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng, 
			int speed) {
		
		HashMap<SharedTaxiRequest, Integer> mRequest2ID = new HashMap<SharedTaxiRequest, Integer>();
		SharedTaxiRequest[] requests = input.getRequests();
		
		System.out.println(name() + "::computeSharedTaxiSolutionCluster there are " + requests.length + " REQUESTS");
		for(int i = 0; i < requests.length; i++){
			System.out.println(name() + "::computeSharedTaxiSolutionCluster, request at " + requests[i].getPickupAddress());
		}
		
		//all the requests has location (lat,lng) and close together in a cluster
		
		
		int N = requests.length;
		for (int i = 0; i < requests.length; i++)
			mRequest2ID.put(requests[i], i);

		GoogleMapsQuery G = new GoogleMapsQuery();
		
		int[][] travelTimes = new int[N][N];// travelTimes[i][j] is the travel
											// time from pickup point of request
											// i to pickup point of request j
		
		HashMap<SharedTaxiRequest, Integer> shortestTravelTime = new HashMap<SharedTaxiRequest, Integer>();
		for(int i = 0; i < N;i++){
			
			int t = G.getTravelTime(requests[i].getPickupAddress(), requests[i].getDeliveryAddress(), "driving");
			
			if(t < 0){
				LatLng llp = mPickup2LatLng.get(requests[i]);
				LatLng lld = mDelivery2LatLng.get(requests[i]);
				double d = G.computeDistanceHaversine(llp.lat, llp.lng, lld.lat, lld.lng);
				t = (int)(d*1000*APPX/speed);
			}
			shortestTravelTime.put(requests[i], t);
		}
		
		/*
		for (int i = 0; i < requests.length; i++) {
			SharedTaxiRequest ri = requests[i];
			LatLng li= mPickup2LatLng.get(ri);
			for (int j = 0; j < requests.length; j++) {
				SharedTaxiRequest rj = requests[j];
				LatLng lj = mPickup2LatLng.get(rj);
				double  d = G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
				d = d*1000;
				int appxt =  (int)(d*APPX/speed);// approximate travel time
				
				int t = G.getTravelTime(requests[i].getPickupAddress(), requests[j].getPickupAddress(), "driving");
				travelTimes[i][j] = t;
				System.out.println(name() + "::computeSharedTaxiSolution, TT[" + i + "," + j + "] = " + travelTimes[i][j]);
				if(travelTimes[i][j] < 0){
					System.out.println(name() + "::computeSharedTaxiSolution exception from " + 
				requests[i].getPickupAddress() + " to " + requests[j].getPickupAddress() + " --> use approximate time");
					//System.exit(-1);
					travelTimes[i][j] = appxt;
				}
			}
		}
		*/
		
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
		ArrayList<Point> pickupPoints = new ArrayList<Point>();
		ArrayList<Point> deliveryPoints = new ArrayList<Point>();
		
		// create pickup points
		for (int i = 0; i < K; i++) {
			id++;
			Point p = new Point(id);
			clientPoints.add(p);
			
			long et = DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
			long lt = DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
			int converted_et = (int)(et - minTimePoint) + dt;
			int converted_lt = (int)(lt - minTimePoint) + dt;
			earliestAllowedArrivalTime.put(p, converted_et);
			latestAllowedArrivalTime.put(p,converted_lt);
			serviceDuration.put(p, pickupDuration);// pickup duration is assumed to be 1 minute
			
			allPoints.add(p);
			pickupPoints.add(p);
			mPoint2Request.put(p, requests[i]);
		}
		
		// create delivery points
		for (int i = 0; i < K; i++) {
			id++;
			Point p = new Point(id);
			clientPoints.add(p);
			
			//long lt = DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
			//int converted_et = (int)(et - minTimePoint) + dt;
			//int converted_lt = (int)(lt - minTimePoint) + dt;
			Point pickupPoint = pickupPoints.get(i);
			int lt = latestAllowedArrivalTime.get(pickupPoint) + shortestTravelTime.get(requests[i]);
			earliestAllowedArrivalTime.put(p, 0);
			latestAllowedArrivalTime.put(p,lt);
			serviceDuration.put(p, pickupDuration);// pickup duration is assumed to be 1 minute
			
			allPoints.add(p);
			deliveryPoints.add(p);
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

		// setup travel time between points
		LatLng pivotLatLng = mDelivery2LatLng.get(requests[0]);
		Point pivotPoint = clientPoints.get(clientPoints.size()/2);// first delivery point
		
		
		ArcWeightsManager awm = new ArcWeightsManager(allPoints);
		for(int i = 0; i < N; i++){
			SharedTaxiRequest ri = requests[i];
			LatLng ll = mPickup2LatLng.get(ri);
			Point p = clientPoints.get(i);
			double d = G.computeDistanceHaversine(pivotLatLng.lat,pivotLatLng.lng,ll.lat,ll.lng);
			int appxt = (int)(d*1000*APPX/speed);// approximate traveltime
			int t = G.getTravelTime(pivotLatLng.lat,pivotLatLng.lng,ll.lat,ll.lng, "driving");
			if(t < 0) t = appxt;
			awm.setWeight(p,pivotPoint, t);
			awm.setWeight(pivotPoint, p,t);
			System.out.println(name() + "::computeSharedTaxiSolutionCluster, traveltime[" + i + ", pivot] = " + t);
		}
		
		for(int i = 0; i < N; i++){
			Point pi = clientPoints.get(i);
			LatLng li = mPickup2LatLng.get(requests[i]);
			for(int j = 0; j < N; j++){
				Point pj = clientPoints.get(j);
				LatLng lj = mPickup2LatLng.get(requests[j]);
			
				//double d = G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
				//int appxt = (int)(d*1000*APPX/speed);// approximate traveltime
				//int t = G.getTravelTime(li.lat,li.lng,lj.lat,lj.lng, "driving");
				//if(t < 0) t = appxt;
				int t = G.estimateTravelTime(li.lat,li.lng,lj.lat,lj.lng, "driving", speed, APPX);
				awm.setWeight(pi, pj, t);
				
				System.out.println(name() + "::computeSharedTaxiSolutionCluster, traveltime[" + i + "," + j + "] = " + t);
			}
			
			int t0 = (int)awm.getWeight(pi,pivotPoint);// in seconds
			for(int j = N; j < clientPoints.size(); j++){
				Point pj = clientPoints.get(j);
				LatLng lj = mDelivery2LatLng.get(requests[j-N]);
				double d = G.computeDistanceHaversine(pivotLatLng.lat,pivotLatLng.lng,lj.lat,lj.lng);
				int t = t0;
				if(d*1000 <= EPS){// in meters
					//awm.setWeight(pi, pj, t0);
					t = t0;
				}else{
					//d = G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
					//int appxt = (int)(d*1000*APPX/speed);// approximate traveltime
					//int t = G.getTravelTime(li.lat,li.lng,lj.lat,lj.lng, "driving");
					//if(t < 0) t = appxt;
					t = G.estimateTravelTime(li.lat,li.lng,lj.lat,lj.lng, "driving", speed, APPX);
					
				}
				awm.setWeight(pi, pj, t);
				System.out.println(name() + "::computeSharedTaxiSolutionCluster, traveltime[" + i + "," + j + "] = " + t);
			}
		}
		for(int i = N; i < clientPoints.size(); i++){
			Point pi = clientPoints.get(i);
			LatLng li = mPickup2LatLng.get(requests[i-N]);
			for(int j = 0; j < N; j++){
				Point pj = clientPoints.get(j);
				LatLng lj = mPickup2LatLng.get(requests[j]);
			
				//double d = G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
				//int appxt = (int)(d*1000*APPX/speed);// approximate traveltime
				//int t = G.getTravelTime(li.lat,li.lng,lj.lat,lj.lng, "driving");
				//if(t < 0) t = appxt;
				double d = G.computeDistanceHaversine(pivotLatLng.lat,pivotLatLng.lng,li.lat,li.lng);
				int t = (int)awm.getWeight(pivotPoint, pj);
				if(d <= EPS){
					//awm.setWeight(pi, pj, awm.getWeight(pivotPoint, pj));
				}else{
					t = G.estimateTravelTime(li.lat,li.lng,lj.lat,lj.lng, "driving", speed, APPX);
					
				}
				awm.setWeight(pi, pj, t);
				System.out.println(name() + "::computeSharedTaxiSolutionCluster, traveltime[" + i + "," + j + "] = " + t);
			}
			
			//int t0 = (int)awm.getWeight(pi,pivotPoint);// in seconds
			for(int j = N; j < clientPoints.size(); j++){
				Point pj = clientPoints.get(j);
				LatLng lj = mDelivery2LatLng.get(requests[j-N]);
				double d = G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
				int t = 0;
				if(d*1000 <= EPS){// in meters
					//awm.setWeight(pi, pj, 0);
				}else{
					//d = G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
					//int appxt = (int)(d*1000*APPX/speed);// approximate traveltime
					//int t = G.getTravelTime(li.lat,li.lng,lj.lat,lj.lng, "driving");
					//if(t < 0) t = appxt;
					t = G.estimateTravelTime(li.lat,li.lng,lj.lat,lj.lng, "driving", speed, APPX);
					
				}
				awm.setWeight(pi, pj, t);
				System.out.println(name() + "::computeSharedTaxiSolutionCluster, traveltime[" + i + "," + j + "] = " + t);
			}
		}
		
		
		/*
		for (int i = 0; i < clientPoints.size(); i++) {
			Point pi = clientPoints.get(i);
			for (int j = 0; j < clientPoints.size(); j++) {
				Point pj = clientPoints.get(j);
				//awm.setWeight(clientPoints.get(i), clientPoints.get(j),	travelTimes[i][j]);
				
			}
		}
		*/
		
		for (Point p : clientPoints) {
			SharedTaxiRequest req = mPoint2Request.get(p);
			for (Point s : startPoints) {
				awm.setWeight(s, p, D);
				awm.setWeight(p, s, D);
				
			}
			for (Point t : endPoints) {
				awm.setWeight(p, t, D);
				awm.setWeight(t, p, D);
				//awm.setWeight(p,t,shortestTravelTime.get(req));
				//awm.setWeight(t,p,shortestTravelTime.get(req));
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
		for(int i = 0; i < clientPoints.size(); i++){
			if(i < N)// pickup points
				nwm.setWeight(clientPoints.get(i), requests[i].getNumberPassengers());
			else// delivery points -> demand 0
				nwm.setWeight(clientPoints.get(i), 0);
		}
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
		HashMap<Point, IFunctionVR> mPoint2AccDis = new HashMap<Point, IFunctionVR>();
		HashMap<Point, IFunctionVR> mPoint2IdxRoute = new HashMap<Point, IFunctionVR>();
		
		for(Point p: clientPoints){
			IFunctionVR ad = new AccumulatedEdgeWeightsOnPathVR(awe, p);
			mPoint2AccDis.put(p, ad);
			
			IFunctionVR idx = new RouteIndex(XR,p);
			mPoint2IdxRoute.put(p, idx);
		}
		for(int i = 0; i < K; i++){
			Point t = XR.endPoint(i+1);// start point of route i+1
			d[i] = new AccumulatedEdgeWeightsOnPathVR(awe, t); 
			w[i] = new AccumulatedNodeWeightsOnPathVR(awn, t);
			CS.post(new Leq(w[i],capacity));
			//CS.post(new Leq(d[i],input.getMaxWaitTime()));
		}		
		HashMap<Point, IFunctionVR> routeOfPoint = new HashMap<Point, IFunctionVR>();
		HashMap<Point, IFunctionVR> indexOfPointOnRoute = new HashMap<Point, IFunctionVR>();
		for(int i = 0; i < pickupPoints.size(); i++){
			Point pickup = pickupPoints.get(i);
			Point delivery = deliveryPoints.get(i);
			IFunctionVR rp = new RouteIndex(XR, pickup);
			IFunctionVR rd = new RouteIndex(XR, delivery);
			routeOfPoint.put(pickup, rp);
			routeOfPoint.put(delivery, rd);
			CS.post(new Eq(rp, rd));

			IFunctionVR ipickup = new IndexOnRoute(XR, pickup);
			IFunctionVR idelivery = new IndexOnRoute(XR, delivery);
			indexOfPointOnRoute.put(pickup, ipickup);
			indexOfPointOnRoute.put(delivery, idelivery);
			CS.post(new Leq(ipickup, idelivery));
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

		//GenericLocalSearch se = new GenericLocalSearch(mgr);
		DichungSearch se = new DichungSearch(clientPoints,mgr);
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.setMaxStable(50);

		se.search(50, input.getMaxTime());

		ArrayList<SharedTaxiRoute> routes = new ArrayList<SharedTaxiRoute>();
		
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			Point s = XR.startPoint(k);
			Point t = XR.endPoint(k);
			if(XR.next(s) != t){
				ArrayList<Point> points = new ArrayList<Point>();
				for(Point x = XR.next(s); x != t; x = XR.next(x)){
					points.add(x);
				}
				//points.add(t);
				SharedTaxiRouteElement[] e = new SharedTaxiRouteElement[points.size()];
				for(int i = 0; i < points.size(); i++){
					Point p = points.get(i);
					int arrTime = (int)eat.getEarliestArrivalTime(p);
					arrTime += minTimePoint - dt;
					String arrDT = DateTimeUtils.unixTimeStamp2DateTime(arrTime);
					SharedTaxiRequest req = mPoint2Request.get(p);
					int maxT = shortestTravelTime.get(req) + input.getMaxWaitTime();
					//IFunctionVR idRoute = mPoint2IdxRoute.get(p);
					//Point t = 
					int T = (int)eat.getEarliestArrivalTime(t) - (int)eat.getEarliestArrivalTime(p);//(int)mPoint2TravelTime2Destination.get(p).getValue();
					e[i] = new SharedTaxiRouteElement(req.getTicketCode(),req.getPickupAddress(),arrDT,req.getLatePickupDateTime(),T,maxT);
				}
				int arrTime2Destination = (int)eat.getEarliestArrivalTime(t);
				arrTime2Destination += minTimePoint - dt;
				String time2Destination = DateTimeUtils.unixTimeStamp2DateTime(arrTime2Destination);
				SharedTaxiRoute r = new SharedTaxiRoute(e,(int)w[k-1].getValue(),time2Destination);
				routes.add(r);
			}
		}
		SharedTaxiRoute[] A = new SharedTaxiRoute[routes.size()];
		for(int i = 0; i < routes.size(); i++){
			A[i] = routes.get(i);
		}
		return new SharedTaxiSolution(A);
	}
	
	public SharedTaxiSolution computeSharedTaxiSolution(SharedTaxiInput input, int speed) {
		HashMap<SharedTaxiRequest, Integer> mRequest2ID = new HashMap<SharedTaxiRequest, Integer>();
		SharedTaxiRequest[] requests = input.getRequests();
		
		HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng = new HashMap<SharedTaxiRequest, LatLng>();
		HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng = new HashMap<SharedTaxiRequest, LatLng>();
		
		int N = requests.length;
		for (int i = 0; i < requests.length; i++)
			mRequest2ID.put(requests[i], i);

		GoogleMapsQuery G = new GoogleMapsQuery();
		
		int[][] travelTimes = new int[N][N];// travelTimes[i][j] is the travel
											// time from pickup point of request
											// i to pickup point of request j
		
		for(int i = 0; i < requests.length; i++){
			String latlngPickup = G.getLatLngFromAddress(requests[i].getPickupAddress());
			String latlngDelivery = G.getLatLngFromAddress(requests[i].getDeliveryAddress());
			
			System.out.println(name() + "::computeSharedTaxiSolution, request " + requests[i].getPickupAddress() +  
					" --> " + latlngPickup + ", " + requests[i].getDeliveryAddress()  + " --> " + latlngDelivery);
			
			mPickup2LatLng.put(requests[i], new LatLng(latlngPickup));
			mDelivery2LatLng.put(requests[i], new LatLng(latlngDelivery));
			
		}
		
		
		for (int i = 0; i < requests.length; i++) {
			SharedTaxiRequest ri = requests[i];
			LatLng li= mPickup2LatLng.get(ri);
			for (int j = 0; j < requests.length; j++) {
				SharedTaxiRequest rj = requests[j];
				LatLng lj = mPickup2LatLng.get(rj);
				double  d = G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
				d = d*1000*APPX;
				travelTimes[i][j] = (int)(d/speed);
				
				//int t = G.getTravelTime(requests[i].getPickupAddress(), requests[j].getPickupAddress(), "driving");
				
				
				//travelTimes[i][j] = t;
				System.out.println(name() + "::computeSharedTaxiSolution, TT[" + i + "," + j + "] = " + travelTimes[i][j]);
				if(travelTimes[i][j] < 0){
					System.out.println(name() + "::computeSharedTaxiSolution exception from " + 
				requests[i].getPickupAddress() + " to " + requests[j].getPickupAddress());
					System.exit(-1);
				}
			}
		}
		
		int[][] A = new int[N][N];
		for(int i = 0; i < N; i++){
			for(int j = 0; j < N; j++){
				if(travelTimes[i][j] <= input.getMaxWaitTime()){
					A[i][j] = 1;
				}else{
					A[i][j] = 0;
				}
			}
		}
		DFS dfs = new DFS();
		ArrayList<ArrayList<Integer>> CC = dfs.computeConnectedComponents(A);
		ArrayList<SharedTaxiRoute> routes = new ArrayList<SharedTaxiRoute>();
		for(ArrayList<Integer> cc : CC){
			SharedTaxiRequest[] R = new SharedTaxiRequest[cc.size()];
			for(int i = 0; i < cc.size(); i++){
				R[i] = requests[cc.get(i)];
			}
			
			SharedTaxiInput I = new SharedTaxiInput(R,input.getVehicleCapacities(),input.getMaxWaitTime(),input.getMaxTime());
			
			SharedTaxiSolution sol = computeSharedTaxiSolutionCluster(I, mPickup2LatLng, mDelivery2LatLng, speed);
			for(SharedTaxiRoute r: sol.getRoutes())
				routes.add(r);
		}
		SharedTaxiRoute[] arr_routes = new SharedTaxiRoute[routes.size()];
		for(int i = 0; i < routes.size(); i++)
			arr_routes[i] = routes.get(i);
		
		for(ArrayList<Integer> cc : CC){
			System.out.println(name() + "::computeSharedTaxiSolution, cc: ");
			for(int i: cc) System.out.print(requests[i].getPickupAddress() + "\n");
			System.out.println();
		}
		return new SharedTaxiSolution(arr_routes);
		
		/*
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
