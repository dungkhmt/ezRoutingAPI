package com.kse.ezRoutingAPI.pickupdeliverycontainers.model;

public class PickupDeliverySolution {
	private boolean solution;
	private double violations;
	private double traveldistance;
	private PickupDeliveryRoute[] routes;

	public PickupDeliverySolution() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PickupDeliverySolution(boolean solution, double violations, double traveldistance, PickupDeliveryRoute[] routes) {
		super();
		this.solution = solution;
		this.violations = violations;
		this.traveldistance = traveldistance;
		this.routes = routes;
	}

	
	public boolean isSolution() {
		return solution;
	}

	public void setSolution(boolean solution) {
		this.solution = solution;
	}

	public double getViolations() {
		return violations;
	}

	public void setViolations(double violations) {
		this.violations = violations;
	}

	public double getTraveldistance() {
		return traveldistance;
	}

	public void setTraveldistance(double traveldistance) {
		this.traveldistance = traveldistance;
	}

	public PickupDeliveryRoute[] getRoutes() {
		return routes;
	}

	public void setRoutes(PickupDeliveryRoute[] routes) {
		this.routes = routes;
	}
	
}
