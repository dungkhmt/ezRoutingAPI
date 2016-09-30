package com.kse.ezRoutingAPI.dichung.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.LatLng;

import com.kse.ezRoutingAPI.dichung.model.SharedTaxiInput;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiRequest;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiRoute;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiRouteElement;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiSolution;
import com.kse.utils.DateTimeUtils;

public class ClusterBasedSolver {

	private SharedTaxiInput input;
	private HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng;
	private HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng;
	SharedTaxiRequest[] requests;
	HashMap<SharedTaxiRequest, Integer> mRequest2ID;
	double[][] estimated_distances;
	HashMap<SharedTaxiRequest, Double> distanceOfRequest;
	HashMap<SharedTaxiRequest, Double> extraDistance;
	int maxWaitTime;
	
	int N;	
	SharedRequestsFeasibleChecker checker;
	
	public ClusterBasedSolver(SharedTaxiInput input,
			HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng,
			HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng,
			HashMap<SharedTaxiRequest, Double> extraDistance,
			int maxWaitTime){
		
		this.maxWaitTime = maxWaitTime;
		this.input = input;
		this.mPickup2LatLng = mPickup2LatLng;
		this.mDelivery2LatLng = mDelivery2LatLng;
		this.extraDistance = extraDistance;
		
		mRequest2ID = new HashMap<SharedTaxiRequest, Integer>();
		requests = input.getRequests();

		System.out.println(name()
				+ "::computeSharedTaxiSolutionCluster there are "
				+ requests.length + " REQUESTS");
		for (int i = 0; i < requests.length; i++) {
			System.out.println(name()
					+ "::computeSharedTaxiSolutionCluster, request at "
					+ requests[i].getPickupAddress());
		}

		// all the requests has location (lat,lng) and close together in a
		// cluster

		N = requests.length;
		for (int i = 0; i < requests.length; i++)
			mRequest2ID.put(requests[i], i);

		GoogleMapsQuery G = new GoogleMapsQuery();

		estimated_distances = new double[N][N];
		distanceOfRequest = new HashMap<SharedTaxiRequest, Double>();
		for (int i = 0; i < N; i++) {
			/*
			int t = G.getTravelTime(requests[i].getPickupAddress(),
					requests[i].getDeliveryAddress(), "driving");

			if (t < 0) {
				LatLng llp = mPickup2LatLng.get(requests[i]);
				LatLng lld = mDelivery2LatLng.get(requests[i]);
				double d = G.computeDistanceHaversine(llp.lat, llp.lng,
						lld.lat, lld.lng);
				t = (int) (d * 1000 * APPX / speed);
			}
			*/
			LatLng llp = mPickup2LatLng.get(requests[i]);
			LatLng lld = mDelivery2LatLng.get(requests[i]);
			
			//int t = G.estimateTravelTime(llp.lat, llp.lng, lld.lat, lld.lng, "driving",speed,APPX);
			//int t =  G.estimateTravelTimeWithTimeFrame(llp.lat, llp.lng, lld.lat, lld.lng, "driving",
			//		requests[i].getEarlyPickupDateTime(), STD_SPEED, HIGH_TRAFFIC_SPEED);
			double d = G.estimateDistanceMeter(llp.lat, llp.lng, lld.lat, lld.lng);
			distanceOfRequest.put(requests[i], d);
			
		}

		for (int i = 0; i < requests.length; i++) {
			SharedTaxiRequest ri = requests[i];
			LatLng li = mPickup2LatLng.get(ri);
			for (int j = 0; j < requests.length; j++) {
				SharedTaxiRequest rj = requests[j];
				LatLng lj = mPickup2LatLng.get(rj);
				
				//travelTimes[i][j] = G.estimateTravelTime(li.lat, li.lng, lj.lat, lj.lng, "driving", speed, APPX);
				
				//travelTimes[i][j] = G.estimateTravelTimeWithTimeFrame(li.lat, li.lng, lj.lat, lj.lng, "driving",
				//		ri.getEarlyPickupDateTime(), STD_SPEED, HIGH_TRAFFIC_SPEED);
				
				estimated_distances[i][j] = G.estimateDistanceMeter(li.lat, li.lng, lj.lat, lj.lng);
				
				//System.out.println(name()
				//		+ "::computeSharedTaxiSolutionCluster, travelTimes["
				//		+ ri.getTicketCode() + "," + rj.getTicketCode() + "] = " + travelTimes[i][j]);
				
				System.out.println(name()
						+ "::computeSharedTaxiSolutionCluster, e_distance["
						+ ri.getTicketCode() + "," + rj.getTicketCode() + "] = " + estimated_distances[i][j]);
				
			}
		}

		checker = new SharedRequestsFeasibleChecker(input,requests, maxWaitTime, estimated_distances, extraDistance, distanceOfRequest);
	}

