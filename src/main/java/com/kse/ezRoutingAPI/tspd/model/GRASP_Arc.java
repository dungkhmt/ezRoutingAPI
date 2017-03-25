package com.kse.ezRoutingAPI.tspd.model;

public class GRASP_Arc {
	private Point i;
	private Point k;
	private double cost;
	public Point getI() {
		return i;
	}
	public void setI(Point i) {
		this.i = i;
	}
	public Point getK() {
		return k;
	}
	public void setK(Point k) {
		this.k = k;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public GRASP_Arc(Point i, Point k, double cost) {
		super();
		this.i = i;
		this.k = k;
		this.cost = cost;
	}
	
}
