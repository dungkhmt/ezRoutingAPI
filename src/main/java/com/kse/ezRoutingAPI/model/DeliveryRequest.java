package com.kse.ezRoutingAPI.model;

public class DeliveryRequest {
	private int point;
	private int demand;// amount of demand
	
	private int earlyDeliveryTime;
	private int lateDeliveryTime;
	private int serviceDuration;
	public int getPoint() {
		return point;
	}
	public void setPoint(int point) {
		this.point = point;
	}
	public int getDemand() {
		return demand;
	}
	public void setDemand(int demand) {
		this.demand = demand;
	}
	public int getEarlyDeliveryTime() {
		return earlyDeliveryTime;
	}
	public void setEarlyDeliveryTime(int earlyDeliveryTime) {
		this.earlyDeliveryTime = earlyDeliveryTime;
	}
	public int getLateDeliveryTime() {
		return lateDeliveryTime;
	}
	public void setLateDeliveryTime(int lateDeliveryTime) {
		this.lateDeliveryTime = lateDeliveryTime;
	}
	public int getServiceDuration() {
		return serviceDuration;
	}
	public void setServiceDuration(int serviceDuration) {
		this.serviceDuration = serviceDuration;
	}
	public DeliveryRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DeliveryRequest(int point, int demand, int earlyDeliveryTime,
			int lateDeliveryTime, int serviceDuration) {
		super();
		this.point = point;
		this.demand = demand;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
		this.serviceDuration = serviceDuration;
	}
	
	
	
}
