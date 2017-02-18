package com.kse.ezRoutingAPI.requestshippermatching.service;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.functions.MaxVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.RouteIndex;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyAddOnePointMoveExplorer;
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
	private LexMultiFunctions F;
	private IFunctionVR a_cost[];
	private IFunctionVR cost;
	
	private ArrayList<Point> pickup_points;
	private ArrayList<Point> delivery_points;
	private ArrayList<Point> startPoints;
	private ArrayList<Point> endPoints;
	private ArrayList<Point> clientPoints;
	private ArrayList<Point> allPoints;
	private HashMap<Point, Shipper> mPoint2Shipper;
	private HashMap<Point, ShipRequest> mPoint2Request;
//	private HashMap<ShipRequest, Point> mPickupRe2Point;
//	private HashMap<ShipRequest, Point> mDeliveryRe2Point;
	
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
 			if(xd==0 && xd2==0) break;
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
 			slmin=slmin.setScale(2, RoundingMode.HALF_UP);
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
 			lRSMR[i]= new RequestShipperMatchingRoute();
 			lRSMR[i].setRoute(lRE);
 		}
 		
 		re.setRoutes(lRSMR);
		return re;
	}
	
	
	public RequestShipperMatchingSolution computeRequestShipperMatchingSolution(RequestShipperInput input){
		// TODO by Tu-Dat
		mPoint2Shipper = new HashMap<Point, Shipper>();
		mPoint2Request = new HashMap<Point, ShipRequest>();
//		mPickupRe2Point = new HashMap<ShipRequest, Point>();
//		mDeliveryRe2Point = new HashMap<ShipRequest, Point>();
		
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
			mPoint2Shipper.put(p, lst_shippers[i]);
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
			mPoint2Request.put(p_pickup, lst_shipRequests[i]);
			//mPickupRe2Point.put(lst_shipRequests[i], p_pickup);
			pointID++;
			
			ll = new LatLng(lst_shipRequests[i].getDeliveryLocation());
			Point p_delivery = new Point(pointID,ll.lat,ll.lng);
			delivery_points.add(p_delivery);
			allPoints.add(p_delivery);
			clientPoints.add(p_delivery);
			mPoint2Request.put(p_delivery, lst_shipRequests[i]);
			//mPickupRe2Point.put(lst_shipRequests[i], p_delivery);
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
		
		RequestShipperMatchingSolution solution = new RequestShipperMatchingSolution();
		RequestShipperMatchingRoute sol_routes[] = new RequestShipperMatchingRoute[n_shipper];
		
		for(int i=1; i<=n_shipper; i++){
			ArrayList<RequestShipperMatchingRouteElement> sol_ele = new ArrayList<RequestShipperMatchingRouteElement>();
			Point x = XR.getStartingPointOfRoute(i);
			Shipper s = mPoint2Shipper.get(x);
			Point tmp = XR.next(x);
			RequestShipperMatchingRouteElement start_point = new RequestShipperMatchingRouteElement(s.getCode(), s.getLocation(), "PICKUP","--",gmq.computeDistanceHaversine(x.getX(), x.getY(),tmp.getX(),tmp.getY())); 
			
			sol_ele.add(start_point);
			int n_point = 1;
//			System.out.println("route["+i+"]: ");
//			System.out.println("end pont of route["+i+"]: "+XR.getTerminatingPointOfRoute(i));
			while(true){
				x = XR.next(x);
				Point nextX = XR.next(x);
				//System.out.print(x.ID+", ");
				if(x == XR.getTerminatingPointOfRoute(i)) break;
				
				RequestShipperMatchingRouteElement point;
				
				if(nextX == XR.getTerminatingPointOfRoute(i)){
					if(pickup_points.contains(x)){
						ShipRequest sq = mPoint2Request.get(x);
						point = new RequestShipperMatchingRouteElement(sq.getCode(), sq.getPickupLocation(),"PICKUP",sq.getPickupAddress(),0);
						sol_ele.add(point);
					}else{
						ShipRequest sq = mPoint2Request.get(x);
						point = new RequestShipperMatchingRouteElement(sq.getCode(), sq.getDeliveryLocation(),"DELIVERY",sq.getDeliveryAddress(),0);
						sol_ele.add(point);
					}
				}else{
					if(pickup_points.contains(x)){
						ShipRequest sq = mPoint2Request.get(x);
						ShipRequest nextSq = mPoint2Request.get(nextX);
						if(pickup_points.contains(nextX)){
							point = new RequestShipperMatchingRouteElement(sq.getCode(), sq.getPickupLocation(),"PICKUP",sq.getPickupAddress(),gmq.computeDistanceHaversine(sq.getPickupLocation(), nextSq.getPickupLocation()));
						}else{
							point = new RequestShipperMatchingRouteElement(sq.getCode(),sq.getPickupLocation(),"PICKUP",sq.getPickupAddress(),gmq.computeDistanceHaversine(sq.getPickupLocation(), nextSq.getDeliveryLocation()));
						}
						sol_ele.add(point);
					}else{
						ShipRequest sq = mPoint2Request.get(x);
						ShipRequest nextSq = mPoint2Request.get(nextX);
						if(pickup_points.contains(nextX)){
							point = new RequestShipperMatchingRouteElement(sq.getCode(), sq.getDeliveryLocation(),"DELIVERY",sq.getDeliveryAddress(),gmq.computeDistanceHaversine(sq.getDeliveryLocation(), nextSq.getPickupLocation()));
						}else{
							point = new RequestShipperMatchingRouteElement(sq.getCode(),sq.getDeliveryLocation(),"DELIVERY",sq.getDeliveryAddress(),gmq.computeDistanceHaversine(sq.getDeliveryLocation(), nextSq.getDeliveryLocation()));
						}
						sol_ele.add(point);
					}
				}
				
				n_point++;
			}
			sol_routes[i-1] = new RequestShipperMatchingRoute();
			RequestShipperMatchingRouteElement sol_route_array[] = new RequestShipperMatchingRouteElement[n_point]; 
			sol_routes[i-1].setRoute(sol_ele.toArray(sol_route_array));
		}
		solution.setRoutes(sol_routes);
		
		return solution;
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
		
		for(int i=0; i<pickup_points.size(); i++){
			Point p_pickup = pickup_points.get(i);
			Point p_delivery = delivery_points.get(i);
			
			IFunctionVR r_pickup = new RouteIndex(XR, p_pickup);
			IFunctionVR r_delivery = new RouteIndex(XR,p_delivery);
			CS.post(new Eq(r_pickup, r_delivery));
			
			IFunctionVR ir_pickup = new IndexOnRoute(XR, p_pickup);
			IFunctionVR ir_delivery = new IndexOnRoute(XR, p_delivery);
			CS.post(new Leq(ir_pickup, ir_delivery));
		}
		
		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		a_cost = new IFunctionVR[n_shipper];
		for(int i=1; i <= n_shipper; i++){
			a_cost[i-1] = new AccumulatedEdgeWeightsOnPathVR(awe, XR.endPoint(i));
		}
		cost = new MaxVR(a_cost);
		F = new LexMultiFunctions();
		F.add(CS);
		F.add(cost);
		
		mgr.close();
		
	}
	
	public void search(){
		print();
		initSequence();
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
		
		GenericLocalSearch gs = new GenericLocalSearch(mgr);
		gs.setNeighborhoodExplorer(NE);
		gs.setObjectiveFunction(F);
		gs.setMaxStable(50);
		
		gs.search(100, 5);
		
		print();
		
	}
	
	public void initSequence(){
		int it=0;
		while(it < pickup_points.size()){
			for(int i=1; i<=n_shipper; i++){
				Point x = XR.getTerminatingPointOfRoute(i);
				x = XR.prev(x);
				mgr.performAddOnePoint(pickup_points.get(it), x);
				x = XR.next(x);
				mgr.performAddOnePoint(delivery_points.get(it), x);
				it++;
			}
		}
	}
	
	public void print(){
		System.out.println(XR);
		for(int i=0; i<n_shipper; i++){
			System.out.println("cost of route "+(i+1)+": "+a_cost[i].getValue());
		}
		System.out.println("violation: "+CS.violations());
	}
}
