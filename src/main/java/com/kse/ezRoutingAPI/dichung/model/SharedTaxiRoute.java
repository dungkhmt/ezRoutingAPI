package com.kse.ezRoutingAPI.dichung.model;

public class SharedTaxiRoute {
	private SharedTaxiRouteElement[] ticketCodes;
	private String taxiType;
	private int nbPeople;
	private String arrTimeDestination;
	
	public SharedTaxiRoute() {
		super();
		// TODO Auto-generated constructor stub
	}

	

	public SharedTaxiRoute(SharedTaxiRouteElement[] ticketCodes, String taxiType, int nbPeople,
			String arrTimeDestination) {
		super();
		this.ticketCodes = ticketCodes;
		this.taxiType = taxiType;
		this.nbPeople = nbPeople;
		this.arrTimeDestination = arrTimeDestination;
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



	public String getArrTimeDestination() {
		return arrTimeDestination;
	}



	public void setArrTimeDestination(String arrTimeDestination) {
		this.arrTimeDestination = arrTimeDestination;
	}



	public SharedTaxiRouteElement[] getTicketCodes() {
		return ticketCodes;
	}

	public void setTicketCodes(SharedTaxiRouteElement[] ticketCodes) {
		this.ticketCodes = ticketCodes;
	}

	
}
