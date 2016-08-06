package com.kse.ezRoutingAPI.model;

public class Route {
	int len;
	double distance;
	RouteElement[] sequence;
	public int getLen() {
		return len;
	}
	public void setLen(int len) {
		this.len = len;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public RouteElement[] getSequence() {
		return sequence;
	}
	public void setSequence(RouteElement[] sequence) {
		this.sequence = sequence;
	}
	public Route() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Route(int len, double distance, RouteElement[] sequence) {
		super();
		this.len = len;
		this.distance = distance;
		this.sequence = sequence;
	}

	
}
