package com.kse.crossDockingAPI.model;

public class TripElement {
	private int fromVehicle;
	private int toVehicle;
	private int time;
	
	public TripElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	public TripElement(int fromVehicle, int toVehicle, int time) {
		super();
		this.fromVehicle = fromVehicle;
		this.toVehicle = toVehicle;
		this.time = time;
	}
	public int getFromVehicle() {
		return fromVehicle;
	}
	public void setFromVehicle(int fromVehicle) {
		this.fromVehicle = fromVehicle;
	}
	public int getToVehicle() {
		return toVehicle;
	}
	public void setToVehicle(int toVehicle) {
		this.toVehicle = toVehicle;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	
	
}
