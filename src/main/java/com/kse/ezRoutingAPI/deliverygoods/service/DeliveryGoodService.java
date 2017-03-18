package com.kse.ezRoutingAPI.deliverygoods.service;

import java.util.ArrayList;
import java.util.HashMap;

import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.LatLng;

import com.kse.ezRoutingAPI.deliverygoods.model.DeliveryGoodInput;
import com.kse.ezRoutingAPI.deliverygoods.model.DeliveryGoodRoute;
import com.kse.ezRoutingAPI.deliverygoods.model.DeliveryGoodRouteElement;
import com.kse.ezRoutingAPI.deliverygoods.model.DeliveryGoodSolution;
import com.kse.ezRoutingAPI.deliverygoods.model.DeliveryRequest;
import com.kse.ezRoutingAPI.deliverygoods.model.Shipper;
import com.kse.ezRoutingAPI.deliverygoods.model.Store;
import com.kse.utils.DateTimeUtils;

public class DeliveryGoodService {
	public static final int SPEED = 16;// 16m/s
	public static final double APPX = 1.5;// approximation factor for distance
	public static final int serviceDuration = 1800;// 30 minutes for pickup or delivery

	
	private DeliveryRequest[] requests;
	private Store store;
	private Shipper[] shippers;

	
	public String name(){
		return "DeliveryGoodService";
	}
	public DeliveryGoodSolution computeDeliveryGoodSolution(DeliveryGoodInput input){
		requests = input.getRequests();
		shippers = input.getShippers();
		store = input.getStore();
		
		System.out.println(name() + "::computeDeliveryGoodSolution, requests.length = " + requests.length);
		
		ArrayList<Point> startPoints = new ArrayList<Point>();
		ArrayList<Point> endPoints = new ArrayList<Point>();
		ArrayList<Point> deliveryPoints = new ArrayList<Point>();
		ArrayList<Point> allPoints = new ArrayList<Point>();
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		
		HashMap<Point, DeliveryRequest> mPoint2Request = new HashMap<Point, DeliveryRequest>();
		HashMap<DeliveryRequest, Point> mReq2Delivery = new HashMap<DeliveryRequest, Point>();
		HashMap<Point, Shipper> mPoint2Shipper = new HashMap<Point, Shipper>();
		
		GoogleMapsQuery G = new GoogleMapsQuery();
		
		double unknownLatLng = -1000000;
		int INFTY = 1000000; 
		
		int id = -1;
		for(int k = 0; k < shippers.length; k++){
			id++;
			LatLng ll = new LatLng(store.getLatlng());
			Point startPoint = new Point(id,ll.lat,ll.lng);
			allPoints.add(startPoint);
			startPoints.add(startPoint);
			mPoint2Shipper.put(startPoint, shippers[k]);
			
			id++;
			ll = new LatLng(unknownLatLng,unknownLatLng);
			Point endPoint = new Point(id,ll.lat,ll.lng);
			allPoints.add(endPoint);
			endPoints.add(endPoint);
			mPoint2Shipper.put(startPoint, shippers[k]);
		}
		for(int i = 0; i < requests.length; i++){
		
			id++;
			LatLng ll = new LatLng(requests[i].getDeliveryLatLng());
			Point p = new Point(id,ll.lat,ll.lng);
			allPoints.add(p);
			deliveryPoints.add(p);
			clientPoints.add(p);
			mPoint2Request.put(p, requests[i]);
			mReq2Delivery.put(requests[i],p);
		}
		
		
		ArcWeightsManager distances = new ArcWeightsManager(allPoints);
		ArcWeightsManager travelTimes = new ArcWeightsManager(allPoints);

		int maxTravelTime = -1;
		for(Point p1: clientPoints){
			for(Point p2: clientPoints){
				double d = G.estimateDistanceMeter(p1.getX(), p1.getY(), p2.getX(), p2.getY());
				int t = G.estimateTravelTime(p1.getX(), p1.getY(), p2.getX(), p2.getY(), "driving", SPEED, APPX);
				distances.setWeight(p1, p2, d);
				travelTimes.setWeight(p1, p2, t);
				if(maxTravelTime < t) maxTravelTime = t;
				
				System.out.println(name() + "::computeDeliveryGoodSolution, distance[" + p1.ID + "," + p2.ID + "] = " + d + ", travelTime = " + t);
			}
		}
		
		for(Point s: startPoints){
			for(Point p: clientPoints){
				double d = G.estimateDistanceMeter(s.getX(), s.getY(), p.getX(), p.getY());
				int t = G.estimateTravelTime(s.getX(), s.getY(), p.getX(), p.getY(), "driving", SPEED, APPX);
				distances.setWeight(s, p, d);
				travelTimes.setWeight(s, p, t);
				if(maxTravelTime < t) maxTravelTime = t;
				System.out.println(name() + "::computeDeliveryGoodSolution, distance[" + s.ID + "," + p.ID + "] = " + d + ", travelTime = " + t);
				
				d = G.estimateDistanceMeter(p.getX(), p.getY(), s.getX(), s.getY());
				t = G.estimateTravelTime(p.getX(), p.getY(), s.getX(), s.getY(), "driving", SPEED, APPX);
				distances.setWeight(p, s, d);
				travelTimes.setWeight(p, s,  t);
				if(maxTravelTime < t) maxTravelTime = t;
				
				
				
			}
		}
		
		
		for(Point e: endPoints){
			for(Point p: clientPoints){
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
		for(int i = 0; i < requests.length; i++){
			Point delivery = mReq2Delivery.get(requests[i]);
			demand.setWeight(delivery, requests[i].getWeight());
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

		int minUnixTime = (int)DateTimeUtils.dateTime2Int(requests[0].getEarlyDeliveryTime());
		
		for(int i = 0; i < requests.length; i++){
			int t = (int)DateTimeUtils.dateTime2Int(requests[i].getEarlyDeliveryTime());
			if(minUnixTime > t) minUnixTime = t;
			t = (int)DateTimeUtils.dateTime2Int(requests[i].getLateDeliveryTime());
			if(minUnixTime > t) minUnixTime = t;
		}
		
		for(int i = 0; i < requests.length; i++){
			Point delivery = mReq2Delivery.get(requests[i]);
			int e = (int)DateTimeUtils.dateTime2Int(requests[i].getEarlyDeliveryTime());
			int l = (int)DateTimeUtils.dateTime2Int(requests[i].getLateDeliveryTime());
			e = e - minUnixTime + maxTravelTime;// normalize time duration
			l = l - minUnixTime + maxTravelTime;// normalize time duration
			earliestAllowedArrivalTime.put(delivery, e);
			serviceDuration.put(delivery, DeliveryGoodService.serviceDuration);
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
		
		System.out.println(name() + "::computeDeliveryGoodSolution, minUnixTime = " + minUnixTime + 
				", inftyDateTime = " + inftyUnixTime + ", maxTravelTime = " + maxTravelTime);


		DeliveryGoodSolver solver = new DeliveryGoodSolver();
		solver.req = requests;
		solver.shippers = shippers;
		solver.startPoints = startPoints;
		solver.endPoints = endPoints;
		solver.allPoints = allPoints;
		solver.deliveryPoints = deliveryPoints;
		solver.clientPoints = clientPoints;
		solver.mPoint2Request = mPoint2Request;
		solver.mPoint2Shipper = mPoint2Shipper;
		solver.mReq2Delivery = mReq2Delivery;
		solver.distances = distances;
		solver.travelTimes = travelTimes;
		solver.demand = demand;
		solver.earliestAllowedArrivalTime = earliestAllowedArrivalTime;
		solver.latestAllowedArrivalTime = latestAllowedArrivalTime;
		solver.serviceDuration = serviceDuration;
		 
		solver.computeSolution();
		
		ArrayList<DeliveryGoodRoute> listRoutes = new ArrayList<DeliveryGoodRoute>();
		for(int k = 1; k <= solver.XR.getNbRoutes(); k++){
			ArrayList<DeliveryGoodRouteElement> listElements = new ArrayList<DeliveryGoodRouteElement>();
			
			Point s = solver.XR.startPoint(k);
			Point ns = solver.XR.next(s);
			DeliveryRequest req = mPoint2Request.get(s);
			Shipper shipper = mPoint2Shipper.get(s);
			String requestCode = shipper.getShipperCode();
			String deliveryAddress = store.getAddress();
			String latlng = store.getLatlng();
			String arrivalDateTime = "-";
			String time2Next = DateTimeUtils.second2HMS((int)travelTimes.getDistance(s, ns));
			String distance2Next = distances.getDistance(s,ns) + "";
			
			DeliveryGoodRouteElement e = new DeliveryGoodRouteElement(requestCode, deliveryAddress, latlng, arrivalDateTime, time2Next, distance2Next);
			
			listElements.add(e);
			
			arrivalDateTime = DateTimeUtils.unixTimeStamp2DateTime((long)(solver.eat.getEarliestArrivalTime(ns)) + 
					+ minUnixTime - maxTravelTime);
			
			for(Point p = ns; p != solver.XR.endPoint(k); p = solver.XR.next(p)){
				Point np = solver.XR.next(p);
				DeliveryRequest rp = mPoint2Request.get(p);
				deliveryAddress = rp.getDeliveryAddress();
				latlng = rp.getDeliveryLatLng();
				distance2Next = "-";
				time2Next = "-";
				requestCode = rp.getRequestCode();
				
				if(np != solver.XR.endPoint(k)){
					distance2Next = distances.getDistance(p, np)+"";
					time2Next = DateTimeUtils.second2HMS((int)travelTimes.getDistance(p, np));
				}
				//String arrTime = DateTimeUtils.unixTimeStamp2DateTime((long)(solver.eat.getEarliestArrivalTime(p)) + minUnixTime - maxTravelTime);
				
				e = new DeliveryGoodRouteElement(requestCode, deliveryAddress, latlng, arrivalDateTime, time2Next, distance2Next);
				listElements.add(e);
				
				if(np != solver.XR.endPoint(k)){
					arrivalDateTime = DateTimeUtils.unixTimeStamp2DateTime(
							(long)(solver.eat.getEarliestArrivalTime(p)) + 
						+ serviceDuration.get(p) + 
						+ (long) travelTimes.getDistance(p, np) + 
						minUnixTime - maxTravelTime);
				}
				
				
			}
			
			DeliveryGoodRouteElement[] routeElements = new DeliveryGoodRouteElement[listElements.size()];
			for(int i = 0; i < listElements.size(); i++)
				routeElements[i] = listElements.get(i);
			
			DeliveryGoodRoute route = new DeliveryGoodRoute(routeElements);
			
			listRoutes.add(route);
		}
		
		DeliveryGoodRoute[] routes = new DeliveryGoodRoute[listRoutes.size()];
		for(int i = 0; i < listRoutes.size(); i++)
			routes[i] = listRoutes.get(i);
		
		return new DeliveryGoodSolution(routes);
	}
}
