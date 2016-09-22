package com.kse.utils.traveltimeestimator;

import java.util.ArrayList;

import com.kse.utils.DateTimeUtils;

public class HanoiTravelTimeEstimator {
	public ArrayList<TravelTimeElement> stdTravelTimes;
	public ArrayList<TravelTimeElement> highTrafficTravelTimes;
	
	public int estimateTravelTime(String timePoint, double distance){
		// distance is in meters
		// return travel time in seconds
		if(DateTimeUtils.isHighTraffic(timePoint)){
			if(distance < 2000) return 600;
			else return (int)(distance/3);// speed is 3m/s about 10km/h
		}else{
			if(distance < 2000) return 300;
			else return (int)(distance/12);// speed is 
		}
	}
}
