package com.kse.ezRoutingAPI.dichung.model;

public class SharedTaxiSolution {
	private int nb2Sharings;
	private int nb3Sharings;
	private int nbRequests;
	
	private SharedTaxiRoute[] routes;

	public SharedTaxiSolution() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SharedTaxiSolution(int nb2Sharings, int nb3Sharings, int nbRequests, SharedTaxiRoute[] routes) {
		super();
		this.nb2Sharings = nb2Sharings;
		this.nb3Sharings = nb3Sharings;
		this.nbRequests = nbRequests;
		this.routes = routes;
	}
	
	public int getNb2Sharings() {
		return nb2Sharings;
	}

	public void setNb2Sharings(int nb2Sharings) {
		this.nb2Sharings = nb2Sharings;
	}

	public int getNb3Sharings() {
		return nb3Sharings;
	}

	public void setNb3Sharings(int nb3Sharings) {
		this.nb3Sharings = nb3Sharings;
	}

	public int getNbRequests() {
		return nbRequests;
	}

	public void setNbRequests(int nbRequests) {
		this.nbRequests = nbRequests;
	}

	public SharedTaxiRoute[] getRoutes() {
		return routes;
	}

	public void setRoutes(SharedTaxiRoute[] routes) {
		this.routes = routes;
	}
	
}