	//3-sharing
	public SharedTaxiSolution compute3SharedTaxiSolution() {

		int C = 6;// input.getVehicleCapacities()[1];
		
		boolean[][] m = new boolean[N][N];// m[i][j] = 1 if request i can be
											// followed requests j (i -> j ->
											// destination)
		for (int i = 0; i < N; i++) {
			SharedTaxiRequest ri = requests[i];
			
			//int ei = (int) DateTimeUtils.dateTime2Int(ri.getEarlyPickupDateTime());
			//int li = (int) DateTimeUtils.dateTime2Int(ri.getLatePickupDateTime());
			for (int j = 0; j < N; j++) {
				SharedTaxiRequest rj = requests[j];
				//int ej = (int) DateTimeUtils.dateTime2Int(rj
				//		.getEarlyPickupDateTime());
				//int lj = (int) DateTimeUtils.dateTime2Int(rj
				//		.getLatePickupDateTime());

				boolean ok = ri.getNumberPassengers()
						+ rj.getNumberPassengers() <= C;
				
				
				m[i][j] = checker.checkFeasibleSharing(i, j);
			}
		}

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++)
				System.out.println(name()
						+ "::computeSharedTaxiSolutionCluster, m[" + requests[i].getTicketCode() + ","
						+ requests[j].getTicketCode() + "] = " + m[i][j]);
		}

		// establish sharing
		ArrayList<Integer> first = new ArrayList<Integer>();
		ArrayList<Integer> second = new ArrayList<Integer>();
		HashSet<Integer> S = new HashSet<Integer>();
		for (int i = 0; i < N; i++)
			S.add(i);
		int[] inDeg = new int[N];
		int[] outDeg = new int[N];
		for (int i = 0; i < N; i++) {
			inDeg[i] = 0;
			outDeg[i] = 0;
		}
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++)if(i != j) {
				if (m[i][j]) {
					outDeg[i]++;
					inDeg[j]++;
				}
			}
		}
		for(int i = 0; i < N; i++){
			System.out.println(name() + "::computeSharedTaxiSolutionCluster, req " + requests[i].getTicketCode() + 
					": inDeg = " + inDeg[i] + ", outDeg = " + outDeg[i]);
		}
		
		while (S.size() > 0) {
			// find i such that outDeg[i] is min
			int minOutDeg = Integer.MAX_VALUE;
			int min_i = -1;
			for (int i : S) {
				min_i = i;
				break;
			}
			for (int i : S) {
				if (outDeg[i] < minOutDeg && outDeg[i] > 0) {
					minOutDeg = i;
					min_i = i;
				}
			}
			first.add(min_i);

			// find j such that m[i][j] = true and inDeg[j] + outDeg[j] is min
			int minInOutDeg = Integer.MAX_VALUE;
			int min_j = -1;
			for (int j : S)
				if (j != min_i && m[min_i][j] == true) {
					if (minInOutDeg > inDeg[j] + outDeg[j]) {
						minInOutDeg = inDeg[j] + outDeg[j];
						min_j = j;
					}
				}

			/*
			 * // share i and j (j follows j) if(min_j == -1){// select randomly
			 * for(int j: S)if(j != min_i){ min_j = j; break; } }
			 */
			second.add(min_j);

			// remove min_i and min_j from S
			for (int j : S) {
				if (min_i > -1) {
					if (m[min_i][j] == true)
						inDeg[j]--;
					if (m[j][min_i] == true)
						outDeg[j]--;
				}
				if (min_j > -1) {
					if (m[min_j][j] == true)
						inDeg[j]--;
					if (m[j][min_j] == true)
						outDeg[j]--;
				}
			}
			S.remove(min_i);
			S.remove(min_j);
			String min_i_tk = "NULL";
			String min_j_tk = "NULL";
			if(min_i != -1) min_i_tk = requests[min_i].getTicketCode();
			if(min_j != -1) min_j_tk = requests[min_j].getTicketCode();
			System.out.println(name() + "::computeSharedTaxiSolutionCluster"
					+ " SHARED " + min_i_tk + " with " + min_j_tk + ", S.sz = "
					+ S.size());
		}
		SharedTaxiRoute[] routes = new SharedTaxiRoute[first.size()];
		for (int k = 0; k < first.size(); k++) {
			int i = first.get(k);
			int j = second.get(k);
			int load = 0;
			ArrayList<SharedTaxiRouteElement> route = new ArrayList<SharedTaxiRouteElement>();

			//long earlyI = DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
			//long lateI = DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
			long expectedPickupTimePointI = DateTimeUtils.dateTime2Int(requests[i].getDepartTime());//(earlyI + lateI) / 2;
			
			String expectedPickupTimeI = DateTimeUtils
					.unixTimeStamp2DateTime(expectedPickupTimePointI);

			SharedTaxiRouteElement eI = new SharedTaxiRouteElement(
					requests[i].getTicketCode(),
					requests[i].getPickupAddress(), 
					mPickup2LatLng.get(requests[i]).toString(),
					"-", expectedPickupTimeI,
					"-", "-", "-","-");
			load += requests[i].getNumberPassengers();
			route.add(eI);

			if (j > -1) {
				//String time2NextI = DateTimeUtils.second2HMS(travelTimes[i][j]);
				//eI.setTravelTimeToNext(time2NextI);
				eI.setDistanceToNext((int)estimated_distances[i][j] + "");
				
				//long earlyJ = DateTimeUtils.dateTime2Int(requests[j].getEarlyPickupDateTime());
				//long lateJ = DateTimeUtils.dateTime2Int(requests[j].getLatePickupDateTime());
				long expectedPickupTimePointJ = DateTimeUtils.dateTime2Int(requests[j].getDepartTime());//(earlyJ + lateJ) / 2;

				/*
				long suggestedPickupTimePointI = DateTimeUtils
						.computeStartTimePoint(earlyI, lateJ,
								travelTimes[i][j], earlyJ, lateJ);
				String suggestedPickupTimeI = DateTimeUtils
						.unixTimeStamp2DateTime(suggestedPickupTimePointI);
				eI.setPickupDateTime(suggestedPickupTimeI);
				*/
				
				/*
				long suggestedPickupTimePointJ = suggestedPickupTimePointI
						+ travelTimes[i][j];
				
				String suggestedPickupTimeJ = DateTimeUtils
						.unixTimeStamp2DateTime(suggestedPickupTimePointJ);
				*/
				
				String expectedPickupTimeJ = DateTimeUtils
						.unixTimeStamp2DateTime(expectedPickupTimePointJ);

				/*
				int T = shortestTravelTime.get(requests[j]);
				String time2NextJ = DateTimeUtils.second2HMS(T);

				String time2DestinationI = DateTimeUtils
						.second2HMS(travelTimes[i][j] + T);
				eI.setTravelTimeToDestination(time2DestinationI);
				*/
				
				SharedTaxiRouteElement eJ = new SharedTaxiRouteElement(
						requests[j].getTicketCode(),
						requests[j].getPickupAddress(),
						mPickup2LatLng.get(requests[j]).toString(),
						//suggestedPickupTimeJ,
						"-",
						//expectedPickupTimeJ, time2NextJ, time2NextJ, "-");
						expectedPickupTimeJ, "-", "-", "-","-");

				load += requests[j].getNumberPassengers();
				route.add(eJ);
			} else {
				//
				/*
				int T = shortestTravelTime.get(requests[i]);
				String time2NextI = DateTimeUtils.second2HMS(T);
				eI.setTravelTimeToNext(time2NextI);
				eI.setTravelTimeToDestination(time2NextI);
				*/
			}
			SharedTaxiRouteElement[] aRoute = new SharedTaxiRouteElement[route
					.size()];
			for (int k1 = 0; k1 < route.size(); k1++)
				aRoute[k1] = route.get(k1);

			SharedTaxiRoute r = new SharedTaxiRoute(aRoute, load, "-");
			routes[k] = r;
		}
		
		int nb2Sharings = 0;
		int nb3Sharings = 0;
		int nbRequests = 0;
		for(int i = 0; i < routes.length; i++){
			if(routes[i].getTicketCodes().length == 2){
				nb2Sharings++;
			}else if(routes[i].getTicketCodes().length == 3){
				nb3Sharings++;
			}
			nbRequests += routes[i].getTicketCodes().length;
		}
		
		return new SharedTaxiSolution(nb2Sharings,nb3Sharings,nbRequests,routes);
	}
	
	
	// 2-sharing
	public SharedTaxiSolution compute2SharedTaxiSolution() {

		int C = 6;// input.getVehicleCapacities()[1];
		double maxSharedDistance = 6000;// the distance of pickup points of shared a ride requests cannot exceed 6000m
		double maxSharedTime = 1800;// the difference between pickup time of shared a ride requests cannot exceed 30 minutes;
		
		boolean[][] m = new boolean[N][N];// m[i][j] = 1 if request i can be
											// followed requests j (i -> j ->
											// destination)
		for (int i = 0; i < N; i++) {
			SharedTaxiRequest ri = requests[i];
			int ti = (int) DateTimeUtils.dateTime2Int(ri.getDepartTime());
			//int ei = (int) DateTimeUtils.dateTime2Int(ri
			//		.getEarlyPickupDateTime());
			//int li = (int) DateTimeUtils.dateTime2Int(ri
			//		.getLatePickupDateTime());
			for (int j = 0; j < N; j++) {
				SharedTaxiRequest rj = requests[j];
				int tj = (int) DateTimeUtils.dateTime2Int(rj.getDepartTime());
				//int ej = (int) DateTimeUtils.dateTime2Int(rj
				//		.getEarlyPickupDateTime());
				//int lj = (int) DateTimeUtils.dateTime2Int(rj
				//		.getLatePickupDateTime());

				boolean ok = ri.getNumberPassengers()
						+ rj.getNumberPassengers() <= C;
				/*
				ok = ok
						&& (ej <= ei + travelTimes[i][j]
								&& ei + travelTimes[i][j] <= lj
								&& travelTimes[i][j] < input.getMaxWaitTime() || ej <= li
								+ travelTimes[i][j]
								&& li + travelTimes[i][j] <= lj
								&& travelTimes[i][j] < input.getMaxWaitTime());
				// ei <= ej + travelTimes[j][i] && ej + travelTimes[j][i] <= li
				// && travelTimes[j][i] < input.getMaxWaitTime() ||
				// ei <= ej + travelTimes[j][i] && ej + travelTimes[j][i] <= li
				// && travelTimes[j][i] < input.getMaxWaitTime());

				ok = ok
						& travelTimes[i][j] + shortestTravelTime.get(rj) < travelTimes[j][i]
								+ shortestTravelTime.get(ri);
				*/
				
				maxSharedDistance = 6000;
				if(DateTimeUtils.isHighTraffic(ri.getDepartTime())){
					maxSharedDistance = 3000; 
				}else{
					
				}
				
				ok = ok & estimated_distances[i][j] <= maxSharedDistance;
				
				
				//int mi = (ei + li)/2;
				//int mj = (ej + lj)/2;
				int mij = tj-ti;//mj-mi;
				
				//if(ri.getTicketCode().equals("TK0016") && rj.getTicketCode().equals("TK0017")){
			//	System.out.println("mj - mi = " + mij + ", maxSharedTime = " + maxSharedTime + ", maxSharedDistance = " + maxSharedDistance
			//				+ ", distance[i-j] = " + estimated_distances[i][j] + ", ri.pickupDateTime = " + ri.getEarlyPickupDateTime() + ", ok = " + ok);
				//}
				
				ok = ok & (0 <= mij && mij <= maxSharedTime);// the30 minutes
				
				//ok = ok & estimated_distances[i][j] + shortestTravelTime.get(rj) < estimated_distances[j][i] + shortestTravelTime.get(ri);
				
				m[i][j] = ok;
				// m[j][i] = ok;
			}
		}

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++)
				System.out.println(name()
						+ "::computeSharedTaxiSolutionCluster, m[" + requests[i].getTicketCode() + ","
						+ requests[j].getTicketCode() + "] = " + m[i][j]);
		}

		// establish sharing
		ArrayList<Integer> first = new ArrayList<Integer>();
		ArrayList<Integer> second = new ArrayList<Integer>();
		HashSet<Integer> S = new HashSet<Integer>();
		for (int i = 0; i < N; i++)
			S.add(i);
		int[] inDeg = new int[N];
		int[] outDeg = new int[N];
		for (int i = 0; i < N; i++) {
			inDeg[i] = 0;
			outDeg[i] = 0;
		}
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++)if(i != j) {
				if (m[i][j]) {
					outDeg[i]++;
					inDeg[j]++;
				}
			}
		}
		for(int i = 0; i < N; i++){
			System.out.println(name() + "::computeSharedTaxiSolutionCluster, req " + requests[i].getTicketCode() + 
					": inDeg = " + inDeg[i] + ", outDeg = " + outDeg[i]);
		}
		
		while (S.size() > 0) {
			// find i such that outDeg[i] is min
			int minOutDeg = Integer.MAX_VALUE;
			int min_i = -1;
			for (int i : S) {
				min_i = i;
				break;
			}
			for (int i : S) {
				if (outDeg[i] < minOutDeg && outDeg[i] > 0) {
					minOutDeg = i;
					min_i = i;
				}
			}
			first.add(min_i);

			// find j such that m[i][j] = true and inDeg[j] + outDeg[j] is min
			int minInOutDeg = Integer.MAX_VALUE;
			int min_j = -1;
			for (int j : S)
				if (j != min_i && m[min_i][j] == true) {
					if (minInOutDeg > inDeg[j] + outDeg[j]) {
						minInOutDeg = inDeg[j] + outDeg[j];
						min_j = j;
					}
				}

			/*
			 * // share i and j (j follows j) if(min_j == -1){// select randomly
			 * for(int j: S)if(j != min_i){ min_j = j; break; } }
			 */
			second.add(min_j);

			// remove min_i and min_j from S
			for (int j : S) {
				if (min_i > -1) {
					if (m[min_i][j] == true)
						inDeg[j]--;
					if (m[j][min_i] == true)
						outDeg[j]--;
				}
				if (min_j > -1) {
					if (m[min_j][j] == true)
						inDeg[j]--;
					if (m[j][min_j] == true)
						outDeg[j]--;
				}
			}
			S.remove(min_i);
			S.remove(min_j);
			String min_i_tk = "NULL";
			String min_j_tk = "NULL";
			if(min_i != -1) min_i_tk = requests[min_i].getTicketCode();
			if(min_j != -1) min_j_tk = requests[min_j].getTicketCode();
			System.out.println(name() + "::computeSharedTaxiSolutionCluster"
					+ " SHARED " + min_i_tk + " with " + min_j_tk + ", S.sz = "
					+ S.size());
		}
		SharedTaxiRoute[] routes = new SharedTaxiRoute[first.size()];
		for (int k = 0; k < first.size(); k++) {
			int i = first.get(k);
			int j = second.get(k);
			int load = 0;
			ArrayList<SharedTaxiRouteElement> route = new ArrayList<SharedTaxiRouteElement>();

			//long earlyI = DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
			//long lateI = DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
			long expectedPickupTimePointI = DateTimeUtils.dateTime2Int(requests[i].getDepartTime());//(earlyI + lateI) / 2;
			String expectedPickupTimeI = DateTimeUtils
					.unixTimeStamp2DateTime(expectedPickupTimePointI);

			SharedTaxiRouteElement eI = new SharedTaxiRouteElement(
					requests[i].getTicketCode(),
					requests[i].getPickupAddress(),
					mPickup2LatLng.get(requests[i]).toString(),
					"-", expectedPickupTimeI,
					"-", "-", "-","-");
			load += requests[i].getNumberPassengers();
			route.add(eI);

			if (j > -1) {
				//String time2NextI = DateTimeUtils.second2HMS(travelTimes[i][j]);
				//eI.setTravelTimeToNext(time2NextI);
				eI.setDistanceToNext((int)estimated_distances[i][j] + "");
				
				//long earlyJ = DateTimeUtils.dateTime2Int(requests[j].getEarlyPickupDateTime());
				//long lateJ = DateTimeUtils.dateTime2Int(requests[j].getLatePickupDateTime());
				long expectedPickupTimePointJ = DateTimeUtils.dateTime2Int(requests[j].getDepartTime());//(earlyJ + lateJ) / 2;

				/*
				long suggestedPickupTimePointI = DateTimeUtils
						.computeStartTimePoint(earlyI, lateJ,
								travelTimes[i][j], earlyJ, lateJ);
				String suggestedPickupTimeI = DateTimeUtils
						.unixTimeStamp2DateTime(suggestedPickupTimePointI);
				eI.setPickupDateTime(suggestedPickupTimeI);
				*/
				
				/*
				long suggestedPickupTimePointJ = suggestedPickupTimePointI
						+ travelTimes[i][j];
				
				String suggestedPickupTimeJ = DateTimeUtils
						.unixTimeStamp2DateTime(suggestedPickupTimePointJ);
				*/
				
				String expectedPickupTimeJ = DateTimeUtils
						.unixTimeStamp2DateTime(expectedPickupTimePointJ);

				/*
				int T = shortestTravelTime.get(requests[j]);
				String time2NextJ = DateTimeUtils.second2HMS(T);

				String time2DestinationI = DateTimeUtils
						.second2HMS(travelTimes[i][j] + T);
				eI.setTravelTimeToDestination(time2DestinationI);
				*/
				
				SharedTaxiRouteElement eJ = new SharedTaxiRouteElement(
						requests[j].getTicketCode(),
						requests[j].getPickupAddress(), 
						mPickup2LatLng.get(requests[j]).toString(),
						//suggestedPickupTimeJ,
						"-",
						//expectedPickupTimeJ, time2NextJ, time2NextJ, "-");
						expectedPickupTimeJ, "-", "-", "-","-");

				load += requests[j].getNumberPassengers();
				route.add(eJ);
			} else {
				//
				/*
				int T = shortestTravelTime.get(requests[i]);
				String time2NextI = DateTimeUtils.second2HMS(T);
				eI.setTravelTimeToNext(time2NextI);
				eI.setTravelTimeToDestination(time2NextI);
				*/
			}
			SharedTaxiRouteElement[] aRoute = new SharedTaxiRouteElement[route
					.size()];
			for (int k1 = 0; k1 < route.size(); k1++)
				aRoute[k1] = route.get(k1);

			SharedTaxiRoute r = new SharedTaxiRoute(aRoute, load, "-");
			routes[k] = r;
		}
		int nb2Sharings = 0;
		int nb3Sharings = 0;
		int nbRequests = 0;
		for(int i = 0; i < routes.length; i++){
			if(routes[i].getTicketCodes().length == 2){
				nb2Sharings++;
			}else if(routes[i].getTicketCodes().length == 3){
				nb3Sharings++;
			}
			nbRequests += routes[i].getTicketCodes().length;
		}
		
		return new SharedTaxiSolution(nb2Sharings,nb3Sharings,nbRequests,routes);
	}

	public String name(){
		return "ClusterBasedSolver";
	}
}
