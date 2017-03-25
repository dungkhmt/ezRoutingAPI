package com.kse.ezRoutingAPI.tspd.model;

import java.util.ArrayList;

public class Tour {
	private TrunkTour TD;
	private ArrayList<DroneDelivery> DD;
	public TrunkTour getTD() {
		return TD;
	}
	public void setTD(TrunkTour tD) {
		TD = tD;
	}
	public ArrayList<DroneDelivery> getDD() {
		return DD;
	}
	public void setDD(ArrayList<DroneDelivery> dD) {
		DD = dD;
	}
	public Tour(TrunkTour tD, ArrayList<DroneDelivery> dD) {
		super();
		TD = tD;
		DD = dD;
	}
	
	
}
