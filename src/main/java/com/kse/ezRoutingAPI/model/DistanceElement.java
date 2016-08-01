package com.kse.ezRoutingAPI.model;

public class DistanceElement {
	private int fromPoint;
	private int toPoint;
	private double distance;
	public int getFromPoint() {
		return fromPoint;
	}
	public DistanceElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DistanceElement(int fromPoint, int toPoint, double distance) {
		super();
		this.fromPoint = fromPoint;
		this.toPoint = toPoint;
		this.distance = distance;
	}
	public void setFromPoint(int fromPoint) {
		this.fromPoint = fromPoint;
	}
	public int getToPoint() {
		return toPoint;
	}
	public void setToPoint(int toPoint) {
		this.toPoint = toPoint;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
}
