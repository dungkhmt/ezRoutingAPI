package com.kse.ezRoutingAPI.model.dichung;

public class SharedTaxiSolution {
	private SharedTaxiRoute[] routes;

	public SharedTaxiSolution() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SharedTaxiSolution(SharedTaxiRoute[] routes) {
		super();
		this.routes = routes;
	}

	public SharedTaxiRoute[] getRoutes() {
		return routes;
	}

	public void setRoutes(SharedTaxiRoute[] routes) {
		this.routes = routes;
	}
	
}
