package com.kse.ezRoutingAPI.routeshipper.model;

public class ShipRequest {
	private String code;
	private String deliveryAddress;
	private String deliveryLocation;// lat,lng, e.g, "57.331474, -111.804997"
	private String deliveryDateTime; // "2017-02-23 10:30:00"
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDeliveryAddress() {
		return deliveryAddress;
	}
	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}
	public String getDeliveryLocation() {
		return deliveryLocation;
	}
	public void setDeliveryLocation(String deliveryLocation) {
		this.deliveryLocation = deliveryLocation;
	}
	public String getDeliveryDateTime() {
		return deliveryDateTime;
	}
	public void setDeliveryDateTime(String deliveryDateTime) {
		this.deliveryDateTime = deliveryDateTime;
	}
	public ShipRequest(String code, String deliveryAddress,
			String deliveryLocation, String deliveryDateTime) {
		super();
		this.code = code;
		this.deliveryAddress = deliveryAddress;
		this.deliveryLocation = deliveryLocation;
		this.deliveryDateTime = deliveryDateTime;
	}
	public ShipRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
