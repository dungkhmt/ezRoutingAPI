package com.kse.ezRoutingAPI.tspd.service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.apache.xmlbeans.impl.jam.xml.TunnelledException;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.GRASP_Arc;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;

public class GRASP {
	private Tour solution_tour;
	private TSPD tspd;
	int nTSP;
	
	public GRASP(TSPD tspd){
		this.tspd = tspd;
		//System.out.println("GRASP::construct::tspd["+tspd.getStartPoint().toString()+","+tspd.getClientPoints().toString()+","+tspd.getEndPoint().toString());
	}
	
	public Tour solve(){
		solution_tour = null;
		double bestObjectiveValue = Double.MAX_VALUE;
		TSP tsp = new TSP(tspd.getStartPoint(), tspd.getClientPoints(), tspd.getEndPoint());
		tsp.setDistances_matrix(tspd.getDistancesTruck());
		
		int iteration = 0;
		nTSP = tspd.getClientPoints().size();
		
		while(iteration < nTSP){
			iteration++;
			//ArrayList<Point> tour = tsp.randomGenerator();
			ArrayList<Point> tour = tsp.lsInitTSP();
			System.out.println("iteration "+iteration);
			System.out.println("tsp random tour = "+tour.toString());
			Tour tspdSolution = split_algorithm(tour);
			System.out.println("tspd after using split = " + tspdSolution.toString()+"  cost = "+tspd.cost(tspdSolution));
			tspdSolution = local_search(tspdSolution);
			System.out.println("tspd after local_search = " + tspdSolution.toString()+"  cost = "+tspd.cost(tspdSolution));
			if(tspd.cost(tspdSolution) < bestObjectiveValue){
				solution_tour = tspdSolution;
				bestObjectiveValue = tspd.cost(tspdSolution);
				iteration = 0;
			}
			System.out.println("bestTour = "+solution_tour.toString()+"   bestObjectiveValue "+bestObjectiveValue);
		}
		
		return solution_tour;
	}
	
	private Point[] P;
	private ArrayList<DroneDelivery> T;
	
	public Tour split_algorithm(ArrayList<Point> tsp_tour){
		build_graph(tsp_tour);
//		System.out.println("GRASP::split_alogrithm::T="+T.toString());
//		System.out.print("GRASP::split_alogrithm::P=[");
//		for(int i=0; i<P.length; i++){
//			System.out.print(P[i].toString()+", ");
//		}
//		System.out.print("]");
//		System.out.println();
		Point pj = tsp_tour.get(nTSP+1);
		ArrayList<Point> Sa = new ArrayList<Point>();
		
		//System.out.println("split_algorithm: tsp_tour"+tsp_tour.toString());
		
		Point pi = new Point(nTSP+2,0,0);
//		System.out.println("split_algorithm: pi is equals depot = "+pi.equals(tsp_tour.get(0)));
//		System.out.println("split_algorithm: nTSP="+nTSP+"  tsp_tour.length="+tsp_tour.size());
//		for(int i=0; i<P.length; i++){
//			System.out.println("P["+i+"]="+P[i].toString());
//		}
		Sa.add(pj);
		while(!pi.equals(tsp_tour.get(0))){
			pi = P[pj.getID()];
			Sa.add(pi);
			pj = pi;
		}
		Collections.reverse(Sa);
		//System.out.println("GRASP::split_algorithm::Sa="+Sa.toString());
		
		ArrayList<Point> St = new ArrayList<Point>();
		ArrayList<DroneDelivery> Sd = new ArrayList<DroneDelivery>();
		
		//Construct Drone Deliveries
		for(int i=0; i<Sa.size()-1; i++){
			Point sai = Sa.get(i);
			Point saj = Sa.get(i+1);
			if(tsp_tour.indexOf(saj)-tsp_tour.indexOf(sai) > 1){
				for(int j=0; j<T.size(); j++){
					DroneDelivery tmp_dd = T.get(j);
					if(tmp_dd.getLauch_node().equals(sai) && tmp_dd.getRendezvous_node().equals(saj)){
						Sd.add(tmp_dd);
					}
				}
			}
		}
		//System.out.println("Drone Deliveries = "+Sd.toString());
		//Construct Truck Tour
		Point currentPosition = tsp_tour.get(0);
		int index = 0;
		while(index <= nTSP+1){
			boolean checkLauchNode =false;
			//System.out.println("currentPosition = "+currentPosition.toString());
			for(int i=0; i<Sd.size(); i++){
				DroneDelivery tmp_dd = Sd.get(i);
				//System.out.println("currentPosition " + currentPosition.toString() +" check droneDelivery"+tmp_dd.toString());
				if(currentPosition.equals(tmp_dd.getLauch_node())){
					//St.add(currentPosition);
					//System.out.println("currentPosition "+currentPosition.toString()+" is lauch_node");
					checkLauchNode = true;
					while(!currentPosition.equals(tmp_dd.getRendezvous_node())){
						if(currentPosition.equals(tmp_dd.getDrone_node())){
							//St.add(currentPosition);
							//System.out.println("currentPosition "+currentPosition.toString()+" is drone_node; St="+St.toString());
							index++;
							currentPosition = tsp_tour.get(index);
							//continue;
						}else{
							St.add(currentPosition);
							//System.out.println("currentPosition "+currentPosition.toString()+" is NOT drone_node; St="+St.toString());
							index++;
							currentPosition = tsp_tour.get(index);
						}
					}
					break;
				}
			}
			if(!checkLauchNode){
				St.add(currentPosition);
				//System.out.println("checkLauchNode false St="+St.toString());			
				index++;
				if(index > nTSP+1) break;
				currentPosition = tsp_tour.get(index);
			}
		}
		//St.add(tsp_tour.get(nTSP+1));
		TruckTour truck_tour = new TruckTour(St);
		Tour tour = new Tour(truck_tour, Sd);
		
		return tour;
	}
	
