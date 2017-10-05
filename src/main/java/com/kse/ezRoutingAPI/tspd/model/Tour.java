package com.kse.ezRoutingAPI.tspd.model;

import java.util.ArrayList;

public class Tour {
	private TruckTour TD;
	private ArrayList<DroneDelivery> DD;
	private double totalCost;
	private double totalTime;
	
	public TruckTour getTD() {
		return TD;
	}
	public void setTD(TruckTour tD) {
		TD = tD;
	}
	public ArrayList<DroneDelivery> getDD() {
		return DD;
	}
	public void setDD(ArrayList<DroneDelivery> dD) {
		DD = dD;
	}
	public Tour(TruckTour tD, ArrayList<DroneDelivery> dD) {
		super();
		TD = tD;
		DD = dD;
	}
	public double getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}
	public double getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}
	public Tour(TruckTour tD, ArrayList<DroneDelivery> dD, double totalCost,
			double totalTime) {
		super();
		TD = tD;
		DD = dD;
		this.totalCost = totalCost;
		this.totalTime = totalTime;
	}
	@Override
	public String toString() {
		return "Tour [TD=" + TD + ", DD=" + DD + ", totalCost=" + totalCost
				+ ", totalTime=" + totalTime + "]";
	}
	
	
}
