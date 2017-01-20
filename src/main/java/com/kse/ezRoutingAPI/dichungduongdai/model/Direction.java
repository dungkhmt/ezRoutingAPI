package com.kse.ezRoutingAPI.dichungduongdai.model;

public class Direction {
	private StepDirection[] stepDirections;

	public StepDirection[] getStepDirections() {
		return stepDirections;
	}

	public void setStepDirections(StepDirection[] stepDirections) {
		this.stepDirections = stepDirections;
	}

	public Direction(StepDirection[] stepDirections) {
		super();
		this.stepDirections = stepDirections;
	}

	public Direction() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
