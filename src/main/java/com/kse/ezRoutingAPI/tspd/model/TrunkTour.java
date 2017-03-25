package com.kse.ezRoutingAPI.tspd.model;

import java.util.ArrayList;

public class TrunkTour {
	private ArrayList<Point> trunk_tour;

	public ArrayList<Point> getTrunk_tour() {
		return trunk_tour;
	}

	public void setTrunk_tour(ArrayList<Point> trunk_tour) {
		this.trunk_tour = trunk_tour;
	}

	public TrunkTour(ArrayList<Point> trunk_tour) {
		super();
		this.trunk_tour = trunk_tour;
	}
	
	
}