	public void build_graph(ArrayList<Point> tsp_tour){
		ArrayList<GRASP_Arc> arcs = new ArrayList<GRASP_Arc>();
		T = new ArrayList<DroneDelivery>();
		//System.out.println("build_graph: tsp_tour input="+tsp_tour.toString());
		for(int i=0; i<tsp_tour.size()-1; i++){
			Point pi = tsp_tour.get(i);
			Point pk = tsp_tour.get(i+1);	
			GRASP_Arc arc = new GRASP_Arc(pi, pk, tspd.cost(pi, pk));
			arcs.add(arc);
		}
		
		//System.out.println("check isDroneDelivery and add auxilary graph");
		for(int i=0; i<tsp_tour.size()-2; i++){
			for(int k=i+2; k<tsp_tour.size(); k++){
				double minValue = Double.MAX_VALUE;
				int minIndex = nTSP+3;
				
				Point pi = tsp_tour.get(i);
				Point pk = tsp_tour.get(k);
				
				//System.out.println("pi="+pi.toString());
				//System.out.println("pk="+pk.toString());
				for(int j=i+1; j<k; j++){
					Point pj = tsp_tour.get(j);
					//System.out.println("pj="+pj.toString());
					if(tspd.inP(pi, pj, pk) && tspd.checkWaitTime(pi, pj, pk, tsp_tour)){
						double d1 = tspd.d_truck(tsp_tour.get(j-1), tsp_tour.get(j+1));
						double d2 = tspd.d_truck(tsp_tour.get(j-1), tsp_tour.get(j));
						double d3 = tspd.d_truck(tsp_tour.get(j), tsp_tour.get(j+1));
						
						double cost = tspd.cost(i, k, tsp_tour) + tspd.getC1()*(d1-d2-d3) + tspd.cost(pi,pj,pk);
						//System.out.println("pi,pj,pk is droneDelivery , cost="+cost);
						if(cost < minValue){
							minValue = cost;
							minIndex = j;
						}
					}
				}
				
				GRASP_Arc arc = new GRASP_Arc(pi, pk, minValue);
				arcs.add(arc);
				//System.out.println("add arc "+arc.toString());
				//System.out.println("minValue = "+minValue+"    minIndex="+minIndex);
				if(minIndex != nTSP+3){
					DroneDelivery dd = new DroneDelivery(pi,tsp_tour.get(minIndex),pk);
					T.add(dd);
				}
			}
		}
		
		//System.out.println("build_graph: auxilary graph="+arcs.toString());
		
		P = new Point[nTSP+2];
		double V[] = new double[nTSP+2];
		V[0] = 0;
		for(int i=1; i<nTSP+2; i++){
			V[i] = Double.MAX_VALUE;
		}
		P[0] = tsp_tour.get(0);
		for(int k=1; k< tsp_tour.size(); k++){
			for(int j=0; j<arcs.size(); j++){
				GRASP_Arc arc = arcs.get(j);
				if(arc.getK().equals(tsp_tour.get(k))){
					int pk = tsp_tour.get(k).getID();
					int pi = arc.getI().getID();
					if(V[pk] > V[pi] + arc.getCost()){
						V[pk] = V[pi] + arc.getCost();
						P[pk] = arc.getI();
					}
				}
			}
		}
//		System.out.println("P=[");
//		for(int i=0; i<nTSP+2; i++){
//			System.out.print(P[i].toString()+", ");
//		}
//		System.out.print("]");
//		System.out.println();
	}
	
