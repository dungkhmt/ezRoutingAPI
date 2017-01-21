package com.kse.ezRoutingAPI.requestshippermatching.service;

import java.util.ArrayList;
import java.util.HashMap;

import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.eq.Eq;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.leq.Leq;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.IndexOnRoute;
import localsearch.domainspecific.vehiclerouting.vrp.functions.RouteIndex;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.LatLng;

import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperInput;
import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperMatchingRouteElement;
import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperMatchingSolution;
import com.kse.ezRoutingAPI.requestshippermatching.model.ShipRequest;
import com.kse.ezRoutingAPI.requestshippermatching.model.Shipper;

public class RequestShipperMatchingService {
	
	private ArcWeightsManager awm;
	private VRManager mgr;
	private VarRoutesVR XR;
	private ConstraintSystemVR CS;
	
	private ArrayList<Point> pickup_points;
	private ArrayList<Point> delivery_points;
	private ArrayList<Point> startPoints;
	private ArrayList<Point> endPoints;
	private ArrayList<Point> clientPoints;
	private ArrayList<Point> allPoints;
	private HashMap<Integer, Shipper> mID2Shipper;
	private HashMap<Integer, ShipRequest> mID2Request;
	private HashMap<ShipRequest, Point> mPickupRe2Point;
	private HashMap<ShipRequest, Point> mDeliveryRe2Point;
	
	private Shipper[] lst_shippers;
	private ShipRequest[] lst_shipRequests;
	private int n_shipper;
	private int n_request;
	
	public RequestShipperMatchingSolution computeRequestShipperMatchingSolution(RequestShipperInput input){
		// TODO by Tu-Dat
		mID2Shipper = new HashMap<Integer, Shipper>();
		mID2Request = new HashMap<Integer, ShipRequest>();
		mPickupRe2Point = new HashMap<ShipRequest, Point>();
		mDeliveryRe2Point = new HashMap<ShipRequest, Point>();
		
		lst_shippers = input.getShippers();
		lst_shipRequests = input.getRequests();
		
		int pointID = 0;
		
		n_shipper = lst_shippers.length;
		n_request = lst_shipRequests.length;
	
		startPoints = new ArrayList<Point>();
		for(int i=0; i<n_shipper; i++){
			LatLng ll = new LatLng(lst_shippers[i].getLocation());
			Point p = new Point(pointID,ll.lat,ll.lng);
			mID2Shipper.put(pointID, lst_shippers[i]);
			startPoints.add(p);
			allPoints.add(p);
			pointID++;
			p = new Point(pointID);
			endPoints.add(p);
			allPoints.add(p);
			pointID++;
			
		}
		
		for(int i=0; i<n_request; i++){
			LatLng ll = new  LatLng(lst_shipRequests[i].getPickupLocation());
			Point p_pickup = new Point(pointID,ll.lat,ll.lng);
			pickup_points.add(p_pickup);
			clientPoints.add(p_pickup);
			allPoints.add(p_pickup);
			mID2Request.put(pointID, lst_shipRequests[i]);
			mPickupRe2Point.put(lst_shipRequests[i], p_pickup);
			pointID++;
			
			ll = new LatLng(lst_shipRequests[i].getDeliveryLocation());
			Point p_delivery = new Point(pointID,ll.lat,ll.lng);
			delivery_points.add(p_delivery);
			allPoints.add(p_delivery);
			clientPoints.add(p_delivery);
			mID2Request.put(pointID, lst_shipRequests[i]);
			mPickupRe2Point.put(lst_shipRequests[i], p_delivery);
			pointID++;
		}
		
		awm = new ArcWeightsManager(allPoints);
		GoogleMapsQuery gmq = new GoogleMapsQuery();
		for(Point pi : clientPoints){
			for(Point pj : clientPoints){
				double w = gmq.computeDistanceHaversine(pi.getX(), pi.getY(), pj.getX(), pj.getY());
				awm.setWeight(pi, pj, w);
			}
		}
		
		for(Point ps : startPoints){
			for(Point p : clientPoints){
				double w = gmq.computeDistanceHaversine(ps.getX(), ps.getY(), p.getX(), p.getY());
				awm.setWeight(ps, p, w);
			}
		}
		
		for(Point pe : endPoints){
			for(Point p : clientPoints){
				awm.setWeight(pe, p, 0);
			}
		}
		
		for(Point ps : startPoints){
			for(Point pe : endPoints){
				awm.setWeight(ps, pe, 0);
			}
		}
		
//		ArrayList<String> route[] = new ArrayList[n_shipper];
//		ArrayList<String> routeID[] = new ArrayList[n_shipper];
//		
//		boolean check_request_assigned[] = new boolean[n_request];
//		
//		for(int i=0; i<n_shipper; i++){
//			route[i] = new ArrayList<String>();
//			
//			Shipper s = lst_shippers[i];
//			
//			route[i].add(s.getLocation());
//		}
//		
//		GoogleMapsQuery gmq = new GoogleMapsQuery();
//		
//		for(int i=0; i<n_request; i++){
//			check_request_assigned[i] = false;
//		}
//		
//		int n_request_assigned = 0;
//		//for(int i=0; i<n_request; i++){
//		while(n_request_assigned < n_request){
//		
//			for(int j=0; j<n_shipper; j++){
//				int indexRoute = -1;
//				double minDistance = 100000;
//				
//				for(int i=0; i<n_request; i++){
//					if(!check_request_assigned[i]){
//						String deliveryLocation = lst_shipRequests[i].getDeliveryLocation();
//						
//						String endOfRouteLocation = route[j].get(route[j].size()-1);
//						double distance = gmq.computeDistanceHaversine(deliveryLocation,endOfRouteLocation);
//						if(distance < minDistance){
//							indexRoute = j;
//							minDistance = distance;
//						}
//					}
//				}
//				route[j].add(lst_shipRequests[indexRoute].getPickupLocation());
//				n_request_assigned++;
//			}
//		}
//		
		return null;
	}
	
	public void stateModel(){
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		
		for(int i=0; i<n_shipper; i++){
			XR.addRoute(startPoints.get(i), endPoints.get(i));
		}
		
		for(int i=0; i<clientPoints.size(); i++){
			XR.addClientPoint(clientPoints.get(i));
		}
		
		for(int i=0; i<lst_shipRequests.length; i++){
			Point p_pickup = mPickupRe2Point.get(lst_shipRequests[i]);
			Point p_delivery = mDeliveryRe2Point.get(lst_shipRequests[i]);
			
			IFunctionVR r_pickup = new RouteIndex(XR, p_pickup);
			IFunctionVR r_delivery = new RouteIndex(XR,p_delivery);
			CS.post(new Eq(r_pickup, r_delivery));
			
			IFunctionVR ir_pickup = new IndexOnRoute(XR, p_pickup);
			IFunctionVR ir_delivery = new IndexOnRoute(XR, p_delivery);
			CS.post(new Leq(ir_pickup, ir_delivery));
			
			mgr.close();
		}
	}
}
