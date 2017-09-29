package com.dailyopt.havestplanning.model;

public class RunParameters {
	private int timeLimit;
	private int nbSteps;
	private int deltaPlantDateLeft;
	private int deltaPlantDateRight;
	
	public RunParameters(int timeLimit, int nbSteps) {
		super();
		this.timeLimit = timeLimit;
		this.nbSteps = nbSteps;
	}

	public int getDeltaPlantDateLeft() {
		return deltaPlantDateLeft;
	}

	public void setDeltaPlantDateLeft(int deltaPlantDateLeft) {
		this.deltaPlantDateLeft = deltaPlantDateLeft;
	}

	public int getDeltaPlantDateRight() {
		return deltaPlantDateRight;
	}

	public void setDeltaPlantDateRight(int deltaPlantDateRight) {
		this.deltaPlantDateRight = deltaPlantDateRight;
	}

	public int getNbSteps() {
		return nbSteps;
	}

	public void setNbSteps(int nbSteps) {
		this.nbSteps = nbSteps;
	}

	public int getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	public RunParameters(int timeLimit) {
		super();
		this.timeLimit = timeLimit;
	}

	public RunParameters() {
		super();
		// TODO Auto-generated constructor stub
	}

	
}
