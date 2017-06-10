package com.kse.ezRoutingAPI.tspd.service;

import java.lang.reflect.Array;
import java.util.ArrayList;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;

public class DronesNeighborHood {
	double savings;
	ArrayList<DroneDelivery> lde;
	int truckPointIndex;
	int ik;
	
	public DronesNeighborHood(int ik,int truckPointIndex ) {
		super();
		this.savings = 0;
		this.lde = new ArrayList<DroneDelivery>();
		this.ik=ik;
		this.truckPointIndex=truckPointIndex;
	}
	public DroneDelivery getDroneDeliveryLast(){
		return lde.get(lde.size()-1);
	}
	public void setDroneLast(DroneDelivery de){
		lde.set(lde.size()-1, de);
	}
	public void addADroneDelivery(DroneDelivery de){
		lde.add(de);
	}
	public void removeLastDroneDelivery(){
		lde.remove(lde.size()-1);
	}
	public double getSavings() {
		return savings;
	}
	public void setSavings(double savings) {
		this.savings = savings;
	}
	public ArrayList<DroneDelivery> getLde() {
		return lde;
	}
	public void setLde(ArrayList<DroneDelivery> lde) {
		this.lde = lde;
	}
	public int getTruckPointIndex() {
		return truckPointIndex;
	}
	public void setTruckPointIndex(int truckPointIndex) {
		this.truckPointIndex = truckPointIndex;
	}
	public int getIk() {
		return ik;
	}
	public void setIk(int ik) {
		this.ik = ik;
	}
	@Override
	public String toString() {
		return "DronesNeighborHood [savings=" + savings + ", lde=" + lde
				+ ", truckPointIndex=" + truckPointIndex + ", ik=" + ik + "]";
	}
	
	
}
