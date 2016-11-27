package com.kse.ezRoutingAPI.dichung.service;

import java.util.ArrayList;
import java.util.Arrays;
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

class DichungSearch extends GenericLocalSearch {
	public ArrayList<Point> clientPoints;

	public DichungSearch(ArrayList<Point> clientPoints, VRManager mgr,
			LexMultiFunctions F,
			ArrayList<INeighborhoodExplorer> neighborhoodExplorer) {
		super(mgr, F, neighborhoodExplorer);
	}

	public DichungSearch(ArrayList<Point> clientPoints, VRManager mgr) {
		super(mgr);
		this.clientPoints = clientPoints;
	}

	public void generateInitialSolution() {
		System.out.println("DichungSearch::generateInitialSolution, XR = "
				+ XR.toString());
		int nbReq = clientPoints.size() / 2;
		for (int i = 0; i < mgr.getVarRoutesVR().getNbRoutes(); i++) {
			Point s = XR.startPoint(i + 1);
			Point p = clientPoints.get(i);
			Point d = clientPoints.get(i + nbReq);
			System.out
					.println("DichungSearch::generateInitialSolution, start addOnePoint("
							+ p.ID + "," + s.ID + ")");
			mgr.performAddOnePoint(p, s);
			mgr.performAddOnePoint(d, p);
			System.out
					.println("DichungSearch::generateInitialSolution, start addOnePoint("
							+ p.ID + "," + s.ID + "), XR = " + XR.toString());
		}
	}

	public void search(int maxIter, int timeLimit) {
		// generateInitialSolution();
		super.search(maxIter, timeLimit);
	}
}

public class DichungService {
	public static double APPX = 1.5;
	public static double EPS = 10;// 10m
	public static double STD_SPEED = 5;// 10m/s
	public static double HIGH_TRAFFIC_SPEED = 2;// 3m/s
	public static double SPEED_TO_AIRPORT = 70;// 70km/h
	public static int DELTA_TIME = 900;// early/late of 15 minutes

	public String name() {
		return "DichungService";
	}

	/*
	 * public SharedTaxiSolution establishSharedTaxiSolution(
	 * ArrayList<SharedRoute> shared_routes, SharedTaxiRequest[] requests,
	 * HashMap<SharedTaxiRequest, Integer> shortestTravelTime, int[][]
	 * travelTimes) {
	 * 
	 * long[] earlyTime = new long[requests.length]; long[] lateTime = new
	 * long[requests.length]; for (int i = 0; i < requests.length; i++) { long
	 * departTime = DateTimeUtils.dateTime2Int(requests[i] .getDepartTime());
	 * earlyTime[i] = departTime - DELTA_TIME;//
	 * DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
	 * lateTime[i] = departTime + DELTA_TIME;//
	 * /DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime()); }
	 * 
	 * SharedTaxiRoute[] routes = new SharedTaxiRoute[shared_routes.size()]; for
	 * (int k = 0; k < shared_routes.size(); k++) { SharedRoute sr =
	 * shared_routes.get(k); int load = 0; ArrayList<SharedTaxiRouteElement>
	 * route = new ArrayList<SharedTaxiRouteElement>(); long[]
	 * suggestedPickupTimePoint = new long[sr.size()]; int[] time2Destination =
	 * new int[sr.size()]; long earliestArrivalTime = earlyTime[sr.get(0)];
	 * 
	 * for (int idx = 0; idx < sr.size() - 1; idx++) { int I = sr.get(idx); int
	 * J = sr.get(idx + 1);
	 * 
	 * suggestedPickupTimePoint[idx] = DateTimeUtils
	 * .computeStartTimePoint(earliestArrivalTime, lateTime[I],
	 * travelTimes[I][J], earlyTime[J], lateTime[J]);
	 * 
	 * earliestArrivalTime = suggestedPickupTimePoint[idx] + travelTimes[I][J];
	 * 
	 * } suggestedPickupTimePoint[sr.size() - 1] = earliestArrivalTime;
	 * time2Destination[sr.size() - 1] = shortestTravelTime
	 * .get(requests[sr.getLast()]); for (int idx = sr.size() - 1; idx > 0;
	 * idx++) { int J = sr.get(idx); int I = sr.get(idx - 1);
	 * time2Destination[idx - 1] = time2Destination[idx] + travelTimes[I][J]; }
	 * 
	 * SharedTaxiRouteElement[] aRoute = new SharedTaxiRouteElement[route
	 * .size()]; for (int k1 = 0; k1 < route.size(); k1++) aRoute[k1] =
	 * route.get(k1);
	 * 
	 * SharedTaxiRoute r = new SharedTaxiRoute(aRoute, load, "-"); routes[k] =
	 * r; } return new SharedTaxiSolution(-1,-1,-1,routes);
	 * 
	 * }
	 */

