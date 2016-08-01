package com.kse.ezRoutingAPI.controller;

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

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kse.ezRoutingAPI.model.PickupDeliveryInput;
import com.kse.ezRoutingAPI.model.PickupDeliveryProblemInput;
import com.kse.ezRoutingAPI.model.PickupDeliveryRequest;
import com.kse.ezRoutingAPI.model.Route;
import com.kse.ezRoutingAPI.model.SolutionRoutes;
import com.kse.ezRoutingAPI.model.VehicleInfo;

import java.util.*;

@RestController

public class RoutingAPI {
	
	public String name(){
		return "RoutingAPI";
	}
	/*
	@RequestMapping(value="/compute-route", method = RequestMethod.POST)
	public SolutionRoutes computeRoute(@RequestBody PickupDeliveryInput input){
		for(int i = 0; i < input.getStartPoints().length; i++){
			System.out.println(name() + "::computeRoute, start end = " + input.getStartPoints()[i] + " --> " + input.getEndPoints()[i]);
		}
		for(int i= 0; i < input.getDistances().length; i++){
			System.out.println(name() + "::computeRoute, distance [" + input.getDistances()[i].getFromPoint() + " -> " + 
		input.getDistances()[i].getToPoint() + "] = " + input.getDistances()[i].getDistance());
		}
	
		
		ArrayList<Point> startPoints = new ArrayList<Point>();
		ArrayList<Point> endPoints = new ArrayList<Point>();
		ArrayList<Point> pickupPoints = new ArrayList<Point>();
		ArrayList<Point> deliveryPoints = new ArrayList<Point>();
		ArrayList<Point> allPoints = new ArrayList<Point>();
		
		HashMap<Integer, Point> mID2Point = new HashMap<Integer, Point>();
		for(int i = 0; i <  input.getStartPoints().length; i++){
			int id = input.getStartPoints()[i];
			Point p = new Point(id);
			startPoints.add(p);
			allPoints.add(p);
			mID2Point.put(id, p);
		}
		for(int i = 0; i <  input.getEndPoints().length; i++){
			int id = input.getEndPoints()[i];
			Point p = new Point(id);
			endPoints.add(p);
			allPoints.add(p);
			mID2Point.put(id, p);
		}
		
		for(int i = 0; i <  input.getPickupPoints().length; i++){
			int id = input.getPickupPoints()[i];
			Point p = new Point(id);
			pickupPoints.add(p);
			allPoints.add(p);
			mID2Point.put(id, p);
		}
		for(int i = 0; i <  input.getDeliveryPoints().length; i++){
			int id = input.getDeliveryPoints()[i];
			Point p = new Point(id);
			deliveryPoints.add(p);
			allPoints.add(p);
			mID2Point.put(id, p);
		}
		
		HashMap<Point, Point> mPickup2Delivery = new HashMap<Point, Point>();
		for(int i = 0; i < pickupPoints.size(); i++){
			mPickup2Delivery.put(pickupPoints.get(i), deliveryPoints.get(i));
		}
		
		ArcWeightsManager awm = new ArcWeightsManager(allPoints);
		for(int i = 0; i < input.getDistances().length; i++){
			int from = input.getDistances()[i].getFromPoint();
			int to = input.getDistances()[i].getToPoint();
			double d = input.getDistances()[i].getDistance();
			Point fromPoint = mID2Point.get(from);
			Point toPoint = mID2Point.get(to);
			awm.setWeight(fromPoint, toPoint, d);
		}
		
		
		ArcWeightsManager travelTime = new ArcWeightsManager(allPoints);
		for(int i = 0; i < input.getTravelTimes().length; i++){
			int from = input.getTravelTimes()[i].getFromPoint();
			int to = input.getTravelTimes()[i].getToPoint();
			double d = input.getTravelTimes()[i].getDistance();
			Point fromPoint = mID2Point.get(from);
			Point toPoint = mID2Point.get(to);
			awm.setWeight(fromPoint, toPoint, d);
		}
		
		NodeWeightsManager nwm = new NodeWeightsManager(allPoints);
		for(int i = 0; i < input.getPickupPoints().length; i++){
			int pickupID = input.getPickupPoints()[i];
			int deliveryID = input.getDeliveryPoints()[i];
			Point pickup = mID2Point.get(pickupID);
			Point delivery = mID2Point.get(deliveryID);
			int d = input.getDemand()[i];
			nwm.setWeight(pickup, d);
			nwm.setWeight(delivery, -d);
		}
		
		for(int i = 0; i < input.getStartPoints().length; i++){
			int startID = input.getStartPoints()[i];
			int endID = input.getEndPoints()[i];
			Point pickup = mID2Point.get(startID);
			Point delivery = mID2Point.get(endID);
			nwm.setWeight(pickup, 0);
			nwm.setWeight(delivery, 0);
		}
		
		HashMap<Point, Integer> earliestAllowedArrivalTime = new HashMap<Point, Integer>();
		HashMap<Point, Integer> serviceDuration = new HashMap<Point, Integer>();
		HashMap<Point, Integer> latestAllowedArrivalTime = new HashMap<Point, Integer>();
		
		for(int i = 0; i < input.getEarlyPickup().length; i++){
			int earlyPickup = input.getEarlyPickup()[i];
			int latePickup = input.getLatePickup()[i];
			int earlyDelivery = input.getEarlyDelivery()[i];
			int lateDelivery = input.getLateDelivery()[i];
			int pickupID = input.getPickupPoints()[i];
			int deliveryID = input.getDeliveryPoints()[i];
			Point pickup = mID2Point.get(pickupID);
			Point delivery = mID2Point.get(deliveryID);
			
			
			
		}
		
		VRManager mgr = new VRManager();
		VarRoutesVR XR = new VarRoutesVR(mgr);
		ConstraintSystemVR CS = new ConstraintSystemVR(mgr);
		
		for(int i = 0; i < startPoints.size(); i++){
			XR.addRoute(startPoints.get(i), endPoints.get(i));
		}
		
		for(int i = 0; i < pickupPoints.size(); i++){
			XR.addClientPoint(pickupPoints.get(i));
			XR.addClientPoint(deliveryPoints.get(i));
		}
		
		int K = XR.getNbRoutes();
		
		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR,
				awm);
		AccumulatedWeightNodesVR awn = new AccumulatedWeightNodesVR(XR,
				nwm);
		AccumulatedWeightEdgesVR arrivalTime = new AccumulatedWeightEdgesVR(XR, travelTime);
		
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			awe.setAccumulatedWeightStartPoint(k, 0);
			arrivalTime.setAccumulatedWeightStartPoint(k, input.getStartPoints()[k-1]);// start time point of vehicle k
			awn.setAccumulatedWeightStartPoint(k, 0);
		}
		HashMap<Point, IFunctionVR> accDemand = new HashMap<Point, IFunctionVR>();
		HashMap<Point, IFunctionVR> accDistance = new HashMap<Point, IFunctionVR>();
		for (Point v : allPoints) {
			IFunctionVR dv = new AccumulatedNodeWeightsOnPathVR(awn, v);
			accDemand.put(v, dv);
			
			IFunctionVR disv = new AccumulatedEdgeWeightsOnPathVR(awe, v);
			accDistance.put(v, disv);
			
			IFunctionVR idR = new RouteIndex(XR,v);
			for(int k = 1; k <= K; k++){
				IConstraintVR c1 = new Eq(idR,k);
				IConstraintVR c2 = new Leq(dv, input.getCapacity()[k-1]); 
				CS.post(new Implicate(c1,c2));
			}
		}
		HashMap<Point, IFunctionVR> routeOfPoint = new HashMap<Point, IFunctionVR>();
		HashMap<Point, IFunctionVR> indexOfPointOnRoute = new HashMap<Point, IFunctionVR>();

		for(int i = 0; i < input.getPickupPoints().length; i++){
			int pickupID = input.getPickupPoints()[i];
			int deliveryID = input.getDeliveryPoints()[i];
			double maxDistance = input.getMaxDistance()[i];
			
			Point pickup = mID2Point.get(pickupID);
			Point delivery = mID2Point.get(deliveryID);
			
			IFunctionVR rp = new RouteIndex(XR, pickup);
			IFunctionVR rd = new RouteIndex(XR, delivery);
			routeOfPoint.put(pickup, rp);
			routeOfPoint.put(delivery, rd);
			CS.post(new Eq(rp, rd));

			IFunctionVR ip = new IndexOnRoute(XR, pickup);
			IFunctionVR id = new IndexOnRoute(XR, delivery);
			indexOfPointOnRoute.put(pickup, ip);
			indexOfPointOnRoute.put(delivery, id);
			CS.post(new Leq(ip, id));
			
			IFunctionVR distancePickup = accDistance.get(pickup);
			IFunctionVR distanceDelivery = accDistance.get(delivery);
			IFunctionVR travelDistance = new Minus(distanceDelivery,distancePickup);
			
			CS.post(new Leq(travelDistance,maxDistance));

		}
		
		
		CS.post(new CPickupDeliveryOfGoodVR(XR, mPickup2Delivery));
		
		EarliestArrivalTimeVR eat = new EarliestArrivalTimeVR(XR,
				 travelTime, earliestAllowedArrivalTime, serviceDuration);
				
				CEarliestArrivalTimeVR twCtrs = new
				 CEarliestArrivalTimeVR(eat, latestAllowedArrivalTime);
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

		se.search(10000, input.getTimeLimit());

		Route[] routes = new Route[startPoints.size()];
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			ArrayList<Point> L = new ArrayList<Point>();
			for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
				L.add(p);
			}
			L.add(XR.endPoint(k));
			int[] sequence = new int[L.size()];
			for(int i = 0; i < L.size(); i++)
				sequence[i] = L.get(i).ID;
			int len = L.size();
			routes[k-1] = new Route(len,sequence);
		}
		return new SolutionRoutes(routes);
		
	}
	*/
	

