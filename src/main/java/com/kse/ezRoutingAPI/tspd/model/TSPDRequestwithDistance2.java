package com.kse.ezRoutingAPI.tspd.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TSPDRequestwithDistance2 {
	private double truckSpeed; //Speed of truck
	private double droneSpeed; //Speed of drone
	private int truckCost;//cost per unit of trunk
	private int droneCost; //cost per unit of drone
	private double delta;
	private double endurance;
	Point2[] listPoints;
	Map<String,Double> map;
	
	public Map<String, Double> getMap() {
		return map;
	}

	public void setMap(Map<String, Double> map) {
		this.map = map;
	}

	public Point2[] getListPoints() {
		return listPoints;
	}

	public void setListPoints(Point2[] listPoints) {
		this.listPoints = listPoints;
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

	public int getTruckCost() {
		return truckCost;
	}

	public void setTruckCost(int truckCost) {
		this.truckCost = truckCost;
	}

	public int getDroneCost() {
		return droneCost;
	}

	public void setDroneCost(int droneCost) {
		this.droneCost = droneCost;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public double getEndurance() {
		return endurance;
	}

	public void setEndurance(double endurance) {
		this.endurance = endurance;
	}

	public TSPDRequestwithDistance2() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Map<Integer,Boolean> getAllowDroneMap(){
		Map<Integer,Boolean> hashMap= new HashMap<Integer, Boolean>();
		//System.out.println("listPoint"+listPoints.toString());
		for(int i=0;i<listPoints.length;i++){
			hashMap.put(listPoints[i].getID(), listPoints[i].isAllowdrone());
		}
		return hashMap;
	}
	public TSPDRequestwithDistance getTspdRequestwithDistance(){
		Point[] lp=new Point[listPoints.length];
		//System.out.println(listPoints);
		for(int i=0;i<listPoints.length;i++){
			//System.out.println(listPoints[i].getID());
			Point p= new Point(listPoints[i].getID(), listPoints[i].getLat(), listPoints[i].getLng());
			lp[i]=p;
		}
		TSPDRequestwithDistance tspdRequestwithDistance= new TSPDRequestwithDistance(truckSpeed, droneSpeed, truckCost, droneCost, delta, endurance, lp, map);
		return tspdRequestwithDistance;
	}
	public TSPDRequestwithDistance2(double truckSpeed, double droneSpeed, int truckCost,
			int droneCost, double delta, double endurance, Point2[] listPoints,Map<String,Double> map) {
		super();
		this.truckSpeed = truckSpeed;
		this.droneSpeed = droneSpeed;
		this.truckCost = truckCost;
		this.droneCost = droneCost;
		this.delta = delta;
		this.endurance = endurance;
		this.listPoints = listPoints;
		this.map=map;
	}

	@Override
	public String toString() {
		return "TSPDRequestwithDistance2 [truckSpeed=" + truckSpeed
				+ ", droneSpeed=" + droneSpeed + ", truckCost=" + truckCost
				+ ", droneCost=" + droneCost + ", delta=" + delta
				+ ", endurance=" + endurance + ", listPoints="
				+ Arrays.toString(listPoints) + ", map=" + map + "]";
	}

}
