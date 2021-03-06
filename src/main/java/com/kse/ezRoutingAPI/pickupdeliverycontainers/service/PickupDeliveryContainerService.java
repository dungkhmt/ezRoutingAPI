package com.kse.ezRoutingAPI.pickupdeliverycontainers.service;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

import com.kse.ezRoutingAPI.model.VehicleInfo;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.MatchedSequenceRequests;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.PickupDeliveryInput;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.PickupDeliveryRequest;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.PickupDeliveryRoute;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.PickupDeliveryRouteElement;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.PickupDeliverySolution;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.Truck;
import com.kse.utils.DateTimeUtils;

public class PickupDeliveryContainerService {
	public static final int TRUCK_CAPACITY = 2;// at most two containers on a truck
	public static final int SPEED = 16;// 16m/s
	public static final double APPX = 1.5;// approximation factor for distance
	public static final int serviceDuration = 1800;// 30 minutes for pickup or delivery
	public static final int deltaPickupDelivery = 1800;// 
	
	public PickupDeliverySolution computePickupDeliveryContainerSolution(PickupDeliveryInput input) {
		
		double maxDistance = 10;//5000; in km
		int maxWaitTime = 3600;
		
		PickupDeliveryRequest[] req = input.getRequests();
		Truck[] trucks = input.getTrucks();
		
		if(req == null || trucks == null){
			return new PickupDeliverySolution(false,-1,-1,null);
		}
		
		ArrayList<Point> startPoints = new ArrayList<Point>();
		ArrayList<Point> endPoints = new ArrayList<Point>();
		ArrayList<Point> pickupPoints = new ArrayList<Point>();
		ArrayList<Point> deliveryPoints = new ArrayList<Point>();
		ArrayList<Point> allPoints = new ArrayList<Point>();
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		
		HashMap<Point, PickupDeliveryRequest> mPoint2Request = new HashMap<Point, PickupDeliveryRequest>();
		HashMap<PickupDeliveryRequest, Point> mReq2Pickup = new HashMap<PickupDeliveryRequest, Point>();
		HashMap<PickupDeliveryRequest, Point> mReq2Delivery = new HashMap<PickupDeliveryRequest, Point>();
		HashMap<Point, Truck> mPoint2Truck = new HashMap<Point, Truck>();
		HashMap<PickupDeliveryRequest, Integer> mReq2Index = new HashMap<PickupDeliveryRequest, Integer>();
		HashMap<Truck, Integer> mTruck2Index = new HashMap<Truck, Integer>();
		
		PickupDeliveryGreedyAssgimentSolver greedyAssigner = new PickupDeliveryGreedyAssgimentSolver(req,trucks);
		greedyAssigner.assignTrucks(maxDistance, maxWaitTime);
		
		if(!greedyAssigner.feasibleSolution()){
			return new PickupDeliverySolution(false,-1,-1,null);
		}
		
		HashMap<MatchedSequenceRequests, Truck> sol2Assigner = greedyAssigner.getTruckOf();
		
		HashSet<Truck> sel_trucks = new HashSet<Truck>();
		Set<MatchedSequenceRequests> MSR = sol2Assigner.keySet();
		for(MatchedSequenceRequests msr: MSR){
			Truck trk = sol2Assigner.get(msr);
			sel_trucks.add(trk);
		}
		
		trucks = new Truck[sel_trucks.size()];// collect used trucks
		int idx = -1;
		for(Truck trk: sel_trucks){
			idx++;
			trucks[idx] = trk;
		}
		System.out.println(name() + "::computePickupDeliveryContainerSolution, selected trucks.sz = " + trucks.length);
		for(int i = 0; i < trucks.length; i++)
			System.out.println(name() + "::computePickupDeliveryContainerSolution, selected trucks " + trucks[i].getCode());
		
		GoogleMapsQuery G = new GoogleMapsQuery();
		
		double unknownLatLng = -1000000;
		int INFTY = 1000000; 
		
		int id = -1;
		for(int k= 0; k < trucks.length; k++){
			id++;
			LatLng ll = null;
			if(trucks[k].getCurrentLatLng().equals("-"))
					ll = new LatLng(unknownLatLng,unknownLatLng);
			else
				ll = new LatLng(trucks[k].getCurrentLatLng());	
			
			Point startPoint = new Point(id,ll.lat,ll.lng);
			
			allPoints.add(startPoint);
			startPoints.add(startPoint);
			mPoint2Truck.put(startPoint, trucks[k]);
			
			id++;
			if(trucks[k].getDepotLatLng().equals("-")){
				ll = new LatLng(unknownLatLng,unknownLatLng);
			}else{
				ll = new LatLng(trucks[k].getDepotLatLng());
			}
			Point endPoint = new Point(id,ll.lat,ll.lng);
			allPoints.add(endPoint);
			endPoints.add(endPoint);
			mPoint2Truck.put(startPoint, trucks[k]);
		}
		for(int i = 0; i < req.length; i++){
			id++;
			LatLng ll = new LatLng(req[i].getPickupLatLng());
			Point p = new Point(id,ll.lat,ll.lng);
			allPoints.add(p);
			pickupPoints.add(p);
			clientPoints.add(p);
			mPoint2Request.put(p, req[i]);
			mReq2Pickup.put(req[i],p);
			
			id++;
			ll = new LatLng(req[i].getDeliveryLatLng());
			p = new Point(id,ll.lat,ll.lng);
			allPoints.add(p);
			deliveryPoints.add(p);
			clientPoints.add(p);
			mPoint2Request.put(p, req[i]);
			mReq2Delivery.put(req[i],p);
		}
		
		
		ArcWeightsManager distances = new ArcWeightsManager(allPoints);
		ArcWeightsManager travelTimes = new ArcWeightsManager(allPoints);

		int maxTravelTime = 0;
		for(Point p1: clientPoints){
			for(Point p2: clientPoints){
				//double d = G.estimateDistanceMeter(p1.getX(), p1.getY(), p2.getX(), p2.getY());
				//int t = G.estimateTravelTime(p1.getX(), p1.getY(), p2.getX(), p2.getY(), "driving", SPEED, APPX);
				
				double d = G.getApproximateDistanceMeter(p1.getX(), p1.getY(), p2.getX(), p2.getY());
				
				int t = G.estimateTravelTime(p1.getX(), p1.getY(), p2.getX(), p2.getY(), "driving", SPEED, APPX);
				
				distances.setWeight(p1, p2, d);
				travelTimes.setWeight(p1, p2, t);
				if(maxTravelTime < t) maxTravelTime = t;
				
				//System.out.println(name() + "::computePickupDeliveryContainerSolution, distance[" + p1.ID + "," + p2.ID + "] = " + d + ", travelTime = " + t);
			}
		}
		
		for(Point s: startPoints){
			for(Point p: clientPoints){
				/*
				double d = G.estimateDistanceMeter(s.getX(), s.getY(), p.getX(), p.getY());
				int t = G.estimateTravelTime(s.getX(), s.getY(), p.getX(), p.getY(), "driving", SPEED, APPX);
				distances.setWeight(s, p, d);
				travelTimes.setWeight(s, p, t);
				if(maxTravelTime < t) maxTravelTime = t;
				System.out.println(name() + "::computePickupDeliveryContainerSolution, distance[" + s.ID + "," + p.ID + "] = " + d + ", travelTime = " + t);
				
				d = G.estimateDistanceMeter(p.getX(), p.getY(), s.getX(), s.getY());
				t = G.estimateTravelTime(p.getX(), p.getY(), s.getX(), s.getY(), "driving", SPEED, APPX);
				distances.setWeight(p, s, d);
				travelTimes.setWeight(p, s,  t);
				if(maxTravelTime < t) maxTravelTime = t;
				*/
				
				distances.setWeight(p, s, 0);
				travelTimes.setWeight(p, s,  0);
				
			}
		}
		
		
		for(Point e: endPoints){
			for(Point p: clientPoints){
				/*
				double d = INFTY;
				if(e.getX() < unknownLatLng)
					d = G.estimateDistanceMeter(e.getX(), e.getY(), p.getX(), p.getY());
				int t = INFTY;
				if(e.getX() < unknownLatLng)
					t = G.estimateTravelTime(e.getX(), e.getY(), p.getX(), p.getY(), "driving", SPEED, APPX);
				distances.setWeight(e, p, d);
				travelTimes.setWeight(e, p, t);
				
				if(e.getX() < unknownLatLng){
					d = G.estimateDistanceMeter(p.getX(), p.getY(), e.getX(), e.getY());
					t = G.estimateTravelTime(p.getX(), p.getY(), e.getX(), e.getY(), "driving", SPEED, APPX);
				}
				distances.setWeight(p, e, d);
				travelTimes.setWeight(p, e,  t);
				*/
				
				distances.setWeight(p, e, 0);
				travelTimes.setWeight(p, e,  0);
				
			}
		}
		
		for(Point s: startPoints){
			for(Point t: endPoints){
				distances.setWeight(s, t, 0);
				distances.setWeight(t, s, 0);
				travelTimes.setWeight(s, t, 0);
				travelTimes.setWeight(t, s, 0);
			}
		}
		
		NodeWeightsManager demand = new NodeWeightsManager(allPoints);
		for(int i = 0; i < req.length; i++){
			Point pickup = mReq2Pickup.get(req[i]);
			Point delivery = mReq2Delivery.get(req[i]);
			demand.setWeight(pickup, req[i].getQuantity());
			demand.setWeight(delivery, -req[i].getQuantity());
		}
		for(Point s: startPoints){
			demand.setWeight(s, 0);
		}
		for(Point t: endPoints){
			demand.setWeight(t, 0);
		}
		
		HashMap<Point, Integer> earliestAllowedArrivalTime = new HashMap<Point, Integer>();
		HashMap<Point, Integer> serviceDuration = new HashMap<Point, Integer>();
		HashMap<Point, Integer> latestAllowedArrivalTime = new HashMap<Point, Integer>();

		System.out.println(name() + "::computePickupDeliveryContainerSolution, req.length  = " + req.length);
		
		int minUnixTime = (int)DateTimeUtils.dateTime2Int(req[0].getPickupDateTime());
		
		for(int i = 0; i < req.length; i++){
			int t = (int)DateTimeUtils.dateTime2Int(req[i].getPickupDateTime());
			if(minUnixTime > t) minUnixTime = t;
			t = (int)DateTimeUtils.dateTime2Int(req[i].getDeliveryDateTime());
			if(minUnixTime > t) minUnixTime = t;
		}
		
		for(int i = 0; i < req.length; i++){
			Point pickup = mReq2Pickup.get(req[i]);
			int t = (int)DateTimeUtils.dateTime2Int(req[i].getPickupDateTime());
			t = t - minUnixTime + maxTravelTime;// normalize time duration
			int e = t - deltaPickupDelivery;
			int l = t + deltaPickupDelivery;
			earliestAllowedArrivalTime.put(pickup, e);
			serviceDuration.put(pickup, PickupDeliveryContainerService.serviceDuration);
			latestAllowedArrivalTime.put(pickup, l);
			
			Point delivery = mReq2Delivery.get(req[i]);
			t = (int)DateTimeUtils.dateTime2Int(req[i].getDeliveryDateTime());
			t = t - minUnixTime + maxTravelTime;// normalize time duration
			e = t - deltaPickupDelivery;
			l = t + deltaPickupDelivery;
			earliestAllowedArrivalTime.put(delivery, e);
			serviceDuration.put(delivery, PickupDeliveryContainerService.serviceDuration);
			latestAllowedArrivalTime.put(delivery, l);
		}
		
		String inftyDateTime = "2100-01-01 10:00:00";
		int inftyUnixTime = Integer.MAX_VALUE;//(int)DateTimeUtils.dateTime2Int(inftyDateTime);
		for(Point s: startPoints){
			earliestAllowedArrivalTime.put(s, 0);
			serviceDuration.put(s, 0);
			latestAllowedArrivalTime.put(s, inftyUnixTime);
		}
		
		for(Point e: endPoints){
			earliestAllowedArrivalTime.put(e, 0);
			serviceDuration.put(e, 0);
			latestAllowedArrivalTime.put(e, inftyUnixTime);
		}
		
		System.out.println(name() + "::computePickupDeliveryContainerSolution, minUnixTime = " + minUnixTime + 
				", inftyDateTime = " + inftyUnixTime + ", maxTravelTime = " + maxTravelTime);
		
		
		PickupDeliveryContainerSolver solver = new PickupDeliveryContainerSolver();
		solver.req = req;
		solver.trucks = trucks;
		solver.startPoints = startPoints;
		solver.endPoints = endPoints;
		solver.allPoints = allPoints;
		solver.pickupPoints = pickupPoints;
		solver.deliveryPoints = deliveryPoints;
		solver.clientPoints = clientPoints;
		solver.mPoint2Request = mPoint2Request;
		solver.mPoint2Truck = mPoint2Truck;
		solver.mReq2Delivery = mReq2Delivery;
		solver.mReq2Pickup = mReq2Pickup;
		solver.distances = distances;
		solver.travelTimes = travelTimes;
		solver.demand = demand;
		solver.earliestAllowedArrivalTime = earliestAllowedArrivalTime;
		solver.latestAllowedArrivalTime = latestAllowedArrivalTime;
		solver.serviceDuration = serviceDuration;
		 
		solver.mMatchedRequest2Truck = sol2Assigner;
		
		//solver.computeSolution();
		solver.computeGreedySolution();

		System.out.println("XR = " + solver.XR.toString() + ", F = "
				+ solver.F.getValues().toString());
		
		ArrayList<PickupDeliveryRoute> routes = new ArrayList<PickupDeliveryRoute>();
		for(int k = 1; k <= solver.XR.getNbRoutes(); k++){
			Point s = solver.XR.startPoint(k);
			Point ns = solver.XR.next(s);
			//PickupDeliveryRequest re = mPoint2Request.get(s);
			Truck tck = mPoint2Truck.get(s);
			ArrayList<PickupDeliveryRouteElement> r = new ArrayList<PickupDeliveryRouteElement>();
			PickupDeliveryRouteElement e = new PickupDeliveryRouteElement(tck.getCode(),"-","-",s.getX() + "," + s.getY(),"-",0,
					DateTimeUtils.second2HMS((int)travelTimes.getDistance(s, ns)), distances.getDistance(s,ns) + "");
			
			r.add(e);
			//String arrTime = DateTimeUtils.unixTimeStamp2DateTime((long)(solver.eat.getEarliestArrivalTime(s)) + 
			//		(long)travelTimes.getDistance(s, ns) + minUnixTime - maxTravelTime);
			String arrTime = DateTimeUtils.unixTimeStamp2DateTime((long)(solver.eat.getEarliestArrivalTime(ns)) + 
					+ minUnixTime - maxTravelTime);
			
			for(Point p = solver.XR.next(s); p != solver.XR.endPoint(k);p = solver.XR.next(p)){
				Point np = solver.XR.next(p);
				PickupDeliveryRequest rp = mPoint2Request.get(p);
				String addr = rp.getPickupAddress();
				String latlng = rp.getPickupLatLng();
				String distance2Next = "-";
				String travelTime2Next = "-";
				
				String action = "PICKUP";
				if(p == mReq2Delivery.get(rp)){
					action = "DELIVERY";
					addr = rp.getDeliveryAddress();
					latlng = rp.getDeliveryLatLng();
				}
				
				if(np != solver.XR.endPoint(k)){
					distance2Next = distances.getDistance(p, np)+"";
					travelTime2Next = DateTimeUtils.second2HMS((int)travelTimes.getDistance(p, np));
				}
				//String arrTime = DateTimeUtils.unixTimeStamp2DateTime((long)(solver.eat.getEarliestArrivalTime(p)) + minUnixTime - maxTravelTime);
				
				e = new PickupDeliveryRouteElement(rp.getRequestCode(), arrTime, addr, latlng, action, rp.getQuantity(), 
						travelTime2Next,
						distance2Next);
				r.add(e);
				
				if(np != solver.XR.endPoint(k)){
				arrTime = DateTimeUtils.unixTimeStamp2DateTime((long)(solver.eat.getEarliestArrivalTime(p)) + 
						+ serviceDuration.get(p) + 
						+ (long) travelTimes.getDistance(p, np) + 
						minUnixTime - maxTravelTime);
				}
				
			}
			PickupDeliveryRouteElement[] arr = new PickupDeliveryRouteElement[r.size()];
			for(int i = 0;i < r.size(); i++) arr[i] = r.get(i);
			PickupDeliveryRoute route = new PickupDeliveryRoute(arr);
			
			routes.add(route);
		}
		
		PickupDeliveryRoute[] arrRoutes = new PickupDeliveryRoute[routes.size()];
		for(int i= 0; i < routes.size(); i++){
			arrRoutes[i] = routes.get(i);
		}
		
		double violations = solver.F.getValues().get(0);
		double traveldistance = solver.F.getValues().get(1);
		return new PickupDeliverySolution(true,violations, traveldistance,arrRoutes);
		
	}
	
	public String name(){
		return "PickupDeliveryContainerService";
	}
}
