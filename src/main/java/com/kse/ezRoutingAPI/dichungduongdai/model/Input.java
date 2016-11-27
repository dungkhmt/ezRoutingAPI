package com.kse.ezRoutingAPI.dichungduongdai.model;

public class Input {
	private Request[] requests;
	private int maxWaitTimePickup;// max time duration allowed from the first pickup and the last pickup of the sharing trip
	private int maxWaitTimeDelivery;// max time duration allowed from the first delivery and the last delivery of the sharing trip
	private int deltaDepartTime;// delta of the departTime of each request , e.g. +-30 minutes
	
	
}
