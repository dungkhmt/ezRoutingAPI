package com.kse.ezRoutingAPI.dichung.model;

public class SharedTaxiRequest {
	private String ticketCode;
	private String pickupAddress;
	private String earlyPickupDateTime;
	private String latePickupDateTime;
	private String deliveryAddress;
	private String lateDeliveryDateTime;
	private int numberPassengers;
	public SharedTaxiRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	public SharedTaxiRequest(String ticketCode, String pickupAddress,
			String earlyPickupDateTime, String latePickupDateTime,
			String deliveryAddress, String lateDeliveryDateTime,
			int numberPassengers) {
		super();
		this.ticketCode = ticketCode;
		this.pickupAddress = pickupAddress;
		this.earlyPickupDateTime = earlyPickupDateTime;
		this.latePickupDateTime = latePickupDateTime;
		this.deliveryAddress = deliveryAddress;
		this.lateDeliveryDateTime = lateDeliveryDateTime;
		this.numberPassengers = numberPassengers;
	}
	public String getTicketCode() {
		return ticketCode;
	}
	public void setTicketCode(String ticketCode) {
		this.ticketCode = ticketCode;
	}
	public String getPickupAddress() {
		return pickupAddress;
	}
	public void setPickupAddress(String pickupAddress) {
		this.pickupAddress = pickupAddress;
	}
	public String getEarlyPickupDateTime() {
		return earlyPickupDateTime;
	}
	public void setEarlyPickupDateTime(String earlyPickupDateTime) {
		this.earlyPickupDateTime = earlyPickupDateTime;
	}
	public String getLatePickupDateTime() {
		return latePickupDateTime;
	}
	public void setLatePickupDateTime(String latePickupDateTime) {
		this.latePickupDateTime = latePickupDateTime;
	}
	public String getDeliveryAddress() {
		return deliveryAddress;
	}
	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}
	public String getLateDeliveryDateTime() {
		return lateDeliveryDateTime;
	}
	public void setLateDeliveryDateTime(String lateDeliveryDateTime) {
		this.lateDeliveryDateTime = lateDeliveryDateTime;
	}
	public int getNumberPassengers() {
		return numberPassengers;
	}
	public void setNumberPassengers(int numberPassengers) {
		this.numberPassengers = numberPassengers;
	}
	
	
}
