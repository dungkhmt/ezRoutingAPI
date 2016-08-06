package com.kse.ezRoutingAPI.model;

public class SolutionRoutes {
	private int constraintViolations;
	private double objective;
	private Route[] routes;
	public int getConstraintViolations() {
		return constraintViolations;
	}
	public void setConstraintViolations(int constraintViolations) {
		this.constraintViolations = constraintViolations;
	}
	public double getObjective() {
		return objective;
	}
	public void setObjective(double objective) {
		this.objective = objective;
	}
	public Route[] getRoutes() {
		return routes;
	}
	public void setRoutes(Route[] routes) {
		this.routes = routes;
	}
	public SolutionRoutes() {
		super();
		// TODO Auto-generated constructor stub
	}
	public SolutionRoutes(int constraintViolations, double objective,
			Route[] routes) {
		super();
		this.constraintViolations = constraintViolations;
		this.objective = objective;
		this.routes = routes;
	}
	
	
	
}