	public SharedTaxiSolution computeSharedTaxiSolutionCluster(
			SharedTaxiInput input,
			HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng,
			HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng) {

		HashMap<SharedTaxiRequest, Integer> mRequest2ID = new HashMap<SharedTaxiRequest, Integer>();
		SharedTaxiRequest[] requests = input.getRequests();

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

		int N = requests.length;
		for (int i = 0; i < requests.length; i++)
			mRequest2ID.put(requests[i], i);

		GoogleMapsQuery G = new GoogleMapsQuery();

		double[][] estimated_distances = new double[N][N];
		int[][] travelTimes = new int[N][N];// travelTimes[i][j] is the travel
											// time from pickup point of request
											// i to pickup point of request j

		HashMap<SharedTaxiRequest, Integer> shortestTravelTime = new HashMap<SharedTaxiRequest, Integer>();
		for (int i = 0; i < N; i++) {
			/*
			 * int t = G.getTravelTime(requests[i].getPickupAddress(),
			 * requests[i].getDeliveryAddress(), "driving");
			 * 
			 * if (t < 0) { LatLng llp = mPickup2LatLng.get(requests[i]); LatLng
			 * lld = mDelivery2LatLng.get(requests[i]); double d =
			 * G.computeDistanceHaversine(llp.lat, llp.lng, lld.lat, lld.lng); t
			 * = (int) (d * 1000 * APPX / speed); }
			 */
			LatLng llp = mPickup2LatLng.get(requests[i]);
			LatLng lld = mDelivery2LatLng.get(requests[i]);

			// int t = G.estimateTravelTime(llp.lat, llp.lng, lld.lat, lld.lng,
			// "driving",speed,APPX);
			// int t = G.estimateTravelTimeWithTimeFrame(llp.lat, llp.lng,
			// lld.lat, lld.lng, "driving",
			// requests[i].getEarlyPickupDateTime(), STD_SPEED,
			// HIGH_TRAFFIC_SPEED);

			// int d = (int) G.estimateDistanceMeter(llp.lat, llp.lng, lld.lat,
			// lld.lng);
			int d = (int) G.getApproximateDistanceMeter(llp.lat, llp.lng,
					lld.lat, lld.lng);

			int t = (int) Math.floor(d / (SPEED_TO_AIRPORT * 1000 / 3600));

			shortestTravelTime.put(requests[i], t);

		}

		for (int i = 0; i < requests.length; i++) {
			SharedTaxiRequest ri = requests[i];
			LatLng li = mPickup2LatLng.get(ri);
			for (int j = 0; j < requests.length; j++) {
				SharedTaxiRequest rj = requests[j];
				LatLng lj = mPickup2LatLng.get(rj);

				// travelTimes[i][j] = G.estimateTravelTime(li.lat, li.lng,
				// lj.lat, lj.lng, "driving", speed, APPX);

				// travelTimes[i][j] = G.estimateTravelTimeWithTimeFrame(li.lat,
				// li.lng, lj.lat, lj.lng, "driving",
				// ri.getEarlyPickupDateTime(), STD_SPEED, HIGH_TRAFFIC_SPEED);

				// estimated_distances[i][j] = G.estimateDistanceMeter(li.lat,
				// li.lng, lj.lat, lj.lng);
				estimated_distances[i][j] = G.getApproximateDistanceMeter(
						li.lat, li.lng, lj.lat, lj.lng);

				travelTimes[i][j] = (int) (estimated_distances[i][j] / input
						.getStdSpeed());

				// System.out.println(name()
				// + "::computeSharedTaxiSolutionCluster, travelTimes["
				// + ri.getTicketCode() + "," + rj.getTicketCode() + "] = " +
				// travelTimes[i][j]);

				System.out.println(name()
						+ "::computeSharedTaxiSolutionCluster, e_distance["
						+ ri.getTicketCode() + "," + rj.getTicketCode()
						+ "] = " + estimated_distances[i][j]);

			}
		}
		int C = 6;// input.getVehicleCapacities()[1];
		double maxSharedDistance = 6000;// the distance of pickup points of
										// shared a ride requests cannot exceed
										// 6000m
		double maxSharedTime = input.getMaxWaitTime();// 1800;// the difference
														// between pickup time
														// of
		// shared a ride requests cannot exceed 30
		// minutes;

		boolean[][] m = new boolean[N][N];// m[i][j] = 1 if request i can be
											// followed requests j (i -> j ->
											// destination)
		for (int i = 0; i < N; i++) {
			SharedTaxiRequest ri = requests[i];
			int ti = (int) DateTimeUtils.dateTime2Int(ri.getDepartTime());
			// int ei = ti - DELTA_TIME;//(int)
			// DateTimeUtils.dateTime2Int(ri.getEarlyPickupDateTime());
			// int li = ti + DELTA_TIME;//(int)
			// DateTimeUtils.dateTime2Int(ri.getLatePickupDateTime());
			for (int j = 0; j < N; j++) {
				SharedTaxiRequest rj = requests[j];
				int tj = (int) DateTimeUtils.dateTime2Int(rj.getDepartTime());
				// int ej = tj - DELTA_TIME;//(int)
				// DateTimeUtils.dateTime2Int(rj.getEarlyPickupDateTime());
				// int lj = tj + DELTA_TIME;//(int)
				// DateTimeUtils.dateTime2Int(rj.getLatePickupDateTime());

				boolean ok = ri.getNumberPassengers()
						+ rj.getNumberPassengers() <= C;
				/*
				 * ok = ok && (ej <= ei + travelTimes[i][j] && ei +
				 * travelTimes[i][j] <= lj && travelTimes[i][j] <
				 * input.getMaxWaitTime() || ej <= li + travelTimes[i][j] && li
				 * + travelTimes[i][j] <= lj && travelTimes[i][j] <
				 * input.getMaxWaitTime()); // ei <= ej + travelTimes[j][i] &&
				 * ej + travelTimes[j][i] <= li // && travelTimes[j][i] <
				 * input.getMaxWaitTime() || // ei <= ej + travelTimes[j][i] &&
				 * ej + travelTimes[j][i] <= li // && travelTimes[j][i] <
				 * input.getMaxWaitTime());
				 * 
				 * ok = ok & travelTimes[i][j] + shortestTravelTime.get(rj) <
				 * travelTimes[j][i] + shortestTravelTime.get(ri);
				 */

				maxSharedDistance = input.getMaxStandardSharingDistance();// 6000;
				if (DateTimeUtils.isHighTraffic(ri.getDepartTime())) {
					maxSharedDistance = input
							.getMaxHighTrafficSharingDistance();// 3000;
				} else {

				}

				ok = ok & estimated_distances[i][j] <= maxSharedDistance;

				// int mi = (ei + li)/2;
				// int mj = (ej + lj)/2;
				// int mij = mj-mi;
				int mij = tj - ti;

				// if(ri.getTicketCode().equals("TK0016") &&
				// rj.getTicketCode().equals("TK0017")){
				// System.out.println("mj - mi = " + mij + ", maxSharedTime = "
				// + maxSharedTime + ", maxSharedDistance = " +
				// maxSharedDistance
				// + ", distance[i-j] = " + estimated_distances[i][j] +
				// ", ri.pickupDateTime = " + ri.getEarlyPickupDateTime() +
				// ", ok = " + ok);
				// }

				ok = ok & (0 <= mij && mij <= maxSharedTime);// the30 minutes

				ok = ok
						& (ri.getPrice() + rj.getPrice() >= input
								.getMinimumSharedPrice());

				// ok = ok & estimated_distances[i][j] +
				// shortestTravelTime.get(rj) < estimated_distances[j][i] +
				// shortestTravelTime.get(ri);

				m[i][j] = ok;
				// m[j][i] = ok;
			}
		}

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++)
				System.out.println(name()
						+ "::computeSharedTaxiSolutionCluster, m["
						+ requests[i].getTicketCode() + ","
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
			for (int j = 0; j < N; j++)
				if (i != j) {
					if (m[i][j]) {
						outDeg[i]++;
						inDeg[j]++;
					}
				}
		}
		for (int i = 0; i < N; i++) {
			System.out.println(name()
					+ "::computeSharedTaxiSolutionCluster, req "
					+ requests[i].getTicketCode() + ": inDeg = " + inDeg[i]
					+ ", outDeg = " + outDeg[i]);
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
			if (min_i != -1)
				min_i_tk = requests[min_i].getTicketCode();
			if (min_j != -1)
				min_j_tk = requests[min_j].getTicketCode();
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

			// long earlyI =
			// DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
			// long lateI =
			// DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
			long expectedPickupTimePointI = DateTimeUtils
					.dateTime2Int(requests[i].getDepartTime());// (earlyI +
																// lateI) / 2;
			String expectedPickupTimeI = DateTimeUtils
					.unixTimeStamp2DateTime(expectedPickupTimePointI);

			SharedTaxiRouteElement eI = new SharedTaxiRouteElement(
					requests[i].getTicketCode(),
					requests[i].getPickupAddress(),
					requests[i].getDeliveryAddress(), mPickup2LatLng.get(
							requests[i]).toString(), "-", expectedPickupTimeI,
					"-", "-", "-", "-");
			load += requests[i].getNumberPassengers();
			route.add(eI);

			if (j > -1) {
				// String time2NextI =
				// DateTimeUtils.second2HMS(travelTimes[i][j]);
				// eI.setTravelTimeToNext(time2NextI);
				eI.setDistanceToNext((int) estimated_distances[i][j] + "");

				// long earlyJ =
				// DateTimeUtils.dateTime2Int(requests[j].getEarlyPickupDateTime());
				// long lateJ =
				// DateTimeUtils.dateTime2Int(requests[j].getLatePickupDateTime());
				long expectedPickupTimePointJ = DateTimeUtils
						.dateTime2Int(requests[j].getDepartTime());// (earlyJ +
																	// lateJ) /
																	// 2;

				/*
				 * long suggestedPickupTimePointI = DateTimeUtils
				 * .computeStartTimePoint(earlyI, lateJ, travelTimes[i][j],
				 * earlyJ, lateJ); String suggestedPickupTimeI = DateTimeUtils
				 * .unixTimeStamp2DateTime(suggestedPickupTimePointI);
				 * eI.setPickupDateTime(suggestedPickupTimeI);
				 */

				/*
				 * long suggestedPickupTimePointJ = suggestedPickupTimePointI +
				 * travelTimes[i][j];
				 * 
				 * String suggestedPickupTimeJ = DateTimeUtils
				 * .unixTimeStamp2DateTime(suggestedPickupTimePointJ);
				 */

				String expectedPickupTimeJ = DateTimeUtils
						.unixTimeStamp2DateTime(expectedPickupTimePointJ);

				/*
				 * int T = shortestTravelTime.get(requests[j]); String
				 * time2NextJ = DateTimeUtils.second2HMS(T);
				 * 
				 * String time2DestinationI = DateTimeUtils
				 * .second2HMS(travelTimes[i][j] + T);
				 * eI.setTravelTimeToDestination(time2DestinationI);
				 */

				SharedTaxiRouteElement eJ = new SharedTaxiRouteElement(
						requests[j].getTicketCode(),
						requests[j].getPickupAddress(),
						requests[j].getDeliveryAddress(), mPickup2LatLng.get(
								requests[j]).toString(),
						// suggestedPickupTimeJ,
						"-",
						// expectedPickupTimeJ, time2NextJ, time2NextJ, "-");
						expectedPickupTimeJ, "-", "-", "-", "-");

				long t = shortestTravelTime.get(requests[j]);
				t = t + DateTimeUtils.dateTime2Int(requests[j].getDepartTime());
				String travelTimeToDestination = DateTimeUtils
						.unixTimeStamp2DateTime(t);

				eJ.setTravelTimeToDestination(travelTimeToDestination);

				load += requests[j].getNumberPassengers();
				route.add(eJ);
			} else {
				long t = shortestTravelTime.get(requests[i]);
				t = t + DateTimeUtils.dateTime2Int(requests[i].getDepartTime());
				String travelTimeToDestination = DateTimeUtils
						.unixTimeStamp2DateTime(t);

				eI.setTravelTimeToDestination(travelTimeToDestination);

				//
				/*
				 * int T = shortestTravelTime.get(requests[i]); String
				 * time2NextI = DateTimeUtils.second2HMS(T);
				 * eI.setTravelTimeToNext(time2NextI);
				 * eI.setTravelTimeToDestination(time2NextI);
				 */
			}
			SharedTaxiRouteElement[] aRoute = new SharedTaxiRouteElement[route
					.size()];
			for (int k1 = 0; k1 < route.size(); k1++)
				aRoute[k1] = route.get(k1);

			String taxiType = "7-places";
			if (load <= 4)
				taxiType = "5-palces";
			SharedTaxiRoute r = new SharedTaxiRoute(aRoute, taxiType, load, "-");
			routes[k] = r;
		}
		int nb2Sharings = 0;
		for (int i = 0; i < routes.length; i++) {
			if (routes[i].getTicketCodes().length == 2)
				nb2Sharings++;
		}
		int nb3Sharings = 0;
		return new SharedTaxiSolution(nb2Sharings, nb3Sharings,
				requests.length, routes);
	}

	public SharedTaxiSolution computeSharedTaxiSolutionCluster0(
			SharedTaxiInput input,
			HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng,
			HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng, int speed) {

		HashMap<SharedTaxiRequest, Integer> mRequest2ID = new HashMap<SharedTaxiRequest, Integer>();
		SharedTaxiRequest[] requests = input.getRequests();

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

		int N = requests.length;
		for (int i = 0; i < requests.length; i++)
			mRequest2ID.put(requests[i], i);

		GoogleMapsQuery G = new GoogleMapsQuery();

		int[][] travelTimes = new int[N][N];// travelTimes[i][j] is the travel
											// time from pickup point of request
											// i to pickup point of request j

		HashMap<SharedTaxiRequest, Integer> shortestTravelTime = new HashMap<SharedTaxiRequest, Integer>();
		for (int i = 0; i < N; i++) {

			int t = G.getTravelTime(requests[i].getPickupAddress(),
					requests[i].getDeliveryAddress(), "driving");

			if (t < 0) {
				LatLng llp = mPickup2LatLng.get(requests[i]);
				LatLng lld = mDelivery2LatLng.get(requests[i]);
				double d = G.computeDistanceHaversine(llp.lat, llp.lng,
						lld.lat, lld.lng);
				t = (int) (d * 1000 * APPX / speed);
			}
			shortestTravelTime.put(requests[i], t);
		}

		for (int i = 0; i < requests.length; i++) {
			SharedTaxiRequest ri = requests[i];
			LatLng li = mPickup2LatLng.get(ri);
			for (int j = 0; j < requests.length; j++) {
				SharedTaxiRequest rj = requests[j];
				LatLng lj = mPickup2LatLng.get(rj);
				double d = G.computeDistanceHaversine(li.lat, li.lng, lj.lat,
						lj.lng);
				d = d * 1000;
				int appxt = (int) (d * APPX / speed);// approximate travel time

				int t = G.getTravelTime(requests[i].getPickupAddress(),
						requests[j].getPickupAddress(), "driving");
				travelTimes[i][j] = t;
				System.out.println(name() + "::computeSharedTaxiSolution, TT["
						+ i + "," + j + "] = " + travelTimes[i][j]);
				if (travelTimes[i][j] < 0) {
					System.out.println(name()
							+ "::computeSharedTaxiSolution exception from "
							+ requests[i].getPickupAddress() + " to "
							+ requests[j].getPickupAddress()
							+ " --> use approximate time");
					// System.exit(-1);
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
		for (int i = 0; i < requests.length; i++) {
			long t = DateTimeUtils.dateTime2Int(requests[i].getDepartTime());
			minTimePoint = minTimePoint < t ? minTimePoint : t;

			// long t =
			// DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
			// minTimePoint = minTimePoint < t ? minTimePoint : t;
			// t =
			// DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
			// minTimePoint = minTimePoint < t ? minTimePoint : t;
		}
		int dt = 10000;
		int pickupDuration = 60;// 60 seconds
		int D = 10000;// distance from start/end poitns to each client point

		for (int i = 0; i < K; i++) {
			id++;
			Point p = new Point(id);
			clientPoints.add(p);

			long t = DateTimeUtils.dateTime2Int(requests[i].getDepartTime());
			long et = t - DELTA_TIME;// DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
			long lt = t + DELTA_TIME;// DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());

			int converted_et = (int) (et - minTimePoint) + dt;
			int converted_lt = (int) (lt - minTimePoint) + dt;
			earliestAllowedArrivalTime.put(p, converted_et);
			latestAllowedArrivalTime.put(p, converted_lt);
			serviceDuration.put(p, pickupDuration);// pickup duration is assumed
													// to be 1 minute

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

		for (Point s : startPoints) {
			earliestAllowedArrivalTime.put(s, 0);
			latestAllowedArrivalTime.put(s, Integer.MAX_VALUE);
			serviceDuration.put(s, 0);// pickup duration is assumed to be 0
		}
		for (Point t : endPoints) {
			earliestAllowedArrivalTime.put(t, 0);
			latestAllowedArrivalTime.put(t, Integer.MAX_VALUE);
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
				// awm.setWeight(p, t, D);
				// awm.setWeight(t, p, D);
				awm.setWeight(p, t, shortestTravelTime.get(req));
				awm.setWeight(t, p, shortestTravelTime.get(req));
			}
		}
		for (Point s : startPoints) {
			for (Point t : endPoints)
				awm.setWeight(s, t, 0);
		}

		int capacity = 0;
		for (int i = 0; i < input.getVehicleCapacities().length; i++)
			capacity = capacity < input.getVehicleCapacities()[i] ? input
					.getVehicleCapacities()[i] : capacity;

		NodeWeightsManager nwm = new NodeWeightsManager(allPoints);
		for (int i = 0; i < clientPoints.size(); i++)
			nwm.setWeight(clientPoints.get(i),
					requests[i].getNumberPassengers());
		for (Point s : startPoints)
			nwm.setWeight(s, 0);
		for (Point t : endPoints)
			nwm.setWeight(t, 0);

		// model
		VRManager mgr = new VRManager();
		VarRoutesVR XR = new VarRoutesVR(mgr);
		for (int i = 0; i < startPoints.size(); i++) {
			XR.addRoute(startPoints.get(i), endPoints.get(i));
		}
		for (Point p : clientPoints)
			XR.addClientPoint(p);

		ConstraintSystemVR CS = new ConstraintSystemVR(mgr);

		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		AccumulatedWeightNodesVR awn = new AccumulatedWeightNodesVR(XR, nwm);

		IFunctionVR[] d = new IFunctionVR[K];// d[i] is the distance of route
												// i+1
		IFunctionVR[] w = new IFunctionVR[K];// w[i] is the accumulated demand
												// on route i+1
		HashMap<Point, IFunctionVR> mPoint2AccDis = new HashMap<Point, IFunctionVR>();
		HashMap<Point, IFunctionVR> mPoint2IdxRoute = new HashMap<Point, IFunctionVR>();

		for (Point p : clientPoints) {
			IFunctionVR ad = new AccumulatedEdgeWeightsOnPathVR(awe, p);
			mPoint2AccDis.put(p, ad);

			IFunctionVR idx = new RouteIndex(XR, p);
			mPoint2IdxRoute.put(p, idx);
		}
		for (int i = 0; i < K; i++) {
			Point t = XR.endPoint(i + 1);// start point of route i+1
			d[i] = new AccumulatedEdgeWeightsOnPathVR(awe, t);
			w[i] = new AccumulatedNodeWeightsOnPathVR(awn, t);
			CS.post(new Leq(w[i], capacity));
			// CS.post(new Leq(d[i],input.getMaxWaitTime()));
		}

		// HashMap<Point, IFunctionVR> mPoint2TravelTime2Destination = new
		// HashMap<Point, IFunctionVR>();
		for (Point p : clientPoints) {
			SharedTaxiRequest req = mPoint2Request.get(p);
			IFunctionVR idx = mPoint2IdxRoute.get(p);
			int maxD = shortestTravelTime.get(req) + input.getMaxWaitTime();

			IFunctionVR accD = mPoint2AccDis.get(p);
			for (int k = 1; k <= K; k++) {
				IConstraintVR c1 = new Eq(idx, k);
				IFunctionVR f = new Minus(d[k - 1], accD);
				// mPoint2TravelTime2Destination.put(p, f);
				IConstraintVR c2 = new Leq(f, maxD);
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

		// GenericLocalSearch se = new GenericLocalSearch(mgr);
		DichungSearch se = new DichungSearch(clientPoints, mgr);
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.setMaxStable(50);

		se.search(50, input.getMaxTime());

		ArrayList<SharedTaxiRoute> routes = new ArrayList<SharedTaxiRoute>();

		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Point t = XR.endPoint(k);
			if (XR.next(s) != t) {
				ArrayList<Point> points = new ArrayList<Point>();
				for (Point x = XR.next(s); x != t; x = XR.next(x)) {
					points.add(x);
				}
				// points.add(t);
				SharedTaxiRouteElement[] e = new SharedTaxiRouteElement[points
						.size()];
				int load = 0;
				for (int i = 0; i < points.size(); i++) {
					Point p = points.get(i);
					int arrTime = (int) eat.getEarliestArrivalTime(p);
					arrTime += minTimePoint - dt;
					String arrDT = DateTimeUtils
							.unixTimeStamp2DateTime(arrTime);
					SharedTaxiRequest req = mPoint2Request.get(p);
					load += req.getNumberPassengers();
					int maxT = shortestTravelTime.get(req)
							+ input.getMaxWaitTime();
					// IFunctionVR idRoute = mPoint2IdxRoute.get(p);
					// Point t =
					int T = (int) eat.getEarliestArrivalTime(t)
							- (int) eat.getEarliestArrivalTime(p);// (int)mPoint2TravelTime2Destination.get(p).getValue();
					e[i] = new SharedTaxiRouteElement(req.getTicketCode(),
							req.getPickupAddress(), req.getDeliveryAddress(),
							mPickup2LatLng.get(req).toString(), arrDT,
							// req.getLatePickupDateTime(), "-", "-", "-","-");
							req.getDepartTime(), "-", "-", "-", "-");
				}
				int arrTime2Destination = (int) eat.getEarliestArrivalTime(t);
				arrTime2Destination += minTimePoint - dt;
				String time2Destination = DateTimeUtils
						.unixTimeStamp2DateTime(arrTime2Destination);

				String taxiType = "7-places";
				if (load <= 4)
					taxiType = "5-places";

				SharedTaxiRoute r = new SharedTaxiRoute(e, taxiType,
						(int) w[k - 1].getValue(), time2Destination);
				routes.add(r);
			}
		}
		SharedTaxiRoute[] A = new SharedTaxiRoute[routes.size()];
		for (int i = 0; i < routes.size(); i++) {
			A[i] = routes.get(i);
		}
		return new SharedTaxiSolution(-1, -1, -1, A);
	}

	public SharedTaxiSolution computeSharedTaxiSolutionCluster1(
			SharedTaxiInput input,
			HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng,
			HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng, int speed) {

		HashMap<SharedTaxiRequest, Integer> mRequest2ID = new HashMap<SharedTaxiRequest, Integer>();
		SharedTaxiRequest[] requests = input.getRequests();

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

		int N = requests.length;
		for (int i = 0; i < requests.length; i++)
			mRequest2ID.put(requests[i], i);

		GoogleMapsQuery G = new GoogleMapsQuery();

		int[][] travelTimes = new int[N][N];// travelTimes[i][j] is the travel
											// time from pickup point of request
											// i to pickup point of request j

		HashMap<SharedTaxiRequest, Integer> shortestTravelTime = new HashMap<SharedTaxiRequest, Integer>();
		for (int i = 0; i < N; i++) {

			int t = G.getTravelTime(requests[i].getPickupAddress(),
					requests[i].getDeliveryAddress(), "driving");

			if (t < 0) {
				LatLng llp = mPickup2LatLng.get(requests[i]);
				LatLng lld = mDelivery2LatLng.get(requests[i]);
				double d = G.computeDistanceHaversine(llp.lat, llp.lng,
						lld.lat, lld.lng);
				t = (int) (d * 1000 * APPX / speed);
			}
			shortestTravelTime.put(requests[i], t);
		}

		/*
		 * for (int i = 0; i < requests.length; i++) { SharedTaxiRequest ri =
		 * requests[i]; LatLng li= mPickup2LatLng.get(ri); for (int j = 0; j <
		 * requests.length; j++) { SharedTaxiRequest rj = requests[j]; LatLng lj
		 * = mPickup2LatLng.get(rj); double d =
		 * G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng); d = d*1000;
		 * int appxt = (int)(d*APPX/speed);// approximate travel time
		 * 
		 * int t = G.getTravelTime(requests[i].getPickupAddress(),
		 * requests[j].getPickupAddress(), "driving"); travelTimes[i][j] = t;
		 * System.out.println(name() + "::computeSharedTaxiSolution, TT[" + i +
		 * "," + j + "] = " + travelTimes[i][j]); if(travelTimes[i][j] < 0){
		 * System.out.println(name() +
		 * "::computeSharedTaxiSolution exception from " +
		 * requests[i].getPickupAddress() + " to " +
		 * requests[j].getPickupAddress() + " --> use approximate time");
		 * //System.exit(-1); travelTimes[i][j] = appxt; } } }
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
		for (int i = 0; i < requests.length; i++) {
			long t = DateTimeUtils.dateTime2Int(requests[i].getDepartTime());
			minTimePoint = minTimePoint < t ? minTimePoint : t;

			// long t =
			// DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
			// minTimePoint = minTimePoint < t ? minTimePoint : t;
			// t =
			// DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
			// minTimePoint = minTimePoint < t ? minTimePoint : t;
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
			long t = DateTimeUtils.dateTime2Int(requests[i].getDepartTime());

			long et = t - DELTA_TIME;// DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
			long lt = t + DELTA_TIME;// DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
			int converted_et = (int) (et - minTimePoint) + dt;
			int converted_lt = (int) (lt - minTimePoint) + dt;
			earliestAllowedArrivalTime.put(p, converted_et);
			latestAllowedArrivalTime.put(p, converted_lt);
			serviceDuration.put(p, pickupDuration);// pickup duration is assumed
													// to be 1 minute

			allPoints.add(p);
			pickupPoints.add(p);
			mPoint2Request.put(p, requests[i]);
		}

		// create delivery points
		for (int i = 0; i < K; i++) {
			id++;
			Point p = new Point(id);
			clientPoints.add(p);

			// long lt =
			// DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
			// int converted_et = (int)(et - minTimePoint) + dt;
			// int converted_lt = (int)(lt - minTimePoint) + dt;
			Point pickupPoint = pickupPoints.get(i);
			int lt = latestAllowedArrivalTime.get(pickupPoint)
					+ shortestTravelTime.get(requests[i]);
			earliestAllowedArrivalTime.put(p, 0);
			latestAllowedArrivalTime.put(p, lt);
			serviceDuration.put(p, pickupDuration);// pickup duration is assumed
													// to be 1 minute

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

		for (Point s : startPoints) {
			earliestAllowedArrivalTime.put(s, 0);
			latestAllowedArrivalTime.put(s, Integer.MAX_VALUE);
			serviceDuration.put(s, 0);// pickup duration is assumed to be 0
		}
		for (Point t : endPoints) {
			earliestAllowedArrivalTime.put(t, 0);
			latestAllowedArrivalTime.put(t, Integer.MAX_VALUE);
			serviceDuration.put(t, 0);// pickup duration is assumed to be 0
		}

		// setup travel time between points
		LatLng pivotLatLng = mDelivery2LatLng.get(requests[0]);
		Point pivotPoint = clientPoints.get(clientPoints.size() / 2);// first
																		// delivery
																		// point

		ArcWeightsManager awm = new ArcWeightsManager(allPoints);
		for (int i = 0; i < N; i++) {
			SharedTaxiRequest ri = requests[i];
			LatLng ll = mPickup2LatLng.get(ri);
			Point p = clientPoints.get(i);
			double d = G.computeDistanceHaversine(pivotLatLng.lat,
					pivotLatLng.lng, ll.lat, ll.lng);
			int appxt = (int) (d * 1000 * APPX / speed);// approximate
														// traveltime
			int t = G.getTravelTime(pivotLatLng.lat, pivotLatLng.lng, ll.lat,
					ll.lng, "driving");
			if (t < 0)
				t = appxt;
			awm.setWeight(p, pivotPoint, t);
			awm.setWeight(pivotPoint, p, t);
			System.out.println(name()
					+ "::computeSharedTaxiSolutionCluster, traveltime[" + i
					+ ", pivot] = " + t);
		}

		for (int i = 0; i < N; i++) {
			Point pi = clientPoints.get(i);
			LatLng li = mPickup2LatLng.get(requests[i]);
			for (int j = 0; j < N; j++) {
				Point pj = clientPoints.get(j);
				LatLng lj = mPickup2LatLng.get(requests[j]);

				// double d =
				// G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
				// int appxt = (int)(d*1000*APPX/speed);// approximate
				// traveltime
				// int t = G.getTravelTime(li.lat,li.lng,lj.lat,lj.lng,
				// "driving");
				// if(t < 0) t = appxt;
				int t = G.estimateTravelTime(li.lat, li.lng, lj.lat, lj.lng,
						"driving", speed, APPX);
				awm.setWeight(pi, pj, t);

				System.out.println(name()
						+ "::computeSharedTaxiSolutionCluster, traveltime[" + i
						+ "," + j + "] = " + t);
			}

			int t0 = (int) awm.getWeight(pi, pivotPoint);// in seconds
			for (int j = N; j < clientPoints.size(); j++) {
				Point pj = clientPoints.get(j);
				LatLng lj = mDelivery2LatLng.get(requests[j - N]);
				double d = G.computeDistanceHaversine(pivotLatLng.lat,
						pivotLatLng.lng, lj.lat, lj.lng);
				int t = t0;
				if (d * 1000 <= EPS) {// in meters
					// awm.setWeight(pi, pj, t0);
					t = t0;
				} else {
					// d =
					// G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
					// int appxt = (int)(d*1000*APPX/speed);// approximate
					// traveltime
					// int t = G.getTravelTime(li.lat,li.lng,lj.lat,lj.lng,
					// "driving");
					// if(t < 0) t = appxt;
					t = G.estimateTravelTime(li.lat, li.lng, lj.lat, lj.lng,
							"driving", speed, APPX);

				}
				awm.setWeight(pi, pj, t);
				System.out.println(name()
						+ "::computeSharedTaxiSolutionCluster, traveltime[" + i
						+ "," + j + "] = " + t);
			}
		}
		for (int i = N; i < clientPoints.size(); i++) {
			Point pi = clientPoints.get(i);
			LatLng li = mPickup2LatLng.get(requests[i - N]);
			for (int j = 0; j < N; j++) {
				Point pj = clientPoints.get(j);
				LatLng lj = mPickup2LatLng.get(requests[j]);

				// double d =
				// G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
				// int appxt = (int)(d*1000*APPX/speed);// approximate
				// traveltime
				// int t = G.getTravelTime(li.lat,li.lng,lj.lat,lj.lng,
				// "driving");
				// if(t < 0) t = appxt;
				double d = G.computeDistanceHaversine(pivotLatLng.lat,
						pivotLatLng.lng, li.lat, li.lng);
				int t = (int) awm.getWeight(pivotPoint, pj);
				if (d <= EPS) {
					// awm.setWeight(pi, pj, awm.getWeight(pivotPoint, pj));
				} else {
					t = G.estimateTravelTime(li.lat, li.lng, lj.lat, lj.lng,
							"driving", speed, APPX);

				}
				awm.setWeight(pi, pj, t);
				System.out.println(name()
						+ "::computeSharedTaxiSolutionCluster, traveltime[" + i
						+ "," + j + "] = " + t);
			}

			// int t0 = (int)awm.getWeight(pi,pivotPoint);// in seconds
			for (int j = N; j < clientPoints.size(); j++) {
				Point pj = clientPoints.get(j);
				LatLng lj = mDelivery2LatLng.get(requests[j - N]);
				double d = G.computeDistanceHaversine(li.lat, li.lng, lj.lat,
						lj.lng);
				int t = 0;
				if (d * 1000 <= EPS) {// in meters
					// awm.setWeight(pi, pj, 0);
				} else {
					// d =
					// G.computeDistanceHaversine(li.lat,li.lng,lj.lat,lj.lng);
					// int appxt = (int)(d*1000*APPX/speed);// approximate
					// traveltime
					// int t = G.getTravelTime(li.lat,li.lng,lj.lat,lj.lng,
					// "driving");
					// if(t < 0) t = appxt;
					t = G.estimateTravelTime(li.lat, li.lng, lj.lat, lj.lng,
							"driving", speed, APPX);

				}
				awm.setWeight(pi, pj, t);
				System.out.println(name()
						+ "::computeSharedTaxiSolutionCluster, traveltime[" + i
						+ "," + j + "] = " + t);
			}
		}

		/*
		 * for (int i = 0; i < clientPoints.size(); i++) { Point pi =
		 * clientPoints.get(i); for (int j = 0; j < clientPoints.size(); j++) {
		 * Point pj = clientPoints.get(j); //awm.setWeight(clientPoints.get(i),
		 * clientPoints.get(j), travelTimes[i][j]);
		 * 
		 * } }
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
				// awm.setWeight(p,t,shortestTravelTime.get(req));
				// awm.setWeight(t,p,shortestTravelTime.get(req));
			}
		}
		for (Point s : startPoints) {
			for (Point t : endPoints)
				awm.setWeight(s, t, 0);
		}

		int capacity = 0;
		for (int i = 0; i < input.getVehicleCapacities().length; i++)
			capacity = capacity < input.getVehicleCapacities()[i] ? input
					.getVehicleCapacities()[i] : capacity;

		NodeWeightsManager nwm = new NodeWeightsManager(allPoints);
		for (int i = 0; i < clientPoints.size(); i++) {
			if (i < N)// pickup points
				nwm.setWeight(clientPoints.get(i),
						requests[i].getNumberPassengers());
			else
				// delivery points -> demand 0
				nwm.setWeight(clientPoints.get(i), 0);
		}
		for (Point s : startPoints)
			nwm.setWeight(s, 0);
		for (Point t : endPoints)
			nwm.setWeight(t, 0);

		// model
		VRManager mgr = new VRManager();
		VarRoutesVR XR = new VarRoutesVR(mgr);
		for (int i = 0; i < startPoints.size(); i++) {
			XR.addRoute(startPoints.get(i), endPoints.get(i));
		}
		for (Point p : clientPoints)
			XR.addClientPoint(p);

		ConstraintSystemVR CS = new ConstraintSystemVR(mgr);

		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		AccumulatedWeightNodesVR awn = new AccumulatedWeightNodesVR(XR, nwm);

		IFunctionVR[] d = new IFunctionVR[K];// d[i] is the distance of route
												// i+1
		IFunctionVR[] w = new IFunctionVR[K];// w[i] is the accumulated demand
												// on route i+1
		HashMap<Point, IFunctionVR> mPoint2AccDis = new HashMap<Point, IFunctionVR>();
		HashMap<Point, IFunctionVR> mPoint2IdxRoute = new HashMap<Point, IFunctionVR>();

		for (Point p : clientPoints) {
			IFunctionVR ad = new AccumulatedEdgeWeightsOnPathVR(awe, p);
			mPoint2AccDis.put(p, ad);

			IFunctionVR idx = new RouteIndex(XR, p);
			mPoint2IdxRoute.put(p, idx);
		}
		for (int i = 0; i < K; i++) {
			Point t = XR.endPoint(i + 1);// start point of route i+1
			d[i] = new AccumulatedEdgeWeightsOnPathVR(awe, t);
			w[i] = new AccumulatedNodeWeightsOnPathVR(awn, t);
			CS.post(new Leq(w[i], capacity));
			// CS.post(new Leq(d[i],input.getMaxWaitTime()));
		}
		HashMap<Point, IFunctionVR> routeOfPoint = new HashMap<Point, IFunctionVR>();
		HashMap<Point, IFunctionVR> indexOfPointOnRoute = new HashMap<Point, IFunctionVR>();
		for (int i = 0; i < pickupPoints.size(); i++) {
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

		// GenericLocalSearch se = new GenericLocalSearch(mgr);
		DichungSearch se = new DichungSearch(clientPoints, mgr);
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.setMaxStable(50);

		se.search(50, input.getMaxTime());

		ArrayList<SharedTaxiRoute> routes = new ArrayList<SharedTaxiRoute>();

		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Point t = XR.endPoint(k);
			if (XR.next(s) != t) {
				ArrayList<Point> points = new ArrayList<Point>();
				for (Point x = XR.next(s); x != t; x = XR.next(x)) {
					points.add(x);
				}
				// points.add(t);
				SharedTaxiRouteElement[] e = new SharedTaxiRouteElement[points
						.size()];

				int load = 0;
				for (int i = 0; i < points.size(); i++) {
					Point p = points.get(i);
					int arrTime = (int) eat.getEarliestArrivalTime(p);
					arrTime += minTimePoint - dt;
					String arrDT = DateTimeUtils
							.unixTimeStamp2DateTime(arrTime);
					SharedTaxiRequest req = mPoint2Request.get(p);
					load += req.getNumberPassengers();
					int maxT = shortestTravelTime.get(req)
							+ input.getMaxWaitTime();
					// IFunctionVR idRoute = mPoint2IdxRoute.get(p);
					// Point t =
					int T = (int) eat.getEarliestArrivalTime(t)
							- (int) eat.getEarliestArrivalTime(p);// (int)mPoint2TravelTime2Destination.get(p).getValue();
					e[i] = new SharedTaxiRouteElement(req.getTicketCode(),
							req.getPickupAddress(), req.getDeliveryAddress(),
							mPickup2LatLng.get(req).toString(), arrDT,
							// req.getLatePickupDateTime(), "-", "-", "-","-");
							req.getDepartTime(), "-", "-", "-", "-");
				}
				int arrTime2Destination = (int) eat.getEarliestArrivalTime(t);
				arrTime2Destination += minTimePoint - dt;
				String time2Destination = DateTimeUtils
						.unixTimeStamp2DateTime(arrTime2Destination);

				String taxiType = "7-places";
				if (load <= 4)
					taxiType = "5-places";

				SharedTaxiRoute r = new SharedTaxiRoute(e, taxiType,
						(int) w[k - 1].getValue(), time2Destination);
				routes.add(r);
			}
		}
		SharedTaxiRoute[] A = new SharedTaxiRoute[routes.size()];
		for (int i = 0; i < routes.size(); i++) {
			A[i] = routes.get(i);
		}
		return new SharedTaxiSolution(-1, -1, -1, A);
	}

	public SharedTaxiRoute establishRouteForOneRequest(SharedTaxiRequest req,
			String latlng) {
		SharedTaxiRouteElement e = establishRouteElementForOneRequest(req,
				latlng);
		SharedTaxiRouteElement[] rr = new SharedTaxiRouteElement[1];
		rr[0] = e;
		String taxiType = "7-places";
		if (req.getNumberPassengers() <= 4)
			taxiType = "5-places";
		return new SharedTaxiRoute(rr, taxiType, req.getNumberPassengers(), "-");

	}

	public SharedTaxiRouteElement establishRouteElementForOneRequest(
			SharedTaxiRequest req, String latlng) {
		String ticketCode = req.getTicketCode();
		String address = req.getPickupAddress();
		String pickupDateTime = req.getDepartTime();
		String expectedPickupDateTime = req.getDepartTime();
		String travelTimeToDestination = "-";
		String travelTimeToNext = "-";
		String distanceToNext = "-";
		String maxTravelTimeToDestinationAllowed = "-";
		SharedTaxiRouteElement e = new SharedTaxiRouteElement(ticketCode,
				address, req.getDeliveryAddress(), latlng, pickupDateTime,
				expectedPickupDateTime, travelTimeToDestination,
				travelTimeToNext, distanceToNext,
				maxTravelTimeToDestinationAllowed);
		return e;
	}

	public SharedTaxiRoute[] matchTripFromAirport(
			ArrayList<SharedTaxiRequest> req, LatLng llAirport,
			int maxWaitTimeAirport, double maxDistance, int maxNbPlaces) {
		// match requests from the airport to other places (e.g., city center)

		ArrayList<SharedTaxiRoute> A = new ArrayList<SharedTaxiRoute>();

		// sort requests in an increasing order of pickup-time
		SharedTaxiRequest[] a = new SharedTaxiRequest[req.size()];
		for (int i = 0; i < req.size(); i++)
			a[i] = req.get(i);
		for (int i = 0; i < a.length - 1; i++) {
			for (int j = i + 1; j < a.length; j++) {
				long di = DateTimeUtils.dateTime2Int(a[i].getDepartTime());
				long dj = DateTimeUtils.dateTime2Int(a[j].getDepartTime());
				if (di > dj) {
					SharedTaxiRequest tmp = a[i];
					a[i] = a[j];
					a[j] = tmp;
				}
			}
		}
		GoogleMapsQuery G = new GoogleMapsQuery();
		// scan and match requests in a greedy way
		int i = 0;
		while (i < a.length) {
			int load = a[i].getNumberPassengers();
			ArrayList<SharedTaxiRequest> cluster = new ArrayList<SharedTaxiRequest>();
			cluster.add(a[i]);

			ArrayList<SharedTaxiRouteElement> E = new ArrayList<SharedTaxiRouteElement>();
			E.add(establishRouteElementForOneRequest(a[i], "latlng"));

			long di = DateTimeUtils.dateTime2Int(a[i].getDepartTime());
			int j = i + 1;
			while (j < a.length) {
				long dj = DateTimeUtils.dateTime2Int(a[j].getDepartTime());
				if (dj - di > maxWaitTimeAirport) {
					break;
				}

				boolean distanceOK = true;
				for (int k = 0; k < cluster.size(); k++) {
					double d = G.computeDistanceHaversine(
							a[j].getDeliveryPos(), a[k].getDeliveryPos());
					if (d > maxDistance) {
						distanceOK = false;
						break;
					}
				}
				if (!distanceOK)
					break;
				if (load + a[j].getNumberPassengers() > maxNbPlaces)
					break;
				load += a[j].getNumberPassengers();
				// add request a[j] to the cluster
				cluster.add(a[j]);
				E.add(establishRouteElementForOneRequest(a[j], "latlng"));

				j++;
			}
			String taxiType = "5-places";
			if (load > 4)
				taxiType = "7-places";

			// establish a shared-route
			SharedTaxiRouteElement[] AE = new SharedTaxiRouteElement[E.size()];
			for (int k = 0; k < E.size(); k++)
				AE[k] = E.get(k);
			SharedTaxiRoute r = new SharedTaxiRoute(AE, taxiType, load, "--");
			A.add(r);

			i = j;
		}
		SharedTaxiRoute[] B = new SharedTaxiRoute[A.size()];
		for (int j = 0; j < A.size(); j++)
			B[j] = A.get(j);
		return B;
	}

	public SharedTaxiSolution matchReturnTrips(
			SharedTaxiRoute[] routesToAirport,
			SharedTaxiRoute[] routesFromAirport, int maxWaitTimeAirport) {
		GoogleMapsQuery G = new GoogleMapsQuery();
		boolean[][] m = new boolean[routesToAirport.length][routesFromAirport.length];
		for (int i = 0; i < routesToAirport.length; i++)
			for (int j = 0; j < routesFromAirport.length; j++)
				m[i][j] = false;
		for (int i = 0; i < routesToAirport.length; i++) {
			SharedTaxiRouteElement e = routesToAirport[i].getTicketCodes()[routesToAirport[i]
					.getTicketCodes().length - 1];
			for (int j = 0; j < routesFromAirport.length; j++) {
				SharedTaxiRouteElement ej = routesToAirport[j].getTicketCodes()[routesToAirport[j]
						.getTicketCodes().length - 1];

				long ti = DateTimeUtils.dateTime2Int(e
						.getTravelTimeToDestination());
				long tj = DateTimeUtils.dateTime2Int(e
						.getExpectedPickupDateTime());
				if (ti < tj && tj - ti <= maxWaitTimeAirport)
					m[i][j] = true;
			}
		}
		ArrayList<Integer> I = new ArrayList<Integer>();//
		ArrayList<Integer> J = new ArrayList<Integer>();
		boolean[] selected_routes = new boolean[routesToAirport.length];// selected_routes[i]
																		// =
																		// true
																		// if
																		// route
																		// i has
																		// been
																		// selected
																		// for
																		// sharing
		boolean[] selected_requests = new boolean[routesFromAirport.length];// selected_requests[j]
																			// =
																			// true
																			// if
																			// request
																			// j
																			// has
																			// been
																			// selected
																			// for
																			// sharing
		Arrays.fill(selected_routes, false);
		Arrays.fill(selected_requests, false);
		while (true) {
			boolean found = false;
			int sel_i = -1;
			int sel_j = -1;
			for (int i = 0; i < routesToAirport.length; i++)
				if (!selected_routes[i]) {
					if (found)
						break;
					for (int j = 0; j < routesFromAirport.length; j++)
						if (!selected_requests[j]) {
							if (found)
								break;
							if (m[i][j]) {
								sel_i = i;
								sel_j = j;
								found = true;
								break;
							}
						}
				}
			if (!found)
				break;
			selected_routes[sel_i] = true;
			selected_requests[sel_j] = true;
			I.add(sel_i);
			J.add(sel_j);
		}
		for (int k = 0; k < I.size(); k++) {
			int i = I.get(k);
			SharedTaxiRoute ri = routesToAirport[i];
			int j = J.get(k);
			SharedTaxiRoute rj = routesFromAirport[j];

			// SharedTaxiRouteElement e =
			// establishRouteElementForOneRequest(req.get(j),mPickup2LatLng.get(req.get(j)).toString());

			SharedTaxiRouteElement[] a = new SharedTaxiRouteElement[ri
					.getTicketCodes().length + rj.getTicketCodes().length];
			for (int i1 = 0; i1 < ri.getTicketCodes().length; i1++) {
				a[i1] = ri.getTicketCodes()[i1];
			}

			for (int i2 = 0; i2 < rj.getTicketCodes().length; i2++) {
				a[ri.getTicketCodes().length + i2] = rj.getTicketCodes()[i2];
			}

			// int load = r.getNbPeople() < req.get(j).getNumberPassengers() ?
			// req.get(j).getNumberPassengers() : r.getNbPeople();
			int load = ri.getNbPeople() < rj.getNbPeople() ? rj.getNbPeople()
					: ri.getNbPeople();

			String taxiType = "7-places";
			if (load <= 4)
				taxiType = "5-places";

			routesToAirport[i] = new SharedTaxiRoute(a, taxiType, 0,
					ri.getArrTimeDestination());
		}
		ArrayList<SharedTaxiRoute> R = new ArrayList<SharedTaxiRoute>();
		for (int j = 0; j < routesFromAirport.length; j++)
			if (!selected_requests[j]) {
				// R.add(establishRouteForOneRequest(req.get(j),mPickup2LatLng.get(req.get(j)).toString()));
				R.add(routesFromAirport[j]);
			}

		for (int i = 0; i < routesToAirport.length; i++) {
			R.add(0, routesToAirport[i]);
		}

		SharedTaxiRoute[] arr = new SharedTaxiRoute[R.size()];
		for (int i = 0; i < R.size(); i++)
			arr[i] = R.get(i);
		System.out.println(name() + "::matchReturnTrips, nbRoutes = "
				+ arr.length + ", nbReturnSharing = " + I.size());
		int nb2Sharings = 0;
		int nb3Sharings = 0;
		int nbRequests = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].getTicketCodes().length == 2) {
				nb2Sharings++;
			} else if (arr[i].getTicketCodes().length == 3) {
				nb3Sharings++;
			}
			nbRequests += arr[i].getTicketCodes().length;
		}

		// sort arr in decresing order of nbSHarings
		for (int i = 0; i < arr.length - 1; i++) {
			for (int j = i + 1; j < arr.length; j++) {
				if (arr[i].getTicketCodes().length < arr[j].getTicketCodes().length) {
					SharedTaxiRoute tmp = arr[i];
					arr[i] = arr[j];
					arr[j] = tmp;
				}
			}
		}
		return new SharedTaxiSolution(nb2Sharings, nb3Sharings, nbRequests, arr);

	}

	public SharedTaxiSolution matchReturnTrips(SharedTaxiRoute[] routes,
			ArrayList<SharedTaxiRequest> req,
			HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng,
			LatLng llNoiBaiAirport, int maxWaitTimeAirport) {
		GoogleMapsQuery G = new GoogleMapsQuery();

		boolean[][] m = new boolean[routes.length][req.size()];
		for (int i = 0; i < routes.length; i++)
			for (int j = 0; j < req.size(); j++)
				m[i][j] = false;
		for (int i = 0; i < routes.length; i++) {
			SharedTaxiRouteElement e = routes[i].getTicketCodes()[routes[i]
					.getTicketCodes().length - 1];
			for (int j = 0; j < req.size(); j++) {
				LatLng ll = mPickup2LatLng.get(req.get(j));
				double d = G.computeDistanceHaversine(ll.lat, ll.lng,
						llNoiBaiAirport.lat, llNoiBaiAirport.lng);
				if (d >= EPS)
					continue;
				long ti = DateTimeUtils.dateTime2Int(e
						.getTravelTimeToDestination());
				long tj = DateTimeUtils
						.dateTime2Int(req.get(j).getDepartTime());
				if (ti < tj && tj - ti <= maxWaitTimeAirport)
					m[i][j] = true;
			}
		}
		ArrayList<Integer> I = new ArrayList<Integer>();//
		ArrayList<Integer> J = new ArrayList<Integer>();
		boolean[] selected_routes = new boolean[routes.length];// selected_routes[i]
																// = true if
																// route i has
																// been selected
																// for sharing
		boolean[] selected_requests = new boolean[req.size()];// selected_requests[j]
																// = true if
																// request j has
																// been selected
																// for sharing
		Arrays.fill(selected_routes, false);
		Arrays.fill(selected_requests, false);
		while (true) {
			boolean found = false;
			int sel_i = -1;
			int sel_j = -1;
			for (int i = 0; i < routes.length; i++)
				if (!selected_routes[i]) {
					if (found)
						break;
					for (int j = 0; j < req.size(); j++)
						if (!selected_requests[j]) {
							if (found)
								break;
							if (m[i][j]) {
								sel_i = i;
								sel_j = j;
								found = true;
								break;
							}
						}
				}
			if (!found)
				break;
			selected_routes[sel_i] = true;
			selected_requests[sel_j] = true;
			I.add(sel_i);
			J.add(sel_j);
		}
		for (int k = 0; k < I.size(); k++) {
			int i = I.get(k);
			SharedTaxiRoute r = routes[i];
			int j = J.get(k);
			SharedTaxiRouteElement e = establishRouteElementForOneRequest(
					req.get(j), mPickup2LatLng.get(req.get(j)).toString());
			SharedTaxiRouteElement[] a = new SharedTaxiRouteElement[r
					.getTicketCodes().length + 1];
			for (int i1 = 0; i1 < r.getTicketCodes().length; i1++) {
				a[i1] = r.getTicketCodes()[i1];
			}
			a[r.getTicketCodes().length] = e;
			int load = r.getNbPeople() < req.get(j).getNumberPassengers() ? req
					.get(j).getNumberPassengers() : r.getNbPeople();

			String taxiType = "7-places";
			if (load <= 4)
				taxiType = "5-places";

			routes[i] = new SharedTaxiRoute(a, taxiType, r.getNbPeople(),
					r.getArrTimeDestination());
		}
		ArrayList<SharedTaxiRoute> R = new ArrayList<SharedTaxiRoute>();
		for (int j = 0; j < req.size(); j++)
			if (!selected_requests[j]) {
				R.add(establishRouteForOneRequest(req.get(j), mPickup2LatLng
						.get(req.get(j)).toString()));
			}
		for (int i = 0; i < routes.length; i++) {
			R.add(0, routes[i]);
		}

		SharedTaxiRoute[] arr = new SharedTaxiRoute[R.size()];
		for (int i = 0; i < R.size(); i++)
			arr[i] = R.get(i);
		System.out.println(name() + "::matchReturnTrips, nbRoutes = "
				+ arr.length + ", nbReturnSharing = " + I.size());
		int nb2Sharings = 0;
		int nb3Sharings = 0;
		int nbRequests = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].getTicketCodes().length == 2) {
				nb2Sharings++;
			} else if (arr[i].getTicketCodes().length == 3) {
				nb3Sharings++;
			}
			nbRequests += arr[i].getTicketCodes().length;
		}

		// sort arr in decresing order of nbSHarings
		for (int i = 0; i < arr.length - 1; i++) {
			for (int j = i + 1; j < arr.length; j++) {
				if (arr[i].getTicketCodes().length < arr[j].getTicketCodes().length) {
					SharedTaxiRoute tmp = arr[i];
					arr[i] = arr[j];
					arr[j] = tmp;
				}
			}
		}
		return new SharedTaxiSolution(nb2Sharings, nb3Sharings, nbRequests, arr);
	}

	public SharedTaxiSolution computeSharedTaxiHanoiNoiBaiSolution(
			SharedTaxiInput input) {
		this.APPX = input.getApproximationDistanceFactor();
		this.STD_SPEED = input.getStdSpeed();
		this.HIGH_TRAFFIC_SPEED = input.getHighTrafficSpeed();
		this.EPS = input.getEps();
		this.DELTA_TIME = input.getDeltaRequestTime();

		int maxNbPlaces = 0;
		for (int i = 0; i < input.getVehicleCapacities().length; i++) {
			if (maxNbPlaces < input.getVehicleCapacities()[i])
				maxNbPlaces = input.getVehicleCapacities()[i];
		}

		GoogleMapsQuery G = new GoogleMapsQuery();
		String airport = input.getAirportAddress();// "Noi Bai International Airport, Phú Cường, Hanoi, Vietnam";
		String ll = input.getAirportPos();// G.getLatLngFromAddress(noiBaiAirport);
		if (ll.equals("") || ll == null)
			ll = "21.218845, 105.804149";
		LatLng llAirport = new LatLng(ll);// G.getCoordinate(noiBaiAirport);

		if (llAirport == null)
			return new SharedTaxiSolution();
		String unknownLatLng = "100000,100000";

		for (int i = 0; i < input.getRequests().length; i++) {
			SharedTaxiRequest r = input.getRequests()[i];
			if (r.getPickupPos().equals("-")) {
				LatLng l = G.getCoordinate(r.getPickupAddress());
				System.out
						.println(name()
								+ "::computeSharedTaxiHanoiNoiBaiSolution, query pickup position of "
								+ r.getPickupAddress() + " = " + l.toString());
				r.setPickupPos(l.lat + "," + l.lng);
			}
			if (r.getDeliveryPos().equals("-")) {
				LatLng l = G.getCoordinate(r.getDeliveryAddress());
				System.out
						.println(name()
								+ "::computeSharedTaxiHanoiNoiBaiSolution, query delivery position of "
								+ r.getDeliveryAddress() + " = " + l.toString());
				r.setDeliveryPos(l.lat + "," + l.lng);
			}
		}

		ArrayList<SharedTaxiRequest> requestShareToAirport = new ArrayList<SharedTaxiRequest>();
		ArrayList<SharedTaxiRequest> requestShareFromAirport = new ArrayList<SharedTaxiRequest>();
		ArrayList<SharedTaxiRequest> requestPrivateToAirport = new ArrayList<SharedTaxiRequest>();
		ArrayList<SharedTaxiRequest> requestPrivateFromAirport = new ArrayList<SharedTaxiRequest>();
		ArrayList<SharedTaxiRequest> remainShareRequests = new ArrayList<SharedTaxiRequest>();
		ArrayList<SharedTaxiRequest> remainPrivateRequests = new ArrayList<SharedTaxiRequest>();

		
		HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng = new HashMap<SharedTaxiRequest, LatLng>();
		HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng = new HashMap<SharedTaxiRequest, LatLng>();

		SharedTaxiRequest[] requests = input.getRequests();
		boolean[] latlngOK = new boolean[requests.length];

		for (int i = 0; i < requests.length; i++) {

			if (requests[i].getShared() == 0) {
				// private requests
				String latlngPickup = requests[i].getPickupPos();// G.getLatLngFromAddress(requests[i].getPickupAddress());
				String latlngDelivery = requests[i].getDeliveryPos();// G.getLatLngFromAddress(requests[i].getDeliveryAddress());

				System.out.println(name()
						+ "::computeSharedTaxiHanoiNoiBaiSolution, request "
						+ requests[i].getPickupAddress() + " --> "
						+ latlngPickup + ", "
						+ requests[i].getDeliveryAddress() + " --> "
						+ latlngDelivery);

				if (!latlngPickup.equals("") && !latlngDelivery.equals("")) {
					LatLng pickupLocation = new LatLng(latlngPickup);
					LatLng deliveryLocation = new LatLng(latlngDelivery);
					mPickup2LatLng.put(requests[i], pickupLocation);
					mDelivery2LatLng.put(requests[i], deliveryLocation);
					latlngOK[i] = true;

					double d = G.computeDistanceHaversine(deliveryLocation.lat,
							deliveryLocation.lng, llAirport.lat, llAirport.lng);
					
					System.out.println(name()
							+ "::computeSharedTaxiHanoiNoiBaiSolution, d = "
							+ d + ", EPS = " + EPS + ", airport = "
							+ llAirport.toString() + ", delivery = "
							+ deliveryLocation.toString());
					if (d < EPS) {
						requestPrivateToAirport.add(requests[i]);
					}else{
						d = G.computeDistanceHaversine(pickupLocation.lat,
								pickupLocation.lng, llAirport.lat, llAirport.lng);
						if(d < EPS){
							requestPrivateFromAirport.add(requests[i]);
						}else{
							remainPrivateRequests.add(requests[i]);
						}
					}

				} else {
					mPickup2LatLng.put(requests[i], new LatLng(unknownLatLng));
					mDelivery2LatLng
							.put(requests[i], new LatLng(unknownLatLng));
					latlngOK[i] = false;
					remainPrivateRequests.add(requests[i]);
				}
				
			} else {
				// sharing requests
				String latlngPickup = requests[i].getPickupPos();// G.getLatLngFromAddress(requests[i].getPickupAddress());
				String latlngDelivery = requests[i].getDeliveryPos();// G.getLatLngFromAddress(requests[i].getDeliveryAddress());

				System.out.println(name()
						+ "::computeSharedTaxiHanoiNoiBaiSolution, request "
						+ requests[i].getPickupAddress() + " --> "
						+ latlngPickup + ", "
						+ requests[i].getDeliveryAddress() + " --> "
						+ latlngDelivery);

				if (!latlngPickup.equals("") && !latlngDelivery.equals("")) {
					LatLng pickupLocation = new LatLng(latlngPickup);
					LatLng deliveryLocation = new LatLng(latlngDelivery);
					mPickup2LatLng.put(requests[i], pickupLocation);
					mDelivery2LatLng.put(requests[i], deliveryLocation);
					latlngOK[i] = true;

					double d = G.computeDistanceHaversine(deliveryLocation.lat,
							deliveryLocation.lng, llAirport.lat, llAirport.lng);
					
					System.out.println(name()
							+ "::computeSharedTaxiHanoiNoiBaiSolution, d = "
							+ d + ", EPS = " + EPS + ", airport = "
							+ llAirport.toString() + ", delivery = "
							+ deliveryLocation.toString());
					if (d < EPS) {
						requestShareToAirport.add(requests[i]);
					}else{
						d = G.computeDistanceHaversine(pickupLocation.lat,
								pickupLocation.lng, llAirport.lat, llAirport.lng);
						if(d < EPS){
							requestShareFromAirport.add(requests[i]);
						}else{
							remainShareRequests.add(requests[i]);
						}
					}

				} else {
					mPickup2LatLng.put(requests[i], new LatLng(unknownLatLng));
					mDelivery2LatLng
							.put(requests[i], new LatLng(unknownLatLng));
					latlngOK[i] = false;
					remainShareRequests.add(requests[i]);
				}

			}
		}

		SharedTaxiRequest[] R = new SharedTaxiRequest[requestShareToAirport
				.size()];
		for (int i = 0; i < requestShareToAirport.size(); i++)
			R[i] = requestShareToAirport.get(i);
		SharedTaxiInput input1 = new SharedTaxiInput(input.getAirportAddress(),
				input.getAirportPos(), R, input.getVehicleCapacities(),
				input.getMinimumSharedPrice(), input.getMaxWaitTime(),
				input.getForbidenStraightDistance(),
				input.getForbidenTimeDistance(),
				input.getMaxStandardSharingDistance(),
				input.getMaxHighTrafficSharingDistance(),
				input.getMaxWaitTimeAirport(), input.getMinWaitTimeAirport(),
				input.getMaxTime(), input.getApproximationDistanceFactor(),
				input.getEps(), input.getStdSpeed(),
				input.getHighTrafficSpeed(), input.getSpeedToAirport(),
				input.getDeltaRequestTime());

		// solution routes to airport
		SharedTaxiSolution solutionToAirport = computeSharedTaxiHanoiToNoiBaiSolution(
				input1, mPickup2LatLng, mDelivery2LatLng);

		
		SharedTaxiRoute[] RFA = matchTripFromAirport(requestShareFromAirport,
				llAirport, input.getMaxWaitTimeAirport(),
				input.getMaxStandardSharingDistance(), maxNbPlaces);

		SharedTaxiSolution solutionToAndFromAirport = matchReturnTrips(solutionToAirport.getRoutes(), RFA, 1800);

		SharedTaxiRoute[] remainRoutes = new SharedTaxiRoute[remainPrivateRequests.size() + remainShareRequests.size()];
		for(int i = 0;i < remainPrivateRequests.size(); i++){
			remainRoutes[i] = establishRouteForOneRequest(remainPrivateRequests.get(i), remainPrivateRequests.get(i).getPickupPos());
		}
		for(int i = 0;i < remainShareRequests.size(); i++){
			remainRoutes[i + remainPrivateRequests.size()] = establishRouteForOneRequest(remainShareRequests.get(i), remainShareRequests.get(i).getPickupPos());
		}
		SharedTaxiRoute[] allRoutes = new SharedTaxiRoute[remainRoutes.length + solutionToAndFromAirport.getRoutes().length];
		
		for(int i = 0; i < solutionToAndFromAirport.getRoutes().length; i++)
			allRoutes[i] = solutionToAndFromAirport.getRoutes()[i];
		for(int i = 0; i < remainRoutes.length; i++)
			allRoutes[i + solutionToAndFromAirport.getRoutes().length] = remainRoutes[i];
		
		return new SharedTaxiSolution(solutionToAndFromAirport.getNb2Sharings(), solutionToAndFromAirport.getNb3Sharings(), requests.length, allRoutes);
				
		
		// return matchReturnTrips(solutionHanoiToNoiBai.getRoutes(),
		// remainRequestHanoiNoiBai,
		// mPickup2LatLng, llAirport, 1800);

		/*
		 * // establish routes for remaining requests SharedTaxiRoute[] rRoutes
		 * = new SharedTaxiRoute[remainRequestHanoiNoiBai .size()]; for (int i =
		 * 0; i < rRoutes.length; i++) { SharedTaxiRequest req =
		 * remainRequestHanoiNoiBai.get(i); SharedTaxiRouteElement e =
		 * establishRouteElementForOneRequest(req); SharedTaxiRouteElement[] rr
		 * = new SharedTaxiRouteElement[1]; rr[0] = e; rRoutes[i] = new
		 * SharedTaxiRoute(rr, req.getNumberPassengers(), "-"); }
		 * 
		 * SharedTaxiRoute[] routes = new
		 * SharedTaxiRoute[solutionHanoiToNoiBai.getRoutes().length +
		 * rRoutes.length]; int ind = -1; for(int i = 0; i <
		 * solutionHanoiToNoiBai.getRoutes().length;i++){ ind++; routes[ind] =
		 * solutionHanoiToNoiBai.getRoutes()[i]; } for(int i = 0; i <
		 * rRoutes.length; i++){ ind++; routes[ind] = rRoutes[i]; }
		 * System.out.println(name() +
		 * "::computeSharedTaxiHanoiNoibaiSolution, nbRoutes = " + routes.length
		 * + ", nbRequests = " + input.getRequests().length);
		 * 
		 * return new SharedTaxiSolution(routes);
		 */
	}

	// applied for Hanoi --> Noibai
	public SharedTaxiSolution computeSharedTaxiHanoiToNoiBaiSolution(
			SharedTaxiInput input,
			HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng,
			HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng) {

		HashMap<SharedTaxiRequest, Integer> mRequest2ID = new HashMap<SharedTaxiRequest, Integer>();
		SharedTaxiRequest[] requests = input.getRequests();

		// HashMap<SharedTaxiRequest, LatLng> mPickup2LatLng = new
		// HashMap<SharedTaxiRequest, LatLng>();
		// HashMap<SharedTaxiRequest, LatLng> mDelivery2LatLng = new
		// HashMap<SharedTaxiRequest, LatLng>();

		int N = requests.length;
		for (int i = 0; i < requests.length; i++)
			mRequest2ID.put(requests[i], i);

		GoogleMapsQuery G = new GoogleMapsQuery();
		// String noiBaiAirport =
		// "Noi Bai International Airport, Phú Cường, Hanoi, Vietnam";
		// LatLng llNoiBaiAirport = G.getCoordinate(noiBaiAirport);

		int[][] distances = new int[N][N];// travelTimes[i][j] is the travel
											// time from pickup point of request
											// i to pickup point of request j

		// String unknownLatLng = "100000,100000";
		// boolean[] latlngOK = new boolean[requests.length];

		/*
		 * for (int i = 0; i < requests.length; i++) { String latlngPickup =
		 * G.getLatLngFromAddress(requests[i] .getPickupAddress()); String
		 * latlngDelivery = G.getLatLngFromAddress(requests[i]
		 * .getDeliveryAddress());
		 * 
		 * System.out.println(name() + "::computeSharedTaxiSolution, request " +
		 * requests[i].getPickupAddress() + " --> " + latlngPickup + ", " +
		 * requests[i].getDeliveryAddress() + " --> " + latlngDelivery);
		 * 
		 * if(!latlngPickup.equals("") && !latlngDelivery.equals("")){
		 * mPickup2LatLng.put(requests[i], new LatLng(latlngPickup));
		 * mDelivery2LatLng.put(requests[i], new LatLng(latlngDelivery));
		 * latlngOK[i] = true; }else{ mPickup2LatLng.put(requests[i], new
		 * LatLng(unknownLatLng)); mDelivery2LatLng.put(requests[i], new
		 * LatLng(unknownLatLng)); latlngOK[i] = false; } }
		 */

		System.out.println(name()
				+ "::computeSharedTaxiHanoiToNoiBaiSolution, N = " + N);

		for (int i = 0; i < requests.length; i++) {
			SharedTaxiRequest ri = requests[i];
			LatLng li = mPickup2LatLng.get(ri);
			for (int j = 0; j < requests.length; j++) {
				SharedTaxiRequest rj = requests[j];
				LatLng lj = mPickup2LatLng.get(rj);
				/*
				 * double d = G.computeDistanceHaversine(li.lat, li.lng, lj.lat,
				 * lj.lng); d = d * 1000 * APPX; travelTimes[i][j] = (int) (d /
				 * speed);
				 */

				// if(latlngOK[i] && latlngOK[j]){
				// travelTimes[i][j] = G.estimateTravelTime(li.lat, li.lng,
				// lj.lat,
				// lj.lng, "driving", speed, APPX);
				distances[i][j] = (int) G.computeDistanceHaversine(li.lat,
						li.lng, lj.lat, lj.lng) * 1000;// in meters
				// }else{
				// distances[i][j] = Integer.MAX_VALUE;
				// }

				// int t = G.getTravelTime(requests[i].getPickupAddress(),
				// requests[j].getPickupAddress(), "driving");

				// travelTimes[i][j] = t;
				System.out.println(name()
						+ "::computeSharedTaxiHanoiNoiBaiSolution, TT[" + i
						+ "," + j + "] = " + distances[i][j]);
				if (distances[i][j] < 0) {
					System.out
							.println(name()
									+ "::computeSharedTaxiHanoiNoiBaiSolution exception from "
									+ requests[i].getPickupAddress() + " to "
									+ requests[j].getPickupAddress());
					System.exit(-1);
				}
			}
		}

		int[][] A = new int[N][N];
		for (int i = 0; i < N - 1; i++) {
			String dt1 = requests[i].getDepartTime();// DateTimeUtils.meanDatetime(requests[i].getEarlyPickupDateTime(),
														// requests[i].getLatePickupDateTime());
			for (int j = i + 1; j < N; j++) {
				String dt2 = requests[j].getDepartTime();// DateTimeUtils.meanDatetime(requests[j].getEarlyPickupDateTime(),
															// requests[j].getLatePickupDateTime());

				A[i][j] = 1;
				if (distances[i][j] >= input.getForbidenStraightDistance())
					A[i][j] = 0;
				if (Math.abs(DateTimeUtils.distance(dt1, dt2)) >= input
						.getForbidenTimeDistance())
					A[i][j] = 0;

				A[j][i] = A[i][j];
			}
		}

		DFS dfs = new DFS();
		ArrayList<ArrayList<Integer>> CC = dfs.computeConnectedComponents(A);

		ArrayList<SharedTaxiRoute> routes = new ArrayList<SharedTaxiRoute>();

		HashMap<SharedTaxiRequest, Double> extraDistance = new HashMap<SharedTaxiRequest, Double>();
		int maxWaitTime = 1800;
		for (int i = 0; i < requests.length; i++)
			extraDistance.put(requests[i], 2000.0);

		for (ArrayList<Integer> cc : CC) {
			SharedTaxiRequest[] R = new SharedTaxiRequest[cc.size()];
			for (int i = 0; i < cc.size(); i++) {
				R[i] = requests[cc.get(i)];
			}

			SharedTaxiInput I = new SharedTaxiInput(input.getAirportAddress(),
					input.getAirportPos(), R, input.getVehicleCapacities(),
					input.getMinimumSharedPrice(), input.getMaxWaitTime(),
					input.getForbidenStraightDistance(),
					input.getForbidenTimeDistance(),
					input.getMaxStandardSharingDistance(),
					input.getMaxHighTrafficSharingDistance(),
					input.getMaxWaitTimeAirport(),
					input.getMinWaitTimeAirport(), input.getMaxTime(),
					input.getApproximationDistanceFactor(), input.getEps(),
					input.getStdSpeed(), input.getHighTrafficSpeed(),
					input.getSpeedToAirport(), input.getDeltaRequestTime());

			// ClusterBasedSolver CS = new
			// ClusterBasedSolver(I,mPickup2LatLng,mDelivery2LatLng,extraDistance,maxWaitTime);

			SharedTaxiSolution sol = computeSharedTaxiSolutionCluster(I,
					mPickup2LatLng, mDelivery2LatLng);
			// SharedTaxiSolution sol = CS.compute3SharedTaxiSolution();

			for (SharedTaxiRoute r : sol.getRoutes())
				routes.add(r);
		}
		SharedTaxiRoute[] arr_routes = new SharedTaxiRoute[routes.size()];
		for (int i = 0; i < routes.size(); i++)
			arr_routes[i] = routes.get(i);

		for (ArrayList<Integer> cc : CC) {
			System.out.println(name() + "::computeSharedTaxiSolution, cc: ");
			for (int i : cc)
				System.out.print(requests[i].getPickupAddress() + "\n");
			System.out.println();
		}

		int nb2Sharings = 0;
		int nb3Sharings = 0;
		int nbRequests = 0;
		for (int i = 0; i < arr_routes.length; i++) {
			if (arr_routes[i].getTicketCodes().length == 2) {
				nb2Sharings++;
			} else if (arr_routes[i].getTicketCodes().length == 3) {
				nb3Sharings++;
			}
			nbRequests += arr_routes[i].getTicketCodes().length;
		}

		return new SharedTaxiSolution(nb2Sharings, nb3Sharings, nbRequests,
				arr_routes);

		/*
		 * HashMap<Point, SharedTaxiRequest> mPoint2Request = new HashMap<Point,
		 * SharedTaxiRequest>();
		 * 
		 * int K = requests.length;// init nbVehicles ArrayList<Point>
		 * startPoints = new ArrayList<Point>(); ArrayList<Point> endPoints =
		 * new ArrayList<Point>(); ArrayList<Point> clientPoints = new
		 * ArrayList<Point>(); ArrayList<Point> allPoints = new
		 * ArrayList<Point>();
		 * 
		 * HashMap<Point, Integer> earliestAllowedArrivalTime = new
		 * HashMap<Point, Integer>(); HashMap<Point, Integer> serviceDuration =
		 * new HashMap<Point, Integer>(); HashMap<Point, Integer>
		 * latestAllowedArrivalTime = new HashMap<Point, Integer>();
		 * 
		 * int id = -1;
		 * 
		 * long minTimePoint = Integer.MAX_VALUE; for(int i = 0; i <
		 * requests.length; i++){ long t =
		 * DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
		 * minTimePoint = minTimePoint < t ? minTimePoint : t; t =
		 * DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
		 * minTimePoint = minTimePoint < t ? minTimePoint : t; } int dt = 10000;
		 * int pickupDuration = 60;// 60 seconds int D = 10000;// distance from
		 * start/end poitns to each client point
		 * 
		 * for (int i = 0; i < K; i++) { id++; Point p = new Point(id);
		 * clientPoints.add(p);
		 * 
		 * long et =
		 * DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
		 * long lt =
		 * DateTimeUtils.dateTime2Int(requests[i].getLateDeliveryDateTime());
		 * int converted_et = (int)(et - minTimePoint) + dt; int converted_lt =
		 * (int)(lt - minTimePoint) + dt; earliestAllowedArrivalTime.put(p,
		 * converted_et); latestAllowedArrivalTime.put(p,converted_lt);
		 * serviceDuration.put(p, pickupDuration);// pickup duration is assumed
		 * to be 1 minute
		 * 
		 * allPoints.add(p);
		 * 
		 * mPoint2Request.put(p, requests[i]); }
		 * 
		 * 
		 * for (int i = 0; i < K; i++) { id++; Point s = new Point(id);
		 * startPoints.add(s); allPoints.add(s);
		 * 
		 * id++; Point t = new Point(id); endPoints.add(t); allPoints.add(t); }
		 * 
		 * for(Point s: startPoints){ earliestAllowedArrivalTime.put(s, 0);
		 * latestAllowedArrivalTime.put(s,Integer.MAX_VALUE);
		 * serviceDuration.put(s, 0);// pickup duration is assumed to be 0 }
		 * for(Point t: endPoints){ earliestAllowedArrivalTime.put(t, 0);
		 * latestAllowedArrivalTime.put(t,Integer.MAX_VALUE);
		 * serviceDuration.put(t, 0);// pickup duration is assumed to be 0 }
		 * 
		 * 
		 * ArcWeightsManager awm = new ArcWeightsManager(allPoints); for (int i
		 * = 0; i < clientPoints.size(); i++) { for (int j = 0; j <
		 * clientPoints.size(); j++) { awm.setWeight(clientPoints.get(i),
		 * clientPoints.get(j), travelTimes[i][j]); } } for (Point p :
		 * clientPoints) { for (Point s : startPoints) { awm.setWeight(s, p, D);
		 * awm.setWeight(p, s, D);
		 * 
		 * } for (Point t : endPoints) { awm.setWeight(p, t, D);
		 * awm.setWeight(t, p, D); } } for(Point s: startPoints){ for(Point t:
		 * endPoints) awm.setWeight(s, t, 0); }
		 * 
		 * int capacity = 0; for(int i = 0; i <
		 * input.getVehicleCapacities().length; i++) capacity = capacity <
		 * input.getVehicleCapacities()[i] ? input.getVehicleCapacities()[i] :
		 * capacity;
		 * 
		 * 
		 * 
		 * NodeWeightsManager nwm = new NodeWeightsManager(allPoints); for(int i
		 * = 0; i < clientPoints.size(); i++) nwm.setWeight(clientPoints.get(i),
		 * requests[i].getNumberPassengers()); for(Point s: startPoints)
		 * nwm.setWeight(s, 0); for(Point t: endPoints) nwm.setWeight(t, 0);
		 * 
		 * 
		 * // model VRManager mgr = new VRManager(); VarRoutesVR XR = new
		 * VarRoutesVR(mgr); for(int i = 0; i < startPoints.size(); i++){
		 * XR.addRoute(startPoints.get(i), endPoints.get(i)); } for(Point p:
		 * clientPoints) XR.addClientPoint(p);
		 * 
		 * ConstraintSystemVR CS = new ConstraintSystemVR(mgr);
		 * 
		 * AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		 * AccumulatedWeightNodesVR awn = new AccumulatedWeightNodesVR(XR, nwm);
		 * 
		 * IFunctionVR[] d = new IFunctionVR[K];// d[i] is the distance of route
		 * i+1 IFunctionVR[] w = new IFunctionVR[K];// w[i] is the accumulated
		 * demand on route i+1 for(int i = 0; i < K; i++){ Point t =
		 * XR.endPoint(i+1);// start point of route i+1 d[i] = new
		 * AccumulatedEdgeWeightsOnPathVR(awe, t); w[i] = new
		 * AccumulatedNodeWeightsOnPathVR(awn, t); CS.post(new
		 * Leq(w[i],capacity)); CS.post(new Leq(d[i],input.getMaxWaitTime())); }
		 * 
		 * EarliestArrivalTimeVR eat = new EarliestArrivalTimeVR(XR, awm,
		 * earliestAllowedArrivalTime, serviceDuration);
		 * 
		 * CEarliestArrivalTimeVR twCtrs = new CEarliestArrivalTimeVR(eat,
		 * latestAllowedArrivalTime); CS.post(twCtrs);
		 * 
		 * IFunctionVR obj = new TotalCostVR(XR, awm); LexMultiFunctions F = new
		 * LexMultiFunctions(); F.add(new ConstraintViolationsVR(CS));
		 * F.add(obj);
		 * 
		 * 
		 * mgr.close();
		 * 
		 * ArrayList<INeighborhoodExplorer> NE = new
		 * ArrayList<INeighborhoodExplorer>(); NE.add(new
		 * GreedyOnePointMoveExplorer(XR, F));
		 * 
		 * NE.add(new GreedyOrOptMove1Explorer(XR, F)); NE.add(new
		 * GreedyOrOptMove2Explorer(XR, F)); NE.add(new
		 * GreedyThreeOptMove1Explorer(XR, F)); NE.add(new
		 * GreedyThreeOptMove2Explorer(XR, F)); NE.add(new
		 * GreedyThreeOptMove3Explorer(XR, F)); NE.add(new
		 * GreedyThreeOptMove4Explorer(XR, F)); NE.add(new
		 * GreedyThreeOptMove5Explorer(XR, F)); NE.add(new
		 * GreedyThreeOptMove6Explorer(XR, F)); NE.add(new
		 * GreedyThreeOptMove7Explorer(XR, F)); NE.add(new
		 * GreedyThreeOptMove8Explorer(XR, F)); NE.add(new
		 * GreedyTwoOptMove1Explorer(XR, F)); NE.add(new
		 * GreedyTwoOptMove2Explorer(XR, F)); NE.add(new
		 * GreedyTwoOptMove3Explorer(XR, F)); NE.add(new
		 * GreedyTwoOptMove4Explorer(XR, F)); NE.add(new
		 * GreedyTwoOptMove5Explorer(XR, F)); NE.add(new
		 * GreedyTwoOptMove6Explorer(XR, F)); NE.add(new
		 * GreedyTwoOptMove7Explorer(XR, F)); NE.add(new
		 * GreedyTwoOptMove8Explorer(XR, F)); // NE.add(new
		 * GreedyTwoPointsMoveExplorer(XR, F)); NE.add(new
		 * GreedyCrossExchangeMoveExplorer(XR, F)); // NE.add(new
		 * GreedyAddOnePointMoveExplorer(XR, F));
		 * 
		 * GenericLocalSearch se = new GenericLocalSearch(mgr);
		 * se.setNeighborhoodExplorer(NE); se.setObjectiveFunction(F);
		 * se.setMaxStable(50);
		 * 
		 * se.search(5, input.getMaxTime());
		 * 
		 * ArrayList<SharedTaxiRoute> routes = new ArrayList<SharedTaxiRoute>();
		 * 
		 * for(int k = 1; k <= XR.getNbRoutes(); k++){ Point s =
		 * XR.startPoint(k); Point t = XR.endPoint(k); if(XR.next(s) != t){
		 * ArrayList<Point> points = new ArrayList<Point>(); for(Point x =
		 * XR.next(s); x != t; x = XR.next(x)){ points.add(x); } String[]
		 * ticketCodes = new String[points.size()]; for(int i = 0; i <
		 * points.size(); i++){ SharedTaxiRequest req =
		 * mPoint2Request.get(points.get(i)); ticketCodes[i] =
		 * req.getTicketCode(); } SharedTaxiRoute r = new
		 * SharedTaxiRoute(ticketCodes); routes.add(r); } } SharedTaxiRoute[] A
		 * = new SharedTaxiRoute[routes.size()]; for(int i = 0; i <
		 * routes.size(); i++){ A[i] = routes.get(i); } return new
		 * SharedTaxiSolution(A);
		 */
	}

}
