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
		System.out.println(name()+tour);
		ArrayList<Point> customerPoints=tspd.getClientPoints();
		System.out.println(name()+"cusPoint "+customerPoints);
		boolean d[]= new boolean[customerPoints.size()];
		maxSavings=0;
		for(int i=0;i<customerPoints.size();i++) d[i]=true;
		while (true){
			ArrayList<Point> truckTourList=tour.getTD().getTruck_tour();
			NeighborHood ne=null;
			for(int i=0;i<customerPoints.size();i++)
				if(d[i]){
				int itInTruck=truckTourList.indexOf(customerPoints.get(i));
				double savings=tspd.cost(truckTourList.get(itInTruck-1),truckTourList.get(itInTruck))+tspd.cost(truckTourList.get(itInTruck),truckTourList.get(itInTruck+1))-tspd.cost(truckTourList.get(itInTruck-1) , truckTourList.get(itInTruck+1));
				
				int vt=truckTourList.indexOf(customerPoints.get(i));
				truckTourList.remove(vt);
				
				TruckTour t= new TruckTour(truckTourList);
				tour.setTD(t);
				//System.out.println(name()+"cus "+i+" savings is "+savings);
				for(int j=0;j<truckTourList.size()-1;j++)
					for(int k=j+1;k<truckTourList.size();k++){
						Point droneNode=tspd.drone(truckTourList.get(j), truckTourList.get(k),tour);
						//System.out.println(name()+"drone "+droneNode);
						if(droneNode!=null){
							NeighborHood nee=relocateAsTruck(customerPoints.get(i), j, k, savings);
							if(nee!=null) 
								ne=nee;
						} else{
							NeighborHood nee=relocateAsDrone(customerPoints.get(i), j, k, savings);
							if(nee!=null) 
								ne=nee;
						}
					}
				truckTourList.add(vt, customerPoints.get(i));
				t= new TruckTour(truckTourList);
				tour.setTD(t);
			}
			System.out.println(name()+maxSavings);
			if(maxSavings>0){
			if(ne.isDroneNode()){
				
				int vt=truckTourList.indexOf(ne.getNj());
				truckTourList.remove(vt);
				TruckTour t= new TruckTour(truckTourList);
				tour.setTD(t);
				DroneDelivery de= new DroneDelivery(ne.getNi(),ne.getNj(),ne.getNk());
				ArrayList<DroneDelivery> lde= tour.getDD();
				lde.add(de);
				tour.setDD(lde);
				if(!ne.getNi().equals(tspd.getStartPoint()))
				d[customerPoints.indexOf(ne.getNi())]=false;
				d[customerPoints.indexOf(ne.getNj())]=false;
				if(!ne.getNk().equals(tspd.getEndPoint()))
				d[customerPoints.indexOf(ne.getNk())]=false;
			} else {
				ArrayList<Point> truckPoint=tour.getTD().getTruck_tour();
				int vt=truckTourList.indexOf(ne.getNj());
				truckTourList.remove(vt);
				truckPoint.add(truckPoint.indexOf(ne.getNk()), ne.getNj());
				TruckTour t= new TruckTour(truckPoint);
				tour.setTD(t);
			}
			maxSavings=0;
			} else{
				break;
			}
			System.out.println(name()+tour);
			
		}
		return tour;
		
	}
	public NeighborHood relocateAsDrone(Point j,int subPointStart,int subPointEnd,double savings){
		
		NeighborHood ne=null;
		System.out.println(name()+"cus "+j);
		for(int i=subPointStart;i<subPointEnd-1;i++)
			for(int k=i+1;k<subPointEnd;k++){
				ArrayList<Point> truckPoint=tour.getTD().getTruck_tour();
				//System.out.println(name()+"truckTour:: "+truckPoint);
				DroneDelivery de= new DroneDelivery(truckPoint.get(i), j, truckPoint.get(k));
				if(tspd.checkNotOverLapDroneDelivery(de, tour)!=1) continue;
				ArrayList<DroneDelivery> lde= tour.getDD();
				lde.add(de);
				tour.setDD(lde);
				if(tspd.checkConstraint(tour)){
					if(tspd.inP(truckPoint.get(i),j ,truckPoint.get(k))){
						double delta=tspd.cost(truckPoint.get(i), j,truckPoint.get(k));
						if (savings-delta>maxSavings){
							ne= new NeighborHood(true,  truckPoint.get(i), j, truckPoint.get(k));//
							maxSavings=savings-delta;
						}
					}
				}
				lde.remove(de);
				tour.setDD(lde);
				//System.out.println(name()+"truckTour2:: "+truckPoint);
			}
		System.out.println(name()+ne);
		return ne;
	}
	public NeighborHood relocateAsTruck(Point j,int subPointStart,int subPointEnd,double savings){
		ArrayList<Point> truckPoint=tour.getTD().getTruck_tour();
		NeighborHood ne=null;
		for(int i=subPointStart;i<subPointEnd-1;i++){
			
			if(tspd.checkConstraint(tour)){
				double delta=tspd.cost(truckPoint.get(i), j)+tspd.cost(j,truckPoint.get(i+1))-tspd.cost(truckPoint.get(i),truckPoint.get(i+1));
				truckPoint.add(i+1, j);
				TruckTour t= new TruckTour(truckPoint);
				tour.setTD(t);
				if(delta<savings)
					if(tspd.checkConstraint(tour)){
						if(savings-delta>maxSavings){
						ne= new NeighborHood(false, truckPoint.get(i), j, truckPoint.get(i+2));//
						maxSavings=savings-delta;
						}
					}
				truckPoint.remove(i+1);
				t= new TruckTour(truckPoint);
				tour.setTD(t);
			}
		}
		return ne;
	}
	String name(){
		return "TSPD_LS:: ";
	}
}