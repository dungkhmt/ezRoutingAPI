package com.dailyopt.havestplanning.solver.multistepsplitfield;

import java.util.ArrayList;
public class AggregateDatesMove {
	private ArrayList<Integer> dates;// list of dates to be aggregated to the consider date
	private int delta;// variation of violations
	
	public AggregateDatesMove(ArrayList<Integer> dates, int delta){
		this.dates = dates;
		this.delta = delta;
	}

	public ArrayList<Integer> getDates() {
		return dates;
	}

	public void setDates(ArrayList<Integer> dates) {
		this.dates = dates;
	}

	public int getDelta() {
		return delta;
	}

	public void setDelta(int delta) {
		this.delta = delta;
	}
	
}
