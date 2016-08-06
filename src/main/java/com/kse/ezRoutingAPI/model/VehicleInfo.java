package com.kse.ezRoutingAPI.model;

public class VehicleInfo {
	private int startPoint;
	private int endPoint;
	private int capacity;

	private int earlyStartTimePoint;
	private int lateStartTimePoint;
	private int earlyEndTimePoint;
	private int lateEndTimePoint;
	public int getStartPoint() {
		return startPoint;
	}
	public void setStartPoint(int startPoint) {
		this.startPoint = startPoint;
	}
	public int getEndPoint() {
		return endPoint;
	}
	public void setEndPoint(int endPoint) {
		this.endPoint = endPoint;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public int getEarlyStartTimePoint() {
		return earlyStartTimePoint;
	}
	public void setEarlyStartTimePoint(int earlyStartTimePoint) {
		this.earlyStartTimePoint = earlyStartTimePoint;
	}
	public int getLateStartTimePoint() {
		return lateStartTimePoint;
	}
	public void setLateStartTimePoint(int lateStartTimePoint) {
		this.lateStartTimePoint = lateStartTimePoint;
	}
	public int getEarlyEndTimePoint() {
		return earlyEndTimePoint;
	}
	public void setEarlyEndTimePoint(int earlyEndTimePoint) {
		this.earlyEndTimePoint = earlyEndTimePoint;
	}
	public int getLateEndTimePoint() {
		return lateEndTimePoint;
	}
	public void setLateEndTimePoint(int lateEndTimePoint) {
		this.lateEndTimePoint = lateEndTimePoint;
	}
	public VehicleInfo(int startPoint, int endPoint, int capacity,
			int earlyStartTimePoint, int lateStartTimePoint,
			int earlyEndTimePoint, int lateEndTimePoint) {
		super();
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.capacity = capacity;
		this.earlyStartTimePoint = earlyStartTimePoint;
		this.lateStartTimePoint = lateStartTimePoint;
		this.earlyEndTimePoint = earlyEndTimePoint;
		this.lateEndTimePoint = lateEndTimePoint;
	}
	public VehicleInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
