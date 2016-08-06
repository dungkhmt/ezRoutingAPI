package com.kse.ezRoutingAPI.model;

public class DeliveryInput {
	private DeliveryRequest[] requests;
	private VehicleInfo[] vehicles;
	
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
	public VehicleInfo[] getVehicles() {
		return vehicles;
	}
	public void setVehicles(VehicleInfo[] vehicles) {
		this.vehicles = vehicles;
	}
	public DistanceElement[] getDistances() {
		return distances;
	}
	public void setDistances(DistanceElement[] distances) {
		this.distances = distances;
	}
	public DistanceElement[] getTravelTimes() {
		return travelTimes;
	}
	public void setTravelTimes(DistanceElement[] travelTimes) {
		this.travelTimes = travelTimes;
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
	public DeliveryInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DeliveryInput(DeliveryRequest[] requests, VehicleInfo[] vehicles,
			DistanceElement[] distances, DistanceElement[] travelTimes,
			int timeLimit, int maxIter) {
		super();
		this.requests = requests;
		this.vehicles = vehicles;
		this.distances = distances;
		this.travelTimes = travelTimes;
		this.timeLimit = timeLimit;
		this.maxIter = maxIter;
	}

	
}
