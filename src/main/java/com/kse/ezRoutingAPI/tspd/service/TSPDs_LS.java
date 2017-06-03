package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.NeighborHood;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;

public class TSPDs_LS {
	int customers;
	Tour tour;
	TSPDs tspds;
	TSP tsp;
	double maxSavings=0;
	
	public TSPDs_LS(TSPDs tspkd) {
		this.tspds = tspkd;
	}
	void printDArr(double [][] arr,int m ,int n){
		System.out.println("***********************************");
		for(int i=0;i<m;i++){
			for(int j=0;j<n;j++)
				System.out.print(arr[i][j]+" ");
			System.out.println();
		}
		System.out.println(">***********************************");
	}
	public void init(){
		
		tsp=new TSP(tspds.getStartPoint(), tspds.getClientPoints(), tspds.getEndPoint());
		tsp.setDistances_matrix(tspds.getDistancesTruck());
		double x[][]=tspds.getDistancesDrone();
		
		System.out.println(tsp.lsInitTSP());
		TruckTour truckTour=new TruckTour(tsp.lsInitTSP());
		ArrayList<DroneDelivery> droneTours=new ArrayList<DroneDelivery>();
		tour= new Tour(truckTour, droneTours);
		printDArr(x, tspds.getClientPoints().size()+2 ,  tspds.getClientPoints().size()+2);
		x=tspds.getDistancesTruck();
		printDArr(x, tspds.getClientPoints().size()+2 ,  tspds.getClientPoints().size()+2);
	}
	public Tour solve(){
		init();
		ArrayList<Point> customerPoints=tspds.getClientPoints();
		boolean d[]= new boolean[customerPoints.size()];
		int dlr[]= new int[customerPoints.size()];
		
		maxSavings=0;
		for(int i=0;i<customerPoints.size();i++) d[i]=true;
		while (true){
			ArrayList<Point> truckTourList=tour.getTD().getTruck_tour();
			NeighborHood ne=null;
			for(int ik=1;ik<=tspds.getK();ik++){
				for(int i=0;i<truckTourList.size()-(ik+2);i++){
					boolean xd=true;
					for(int j=i+1;j<i+ik+2;j++){
						if(d[i]==false){
							xd=false;
							break;
						}
					}
					if(xd==false) continue;
				}
			}
			System.out.println(name()+tour);
			
		}
		return tour;
		
	}
	public NeighborHood relocateAsDrone(Point j,int subPointStart,int subPointEnd,double savings){
		
		NeighborHood ne=null;
		for(int i=subPointStart;i<subPointEnd-1;i++)
			for(int k=i+1;k<subPointEnd;k++){
				ArrayList<Point> truckPoint=tour.getTD().getTruck_tour();
				//System.out.println(name()+"truckTour:: "+truckPoint);
				DroneDelivery de= new DroneDelivery(truckPoint.get(i), j, truckPoint.get(k));
				if(tspds.checkOverQuantityDrone(de, tour,tspds.getK())!=1) continue;
				ArrayList<DroneDelivery> lde= tour.getDD();
				lde.add(de);
				tour.setDD(lde);
				if(tspds.checkConstraint(tour)){
					if(tspds.inP(truckPoint.get(i),j ,truckPoint.get(k))){
						double delta=tspds.cost(truckPoint.get(i), j,truckPoint.get(k));
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
		
		return ne;
	}
	public NeighborHood relocateAsTruck(Point j,int subPointStart,int subPointEnd,double savings){
		ArrayList<Point> truckPoint=tour.getTD().getTruck_tour();
		NeighborHood ne=null;
		for(int i=subPointStart;i<subPointEnd-1;i++){
			
			if(tspds.checkConstraint(tour)){
				double delta=tspds.cost(truckPoint.get(i), j)+tspds.cost(j,truckPoint.get(i+1))-tspds.cost(truckPoint.get(i),truckPoint.get(i+1));
				truckPoint.add(i+1, j);
				TruckTour t= new TruckTour(truckPoint);
				tour.setTD(t);
				if(delta<savings)
					if(tspds.checkConstraint(tour)){
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