	public Tour local_search(Tour tspdSolution){
		Random rand = new Random();
		
		Tour next_tour = null;
		System.out.println("local_search::tspd input="+tspdSolution.toString());
		ArrayList<Point> totalPoints = new ArrayList<Point>();
		ArrayList<Point> truckTour = tspdSolution.getTD().getTruck_tour();
		
		for(int i=1; i<truckTour.size()-1; i++){
			totalPoints.add(truckTour.get(i));
		}
		
		ArrayList<Point> droneNodes = tspd.getDroneNodes(tspdSolution);
		for(int i=0; i<droneNodes.size(); i++){
			totalPoints.add(droneNodes.get(i));
		}
		//System.out.println("GRASP::local_search:: totalPoints="+totalPoints.toString());
		//System.out.println()
		int maxIter = 20;
		int it = 0;
		while(next_tour == null && it < maxIter){
			int iMoveOperator = rand.nextInt(4);
			int iPa = rand.nextInt(totalPoints.size());
			int iPb = rand.nextInt(totalPoints.size());
			while(iPa==iPb){
				iPb = rand.nextInt(totalPoints.size());
			}
			int iPc = rand.nextInt(totalPoints.size());
			while(iPc==iPa || iPc==iPb ){
				iPc = rand.nextInt(totalPoints.size());
			}
			
			Point a = totalPoints.get(iPa);
			Point b = totalPoints.get(iPb);
			Point c = totalPoints.get(iPc);
			System.out.println("("+a.toString()+", "+b.toString()+", "+c.toString()+") is chosen to move");
			if(iMoveOperator == 0){
				next_tour = relocate(tspdSolution, a, b);
			}else if(iMoveOperator == 1){
				next_tour = relocate(tspdSolution, a, b, c);
			}else if(iMoveOperator == 2){
				next_tour = remove(tspdSolution, a, b);
			}else{
				next_tour = two_exchange(tspdSolution, a, b);
			}
			it++ ;
		}
		
		if(next_tour == null ) return tspdSolution;
		
 		return next_tour;
	}
	
