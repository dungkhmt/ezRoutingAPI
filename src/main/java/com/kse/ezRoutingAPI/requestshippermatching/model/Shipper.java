package com.kse.ezRoutingAPI.requestshippermatching.model;

public class Shipper {
	private String code;
	private String location;// latlng
	private int capacity;
	
	public Shipper(String code, String location, int capacity) {
		super();
		this.code = code;
		this.location = location;
		this.capacity = capacity;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Shipper() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		return "Shipper [code=" + code + ", location=" + location
				+ ", capacity=" + capacity + "]";
	}
	
	
}
