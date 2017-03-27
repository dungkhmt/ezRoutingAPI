package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.NeighborHood;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;

public class TSPD_LS {
	int customers;
	Tour tour;
	TSPD tspd;
	TSP tsp;
	double maxSavings=0;
	
	public TSPD_LS(TSPD tspd) {
		super();
		this.tspd = tspd;
	}
	public void init(){
		
		tsp=new TSP(tspd.getStartPoint(), tspd.getClientPoints(), tspd.getEndPoint());
		TruckTour truckTour=new TruckTour(tsp.randomGenerator());
		ArrayList<DroneDelivery> droneTours=new ArrayList<DroneDelivery>();
		tour= new Tour(truckTour, droneTours);
	}
	public Tour solve(){
		init();
		TruckTour truckTour=tour.getTD();
		ArrayList<Point> customerPoints=tspd.getClientPoints();
		
		while (true){
			ArrayList<Point> truckTourList=truckTour.getTruck_tour();
			for(int i=0;i<customerPoints.size();i++){
				NeighborHood ne=null;
				int itInTruck=truckTourList.indexOf(customerPoints.get(i));
				double savings=tspd.d_truck(truckTourList.get(itInTruck-1) , truckTourList.get(itInTruck+1));
				for(int j=0;j<truckTourList.size()-1;j++)
					for(int k=j+1;k<truckTourList.size();k++){
						Point droneNode=tspd.drone(truckTourList.get(j), truckTourList.get(k),tour);
						if(droneNode!=null){
							ne=relocateAsTruck(customerPoints.get(i), j, k, savings);
						} else{
							
						}
					}
				
			}
		}
		return null;
	}
	public NeighborHood relocateAsDrone(Point j,int subPointStart,int subPointEnd,double savings){
		ArrayList<Point> truckPoint=tour.getTD().getTruck_tour();
		NeighborHood ne;
		
		for(int i=subPointStart;i<subPointEnd-1;i++)
			for(int k=i+1;k<subPointEnd;k++){
				int vt=truckPoint.indexOf(j);
				truckPoint.remove(vt);
				
				TruckTour t= new TruckTour(truckPoint);
				tour.setTD(t);
				if(tspd.checkConstraint(tour)){
					DroneDelivery de= new DroneDelivery(truckPoint.get(i), j, truckPoint.get(k));
					
					double delta=tspd.d_drone(truckPoint.get(i), j)+tspd.d_drone(j,truckPoint.get(k));
				}
			}
		return null;
	}
	public NeighborHood relocateAsTruck(Point j,int subPointStart,int subPointEnd,double savings){
		ArrayList<Point> truckPoint=tour.getTD().getTruck_tour();
		NeighborHood ne=null;
		for(int i=subPointStart;i<subPointEnd-1;i++){
			int vt=truckPoint.indexOf(j);
			truckPoint.remove(vt);
			
			TruckTour t= new TruckTour(truckPoint);
			tour.setTD(t);
			if(tspd.checkConstraint(tour)){
				truckPoint.add(i, j);
				t= new TruckTour(truckPoint);
				tour.setTD(t);
				double delta=tspd.d_truck(truckPoint.get(i), j)+tspd.d_truck(j,truckPoint.get(i+1))-tspd.d_truck(truckPoint.get(i),truckPoint.get(i+1));
				if(delta<savings)
					if(tspd.checkConstraint(tour)){
						ne= new NeighborHood(false, savings-delta, truckPoint.get(i), j, truckPoint.get(i+2));//
					}

				truckPoint.remove(i);
			}
			
			truckPoint.add(vt, j);
			t= new TruckTour(truckPoint);
			tour.setTD(t);
		}
		
		return ne;
	}
}
