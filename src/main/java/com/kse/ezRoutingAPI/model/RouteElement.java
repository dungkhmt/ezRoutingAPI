package com.kse.ezRoutingAPI.model;

public class RouteElement {
	private int point;
	private int arivalTime;
	private int departureTime;
	private double accumulatedDistance;
	public int getPoint() {
		return point;
	}
	public void setPoint(int point) {
		this.point = point;
	}
	public int getArivalTime() {
		return arivalTime;
	}
	public void setArivalTime(int arivalTime) {
		this.arivalTime = arivalTime;
	}
	public int getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(int departureTime) {
		this.departureTime = departureTime;
	}
	public double getAccumulatedDistance() {
		return accumulatedDistance;
	}
	public void setAccumulatedDistance(double accumulatedDistance) {
		this.accumulatedDistance = accumulatedDistance;
	}
	public RouteElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	public RouteElement(int point, int arivalTime, int departureTime,
			double accumulatedDistance) {
		super();
		this.point = point;
		this.arivalTime = arivalTime;
		this.departureTime = departureTime;
		this.accumulatedDistance = accumulatedDistance;
	}
	@Override
	public String toString() {
		return "RouteElement [point=" + point + ", arivalTime=" + arivalTime
				+ ", departureTime=" + departureTime + ", accumulatedDistance="
				+ accumulatedDistance + "]";
	}
	
	
}
