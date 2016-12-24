package com.kse.ezRoutingAPI.dichung.model;

import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripInput;

public class GlobalSharedTaxiInput {
	private SharedTaxiInput[] airportInput;
	private SharedLongTripInput[] longtripInput;
	public GlobalSharedTaxiInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	public GlobalSharedTaxiInput(SharedTaxiInput[] airportInput,
			SharedLongTripInput[] longtripInput) {
		super();
		this.airportInput = airportInput;
		this.longtripInput = longtripInput;
	}
	public SharedTaxiInput[] getAirportInput() {
		return airportInput;
	}
	public void setAirportInput(SharedTaxiInput[] airportInput) {
		this.airportInput = airportInput;
	}
	public SharedLongTripInput[] getLongtripInput() {
		return longtripInput;
	}
	public void setLongtripInput(SharedLongTripInput[] longtripInput) {
		this.longtripInput = longtripInput;
	}
	
	
}
