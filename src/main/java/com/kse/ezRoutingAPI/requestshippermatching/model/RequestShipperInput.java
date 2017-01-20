package com.kse.ezRoutingAPI.requestshippermatching.model;

public class RequestShipperInput {
	private ShipRequest[] requests;
	private Shipper[] shippers;
	public ShipRequest[] getRequests() {
		return requests;
	}
	public void setRequests(ShipRequest[] requests) {
		this.requests = requests;
	}
	public Shipper[] getShippers() {
		return shippers;
	}
	public void setShippers(Shipper[] shippers) {
		this.shippers = shippers;
	}
	public RequestShipperInput(ShipRequest[] requests, Shipper[] shippers) {
		super();
		this.requests = requests;
		this.shippers = shippers;
	}
	public RequestShipperInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
