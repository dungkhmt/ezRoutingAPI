package com.kse.ezRoutingAPI.dichung.model;

public class SharedTaxiRouteElement {
	private String ticketCode;
	private String address;
	private String deliveryAddress;
	private String latlng;
	private String pickupDateTime;
	private String expectedPickupDateTime;
	private String travelTimeToDestination;
	private String travelTimeToNext;
	private String distanceToNext;
	private String maxTravelTimeToDestinationAllowed; 
	private String type = "-";// PICKUP / DELIVERY
	
	public SharedTaxiRouteElement() {
		super();
		// TODO Auto-generated constructor stub
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public SharedTaxiRouteElement(String ticketCode, String address,
			String deliveryAddress,
			String latlng,
			String pickupDateTime, String expectedPickupDateTime,
			String travelTimeToDestination, String travelTimeToNext,
			String distanceToNext, String maxTravelTimeToDestinationAllowed) {
		super();
		this.ticketCode = ticketCode;
		this.address = address;
		this.deliveryAddress = deliveryAddress;
		this.latlng = latlng;
		this.pickupDateTime = pickupDateTime;
		this.expectedPickupDateTime = expectedPickupDateTime;
		this.travelTimeToDestination = travelTimeToDestination;
		this.travelTimeToNext = travelTimeToNext;
		this.distanceToNext = distanceToNext;
		this.maxTravelTimeToDestinationAllowed = maxTravelTimeToDestinationAllowed;
	}


	
	public String getDeliveryAddress() {
		return deliveryAddress;
	}


	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}


	public String getLatlng() {
		return latlng;
	}


	public void setLatlng(String latlng) {
		this.latlng = latlng;
	}


	public String getDistanceToNext() {
		return distanceToNext;
	}


	public void setDistanceToNext(String distanceToNext) {
		this.distanceToNext = distanceToNext;
	}


	public String getTicketCode() {
		return ticketCode;
	}

	public void setTicketCode(String ticketCode) {
		this.ticketCode = ticketCode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPickupDateTime() {
		return pickupDateTime;
	}

	public void setPickupDateTime(String pickupDateTime) {
		this.pickupDateTime = pickupDateTime;
	}

	public String getExpectedPickupDateTime() {
		return expectedPickupDateTime;
	}

	public void setExpectedPickupDateTime(String expectedPickupDateTime) {
		this.expectedPickupDateTime = expectedPickupDateTime;
	}

	public String getTravelTimeToDestination() {
		return travelTimeToDestination;
	}

	public void setTravelTimeToDestination(String travelTimeToDestination) {
		this.travelTimeToDestination = travelTimeToDestination;
	}

	public String getTravelTimeToNext() {
		return travelTimeToNext;
	}

	public void setTravelTimeToNext(String travelTimeToNext) {
		this.travelTimeToNext = travelTimeToNext;
	}

	public String getMaxTravelTimeToDestinationAllowed() {
		return maxTravelTimeToDestinationAllowed;
	}

	public void setMaxTravelTimeToDestinationAllowed(
			String maxTravelTimeToDestinationAllowed) {
		this.maxTravelTimeToDestinationAllowed = maxTravelTimeToDestinationAllowed;
	}
	
	


	
}
