package com.kse.ezRoutingAPI.requestshippermatching.service;

import java.util.ArrayList;
import java.util.HashMap;

import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;



import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.LatLng;

import com.kse.ezRoutingAPI.model.Route;
import com.kse.ezRoutingAPI.model.RouteElement;
import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperInput;
import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperMatchingRoute;
import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperMatchingRouteElement;
import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperMatchingSolution;
import com.kse.ezRoutingAPI.requestshippermatching.model.ShipRequest;
import com.kse.ezRoutingAPI.requestshippermatching.model.Shipper;

public class RequestShipperMatchingService {
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
 			p= new Point(idPoint,llng.lat,llng.lng);
 			//clientPoints.add(p);
 			deliveryPoints.add(p);
 			allPoints.add(p);
 			p2ShipReDe.put(p, srs[i]);
 			point2Index.put(p, idPoint);
 		}
 		System.out.println("Num of shipper"+shps.length);
 		double dis[][]= new double[allPoints.size()][allPoints.size()];
 		GoogleMapsQuery G= new GoogleMapsQuery();
 		for(int i=0;i<allPoints.size();i++) dis[i][i]=100000000;
 		for(int i=0;i<allPoints.size();i++)
 			for(int j=i+1;j<allPoints.size();j++){
 				dis[i][j]=allPoints.get(i).distance(allPoints.get(j));
 				dis[j][i]=dis[i][j];
 			}
 		
 		int d[]=new int[pickupPoints.size()];
 		for(int i=0;i<d.length;i++) d[i]=0;
 		int itShp=0;
 		int itRe=0;
 		ArrayList<ArrayList<RequestShipperMatchingRouteElement>> lr = new ArrayList<ArrayList<RequestShipperMatchingRouteElement>>();
 		for(int i=0;i<shiperPoint.size();i++) {
 			lr.add(new ArrayList<RequestShipperMatchingRouteElement>());
 			
 			lr.get(i).add(new RequestShipperMatchingRouteElement(p2shiper.get(shiperPoint.get(i)).getCode(),"PICKUP"));
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
 			if(xd==0) break;
 			lr.get(itShp).add(
 					new RequestShipperMatchingRouteElement(p2ShipRePi.get(pickupPoints.get(vtmin)).getCode(), "PICKUP"));
 			lr.get(itShp).add(
 					new RequestShipperMatchingRouteElement(p2ShipReDe.get(deliveryPoints.get(vtmin)).getCode(), "DELIVERY"));
 					
 			shiperPoint.set(itShp, deliveryPoints.get(vtmin));
 			itShp=(itShp+1) % shiperPoint.size();
 			d[vtmin]=1;
 			
 			
 		}
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
}
