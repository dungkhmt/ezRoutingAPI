package com.kse.ezRoutingAPI.tspd.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
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

public class GRASPkDrone {
	private Tour solution_tour;
	private TSPD tspd;
	int nTSP;
	
	public GRASPkDrone(TSPD tspd){
		this.tspd = tspd;
		//System.out.println("GRASP::construct::tspd["+tspd.getStartPoint().toString()+","+tspd.getClientPoints().toString()+","+tspd.getEndPoint().toString());
		/*PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream("output.txt"));
			System.setOut(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
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
			System.out.println("GRASP::solve:: iteration "+iteration);
			ArrayList<Point> tour = tsp.lsInitTSP();
			System.out.println("tsp random tour = "+tour.toString());
			Tour tspdSolution = split_algorithm(tour);
			
			/*
			 * record solution 
			 * need make new object to record solution
			 */
			if(tspd.cost(tspdSolution) < bestObjectiveValue){
				//solution_tour = tspdSolution;
				ArrayList<Point> trucktour = tspdSolution.getTD().getTruck_tour();
				ArrayList<Point> solution_truckTour = new ArrayList<Point>();
				for(int i=0; i<trucktour.size(); i++){
					Point tmp_solutionPoint = new Point(trucktour.get(i).getID(),trucktour.get(i).getLat(),trucktour.get(i).getLng());
					solution_truckTour.add(tmp_solutionPoint);
				}
				ArrayList<DroneDelivery> droneDeliveries = tspdSolution.getDD();
				ArrayList<DroneDelivery> solution_droneDeliveries = new ArrayList<DroneDelivery>();
				for(int i=0; i<droneDeliveries.size(); i++){
					DroneDelivery dd = droneDeliveries.get(i);
					Point solution_launchNode = new Point(dd.getLauch_node().getID(),dd.getLauch_node().getLat(),dd.getLauch_node().getLng());
					Point solution_droneNode = new Point(dd.getDrone_node().getID(),dd.getDrone_node().getLat(),dd.getDrone_node().getLng());
					Point solution_rendezvousNode = new Point(dd.getRendezvous_node().getID(),dd.getRendezvous_node().getLat(),dd.getRendezvous_node().getLng());
					solution_droneDeliveries.add(new DroneDelivery(solution_launchNode, solution_droneNode, solution_rendezvousNode));
				}
				
				solution_tour = new Tour(new TruckTour(solution_truckTour), solution_droneDeliveries);
				bestObjectiveValue = tspd.cost(tspdSolution);
			}
			System.out.println("tspd after using split = " + tspdSolution.toString()+"  cost = "+tspd.cost(tspdSolution));
			tspdSolution = local_search(tspdSolution);
			System.out.println("tspd after local_search = " + tspdSolution.toString()+"  cost = "+tspd.cost(tspdSolution));
			if(tspd.cost(tspdSolution) < bestObjectiveValue){
				solution_tour = tspdSolution;
				bestObjectiveValue = tspd.cost(tspdSolution);
				iteration = 0;
			}
			System.out.println("bestTour = "+solution_tour.toString()+"   cost = "+tspd.cost(solution_tour)+"    bestObjectiveValue "+bestObjectiveValue);
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
		System.out.println("GRASP::split_algorithm::Sa="+Sa.toString());
		
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
		System.out.println("Drone Deliveries = "+Sd.toString());
		//Construct Truck Tour
		Point currentPosition = tsp_tour.get(0);
		int index = 0;
		while(index <= nTSP+1){
			//boolean checkLauchNode =false;
			//System.out.println("currentPosition = "+currentPosition.toString());
			ArrayList<Point> droneNodes = new ArrayList<Point>();
			Point rendezvousNode = null;
			for(int i=0; i<Sd.size(); i++){
				DroneDelivery tmp_dd = Sd.get(i);
				//System.out.println("currentPosition " + currentPosition.toString() +" check droneDelivery"+tmp_dd.toString());
				if(currentPosition.equals(tmp_dd.getLauch_node())){
					droneNodes.add(tmp_dd.getDrone_node());
					rendezvousNode = tmp_dd.getRendezvous_node();
					//St.add(currentPosition);
					//System.out.println("currentPosition "+currentPosition.toString()+" is lauch_node");
					//checkLauchNode = true;
//					break;
				}
			}
			
			if(rendezvousNode == null){
				St.add(currentPosition);
				//System.out.println("checkLauchNode false St="+St.toString());			
				index++;
				if(index > nTSP+1) break;
				currentPosition = tsp_tour.get(index);
			}else{
				while(!currentPosition.equals(rendezvousNode)){
					boolean checkDroneNode = false;
					for(int iDroneNode = 0; iDroneNode < droneNodes.size(); iDroneNode++){
						if(currentPosition.equals(droneNodes.get(iDroneNode)))
							checkDroneNode = true;
								
					}
					if(checkDroneNode){
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
		
		//int kDrone = 2;
		
		//System.out.println("check isDroneDelivery and add auxilary graph");
		for(int i=0; i<tsp_tour.size()-2; i++){
			for(int k=i+2; k<tsp_tour.size(); k++){
				double minValue = Double.MAX_VALUE;
				ArrayList<Integer> minIndex = new ArrayList<Integer>();
				
				Point pi = tsp_tour.get(i);
				Point pk = tsp_tour.get(k);
				//System.out.println("pi="+pi.toString());
				//System.out.println("pk="+pk.toString());
				
				//for(int ikDrone = 1; ikDrone <= kDrone; ikDrone++){
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
							minIndex = new ArrayList<Integer>();
							minIndex.add(j);
						}
					}
				}
				
				for(int j=i+1; j<k-1; j++){
					Point pj = tsp_tour.get(j);
					
					if(tspd.inP(pi, pj, pk) && tspd.checkWaitTime(pi, pj, pk, tsp_tour)){
						double d1 = tspd.d_truck(tsp_tour.get(j-1), tsp_tour.get(j+1));
						double d2 = tspd.d_truck(tsp_tour.get(j-1), tsp_tour.get(j));
						double d3 = tspd.d_truck(tsp_tour.get(j), tsp_tour.get(j+1));
						double cost = tspd.cost(i, k, tsp_tour) + tspd.getC1()*(d1-d2-d3) + tspd.cost(pi,pj,pk);
						
						for(int j1=j+1; j1<k; j1++){
							Point pj1 = tsp_tour.get(j1);
							if(tspd.inP(pi, pj1, pk) && tspd.checkWaitTime(pi, pj1, pk, tsp_tour)){
								double dj1 = tspd.d_truck(tsp_tour.get(j1-1), tsp_tour.get(j1+1));
								double dj2 = tspd.d_truck(tsp_tour.get(j1-1), tsp_tour.get(j1));
								double dj3 = tspd.d_truck(tsp_tour.get(j1), tsp_tour.get(j1+1));
								
								cost += tspd.getC1()*(dj1 - dj2 - dj3) + tspd.cost(pi,pj1,pk); 
								if(cost < minValue){
									minValue = cost;
									minIndex = new ArrayList<Integer>();
									minIndex.add(j);
									minIndex.add(j1);
								}
							}
						}
					}
				}
				
				GRASP_Arc arc = new GRASP_Arc(pi, pk, minValue);
				arcs.add(arc);
//				System.out.println("add arc "+arc.toString());
//				System.out.println("minValue = "+minValue+"    minIndex="+minIndex.toString());
				for(int iMinIndex = 0; iMinIndex < minIndex.size(); iMinIndex++){	
					DroneDelivery dd = new DroneDelivery(pi,tsp_tour.get(minIndex.get(iMinIndex)),pk);
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
		int maxIter = 100;
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
		System.out.println("relocate(a="+a.toString()+", b="+b.toString()+")");
		System.out.println("relocate::tspd input = "+tspd_tour.toString());
		if(truckOnlyNodes.contains(a) && truckTour.contains(b)){
			int iInsert = truckTour.indexOf(b);
			int pRelocation = truckTour.indexOf(a);
			truckTour.remove(pRelocation);
			truckTour.add(iInsert, a);
			
			Tour new_tour = new Tour(new TruckTour(truckTour), tspd_tour.getDD());
			System.out.println("checkConstraint::trucktour = "+truckTour.toString());
			if(tspd.checkConstraint(new_tour)){
				System.out.println("NEW_TOUR = "+new_tour.toString());
				return new_tour;
			}
			truckTour.remove(iInsert);
			truckTour.add(pRelocation,a);
			System.out.println("checkConstraint false, tspd input = "+tspd_tour.toString());
		}
		System.out.println("a is NOT in truckOnlyNodes or b is NOT in truckTour => RETURN NULL");
		System.out.println("tspd input = "+tspd_tour.toString());
		return null;
	}
	
	public Tour relocate(Tour tspd_tour, Point a, Point i, Point k){
		System.out.println("relocate(a="+a.toString()+", i="+i.toString()+", k="+k.toString()+")");
		
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tspd_tour);
		ArrayList<Point> droneNodes = tspd.getDroneNodes(tspd_tour);
		
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();

		Tour new_tour;
		
		if(a.equals(i) && i.equals(k) && k.equals(a)) {
			System.out.println("a=i=k => return null");
			return null;
		}
		
		if(!(truckTour.contains(i) && truckTour.contains(k))) {
			System.out.println("i,k not in truckTour => return null");
			return null;
		}
		
		int index_i = truckTour.indexOf(i);
		int index_k = truckTour.indexOf(k);
		if(index_k < index_i){
			System.out.println("k = "+k.toString()+" index_k = "+index_k+"< i= "+i.toString()+"  index_i = "+index_i+" => return null");
			return null;
		}
		
		if(!tspd.isDroneDelivery(i, a, k, truckTour)){
			System.out.println("(i,a,k) = ("+i.toString()+","+a.toString()+","+k.toString()+" is not drone delivery => return null");
			return null;
		}
		
		if(!(truckOnlyNodes.contains(a) || droneNodes.contains(a))) {
			System.out.println("a = "+a.toString()+" is not in truckOnlyNode or droneNodes => return null");
			return null;
		}
		
		//check how many drone launch from point i and how many drone rendezvous at point k
		//if amount of drone launch from point i high k drone return null
		int nDroneLauchFrompi = 0;
		int nDroneRendezvousAtk = 0;
		for(int iDroneDelivery=0; iDroneDelivery < droneDeliveries.size(); iDroneDelivery++){
			DroneDelivery tmp_droneDelivery = droneDeliveries.get(iDroneDelivery);
			if(i.equals(tmp_droneDelivery.getLauch_node())){
				nDroneLauchFrompi ++;
			}
			if(k.equals(tmp_droneDelivery.getRendezvous_node())){
				nDroneRendezvousAtk ++;
			}
		}
		if(nDroneLauchFrompi >= 2 || nDroneRendezvousAtk >= 2) {
			System.out.println("amount of drone in node higher than k => return null");
			return null;
		}
		
		if(truckOnlyNodes.contains(a)){
			if(tspd.isDroneDelivery(i, a, k, truckTour)){
				truckTour.remove(a);
				DroneDelivery dd = new DroneDelivery(i, a, k);
				droneDeliveries.add(dd);
				
				new_tour = new Tour(new TruckTour(truckTour), droneDeliveries);
				System.out.println("a = "+a.toString()+" is truckNode NEW_TOUR = "+new_tour.toString());
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
					System.out.println("a = "+a.toString()+" is drone node NEW_TOUR="+new_tour.toString());
					return new_tour;
				}
			}
			
			System.out.println("a = "+a.toString()+" is not droneNode => RETURN NULL");
			return null;
			
		}else{
			System.out.println("a = "+a.toString()+" not in NT union ND => RETURN NULL");
			return null;
		}
	}
	
	public Tour remove(Tour tspd_tour, Point j, Point k){
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		ArrayList<Point> droneNodes = tspd.getDroneNodes(tspd_tour);
		System.out.println("remove(j="+j.toString()+", k="+k.toString()+")");
		if(truckTour.contains(j)){
			System.out.println(j.toString()+" in truckTour => return null");
			return null;
		}
		if(!droneNodes.contains(j)){
			System.out.println(j.toString()+" is not droneNodes => return null");
			return null;
		}
		if(!truckTour.contains(k)){
			System.out.println(k.toString()+" is not in truckTour => return null");
			return null;
		}
		if(k.equals(truckTour.get(0))) {
			System.out.println(k.toString()+" is depot => return null");
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
		System.out.println("NEW_TOUR = "+new_tour.toString());
		return new_tour;
	}
	
	public Tour two_exchange(Tour tspd_tour, Point a , Point b){
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<Point> droneNodes = tspd.getDroneNodes(tspd_tour);
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		System.out.println("two_exchange(a="+a.toString()+", b="+b.toString()+")");
		System.out.println("tpsd input = "+ tspd_tour.toString());
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
			System.out.println("a is truckNode, b is droneNode, after change truckTour ="+truckTour.toString());
			for(int i=0; i<droneDeliveries.size(); i++){
				DroneDelivery dd = droneDeliveries.get(i);
				if(dd.getDrone_node().equals(b)){
					dd.setDrone_node(a);
					Tour tmp_tour= new Tour(new TruckTour(truckTour),droneDeliveries);
					System.out.println("tour check constraint = "+tmp_tour.toString());
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
						System.out.println("checkConstraint is not valid return null");
						return null;
					}
					Tour new_tour = new Tour(new TruckTour(truckTour),droneDeliveries);
					System.out.println("new_tour="+new_tour.toString());
					return new_tour;
				}
			}
			System.out.println("GRASP::two_exchange::error when compare drone node (no drone node == b)==> return null");
			return null;
		}
		else if(droneNodes.contains(a) && droneNodes.contains(b)){
			System.out.println("a = "+a.toString()+"  b = "+b.toString()+" is both drone node");
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
				System.out.println("new_tour="+new_tour.toString());
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
				System.out.println("check constraint false when exchange a and ==> return null");
				return null;
			}
		}
		else if(truckTour.contains(a) && truckTour.contains(b)){
			System.out.println("a = "+a.toString()+"  b = "+b.toString()+" is both in truck tour");
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
				System.out.println("new_tour="+new_tour.toString());
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
				System.out.println("check constraint when swap a and b false ==> return null");
				return null;
			}
		}
		else{
			//return two_exchange(tspd_tour, b, a);
			System.out.println("truckTour.contains(a) && droneNodes.contains(b) false");
			System.out.println("truckTour.contains(a) && truckTour.contains(b) false");
			System.out.println("droneNodes.contains(a) && droneNodes.contains(b) false");
			return null;
		}
	}
}