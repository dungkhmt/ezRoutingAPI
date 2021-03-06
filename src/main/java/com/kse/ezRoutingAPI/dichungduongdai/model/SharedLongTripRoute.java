package com.kse.ezRoutingAPI.dichungduongdai.model;

public class SharedLongTripRoute {
	private SharedLongTripElement[] routeElements;
	private String taxiType;
	private int nbPeople;
	private int nbRequests;
	
	
	
	public SharedLongTripRoute() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SharedLongTripRoute(SharedLongTripElement[] routeElements,
			String taxiType, int nbPeople, int nbRequests) {
		super();
		this.routeElements = routeElements;
		this.taxiType = taxiType;
		this.nbPeople = nbPeople;
		this.nbRequests = nbRequests;
	}
	
	public void print(){
		System.out.print("Shared Route of " + nbRequests + " requests up to " + nbPeople + " people: ");
		
		for(SharedLongTripElement slte : routeElements){
			System.out.print(slte.getTicketCode() + "(" + slte.getDepartTime() + ")" + "->");
		}
	
		System.out.println();
		
		
	}

	public SharedLongTripElement[] getRouteElements() {
		return routeElements;
	}



	public void setRouteElements(SharedLongTripElement[] routeElements) {
		this.routeElements = routeElements;
	}



	public String getTaxiType() {
		return taxiType;
	}



	public void setTaxiType(String taxiType) {
		this.taxiType = taxiType;
	}



	public int getNbPeople() {
		return nbPeople;
	}



	public void setNbPeople(int nbPeople) {
		this.nbPeople = nbPeople;
	}


	public int getNbRequests() {
		return nbRequests;
	}


	public void setNbRequests(int nbRequests) {
		this.nbRequests = nbRequests;
	}
	
	
}
