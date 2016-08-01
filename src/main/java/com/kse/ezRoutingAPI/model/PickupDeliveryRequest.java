package com.kse.ezRoutingAPI.model;

public class PickupDeliveryRequest {
	private int pickupPoint;
	private int deliveryPoint;
	
	private int demand;
	
	private int earlyPickupTime;
	private int latePickupTime;
	private int pickupDuration;
	
	private int earlyDeliveryTime;
	private int lateDeliveryTime;
	private int deliveryDuration;
	
	private double maxDistance;

	public int getPickupPoint() {
		return pickupPoint;
	}

	public void setPickupPoint(int pickupPoint) {
		this.pickupPoint = pickupPoint;
	}

	public int getDeliveryPoint() {
		return deliveryPoint;
	}

	public void setDeliveryPoint(int deliveryPoint) {
		this.deliveryPoint = deliveryPoint;
	}

	public int getDemand() {
		return demand;
	}

	public void setDemand(int demand) {
		this.demand = demand;
	}

	public int getEarlyPickupTime() {
		return earlyPickupTime;
	}

	public void setEarlyPickupTime(int earlyPickupTime) {
		this.earlyPickupTime = earlyPickupTime;
	}

	public int getLatePickupTime() {
		return latePickupTime;
	}

	public void setLatePickupTime(int latePickupTime) {
		this.latePickupTime = latePickupTime;
	}

	public int getPickupDuration() {
		return pickupDuration;
	}

	public void setPickupDuration(int pickupDuration) {
		this.pickupDuration = pickupDuration;
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

	public int getDeliveryDuration() {
		return deliveryDuration;
	}

	public void setDeliveryDuration(int deliveryDuration) {
		this.deliveryDuration = deliveryDuration;
	}

	public double getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(double maxDistance) {
		this.maxDistance = maxDistance;
	}

	public PickupDeliveryRequest(int pickupPoint, int deliveryPoint,
			int demand, int earlyPickupTime, int latePickupTime,
			int pickupDuration, int earlyDeliveryTime, int lateDeliveryTime,
			int deliveryDuration, double maxDistance) {
		super();
		this.pickupPoint = pickupPoint;
		this.deliveryPoint = deliveryPoint;
		this.demand = demand;
		this.earlyPickupTime = earlyPickupTime;
		this.latePickupTime = latePickupTime;
		this.pickupDuration = pickupDuration;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
		this.deliveryDuration = deliveryDuration;
		this.maxDistance = maxDistance;
	}

	public PickupDeliveryRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
