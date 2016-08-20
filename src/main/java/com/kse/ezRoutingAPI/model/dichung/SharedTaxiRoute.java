package com.kse.ezRoutingAPI.model.dichung;

public class SharedTaxiRoute {
	private String[] ticketCodes;

	public SharedTaxiRoute() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SharedTaxiRoute(String[] ticketCodes) {
		super();
		this.ticketCodes = ticketCodes;
	}

	public String[] getTicketCodes() {
		return ticketCodes;
	}

	public void setTicketCodes(String[] ticketCodes) {
		this.ticketCodes = ticketCodes;
	}
	
}
