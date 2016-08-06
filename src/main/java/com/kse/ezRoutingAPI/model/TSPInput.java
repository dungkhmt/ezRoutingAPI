package com.kse.ezRoutingAPI.model;

public class TSPInput {
	private DeliveryRequest[] requests;
	private VehicleInfo vehicle;
	
	private DistanceElement[] distances;
	private DistanceElement[] travelTimes;
	
	
	private int timeLimit;
	private int maxIter;
	public DeliveryRequest[] getRequests() {
		return requests;
	}
	public void setRequests(DeliveryRequest[] requests) {
		this.requests = requests;
	}
	public VehicleInfo getVehicle() {
		return vehicle;
	}
	public void setVehicle(VehicleInfo vehicle) {
		this.vehicle = vehicle;
	}
	public DistanceElement[] getDistances() {
		return distances;
	}
	public void setDistances(DistanceElement[] distances) {
		this.distances = distances;
	}
	public int getTimeLimit() {
		return timeLimit;
	}
	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}
	public int getMaxIter() {
		return maxIter;
	}
	public void setMaxIter(int maxIter) {
		this.maxIter = maxIter;
	}
	
	
	public DistanceElement[] getTravelTimes() {
		return travelTimes;
	}
	public void setTravelTimes(DistanceElement[] travelTimes) {
		this.travelTimes = travelTimes;
	}
	public TSPInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	public TSPInput(DeliveryRequest[] requests, VehicleInfo vehicle,
			DistanceElement[] distances, DistanceElement[] travelTimes,
			int timeLimit, int maxIter) {
		super();
		this.requests = requests;
		this.vehicle = vehicle;
		this.distances = distances;
		this.travelTimes = travelTimes;
		this.timeLimit = timeLimit;
		this.maxIter = maxIter;
	}
	
	
	
}
