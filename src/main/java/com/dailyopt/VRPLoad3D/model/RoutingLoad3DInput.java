package com.dailyopt.VRPLoad3D.model;

public class RoutingLoad3DInput {
	private Request[] requests;
	private Vehicle[] vehicles;
	private DistanceElement[] distances;
	private Depot depot;
	
	
	public Depot getDepot() {
		return depot;
	}
	public void setDepot(Depot depot) {
		this.depot = depot;
	}
	public Request[] getRequests() {
		return requests;
	}
	public void setRequests(Request[] requests) {
		this.requests = requests;
	}
	public Vehicle[] getVehicles() {
		return vehicles;
	}
	public void setVehicles(Vehicle[] vehicles) {
		this.vehicles = vehicles;
	}
	public DistanceElement[] getDistances() {
		return distances;
	}
	public void setDistances(DistanceElement[] distances) {
		this.distances = distances;
	}
	public RoutingLoad3DInput(Request[] requests, Vehicle[] vehicles,
			DistanceElement[] distances, Depot depot) {
		super();
		this.requests = requests;
		this.vehicles = vehicles;
		this.distances = distances;
		this.depot = depot;
	}
	public RoutingLoad3DInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
