package com.kse.ezRoutingAPI.model;

public class PickupDeliveryInput {
	private int[] startPoints;
	private int[] endPoints;
	private int[] startTimePoint;
	public int[] getStartTimePoint() {
		return startTimePoint;
	}

	public void setStartTimePoint(int[] startTimePoint) {
		this.startTimePoint = startTimePoint;
	}

	public int[] getCapacity() {
		return capacity;
	}

	public void setCapacity(int[] capacity) {
		this.capacity = capacity;
	}

	public int[] getEarlyPickup() {
		return earlyPickup;
	}

	public void setEarlyPickup(int[] earlyPickup) {
		this.earlyPickup = earlyPickup;
	}

	public int[] getLatePickup() {
		return latePickup;
	}

	public void setLatePickup(int[] latePickup) {
		this.latePickup = latePickup;
	}

	public int[] getEarlyDelivery() {
		return earlyDelivery;
	}

	public void setEarlyDelivery(int[] earlyDelivery) {
		this.earlyDelivery = earlyDelivery;
	}

	public int[] getLateDelivery() {
		return lateDelivery;
	}

	public void setLateDelivery(int[] lateDelivery) {
		this.lateDelivery = lateDelivery;
	}

	public int[] getDemand() {
		return demand;
	}

	public void setDemand(int[] demand) {
		this.demand = demand;
	}

	public int[] getServiceDuration() {
		return serviceDuration;
	}

	public void setServiceDuration(int[] serviceDuration) {
		this.serviceDuration = serviceDuration;
	}

	public double[] getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(double[] maxDistance) {
		this.maxDistance = maxDistance;
	}

	public DistanceElement[] getTravelTimes() {
		return travelTimes;
	}

	public void setTravelTimes(DistanceElement[] travelTimes) {
		this.travelTimes = travelTimes;
	}

	private int[] capacity;// e.g., number of places for passengers on a taxi (4, 6)
	
	private int[] pickupPoints;
	private int[] deliveryPoints;
	private int[] earlyPickup;
	private int[] latePickup;
	private int[] earlyDelivery;
	private int[] lateDelivery;
	private int[] demand;// e.g., number of people of the request
	private int[] serviceDuration;//
	private double[] maxDistance;
	
	private DistanceElement[] distances;
	private DistanceElement[] travelTimes;
	
	private int timeLimit;
	
	public PickupDeliveryInput(int[] startPoints, int[] endPoints,
			int[] pickupPoints, int[] deliveryPoints,
			DistanceElement[] distances, int timeLimit) {
		super();
		this.startPoints = startPoints;
		this.endPoints = endPoints;
		this.pickupPoints = pickupPoints;
		this.deliveryPoints = deliveryPoints;
		this.distances = distances;
		this.timeLimit = timeLimit;
	}

	public int getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	public PickupDeliveryInput() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int[] getStartPoints() {
		return startPoints;
	}

	public void setStartPoints(int[] startPoints) {
		this.startPoints = startPoints;
	}

	public int[] getEndPoints() {
		return endPoints;
	}

	public void setEndPoints(int[] endPoints) {
		this.endPoints = endPoints;
	}

	public int[] getPickupPoints() {
		return pickupPoints;
	}

	public void setPickupPoints(int[] pickupPoints) {
		this.pickupPoints = pickupPoints;
	}

	public int[] getDeliveryPoints() {
		return deliveryPoints;
	}

	public void setDeliveryPoints(int[] deliveryPoints) {
		this.deliveryPoints = deliveryPoints;
	}

	public DistanceElement[] getDistances() {
		return distances;
	}

	public void setDistances(DistanceElement[] distances) {
		this.distances = distances;
	}
}
