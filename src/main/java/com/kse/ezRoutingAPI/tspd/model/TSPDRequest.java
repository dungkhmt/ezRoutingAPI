package com.kse.ezRoutingAPI.tspd.model;

import java.util.ArrayList;
import java.util.Arrays;

public class TSPDRequest {
	Point[] listPoints;

	public Point[] getListPoints() {
		return listPoints;
	}

	public void setListPoints(Point[] listPoints) {
		this.listPoints = listPoints;
	}

	public TSPDRequest(Point[] listPoints) {
		super();
		this.listPoints = listPoints;
	}

	public TSPDRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "TSPDRequest [listPoints=" + Arrays.toString(listPoints) + "]";
	}
	
	
}
