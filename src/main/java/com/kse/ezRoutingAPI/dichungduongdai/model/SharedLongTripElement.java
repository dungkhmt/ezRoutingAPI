package com.kse.ezRoutingAPI.dichungduongdai.model;

public class SharedLongTripElement {
	public static final String PICKUP = "PICKUP";
	public static final String DELIVERY = "DELIVERY";
	
	private String ticketCode;
	private String departTime;
	private String pickupAddress;
	private String pickupPosition;// lat-lng
	private String deliveryAddress;
	private String deliveryPosition;// lat-lng
	private String type;// PICKUP / DELIVERY
	
	public SharedLongTripElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public SharedLongTripElement(String ticketCode, String departTime,
			String pickupAddress, String pickupPosition,
			String deliveryAddress, String deliveryPosition, String type) {
		super();
		this.ticketCode = ticketCode;
		this.departTime = departTime;
		this.pickupAddress = pickupAddress;
		this.pickupPosition = pickupPosition;
		this.deliveryAddress = deliveryAddress;
		this.deliveryPosition = deliveryPosition;
		this.type = type;
	}

	
	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getTicketCode() {
		return ticketCode;
	}
	public void setTicketCode(String ticketCode) {
		this.ticketCode = ticketCode;
	}
	public String getDepartTime() {
		return departTime;
	}
	public void setDepartTime(String departTime) {
		this.departTime = departTime;
	}
	public String getPickupAddress() {
		return pickupAddress;
	}
	public void setPickupAddress(String pickupAddress) {
		this.pickupAddress = pickupAddress;
	}
	public String getPickupPosition() {
		return pickupPosition;
	}
	public void setPickupPosition(String pickupPosition) {
		this.pickupPosition = pickupPosition;
	}
	public String getDeliveryAddress() {
		return deliveryAddress;
	}
	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}
	public String getDeliveryPosition() {
		return deliveryPosition;
	}
	public void setDeliveryPosition(String deliveryPosition) {
		this.deliveryPosition = deliveryPosition;
	}
	
}
