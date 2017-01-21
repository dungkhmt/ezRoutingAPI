package com.kse.ezRoutingAPI.dichungduongdai.model;

public class StepDirection {
	private String startPosition; // lat,lng
	private String endPosition;// lat,lng
	private int duration;// travel duration in second
	private int distance;// in meters
	public String getStartPosition() {
		return startPosition;
	}
	public void setStartPosition(String startPosition) {
		this.startPosition = startPosition;
	}
	public String getEndPosition() {
		return endPosition;
	}
	public void setEndPosition(String endPosition) {
		this.endPosition = endPosition;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public int getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
	public StepDirection(String startPosition, String endPosition,
			int duration, int distance) {
		super();
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.duration = duration;
		this.distance = distance;
	}
	public StepDirection() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
