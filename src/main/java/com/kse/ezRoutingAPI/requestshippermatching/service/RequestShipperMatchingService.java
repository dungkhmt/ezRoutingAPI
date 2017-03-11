package com.kse.ezRoutingAPI.requestshippermatching.service;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.IndexOnRoute;
import localsearch.domainspecific.vehiclerouting.vrp.functions.RouteIndex;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.LatLng;

import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperInput;
import com.kse.ezRoutingAPI.model.Route;
import com.kse.ezRoutingAPI.model.RouteElement;
import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperMatchingRoute;
import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperMatchingRouteElement;
import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperMatchingSolution;
import com.kse.ezRoutingAPI.requestshippermatching.model.ShipRequest;
import com.kse.ezRoutingAPI.requestshippermatching.model.Shipper;

public class RequestShipperMatchingService {
	
	private ArcWeightsManager awm;
	private VRManager mgr;
	private VarRoutesVR XR;
	private ConstraintSystemVR CS;
	private IFunctionVR cost[];
	
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
	
	public String name(){
		return "RequestShipperMatchingService:: ";
	}
	public RequestShipperMatchingSolution computeRequestShipperMatchingSolutionGreedy(RequestShipperInput input){
		ArrayList<Point> shiperPoint= new ArrayList<Point>();
		ArrayList<Point> endPoints= new ArrayList<Point>();
		ArrayList<Point> pickupPoints= new ArrayList<Point>();
		ArrayList<Point> deliveryPoints= new ArrayList<Point>();
		//ArrayList<Point> clientPoints= new ArrayList<Point>();
		ArrayList<Point> allPoints= new ArrayList<Point>();
		HashMap<Point, Shipper> p2shiper= new HashMap<Point, Shipper>();
		HashMap<Point , ShipRequest> p2ShipReDe= new HashMap<Point, ShipRequest>();
		HashMap<Point , ShipRequest> p2ShipRePi= new HashMap<Point, ShipRequest>();
		HashMap<Point,Point > p2pReDePi= new HashMap<Point, Point>();
		HashMap<Point,Point > p2pRePiDe= new HashMap<Point, Point>();
		HashMap<Point, Integer> point2Index=new HashMap<Point, Integer>();
		
		ShipRequest srs[] =input.getRequests();
		Shipper[] shps=input.getShippers();
		int idPoint=-1;
 		for(int i=0;i<shps.length;i++){
 			idPoint++;
 			LatLng llng;
 			if(shps[i].getLocation().equals("-")){
 				llng=new LatLng(-100000, -100000);
 			} else llng = new LatLng(shps[i].getLocation());
 			Point p= new Point(idPoint, llng.lat, llng.lng);
 			shiperPoint.add(p);
 			allPoints.add(p);
 			endPoints.add(p);
 			p2shiper.put(p,shps[i]);
 			point2Index.put(p, idPoint);
		}
 		
 		for(int i=0;i<srs.length;i++){
 			idPoint++;
 			LatLng llng;
 			if(srs[i].getPickupLocation().equals("-")){
 				llng=new LatLng(-100000, -100000);
 			} else llng = new LatLng(srs[i].getPickupLocation());
 			Point p= new Point(idPoint,llng.lat,llng.lng);
 			//clientPoints.add(p);
 			pickupPoints.add(p);
 			//allPoints.add(p);
 			p2ShipRePi.put(p, srs[i]);
 			point2Index.put(p, idPoint);
 			allPoints.add(p);
 			idPoint++;
 			if(srs[i].getDeliveryLocation().equals("-")){
 				llng=new LatLng(-100000, -100000);
 			} else llng = new LatLng(srs[i].getDeliveryLocation());
 			Point p2= new Point(idPoint,llng.lat,llng.lng);
 			//clientPoints.add(p);
 			deliveryPoints.add(p2);
 			allPoints.add(p2);
 			p2ShipReDe.put(p2, srs[i]);
 			point2Index.put(p2, idPoint);
 			p2pReDePi.put(p2, p);
 			p2pRePiDe.put(p, p2);
 		}
 		System.out.println("Num of shipper"+shps.length);
 		double dis[][]= new double[allPoints.size()][allPoints.size()];
 		GoogleMapsQuery G= new GoogleMapsQuery();
 		for(int i=0;i<allPoints.size();i++) dis[i][i]=100000000;
 		for(int i=0;i<allPoints.size();i++)
 			for(int j=i+1;j<allPoints.size();j++){
 				dis[i][j]=G.getApproximateDistanceMeter(allPoints.get(i).getX(), allPoints.get(i).getY(), allPoints.get(j).getX(), allPoints.get(j).getY());
 				dis[j][i]=dis[i][j];
 			}
 		
 		int d[]=new int[pickupPoints.size()];
 		for(int i=0;i<d.length;i++) d[i]=0;
 		int itShp=0;
 		int itRe=0;
 		ArrayList<Double> lrmi= new ArrayList<Double>();
 		ArrayList<ArrayList<RequestShipperMatchingRouteElement>> lr = new ArrayList<ArrayList<RequestShipperMatchingRouteElement>>();
 		for(int i=0;i<shiperPoint.size();i++) {
 			lr.add(new ArrayList<RequestShipperMatchingRouteElement>());
 			lrmi.add(0.0);
 			lr.get(i).add(new RequestShipperMatchingRouteElement(p2shiper.get(shiperPoint.get(i)).getCode(),p2shiper.get(shiperPoint.get(i)).getLocation(),"PICKUP","-",0));
 		}
 		//code not capacity
 		/*while(itRe<pickupPoints.size()){
 			double min=1000000;
 			int vtmin=-1;
 			int xd=0;
 			Point poSh= shiperPoint.get(itShp);
 			for(int i=0;i<pickupPoints.size();i++){
 				if(d[i]==0)
 				if(dis[point2Index.get(poSh)]
 						[point2Index.get(pickupPoints.get(i))]<min){
 					min=dis[point2Index.get(poSh)][point2Index.get(pickupPoints.get(i))];
 					vtmin=i;
 					xd=1;
 				}
 			}
 			lrmi.set(itShp, lrmi.get(itShp)+min);
 			if(xd==0) break;
 			lr.get(itShp).add(
 					new RequestShipperMatchingRouteElement(p2ShipRePi.get(pickupPoints.get(vtmin)).getCode(),p2ShipRePi.get(pickupPoints.get(vtmin)).getPickupLocation(), "PICKUP"));
 			lr.get(itShp).add(
 					new RequestShipperMatchingRouteElement(p2ShipReDe.get(deliveryPoints.get(vtmin)).getCode(),p2ShipReDe.get(deliveryPoints.get(vtmin)).getDeliveryLocation(), "DELIVERY"));
 			shiperPoint.set(itShp, deliveryPoints.get(vtmin));
 			itShp=(itShp+1) % shiperPoint.size();
 			d[vtmin]=1;
 		}*/
 		ArrayList<ArrayList<Point>> bagShipper= new ArrayList<ArrayList<Point>>();
 		int capShp[]= new int[shiperPoint.size()];
 		for(int i=0;i< shiperPoint.size();i++){
 			ArrayList<Point> tt= new ArrayList<Point>();
 			bagShipper.add(tt);
 			capShp[i]=0;
 		}
 		while(itRe<pickupPoints.size()){
 			double min=1000000;
 			int vtmin=-1;
 			int xd=0;
 			Point poSh= shiperPoint.get(itShp);
 			for(int i=0;i<pickupPoints.size();i++){
 				if(d[i]==0)
 				if(dis[point2Index.get(poSh)]
 						[point2Index.get(pickupPoints.get(i))]<min){
 					min=dis[point2Index.get(poSh)][point2Index.get(pickupPoints.get(i))];
 					vtmin=i;
 					xd=1;
 				}
 			}
 			double min2=1000000;
 			int vtmin2=-1;
 			int xd2=0;
 			ArrayList<Point> cLBShipper= bagShipper.get(itShp);
 			if(cLBShipper!=null){
 				for(int i=0;i<cLBShipper.size();i++){
 						if(dis[point2Index.get(poSh)]
 								[point2Index.get(cLBShipper.get(i))]<min2){
 							min2=dis[point2Index.get(poSh)][point2Index.get(cLBShipper.get(i))];
 							vtmin2=i;
 							xd2=1;
 						}
 				}
 			}
 			int maxCap=3;
 			int sl=-1;
 			BigDecimal slmin;
 			//value.setScale(4);
 			String tmp="PICKUP";
 			ShipRequest shpR=null;
 			if(xd==0 && xd2==0){
 				System.out.println(name()+bagShipper+ cLBShipper+" "+itShp);
 				int xd3=0;
 				for(int i=0;i<bagShipper.size();i++){
 					if(bagShipper.get(i).size()>0) xd3=1;
 					System.out.println(name()+"size "+bagShipper.get(i).size());
 				}
 				System.out.println(name()+" "+xd3);
 				if (xd3==0) break;
 				else{
 					itShp=(itShp+1) % shiperPoint.size();
 					continue;
 				}
 			}
 			if (xd==1 && xd2==0) {
 				shpR=p2ShipRePi.get(pickupPoints.get(vtmin));
				slmin=new BigDecimal(min);
				d[vtmin]=1;
				cLBShipper.add(p2pRePiDe.get(pickupPoints.get(vtmin)));
				bagShipper.set(itShp, cLBShipper);
				shiperPoint.set(itShp, pickupPoints.get(vtmin));
 			} else
 			if(xd==1&&xd2==1&&cLBShipper.size()<shps[itShp].getCapacity() && min<min2 ){
 					shpR=p2ShipRePi.get(pickupPoints.get(vtmin));
 					slmin=new BigDecimal(min);
 					d[vtmin]=1;
 					cLBShipper.add(p2pRePiDe.get(pickupPoints.get(vtmin)));
 					bagShipper.set(itShp, cLBShipper);
 					shiperPoint.set(itShp, pickupPoints.get(vtmin));
 			
 			} 
 			else {
 				shpR= p2ShipReDe.get(cLBShipper.get(vtmin2));
 				slmin=new BigDecimal(min2);
 				shiperPoint.set(itShp, cLBShipper.get(vtmin2));
 				cLBShipper.remove(vtmin2);
 				bagShipper.set(itShp, cLBShipper);
 				tmp="DELIVERY";
 			}
 			//System.out.println(name()+slmin);
 			slmin=slmin.setScale(0, RoundingMode.HALF_UP);
 			//System.out.println(name()+slmin.setScale(2, RoundingMode.HALF_UP));
 			lrmi.set(itShp, lrmi.get(itShp)+slmin.doubleValue());
 			
 			
 			if(tmp.equals("PICKUP"))
 			lr.get(itShp).add(
 					new RequestShipperMatchingRouteElement(shpR.getCode(),shpR.getPickupLocation(), tmp,shpR.getPickupAddress(),slmin.doubleValue()));
 			else 
 				lr.get(itShp).add(
 	 					new RequestShipperMatchingRouteElement(shpR.getCode(),shpR.getDeliveryLocation(), tmp,shpR.getDeliveryAddress(),slmin.doubleValue()));
 			//shiperPoint.set(itShp, deliveryPoints.get(vtmin));
 			itShp=(itShp+1) % shiperPoint.size();
 			//System.out.println(name()+"end");
 		}
 		System.out.println(lrmi);
 		RequestShipperMatchingSolution re= new RequestShipperMatchingSolution();
 		RequestShipperMatchingRoute lRSMR[]=new RequestShipperMatchingRoute[lr.size()];
 		for(int i=0;i<lr.size();i++){
 			RequestShipperMatchingRouteElement lRE[]=new RequestShipperMatchingRouteElement[lr.get(i).size()];
 			lr.get(i).toArray(lRE);
 			for(int j=0;j<lRE.length-1;j++){
 				double dd=G.getDistance(lRE[j].getLatlng(), lRE[j+1].getLatlng());
 				if(dd==-1)
 					lRE[j].setDistance2Next(lRE[j+1].getDistance2Next());
 				else lRE[j].setDistance2Next(dd*1000);
 			}
 			lRE[lRE.length-1].setDistance2Next(0);
 			lRSMR[i]= new RequestShipperMatchingRoute();
 			lRSMR[i].setRoute(lRE);
 		}
 		
 		re.setRoutes(lRSMR);
		return re;
	}
	
	
	public RequestShipperMatchingSolution computeRequestShipperMatchingSolution(RequestShipperInput input){
		// TODO by Tu-Dat
		mID2Shipper = new HashMap<Integer, Shipper>();
		mID2Request = new HashMap<Integer, ShipRequest>();
		mPickupRe2Point = new HashMap<ShipRequest, Point>();
		mDeliveryRe2Point = new HashMap<ShipRequest, Point>();
		
		pickup_points = new ArrayList<Point>();
		delivery_points = new ArrayList<Point>();
		startPoints = new ArrayList<Point>();
		endPoints = new ArrayList<Point>();
		clientPoints = new ArrayList<Point>();
		allPoints = new ArrayList<Point>();
		
		lst_shippers = input.getShippers();
		lst_shipRequests = input.getRequests();
		
		int pointID = 0;
		
		n_shipper = lst_shippers.length;
		n_request = lst_shipRequests.length;
	
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
		
		stateModel();
		search();
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
		CS = new ConstraintSystemVR(mgr);
		
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
		}
		
		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		cost = new IFunctionVR[n_shipper];
		for(int i=1; i <= n_shipper; i++){
			cost[i-1] = new AccumulatedEdgeWeightsOnPathVR(awe, XR.endPoint(i));
		}
		
		mgr.close();
		
	}
	
	public void search(){
		print();
		XR.setRandom();
		print();
		
	}
	
	public void initGreedy(){
		
	}
	
	public void print(){
		System.out.println(XR);
		for(int i=0; i<n_shipper; i++){
			System.out.println("cost of route "+(i+1)+": "+cost[i].getValue());
		}
		System.out.println("violation: "+CS.violations());
	}
}
