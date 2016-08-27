package com.kse.ezRoutingAPI.dichung.model;

public class SharedTaxiRouteElement {
	private String ticketCode;
	private String address;
	private String pickupDateTime;
	private String expectedPickupDateTime;
	private int travelTimeToDestination;
	private int maxTravelTimeToDestinationAllowed; 
	
	public SharedTaxiRouteElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	


	public SharedTaxiRouteElement(String ticketCode, String address,
			String pickupDateTime, String expectedPickupDateTime,
			int travelTimeToDestination, int maxTravelTimeToDestinationAllowed) {
		super();
		this.ticketCode = ticketCode;
		this.address = address;
		this.pickupDateTime = pickupDateTime;
		this.expectedPickupDateTime = expectedPickupDateTime;
		this.travelTimeToDestination = travelTimeToDestination;
		this.maxTravelTimeToDestinationAllowed = maxTravelTimeToDestinationAllowed;
	}




	public int getTravelTimeToDestination() {
		return travelTimeToDestination;
	}




	public void setTravelTimeToDestination(int travelTimeToDestination) {
		this.travelTimeToDestination = travelTimeToDestination;
	}




	public int getMaxTravelTimeToDestinationAllowed() {
		return maxTravelTimeToDestinationAllowed;
	}




	public void setMaxTravelTimeToDestinationAllowed(
			int maxTravelTimeToDestinationAllowed) {
		this.maxTravelTimeToDestinationAllowed = maxTravelTimeToDestinationAllowed;
	}




	public String getExpectedPickupDateTime() {
		return expectedPickupDateTime;
	}


	public void setExpectedPickupDateTime(String expectedPickupDateTime) {
		this.expectedPickupDateTime = expectedPickupDateTime;
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
	
}
