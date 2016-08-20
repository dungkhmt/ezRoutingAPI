package com.kse.crossDockingAPI.model;

public class CrossDockingSystemSolution {
	private int constraintViolations;
	private int objective;
	private int[] inVehicleAssignments;
	private int[] outVehicleAssignments;
	public int getConstraintViolations() {
		return constraintViolations;
	}
	public void setConstraintViolations(int constraintViolations) {
		this.constraintViolations = constraintViolations;
	}
	public int getObjective() {
		return objective;
	}
	public void setObjective(int objective) {
		this.objective = objective;
	}
	public int[] getInVehicleAssignments() {
		return inVehicleAssignments;
	}
	public void setInVehicleAssignments(int[] inVehicleAssignments) {
		this.inVehicleAssignments = inVehicleAssignments;
	}
	public int[] getOutVehicleAssignments() {
		return outVehicleAssignments;
	}
	public void setOutVehicleAssignments(int[] outVehicleAssignments) {
		this.outVehicleAssignments = outVehicleAssignments;
	}
	public CrossDockingSystemSolution(int constraintViolations, int objective,
			int[] inVehicleAssignments, int[] outVehicleAssignments) {
		super();
		this.constraintViolations = constraintViolations;
		this.objective = objective;
		this.inVehicleAssignments = inVehicleAssignments;
		this.outVehicleAssignments = outVehicleAssignments;
	}
	
	
	
	
}
