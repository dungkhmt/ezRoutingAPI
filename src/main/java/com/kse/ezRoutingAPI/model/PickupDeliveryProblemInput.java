package com.kse.ezRoutingAPI.model;

public class PickupDeliveryProblemInput {
	private PickupDeliveryRequest[] requests;
	private VehicleInfo[] vehicles;
	
	private DistanceElement[] distances;
	private DistanceElement[] travelTimes;
	
	private int timeLimit;

	public PickupDeliveryRequest[] getRequests() {
		return requests;
	}

	public void setRequests(PickupDeliveryRequest[] requests) {
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

	public PickupDeliveryProblemInput() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PickupDeliveryProblemInput(PickupDeliveryRequest[] requests,
			VehicleInfo[] vehicles, DistanceElement[] distances,
			DistanceElement[] travelTimes, int timeLimit) {
		super();
		this.requests = requests;
		this.vehicles = vehicles;
		this.distances = distances;
		this.travelTimes = travelTimes;
		this.timeLimit = timeLimit;
	}
	
	
}
