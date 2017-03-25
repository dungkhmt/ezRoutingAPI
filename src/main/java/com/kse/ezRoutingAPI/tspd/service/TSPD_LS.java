package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;

import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;

public class TSPD_LS {
	int customers;
	TruckTour truckTour;
	TSPD tspd;
	double maxSavings=0;
	
	public void init(){
		tspd= new TSPD();
		
	}
	public Tour solve(){
		init();
		ArrayList<Point> customerPoints=tspd.getClientPoints();
		while (true){
			for(int i=0;i<customerPoints.size();i++){
				double savings=
			}
		}
		return null;
	}
}