	public Tour relocate(Tour tspd_tour, Point a, Point b){
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tspd_tour);
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		//System.out.println("relocate("+a.toString()+", "+b.toString()+")");
		if(truckOnlyNodes.contains(a) && truckTour.contains(b)){
			int iInsert = truckTour.indexOf(b);
			int pRelocation = truckTour.indexOf(a);
			truckTour.remove(pRelocation);
			truckTour.add(iInsert, a);
			
			Tour new_tour = new Tour(new TruckTour(truckTour), tspd_tour.getDD());
			if(tspd.checkConstraint(new_tour)){
				//System.out.println("NEW_TOUR = "+new_tour.toString());
				return new_tour;
			}
	
			truckTour.remove(iInsert);
			truckTour.add(pRelocation,a);
		}
		//System.out.println("a is NOT in truckOnlyNodes or b is NOT in truckTour => RETURN NULL");
		return null;
	}
	
	public Tour relocate(Tour tspd_tour, Point a, Point i, Point k){
		//System.out.println("relocate("+a.toString()+", "+i.toString()+", "+k.toString()+")");
		
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tspd_tour);
		ArrayList<Point> droneNodes = tspd.getDroneNodes(tspd_tour);
		
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();

		Tour new_tour;
		
		if(a.equals(i) && i.equals(k) && k.equals(a)) {
			//System.out.println("a=i=k => return null");
			return null;
		}
		
		if(!(truckTour.contains(i) && truckTour.contains(k))) {
			//System.out.println("i,k not in truckTour => return null");
			return null;
		}
		
		int index_i = truckTour.indexOf(i);
		int index_k = truckTour.indexOf(k);
		if(index_k < index_i){
			//System.out.println("index_k = "+index_k+" < index_i = "+index_i+" => return null");
			return null;
		}
		
		if(!tspd.isDroneDelivery(i, a, k, truckTour)){
			//System.out.println("(i,a,k) is not drone delivery => return null");
			return null;
		}
		
		if(!(truckOnlyNodes.contains(a) || droneNodes.contains(a))) {
			//System.out.println("a is not in truckOnlyNode or droneNodes => return null");
			return null;
		}
		
		if(truckOnlyNodes.contains(a)){
			if(tspd.isDroneDelivery(i, a, k, truckTour)){
				truckTour.remove(a);
				DroneDelivery dd = new DroneDelivery(i, a, k);
				droneDeliveries.add(dd);
				
				new_tour = new Tour(new TruckTour(truckTour), droneDeliveries);
				//System.out.println("a is truckNode NEW_TOUR = "+new_tour.toString());
				return new_tour;
			}	
			//System.out.println("(i,a,k is not drone delivery => return null)");
			return null;
		}else if(droneNodes.contains(a)){
			for(int index=0; index<droneDeliveries.size(); index++){
				DroneDelivery dd_tmp = droneDeliveries.get(index);
				
				if(dd_tmp.getDrone_node().equals(a)){
					dd_tmp.setLauch_node(i);
					dd_tmp.setRendezvous_node(k);
					
					new_tour = new Tour(new TruckTour(truckTour), droneDeliveries);
					//System.out.println("a is drone node NEW_TOUR="+new_tour.toString());
					return new_tour;
				}
			}
			
			//System.out.println("a is not droneNode => RETURN NULL");
			return null;
			
		}else{
			//System.out.println("a not in NT union ND => RETURN NULL");
			return null;
		}
	}
	
	public Tour remove(Tour tspd_tour, Point j, Point k){
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		ArrayList<Point> droneNodes = tspd.getDroneNodes(tspd_tour);
		//System.out.println("remove("+j.toString()+", "+k.toString()+")");
		if(truckTour.contains(j)){
			//System.out.println("j in truckTour => return null");
			return null;
		}
		if(!droneNodes.contains(j)){
			//System.out.println("j is not droneNodes => return null");
			return null;
		}
		if(!truckTour.contains(k)){
			//System.out.println("k is not in truckTour => return null");
			return null;
		}
		if(k.equals(truckTour.get(0))) {
			//System.out.println("k is depot => return null");
			return null;
		}
		
		int index_k = truckTour.indexOf(k);
		truckTour.add(index_k, j);
		
		for(int index=0; index<droneDeliveries.size(); index++){
			DroneDelivery dd = droneDeliveries.get(index);
			
			if(dd.getDrone_node().equals(j)){
				droneDeliveries.remove(dd);
				break;
			}
		}
		
		Tour new_tour = new Tour(new TruckTour(truckTour), droneDeliveries);
		//System.out.println("NEW_TOUR = "+new_tour.toString());
		return new_tour;
	}
	
	public Tour two_exchange(Tour tspd_tour, Point a , Point b){
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<Point> droneNodes = tspd.getDroneNodes(tspd_tour);
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		//System.out.println("two_exchange("+a.toString()+", "+b.toString()+")");
		//System.out.println("truckTour input = "+ truckTour.toString());
		if(truckTour.contains(a) && droneNodes.contains(b)){
			int index_a = truckTour.indexOf(a);
			truckTour.remove(a);
			truckTour.add(index_a,b);
			for(int i=0; i<droneDeliveries.size(); i++){
				DroneDelivery dd = droneDeliveries.get(i);
				if(dd.getLauch_node().equals(a)){
					dd.setLauch_node(b);
				}
				if(dd.getRendezvous_node().equals(a)){
					dd.setRendezvous_node(b);
				}
			}
			//System.out.println("a is truckNode, b is droneNode, after change truckTour ="+truckTour.toString());
			for(int i=0; i<droneDeliveries.size(); i++){
				DroneDelivery dd = droneDeliveries.get(i);
				if(dd.getDrone_node().equals(b)){
					dd.setDrone_node(a);
					Tour tmp_tour= new Tour(new TruckTour(truckTour),droneDeliveries);
					//System.out.println("tour check constraint = "+tmp_tour.toString());
					if(!tspd.checkConstraint(tmp_tour)){
						dd.setDrone_node(b);
						truckTour.remove(b);
						truckTour.add(index_a,a);
						for(int in=0; in<droneDeliveries.size(); in++){
							DroneDelivery tmp_dd = droneDeliveries.get(in);
							if(tmp_dd.getLauch_node().equals(b)){
								tmp_dd.setLauch_node(a);
							}
							if(tmp_dd.getRendezvous_node().equals(b)){
								tmp_dd.setRendezvous_node(a);
							}
						}
						//System.out.println("checkConstraint is not valid return null");
						return null;
					}
					Tour new_tour = new Tour(new TruckTour(truckTour),droneDeliveries);
					//System.out.println("new_tour="+new_tour.toString());
					return new_tour;
				}
			}
			//System.out.println("GRASP::two_exchange::error when compare drone node");
			return null;
		}
		else if(droneNodes.contains(a) && droneNodes.contains(b)){
			for(int i=0; i<droneDeliveries.size(); i++){
				DroneDelivery dd = droneDeliveries.get(i);
				if(dd.getDrone_node().equals(a)){
					dd.setDrone_node(b);
				}
				if(dd.getDrone_node().equals(b)){
					dd.setDrone_node(a);
				}
			}
			Tour new_tour = new Tour(new TruckTour(truckTour), droneDeliveries);
			if(tspd.checkConstraint(new_tour)){
				//System.out.println("new_tour="+new_tour.toString());
				return new_tour;
			}else{
				for(int i=0; i<droneDeliveries.size(); i++){
					DroneDelivery dd = droneDeliveries.get(i);
					if(dd.getDrone_node().equals(a)){
						dd.setDrone_node(b);
					}
					if(dd.getDrone_node().equals(b)){
						dd.setDrone_node(a);
					}
				}
				//System.out.println("return null");
				return null;
			}
		}
		else if(truckTour.contains(a) && truckTour.contains(b)){
			int index_a = truckTour.indexOf(a);
			int index_b = truckTour.indexOf(b);
			
			truckTour.remove(a);
			truckTour.add(index_a,b);
			truckTour.remove(b);
			truckTour.add(index_b,a);
			
			for(int i=0; i<droneDeliveries.size(); i++){
				DroneDelivery dd = droneDeliveries.get(i);
				if(dd.getLauch_node().equals(a)){
					dd.setLauch_node(b);
				}else if(dd.getLauch_node().equals(b)){
					dd.setLauch_node(a);
				}
				
				if(dd.getRendezvous_node().equals(a)){
					dd.setRendezvous_node(b);
				}else if(dd.getRendezvous_node().equals(b)){
					dd.setRendezvous_node(a);
				}
			}
			
			Tour new_tour = new Tour(new TruckTour(truckTour), droneDeliveries);
			if(tspd.checkConstraint(new_tour)){
				//System.out.println("new_tour="+new_tour.toString());
				return new_tour;
			}else{
				truckTour.remove(a);
				truckTour.add(index_a,a);
				truckTour.remove(b);
				truckTour.add(index_b,b);
				
				for(int i=0; i<droneDeliveries.size(); i++){
					DroneDelivery dd = droneDeliveries.get(i);
					if(dd.getLauch_node().equals(a)){
						dd.setLauch_node(b);
					}else if(dd.getLauch_node().equals(b)){
						dd.setLauch_node(a);
					}
					if(dd.getRendezvous_node().equals(a)){
						dd.setRendezvous_node(b);
					}else if(dd.getRendezvous_node().equals(b)){
						dd.setRendezvous_node(a);
					}
				}
				//System.out.println("return null");
				return null;
			}
		}
		else{
			return two_exchange(tspd_tour, b, a);
		}
	}
}
