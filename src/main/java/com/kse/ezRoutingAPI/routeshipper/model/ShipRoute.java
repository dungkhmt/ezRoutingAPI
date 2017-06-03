package com.kse.ezRoutingAPI.routeshipper.model;

public class ShipRoute {
	private ShipRouteElement[] route;

	public ShipRouteElement[] getRoute() {
		return route;
	}

	public void setRoute(ShipRouteElement[] route) {
		this.route = route;
	}

	public ShipRoute(ShipRouteElement[] route) {
		super();
		this.route = route;
	}

	public ShipRoute() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
