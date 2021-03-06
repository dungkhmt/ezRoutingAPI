package com.kse.ezRoutingAPI.dichungduongdai.model;

public class SharedLongTripSolution {
	private int nbRequests;
	private SharedLongTripRoute[] routes;
	
	
	public SharedLongTripSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	public SharedLongTripSolution(int nbRequests, SharedLongTripRoute[] routes) {
		super();
		this.nbRequests = nbRequests;
		this.routes = routes;
	}
	
	public int getNbRequests() {
		return nbRequests;
	}
	
	public void setNbRequests(int nbRequests) {
		this.nbRequests = nbRequests;
	}
	
	public SharedLongTripRoute[] getRoutes() {
		return routes;
	}
	
	public void setRoutes(SharedLongTripRoute[] routes) {
		this.routes = routes;
	}
	
	public void print(){
		System.out.println("Number of requests: " + nbRequests);
		for(SharedLongTripRoute str : routes){
			str.print();
		}
	}
	
}
