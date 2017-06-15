package com.dailyopt.havestplanning.model;

import java.util.Date;

public class Field {
	private String code;
	private String districtCode;
	private String ownerCode;
	private double area;
	
	//private Date date;// optimal havesting date
	private String date;
	
	private int quantity;
	private int deltaDays;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDistrictCode() {
		return districtCode;
	}
	public void setDistrictCode(String districtCode) {
		this.districtCode = districtCode;
	}
	public String getOwnerCode() {
		return ownerCode;
	}
	public void setOwnerCode(String ownerCode) {
		this.ownerCode = ownerCode;
	}
	public double getArea() {
		return area;
	}
	public void setArea(double area) {
		this.area = area;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public int getDeltaDays() {
		return deltaDays;
	}
	public void setDeltaDays(int deltaDays) {
		this.deltaDays = deltaDays;
	}
	public Field(String code, String districtCode, String ownerCode,
			double area, String date, int quantity, int deltaDays) {
		super();
		this.code = code;
		this.districtCode = districtCode;
		this.ownerCode = ownerCode;
		this.area = area;
		this.date = date;
		this.quantity = quantity;
		this.deltaDays = deltaDays;
	}
	public Field() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
