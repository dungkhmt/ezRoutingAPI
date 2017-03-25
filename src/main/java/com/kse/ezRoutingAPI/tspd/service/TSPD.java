package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;

public class TSPD {
	private int C1 = 30;//cost per unit of trunk
	private int C2 = 2; //cost per unit of drone
	private double delta=0.5;
	private double e = 15;
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

	
	public double d_drone(Point i, Point j){
		return Math.sqrt(Math.pow(i.getLat()-j.getLng(), 2)+Math.pow(i.getLng()-j.getLng(), 2));
	}
	
	public double d_truck(Point i, Point j){
		return Math.sqrt(Math.pow(i.getLat()-j.getLng(), 2)+Math.pow(i.getLng()-j.getLng(), 2));
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
	
	public boolean checkWaitTime(Point i, Point j, Point k){
		return Math.abs(d_truck(i, k) - (d_drone(i, j) + d_drone(j, k))) <= delta;
	}
	
	public boolean isDroneDelivery(Point i, Point j, Point k){
		for(int index=0; index<P.size(); index++){
			DroneDelivery tmp_dd = P.get(index);
			boolean check = tmp_dd.getLauch_node().equals(i) && tmp_dd.getDrone_node().equals(j) && tmp_dd.getRendezvous_node().equals(k);
			if(check && checkWaitTime(i, j, k)){
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Point> getTrunkOnlyNodes(Tour tspd){
		ArrayList<Point> TD = tspd.getTD().getTrunk_tour();
		ArrayList<DroneDelivery> DD = tspd.getDD();
		
		ArrayList<Point> trunkOnlyNodes = new ArrayList<Point>();
		
		for(int i=0; i<TD.size(); i++){
			Point pi = TD.get(i);
			for(int j=0; j<DD.size(); j++){
				
				if()
			}
		}
	}
}
