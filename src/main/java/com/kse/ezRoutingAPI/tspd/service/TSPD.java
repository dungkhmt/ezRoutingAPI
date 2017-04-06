package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;

import org.apache.xmlbeans.impl.jam.xml.TunnelledException;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;

public class TSPD {
	private int C1;//cost per unit of trunk
	private int C2; //cost per unit of drone
	private double delta;
	private double e;
	private double truckSpeed; //Speed of truck
	private double droneSpeed; //Speed of drone
	private Point startPoint;
	private ArrayList<Point> clientPoints;
	private Point endPoint;
	private ArrayList<DroneDelivery> P;
	
	
	public int getC1() {
		return C1;
	}
	public void setC1(int c1) {
		C1 = c1;
	}
	public int getC2() {
		return C2;
	}
	public void setC2(int c2) {
		C2 = c2;
	}
	public double getDelta() {
		return delta;
	}
	public void setDelta(double delta) {
		this.delta = delta;
	}
	public double getE() {
		return e;
	}
	public void setE(double e) {
		this.e = e;
	}
	public Point getStartPoint() {
		return startPoint;
	}
	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}
	public ArrayList<Point> getClientPoints() {
		return clientPoints;
	}
	public void setClientPoints(ArrayList<Point> clientPoints) {
		this.clientPoints = clientPoints;
	}
	public Point getEndPoint() {
		return endPoint;
	}
	public void setEndPoint(Point endPoint) {
		this.endPoint = endPoint;
	}	
	public double getTruckSpeed() {
		return truckSpeed;
	}
	public void setTruckSpeed(double truckSpeed) {
		this.truckSpeed = truckSpeed;
	}
	public double getDroneSpeed() {
		return droneSpeed;
	}
	public void setDroneSpeed(double droneSpeed) {
		this.droneSpeed = droneSpeed;
	}
	
	public TSPD(int c1, int c2, double delta, double e, double truckSpeed,
			double droneSpeed, Point startPoint, ArrayList<Point> clientPoints,
			Point endPoint) {
		super();
		C1 = c1;
		C2 = c2;
		this.delta = delta;
		this.e = e;
		this.truckSpeed = truckSpeed;
		this.droneSpeed = droneSpeed;
		this.startPoint = startPoint;
		this.clientPoints = clientPoints;
		this.endPoint = endPoint;
		build_P();
	}
	
	
	public void build_P(){
		P = new ArrayList<DroneDelivery>();
		for(int i=0; i<clientPoints.size()-2; i++){
			for(int j=i+1; j<clientPoints.size()-1; j++){
				for(int k=j+1; k<clientPoints.size(); k++){
					Point pi = clientPoints.get(i);
					Point pj = clientPoints.get(j);
					Point pk = clientPoints.get(k);
					double dDrone = d_drone(pi, pj) + d_drone(pj, pk);
					if(dDrone <= e && Math.abs(d_truck(pi, pk)-dDrone) <= delta){
						P.add(new DroneDelivery(pi,pj,pk));
					}
				}
			}
		}
		
		for(int i=0; i<clientPoints.size()-1; i++){
			for(int j=0; j<clientPoints.size(); j++){
				Point pi = clientPoints.get(i);
				Point pj = clientPoints.get(j);
				
				double dsDrone = d_drone(startPoint, pi) + d_drone(pi, pj);
				double deDrone = d_drone(pi, pj) + d_drone(pj, endPoint);
				
				if(dsDrone <= e && Math.abs(d_truck(startPoint, pj)-dsDrone) <= delta){
					P.add(new DroneDelivery(startPoint, pi, pj));
				}
				
				if(deDrone <= e && Math.abs(d_truck(pi,endPoint)-deDrone) <= delta){
					P.add(new DroneDelivery(pi,pj,endPoint));
				}
			}
		}
		System.out.println("P="+P.toString());
	}
	
	public boolean inP(Point i, Point j, Point k){
		for(int in=0; in<P.size(); in++){
			DroneDelivery dd = P.get(in);
			if(dd.getLauch_node().equals(i) && dd.getDrone_node().equals(j) && dd.getRendezvous_node().equals(k)){
				return true;
			}
		}
		return false;
	}
	
	public double d_drone(Point i, Point j){
		return Math.sqrt(Math.pow(i.getLat()-j.getLat(), 2)+Math.pow(i.getLng()-j.getLng(), 2));
	}
	
	public double d_truck(Point i, Point j){
		return Math.sqrt(Math.pow(i.getLat()-j.getLat(), 2)+Math.pow(i.getLng()-j.getLng(), 2));
	}
	
	public double cost(Point i, Point j){
		return C1*d_truck(i, j);
	}
	
	public double cost(TruckTour td){
		ArrayList<Point> point_tour= td.getTruck_tour();
		double cost = 0;
		for(int i=0; i<point_tour.size()-1; i++){
			cost += C1*d_truck(point_tour.get(i), point_tour.get(i+1));
		}
		return cost;
	}
	
	public double cost(DroneDelivery dd){
		return C2*(d_drone(dd.getLauch_node(), dd.getDrone_node())+ d_drone(dd.getDrone_node(),dd.getRendezvous_node()));
	}
	
	public double cost(ArrayList<DroneDelivery> dd){
		double cost = 0;
		for(int i=0; i< dd.size(); i++){
			cost += cost(dd.get(i));
		}
		return cost;
	}
	
	public double cost(int i, int k, ArrayList<Point> s){
		double cost = 0;
		for(int j=i; j<k; j++){
			cost += C1*d_truck(s.get(j), s.get(j+1));
		}
		return cost;
	}
	
	public double cost(Point i, Point j, Point k){
		return C2*(d_drone(i, j)+d_drone(j, k));
	}
	
	public double cost(Tour tspd){
		TruckTour td = tspd.getTD();
		ArrayList<DroneDelivery> dd = tspd.getDD();
		return cost(td) + cost(dd); 
	}
	
	public boolean checkWaitTime(Point i, Point j, Point k, ArrayList<Point> truckTour){
		int iLaunchNode = truckTour.indexOf(i);
		int irendezvousNode = truckTour.indexOf(k);
//		System.out.println("TSPD::checkWaitTime("+i.getID()+","+j.getID()+","+k.getID()+")::index_i="+iLaunchNode+"  index_k="+irendezvousNode);
//		System.out.println("TSPD::checkWaitTime::trunckTour: "+truckTour.toString());
		double distanceTruck = 0;
		//System.out.println(name()+"size "+truckTour.size());
		for(int in=iLaunchNode; in<irendezvousNode; in++){
			//System.out.println(name()+"in "+in);
				distanceTruck += d_truck(truckTour.get(in),
						truckTour.get(in+1));
		}
		return Math.abs(distanceTruck - (d_drone(i, j) + d_drone(j, k))) <= delta;
	}
	
	public boolean isDroneDelivery(Point i, Point j, Point k, ArrayList<Point> trucktour){
		for(int index=0; index<P.size(); index++){
			DroneDelivery tmp_dd = P.get(index);
			boolean check = tmp_dd.getLauch_node().equals(i) && tmp_dd.getDrone_node().equals(j) && tmp_dd.getRendezvous_node().equals(k);
			if(check && checkWaitTime(i, j, k, trucktour)){
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Point> getTruckOnlyNodes(Tour tspd){
		ArrayList<Point> TD = tspd.getTD().getTruck_tour();
		ArrayList<DroneDelivery> DD = tspd.getDD();
		
		ArrayList<Point> trunkOnlyNodes = new ArrayList<Point>();
		
		for(int i=0; i<TD.size(); i++){
			Point pi = TD.get(i);
			int check = 0;
			for(int j=0; j<DD.size(); j++){
				DroneDelivery dd_tmp = DD.get(j);
				if(dd_tmp.checkDroneDeliveryPoint(pi)){
					check++;
				}
			}
			if(check == 0){
				trunkOnlyNodes.add(pi);
			}
		}
		
		return trunkOnlyNodes;
	}
	
	public ArrayList<Point> getDroneNodes(Tour tspd){
		ArrayList<Point> TD = tspd.getTD().getTruck_tour();
		ArrayList<DroneDelivery> DD = tspd.getDD();
		
		ArrayList<Point> droneNodes = new ArrayList<Point>();
		
		for(int j=0; j<DD.size(); j++){
			DroneDelivery dd_tmp = DD.get(j);
			droneNodes.add(dd_tmp.getDrone_node());	
		}
		
		return droneNodes;
	}
	
	public boolean checkConstraint(Tour tour){
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> dronDeliveries = tour.getDD();
		
		boolean checkWaitime = true;
		boolean checkDroneEndurance = false;
		for(int i=0; i<dronDeliveries.size(); i++){
			DroneDelivery dd_tmp = dronDeliveries.get(i);
			if(!checkWaitTime(dd_tmp.getLauch_node(), dd_tmp.getDrone_node(), dd_tmp.getRendezvous_node(), truckTour)){
				checkWaitime = false;
			}
			double droneEndurance = d_drone(dd_tmp.getLauch_node(), dd_tmp.getDrone_node()) + d_drone(dd_tmp.getDrone_node(),dd_tmp.getRendezvous_node());
			
			if(droneEndurance <= e) 
				checkDroneEndurance = true;
		}
		
		return checkWaitime && checkDroneEndurance;
	}
	public Point drone(Point start,Point end,Tour tour){
		ArrayList<Point> listTruckPoint=tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> listDroneDeli= tour.getDD();
		for(int i=0;i<listDroneDeli.size();i++ ){
			DroneDelivery d=listDroneDeli.get(i);
			if(d.getLauch_node().equals(startPoint)||d.getRendezvous_node().equals(end)){
				return d.getDrone_node();
			}
		}
		return null;
	}
	
	public int checkNotOverLapDroneDelivery(DroneDelivery de,Tour tour){
		int d[]= new int[tour.getTD().getTruck_tour().size()];
		for(int i=0;i<d.length-1;i++){
			d[i]=0;
		}
		ArrayList<DroneDelivery> lde=tour.getDD();
		ArrayList<Point> ltrt=tour.getTD().getTruck_tour();
		for(int i=0;i<lde.size();i++){
			DroneDelivery ide=lde.get(i);
			for(int j=ltrt.indexOf(ide.getLauch_node());j<ltrt.indexOf(ide.getRendezvous_node());j++){
				d[j]+=1;
				if(d[j]>1) return -1;
			}
		}
		for(int j=ltrt.indexOf(de.getLauch_node());j<ltrt.indexOf(de.getRendezvous_node());j++){
			if(d[j]>0) return 0;
		}
		return 1;
	}
	String name(){
		return "TSPD:: ";
	}
}
