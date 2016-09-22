package com.kse.ezRoutingAPI.pickupdeliverycontainers.model;

public class PickupDeliveryRouteElement {
	private String requestCode;
	private String arrivalDateTime;
	private String action;// PICKUP or DELIVERY
	private int quantity;
	public PickupDeliveryRouteElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PickupDeliveryRouteElement(String requestCode,
			String arrivalDateTime, String action, int quantity) {
		super();
		this.requestCode = requestCode;
		this.arrivalDateTime = arrivalDateTime;
		this.action = action;
		this.quantity = quantity;
	}
	
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public String getRequestCode() {
		return requestCode;
	}
	public void setRequestCode(String requestCode) {
		this.requestCode = requestCode;
	}
	public String getArrivalDateTime() {
		return arrivalDateTime;
	}
	public void setArrivalDateTime(String arrivalDateTime) {
		this.arrivalDateTime = arrivalDateTime;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	
}
