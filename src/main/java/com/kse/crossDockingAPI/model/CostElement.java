package com.kse.crossDockingAPI.model;

public class CostElement {
	private int fromDoor;
	private int toDoor;
	private double cost;
	
	public CostElement(int fromDoor, int toDoor, double cost) {
		super();
		this.fromDoor = fromDoor;
		this.toDoor = toDoor;
		this.cost = cost;
	}
	public int getFromDoor() {
		return fromDoor;
	}
	public void setFromDoor(int fromDoor) {
		this.fromDoor = fromDoor;
	}
	public int getToDoor() {
		return toDoor;
	}
	public void setToDoor(int toDoor) {
		this.toDoor = toDoor;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public CostElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