	@RequestMapping(value="/compute-route", method = RequestMethod.POST)
	public SolutionRoutes computeRoute(@RequestBody PickupDeliveryProblemInput input){
	
		
		ArrayList<Point> startPoints = new ArrayList<Point>();
		ArrayList<Point> endPoints = new ArrayList<Point>();
		ArrayList<Point> pickupPoints = new ArrayList<Point>();
		ArrayList<Point> deliveryPoints = new ArrayList<Point>();
		ArrayList<Point> allPoints = new ArrayList<Point>();
		
		HashMap<Integer, Point> mID2Point = new HashMap<Integer, Point>();
		for(int i = 0; i < input.getVehicles().length; i++){
			VehicleInfo vh = input.getVehicles()[i];
			int startID = vh.getStartPoint();
			Point startPoint = new Point(startID);
			startPoints.add(startPoint);
			allPoints.add(startPoint);
			mID2Point.put(startID, startPoint);
			
			int endID = vh.getEndPoint();
			Point endPoint = new Point(endID);
			endPoints.add(endPoint);
			allPoints.add(endPoint);
			mID2Point.put(endID, endPoint);
			
		}
		
		HashMap<Point, Point> mPickup2Delivery = new HashMap<Point, Point>();
		
		for(int i = 0; i < input.getRequests().length; i++){
			PickupDeliveryRequest req = input.getRequests()[i];
			int id = req.getPickupPoint();
			Point pickup = new Point(id);
			pickupPoints.add(pickup);
			allPoints.add(pickup);
			mID2Point.put(id, pickup);
			
			id = req.getDeliveryPoint();
			Point delivery = new Point(id);
			deliveryPoints.add(delivery);
			allPoints.add(delivery);
			mID2Point.put(id, delivery);
			
			mPickup2Delivery.put(pickup, delivery);
			
			
		}
		
		/*
		HashMap<Point, Point> mPickup2Delivery = new HashMap<Point, Point>();
		for(int i = 0; i < pickupPoints.size(); i++){
			mPickup2Delivery.put(pickupPoints.get(i), deliveryPoints.get(i));
		}
		*/
		
		ArcWeightsManager awm = new ArcWeightsManager(allPoints);
		for(int i = 0; i < input.getDistances().length; i++){
			int from = input.getDistances()[i].getFromPoint();
			int to = input.getDistances()[i].getToPoint();
			double d = input.getDistances()[i].getDistance();
			Point fromPoint = mID2Point.get(from);
			Point toPoint = mID2Point.get(to);
			awm.setWeight(fromPoint, toPoint, d);
		}
		
		
		ArcWeightsManager travelTime = new ArcWeightsManager(allPoints);
		for(int i = 0; i < input.getTravelTimes().length; i++){
			int from = input.getTravelTimes()[i].getFromPoint();
			int to = input.getTravelTimes()[i].getToPoint();
			double d = input.getTravelTimes()[i].getDistance();
			Point fromPoint = mID2Point.get(from);
			Point toPoint = mID2Point.get(to);
			awm.setWeight(fromPoint, toPoint, d);
		}
		
		NodeWeightsManager nwm = new NodeWeightsManager(allPoints);
		for(int i = 0; i < input.getRequests().length; i++){
			PickupDeliveryRequest req = input.getRequests()[i];
			Point pickup = mID2Point.get(req.getPickupPoint());
			Point delivery = mID2Point.get(req.getDeliveryPoint());
			nwm.setWeight(pickup, req.getDemand());
			nwm.setWeight(delivery, -req.getDemand());
		}

		for(int i = 0; i < input.getVehicles().length; i++){
			VehicleInfo vh = input.getVehicles()[i];
			Point start = mID2Point.get(vh.getStartPoint());
			Point end = mID2Point.get(vh.getEndPoint());
			nwm.setWeight(start, 0);
			nwm.setWeight(end, 0);
		}
		
		
		HashMap<Point, Integer> earliestAllowedArrivalTime = new HashMap<Point, Integer>();
		HashMap<Point, Integer> serviceDuration = new HashMap<Point, Integer>();
		HashMap<Point, Integer> latestAllowedArrivalTime = new HashMap<Point, Integer>();

		ArrayList<Point> requestPoints = new ArrayList<Point>();

		for(int i = 0; i < input.getRequests().length; i++){
			PickupDeliveryRequest req = input.getRequests()[i];
			Point pickup = mID2Point.get(req.getPickupPoint());
			Point delivery = mID2Point.get(req.getDeliveryPoint());

			requestPoints.add(pickup);
			requestPoints.add(delivery);

			earliestAllowedArrivalTime.put(pickup, req.getEarlyPickupTime());
			earliestAllowedArrivalTime.put(delivery, req.getEarlyDeliveryTime());
			
			latestAllowedArrivalTime.put(pickup, req.getLatePickupTime());
			latestAllowedArrivalTime.put(delivery, req.getLateDeliveryTime());
			
			serviceDuration.put(pickup, req.getPickupDuration());
			serviceDuration.put(delivery, req.getDeliveryDuration());
		}
		
		for(int i = 0; i < input.getVehicles().length; i++){
			VehicleInfo vh = input.getVehicles()[i];
			Point startPoint = mID2Point.get(vh.getStartPoint());
			Point endPoint = mID2Point.get(vh.getEndPoint());
			earliestAllowedArrivalTime.put(startPoint, vh.getEarlyStartTimePoint());
			latestAllowedArrivalTime.put(startPoint, vh.getLateStartTimePoint());
			serviceDuration.put(startPoint, 0);
			
			earliestAllowedArrivalTime.put(endPoint, vh.getEarlyEndTimePoint());
			latestAllowedArrivalTime.put(endPoint, vh.getLateEndTimePoint());
			serviceDuration.put(endPoint, 0);
		}
		
		VRManager mgr = new VRManager();
		VarRoutesVR XR = new VarRoutesVR(mgr);
		ConstraintSystemVR CS = new ConstraintSystemVR(mgr);
		
		for(int i = 0; i < startPoints.size(); i++){
			XR.addRoute(startPoints.get(i), endPoints.get(i));
		}
		
		for(int i = 0; i < pickupPoints.size(); i++){
			XR.addClientPoint(pickupPoints.get(i));
			XR.addClientPoint(deliveryPoints.get(i));
		}
		
		int K = XR.getNbRoutes();
		
		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR,
				awm);
		AccumulatedWeightNodesVR awn = new AccumulatedWeightNodesVR(XR,
				nwm);
		AccumulatedWeightEdgesVR arrivalTime = new AccumulatedWeightEdgesVR(XR, travelTime);
		
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			VehicleInfo vh = input.getVehicles()[k-1];
			awe.setAccumulatedWeightStartPoint(k, 0);
			arrivalTime.setAccumulatedWeightStartPoint(k, vh.getEarlyStartTimePoint());// start time point of vehicle k
			awn.setAccumulatedWeightStartPoint(k, 0);
		}
		HashMap<Point, IFunctionVR> accDemand = new HashMap<Point, IFunctionVR>();
		HashMap<Point, IFunctionVR> accDistance = new HashMap<Point, IFunctionVR>();
		for (Point v : allPoints) {
			IFunctionVR dv = new AccumulatedNodeWeightsOnPathVR(awn, v);
			accDemand.put(v, dv);
			
			IFunctionVR disv = new AccumulatedEdgeWeightsOnPathVR(awe, v);
			accDistance.put(v, disv);
			
			IFunctionVR idR = new RouteIndex(XR,v);
			for(int k = 1; k <= K; k++){
				
				IConstraintVR c1 = new Eq(idR,k);
				IConstraintVR c2 = new Leq(dv, input.getVehicles()[k-1].getCapacity()); 
				CS.post(new Implicate(c1,c2));
			}
		}
		HashMap<Point, IFunctionVR> routeOfPoint = new HashMap<Point, IFunctionVR>();
		HashMap<Point, IFunctionVR> indexOfPointOnRoute = new HashMap<Point, IFunctionVR>();
		
		
		for(int i = 0; i < input.getRequests().length; i++){
			PickupDeliveryRequest req = input.getRequests()[i];
			int pickupID = req.getPickupPoint();
			int deliveryID = req.getDeliveryPoint();
			double maxDistance = req.getMaxDistance();
			
			Point pickup = mID2Point.get(pickupID);
			Point delivery = mID2Point.get(deliveryID);
			
			
			IFunctionVR rp = new RouteIndex(XR, pickup);
			IFunctionVR rd = new RouteIndex(XR, delivery);
			routeOfPoint.put(pickup, rp);
			routeOfPoint.put(delivery, rd);
			CS.post(new Eq(rp, rd));

			IFunctionVR ip = new IndexOnRoute(XR, pickup);
			IFunctionVR id = new IndexOnRoute(XR, delivery);
			indexOfPointOnRoute.put(pickup, ip);
			indexOfPointOnRoute.put(delivery, id);
			CS.post(new Leq(ip, id));
			
			IFunctionVR distancePickup = accDistance.get(pickup);
			IFunctionVR distanceDelivery = accDistance.get(delivery);
			IFunctionVR travelDistance = new Minus(distanceDelivery,distancePickup);
			
			CS.post(new Leq(travelDistance,maxDistance));

		}
		
		//CS.post(new CPickupDeliveryOfGoodVR(XR, mPickup2Delivery));
		
		
		EarliestArrivalTimeVR eat = new EarliestArrivalTimeVR(XR,
				 travelTime, earliestAllowedArrivalTime, serviceDuration);
				
				CEarliestArrivalTimeVR twCtrs = new
				 CEarliestArrivalTimeVR(eat, latestAllowedArrivalTime);
				CS.post(twCtrs);
		
		IFunctionVR obj = new TotalCostVR(XR, awm);
		LexMultiFunctions F = new LexMultiFunctions();
		F.add(new ConstraintViolationsVR(CS));
		F.add(obj);
		
		mgr.close();
		/*
		XR.setRandom();
		for(Point p: requestPoints){
			System.out.println("routeOfPoint " + p.ID + " = " + routeOfPoint.get(p).getValue() + 
					", index of point " + p.ID + " = " + indexOfPointOnRoute.get(p).getValue());
		}
		System.out.println("XR = " + XR.toString() + ", F = " + F.getValues().toString());
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
		
		
		GenericLocalSearch se = new GenericLocalSearch(mgr);
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.setMaxStable(50);
		
		se.search(1000, input.getTimeLimit());
		for(Point p: requestPoints){
			System.out.println(name() + ":: computeRoute finished: routeOfPoint " + p.ID + " = " + routeOfPoint.get(p).getValue() + 
					", index of point " + p.ID + " = " + indexOfPointOnRoute.get(p).getValue() +
					", earliestArrivalTime of " + p.ID + " = " + eat.getEarliestArrivalTime(p));
		}
		System.out.println("XR = " + XR.toString() + ", F = " + F.getValues().toString());
		
		
		Route[] routes = new Route[startPoints.size()];
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			ArrayList<Point> L = new ArrayList<Point>();
			for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
				L.add(p);
			}
			L.add(XR.endPoint(k));
			int[] sequence = new int[L.size()];
			for(int i = 0; i < L.size(); i++)
				sequence[i] = L.get(i).ID;
			int len = L.size();
			routes[k-1] = new Route(len,sequence);
		}
		return new SolutionRoutes(routes);
		
	}

}
