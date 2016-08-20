package com.kse.crossDockingAPI.model;

public class CrossDockingSystemInput {
	private int inDoorNum;
	private int outDoorNum;
	private int inVehicleNum;
	private int outVehicleNum;
	
	private CostElement[] costs;
	private TripElement[] trips;
	
	public CrossDockingSystemInput() {
		super();
		// TODO Auto-generated constructor stub
	}

	private int[] inDoorCapacities;
	private int[] outDoorCapacities;
	private int[] inVehicleDemands;
	private int[] outVehicleDemands;
	
	public CrossDockingSystemInput(int inDoorNum, int outDoorNum,
			int inVehicleNum, int outVehicleNum, CostElement[] costs,
			TripElement[] trips, int[] inDoorCapacities,
			int[] outDoorCapacities, int[] inVehicleDemands,
			int[] outVehicleDemands) {
		super();
		this.inDoorNum = inDoorNum;
		this.outDoorNum = outDoorNum;
		this.inVehicleNum = inVehicleNum;
		this.outVehicleNum = outVehicleNum;
		this.costs = costs;
		this.trips = trips;
		this.inDoorCapacities = inDoorCapacities;
		this.outDoorCapacities = outDoorCapacities;
		this.inVehicleDemands = inVehicleDemands;
		this.outVehicleDemands = outVehicleDemands;
	}

	public int getInDoorNum() {
		return inDoorNum;
	}

	public void setInDoorNum(int inDoorNum) {
		this.inDoorNum = inDoorNum;
	}

	public int getOutDoorNum() {
		return outDoorNum;
	}

	public void setOutDoorNum(int outDoorNum) {
		this.outDoorNum = outDoorNum;
	}

	public int getInVehicleNum() {
		return inVehicleNum;
	}

	public void setInVehicleNum(int inVehicleNum) {
		this.inVehicleNum = inVehicleNum;
	}

	public int getOutVehicleNum() {
		return outVehicleNum;
	}

	public void setOutVehicleNum(int outVehicleNum) {
		this.outVehicleNum = outVehicleNum;
	}

	public CostElement[] getCosts() {
		return costs;
	}

	public void setCosts(CostElement[] costs) {
		this.costs = costs;
	}

	public TripElement[] getTrips() {
		return trips;
	}

	public void setTrips(TripElement[] trips) {
		this.trips = trips;
	}

	public int[] getInDoorCapacities() {
		return inDoorCapacities;
	}

	public void setInDoorCapacities(int[] inDoorCapacities) {
		this.inDoorCapacities = inDoorCapacities;
	}

	public int[] getOutDoorCapacities() {
		return outDoorCapacities;
	}

	public void setOutDoorCapacities(int[] outDoorCapacities) {
		this.outDoorCapacities = outDoorCapacities;
	}

	public int[] getInVehicleDemands() {
		return inVehicleDemands;
	}

	public void setInVehicleDemands(int[] inVehicleDemands) {
		this.inVehicleDemands = inVehicleDemands;
	}

	public int[] getOutVehicleDemands() {
		return outVehicleDemands;
	}

	public void setOutVehicleDemands(int[] outVehicleDemands) {
		this.outVehicleDemands = outVehicleDemands;
	}
	
	
}
