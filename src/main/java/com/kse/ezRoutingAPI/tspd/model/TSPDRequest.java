package com.kse.ezRoutingAPI.tspd.model;


public class TSPDRequest {
	private double truckSpeed; //Speed of truck
	private double droneSpeed; //Speed of drone
	private int truckCost;//cost per unit of trunk
	private int droneCost; //cost per unit of drone
	private double delta;
	private double endurance;
	Point[] listPoints;

	public Point[] getListPoints() {
		return listPoints;
	}

	public void setListPoints(Point[] listPoints) {
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

	public TSPDRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TSPDRequest(double truckSpeed, double droneSpeed, int truckCost,
			int droneCost, double delta, double endurance, Point[] listPoints) {
		super();
		this.truckSpeed = truckSpeed;
		this.droneSpeed = droneSpeed;
		this.truckCost = truckCost;
		this.droneCost = droneCost;
		this.delta = delta;
		this.endurance = endurance;
		this.listPoints = listPoints;
	}

	
	
}
