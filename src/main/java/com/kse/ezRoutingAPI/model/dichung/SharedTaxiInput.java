package com.kse.ezRoutingAPI.model.dichung;

public class SharedTaxiInput {
	private SharedTaxiRequest[] requests;
	private int[] vehicleCapacities;
	private int maxWaitTime;// in seconds
	
	private int maxTime;
	
	public SharedTaxiInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public SharedTaxiInput(SharedTaxiRequest[] requests,
			int[] vehicleCapacities, int maxWaitTime, int maxTime) {
		super();
		this.requests = requests;
		this.vehicleCapacities = vehicleCapacities;
		this.maxTime = maxTime;
		this.maxWaitTime = maxWaitTime;
	}
	
	public int getMaxWaitTime() {
		return maxWaitTime;
	}

	public void setMaxWaitTime(int maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	public int[] getVehicleCapacities() {
		return vehicleCapacities;
	}
	public void setVehicleCapacities(int[] vehicleCapacities) {
		this.vehicleCapacities = vehicleCapacities;
	}
	public SharedTaxiRequest[] getRequests() {
		return requests;
	}
	public void setRequests(SharedTaxiRequest[] requests) {
		this.requests = requests;
	}
	public int getMaxTime() {
		return maxTime;
	}
	public void setMaxTime(int maxTime) {
		this.maxTime = maxTime;
	}
	
	
}
