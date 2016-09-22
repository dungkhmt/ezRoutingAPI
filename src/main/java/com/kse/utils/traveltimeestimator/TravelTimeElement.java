package com.kse.utils.traveltimeestimator;

public class TravelTimeElement {
	public int fromDistance;// in meters
	public int toDistance;// in meters
	public int traveltime;// in seconds
	public TravelTimeElement(int fromDistance, int toDistance, int traveltime) {
		super();
		this.fromDistance = fromDistance;
		this.toDistance = toDistance;
		this.traveltime = traveltime;
	}
	
}
