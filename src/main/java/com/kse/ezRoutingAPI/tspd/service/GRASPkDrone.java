package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.GRASP_Arc;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;
import com.kse.utils.LOGGER;

public class GRASPkDrone {
	private Tour solution_tour;
	private TSPD tspd;
	private Map<Integer,Boolean> allowDrone;
	int nTSP;
	int nDrone;
	
	public GRASPkDrone(TSPD tspd){
		this.tspd = tspd;
		//LOGGER.LOGGER.log(Level.INFO,"GRASP::construct::tspd["+tspd.getStartPoint().toString()+","+tspd.getClientPoints().toString()+","+tspd.getEndPoint().toString());
		/*PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream("output.txt"));
			System.setOut(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	public GRASPkDrone(TSPD tspd, Map<Integer, Boolean> allowDrone, int nDrone){
		this.tspd = tspd;
		this.allowDrone = allowDrone;
		this.nDrone = nDrone;
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
			LOGGER.LOGGER.log(Level.INFO,"it "+iteration+" init tsp tour cost = "+tspd.cost(0,tour.size()-1,tour));
			
			Tour tspdSolution = split_algorithm(tour);
			LOGGER.LOGGER.log(Level.INFO,"tspd after using split cost = "+tspd.cost(tspdSolution));
			
			local_search(tspdSolution);
			LOGGER.LOGGER.log(Level.INFO,"tspd after local_search cost = "+tspd.cost(tspdSolution));
			
			if(tspd.cost(tspdSolution) < bestObjectiveValue){
				solution_tour = tspdSolution;
				bestObjectiveValue = tspd.cost(tspdSolution);
				iteration = 0;
				LOGGER.LOGGER.log(Level.INFO,"find bestTour cost = "+tspd.cost(solution_tour)+"    bestObjectiveValue "+bestObjectiveValue);
			}
		}
		LOGGER.LOGGER.log(Level.INFO,"bestTour cost = "+tspd.cost(solution_tour)+"    bestObjectiveValue "+bestObjectiveValue);
		solution_tour.setTotalCost(tspd.cost(solution_tour));
		return solution_tour;
	}
	
	private Point[] P;
	private ArrayList<DroneDelivery> T;
	
	public Tour split_algorithm(ArrayList<Point> tsp_tour){
		
		build_graph(tsp_tour);
//		loggger.info("T="+T.toString());
		//LOGGER.LOGGER.log(Level.INFO,"GRASP::split_alogrithm:: start");
//		for(int i=0; i<P.length; i++){
//			System.out.print(P[i].toString()+", ");
//		}
//		System.out.print("]");
//		LOGGER.LOGGER.log(Level.INFO,);
		Point pj = tsp_tour.get(nTSP+1);
		ArrayList<Point> Sa = new ArrayList<Point>();
		
		//LOGGER.LOGGER.log(Level.INFO,"split_algorithm: tsp_tour"+tsp_tour.toString());
		
		Point pi = new Point(nTSP+2,0,0);
//		LOGGER.LOGGER.log(Level.INFO,"split_algorithm: pi is equals depot = "+pi.equals(tsp_tour.get(0)));
//		LOGGER.LOGGER.log(Level.INFO,"split_algorithm: nTSP="+nTSP+"  tsp_tour.length="+tsp_tour.size());
//		for(int i=0; i<P.length; i++){
//			LOGGER.LOGGER.log(Level.INFO,"P["+i+"]="+P[i].toString());
//		}
		Sa.add(pj);
		while(!pi.equals(tsp_tour.get(0))){
			pi = P[pj.getID()];
			Sa.add(pi);
			pj = pi;
		}
		Collections.reverse(Sa);
		//log.info("Sa="+Sa.toString());
		
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
		//log.info("Drone Deliveries = "+Sd.toString());
		//Construct Truck Tour
		Point currentPosition = tsp_tour.get(0);
		int index = 0;
		while(index <= nTSP+1){
			//boolean checkLauchNode =false;
			//LOGGER.LOGGER.log(Level.INFO,"currentPosition = "+currentPosition.toString());
			ArrayList<Point> droneNodes = new ArrayList<Point>();
			Point rendezvousNode = null;
			for(int i=0; i<Sd.size(); i++){
				DroneDelivery tmp_dd = Sd.get(i);
				//LOGGER.LOGGER.log(Level.INFO,"currentPosition " + currentPosition.toString() +" check droneDelivery"+tmp_dd.toString());
				if(currentPosition.equals(tmp_dd.getLauch_node())){
					droneNodes.add(tmp_dd.getDrone_node());
					rendezvousNode = tmp_dd.getRendezvous_node();
					//St.add(currentPosition);
					//LOGGER.LOGGER.log(Level.INFO,"currentPosition "+currentPosition.toString()+" is lauch_node");
					//checkLauchNode = true;
//					break;
				}
			}
			
			if(rendezvousNode == null){
				St.add(currentPosition);
				//LOGGER.LOGGER.log(Level.INFO,"checkLauchNode false St="+St.toString());			
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
						//LOGGER.LOGGER.log(Level.INFO,"currentPosition "+currentPosition.toString()+" is drone_node; St="+St.toString());
						index++;
						currentPosition = tsp_tour.get(index);
						//continue;
					}else{
						St.add(currentPosition);
						//LOGGER.LOGGER.log(Level.INFO,"currentPosition "+currentPosition.toString()+" is NOT drone_node; St="+St.toString());
						index++;
						currentPosition = tsp_tour.get(index);
					}
				}
			}	
		}
		//St.add(tsp_tour.get(nTSP+1));
		TruckTour truck_tour = new TruckTour(St);
		Tour tour = new Tour(truck_tour, Sd);
		//LOGGER.LOGGER.log(Level.INFO,"GRASP::split_alogrithm:: done");
		return tour;
	}
	
	public void build_graph(ArrayList<Point> tsp_tour){
		ArrayList<GRASP_Arc> arcs = new ArrayList<GRASP_Arc>();
		T = new ArrayList<DroneDelivery>();
		//LOGGER.LOGGER.log(Level.INFO,"build_graph:: start");
		for(int i=0; i<tsp_tour.size()-1; i++){
			Point pi = tsp_tour.get(i);
			Point pk = tsp_tour.get(i+1);	
			GRASP_Arc arc = new GRASP_Arc(pi, pk, tspd.cost(pi, pk));
			arcs.add(arc);
		}
		
		//LOGGER.LOGGER.log(Level.INFO,"compute cost");
		for(int i=0; i<tsp_tour.size()-2; i++){
			for(int k=i+2; k<tsp_tour.size(); k++){
				double minValue = Double.MAX_VALUE;
				List<Point> minIndex = new ArrayList<Point>();
				
				Point pi = tsp_tour.get(i);
				Point pk = tsp_tour.get(k);
				//LOGGER.LOGGER.log(Level.INFO,"pi="+pi.toString());
				//LOGGER.LOGGER.log(Level.INFO,"pk="+pk.toString());
				
				ArrayList<Point> list_drone_points = new ArrayList<Point>();
				for(int j=i+1; j<k; j++){
					list_drone_points.add(tsp_tour.get(j));
				}
				
				//generate combinations 
				List<List<Point>> list_drones = new ArrayList<List<Point>>();
				for(int ikDrone =1 ; ikDrone<=nDrone; ikDrone++){
					list_drones = combination(list_drone_points, ikDrone);
					for(int in=0; in<list_drones.size(); in++){
						List<Point> drone_points = list_drones.get(in);
						boolean check_allow_drone = true;
						for(int in1=0; in1<drone_points.size(); in1++){
							int point_id = drone_points.get(in1).getID();
							check_allow_drone = check_allow_drone && allowDrone.get(point_id);
						}
						if(check_allow_drone){
							double cost = tspd.cost(i, k, list_drones.get(in), tsp_tour);
							if(cost < minValue){
								minValue = cost;
								minIndex = list_drones.get(in);
							}
						}
					}
				}
				
				GRASP_Arc arc = new GRASP_Arc(pi, pk, minValue);
				arcs.add(arc);
//				LOGGER.LOGGER.log(Level.INFO,"add arc "+arc.toString());
//				LOGGER.LOGGER.log(Level.INFO,"minValue = "+minValue+"    minIndex="+minIndex.toString());
				for(int iMinIndex = 0; iMinIndex < minIndex.size(); iMinIndex++){	
					DroneDelivery dd = new DroneDelivery(pi,minIndex.get(iMinIndex),pk);
					T.add(dd);
				}
			}
		}
		
		//LOGGER.LOGGER.log(Level.INFO,"compute cost done");
		
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
		//LOGGER.LOGGER.log(Level.INFO,"build_graph:: done");
//		LOGGER.LOGGER.log(Level.INFO,"P=[");
//		for(int i=0; i<nTSP+2; i++){
//			System.out.print(P[i].toString()+", ");
//		}
//		System.out.print("]");
//		LOGGER.LOGGER.log(Level.INFO,);
	}
	
	public static <T> List<List<T>> combination(List<T> values, int size) {

	    if (0 == size) {
	        return Collections.singletonList(Collections.<T> emptyList());
	    }

	    if (values.isEmpty()) {
	        return Collections.emptyList();
	    }

	    List<List<T>> combination = new LinkedList<List<T>>();

	    T actual = values.iterator().next();

	    List<T> subSet = new LinkedList<T>(values);
	    subSet.remove(actual);

	    List<List<T>> subSetCombination = combination(subSet, size - 1);

	    for (List<T> set : subSetCombination) {
	        List<T> newSet = new LinkedList<T>(set);
	        newSet.add(0, actual);
	        combination.add(newSet);
	    }

	    combination.addAll(combination(subSet, size));

	    return combination;
	}
	
	public void local_search(Tour tspdSolution){
		//LOGGER.LOGGER.log(Level.INFO,"local_search start");
		
		int maxIter = 1000;
		int it = 0;
		
		Random R = new Random();
		
		int nOperator = 4;
		int[] nChoseOperator = new int[nOperator];
		for(int i=0; i<nOperator; i++){
			nChoseOperator[i] = 0;
		}
		
		while(it++ < maxIter){
			//LOGGER.LOGGER.log(Level.INFO,"iter "+it);
			int i = R.nextInt(4);
			nChoseOperator[i]++;
			switch(i){
				case 0: {
					//LOGGER.LOGGER.log(Level.INFO,"prev_cost = "+tspd.cost(tspdSolution));
					relocate_T(tspdSolution);
					//LOGGER.LOGGER.log(Level.INFO,"new_cost = "+tspd.cost(tspdSolution));
					break;
				}
				case 1: {
					//LOGGER.LOGGER.log(Level.INFO,"prev_cost = "+tspd.cost(tspdSolution));
					relocate_D(tspdSolution); 
					//LOGGER.LOGGER.log(Level.INFO,"new_cost = "+tspd.cost(tspdSolution));
					break;
				}
				case 2: {
					//LOGGER.LOGGER.log(Level.INFO,"prev_cost = "+tspd.cost(tspdSolution));
					remove(tspdSolution); 
					//LOGGER.LOGGER.log(Level.INFO,"new_cost = "+tspd.cost(tspdSolution));
					break;
				}
				case 3: {
					//LOGGER.LOGGER.log(Level.INFO,"prev_cost = "+tspd.cost(tspdSolution));
					two_exchange(tspdSolution); 
					//LOGGER.LOGGER.log(Level.INFO,"new_cost = "+tspd.cost(tspdSolution));
					break;
				}
			}
		}
		
		LOGGER.LOGGER.log(Level.INFO,"number chosen operator: relocate_T="+nChoseOperator[0]+" relocate_D="
		+nChoseOperator[1]+" remove="+nChoseOperator[2]+" two_exchange="+nChoseOperator[3]);
	}
	
	public void relocate_T(Tour tspd_tour){
		//LOGGER.LOGGER.log(Level.INFO,"chose relocate_T, tspd input = "+tspd_tour.toString()+"\ncost = "+tspd.cost(tspd_tour));
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tspd_tour);
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		
		if(truckOnlyNodes.size() == 0 || truckOnlyNodes == null){
			//LOGGER.LOGGER.log(Level.INFO,"truckOnlyNodes size = 0");
			return;
		}
		
		Random R = new Random();
		
		int iInsert = R.nextInt(truckTour.size()-1)+1;
		int ia = R.nextInt(truckOnlyNodes.size());
		
		Point a = truckOnlyNodes.get(ia);
		int pRelocation = truckTour.indexOf(a);
		
		Point b = truckTour.get(iInsert);
		
		//check a==b or a is previous point of b
		int it = 0;
		while((a.equals(b) || iInsert-pRelocation == 1) && it != truckTour.size()){
			iInsert = R.nextInt(truckTour.size()-1)+1;
			ia = R.nextInt(truckOnlyNodes.size());
			
			a = truckOnlyNodes.get(ia);
			pRelocation = truckTour.indexOf(a);
			
			b = truckTour.get(iInsert);
			
			it++;
		}
		if(it >= truckTour.size()){
			//LOGGER.LOGGER.log(Level.INFO,"Fail to chose a and b because a==b");
			return;
		}
		
		Point prev_a = truckTour.get(pRelocation-1);
		Point next_a = truckTour.get(pRelocation+1);
		Point prev_b = truckTour.get(iInsert-1);
		
		//LOGGER.LOGGER.log(Level.INFO,"a = "+a.toString()+" b = "+b.toString()+" prev_a = "+prev_a.toString()+" next_a = "+next_a.toString()+" prev_b = "+prev_b.toString());
		
		//compute cost 
		double prev_cost = tspd.cost(prev_a, a) + tspd.cost(a, next_a) + tspd.cost(prev_b, b);
		double new_cost = tspd.cost(prev_a,next_a) + tspd.cost(prev_b,a)+ tspd.cost(a,b);
		
		if(new_cost < prev_cost){
			truckTour.remove(pRelocation);
			iInsert = truckTour.indexOf(b);
			truckTour.add(iInsert, a);
			if(tspd.checkConstraint(tspd_tour)){
				//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR = "+tspd_tour.toString()+"\ncost="+tspd.cost(tspd_tour));
				//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR improve cost = "+(new_cost-prev_cost));
				return;
			}
			//reverse input 
			truckTour.remove(iInsert);
			truckTour.add(pRelocation,a);
			//LOGGER.LOGGER.log(Level.INFO,"checkConstraint false");
			return;
		}	
		//LOGGER.LOGGER.log(Level.INFO,"apply this operation not improve");
		return;
	}
	
	public void relocate_D(Tour tspd_tour){
		//LOGGER.LOGGER.log(Level.INFO,"chose relocate_D");
		
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tspd_tour);
		
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();

		//chose point a, point i, point k
		Random R  = new Random();
		int i_a_truck = -1;
		if(truckOnlyNodes.size() != 0 && truckOnlyNodes != null){
			i_a_truck = R.nextInt(truckOnlyNodes.size());
		}
		int i_da_drone = -1;
		DroneDelivery da = null;
		if(droneDeliveries.size() != 0 && droneDeliveries != null){
			i_da_drone = R.nextInt(droneDeliveries.size());
			da = droneDeliveries.get(i_da_drone);
		}
		
		int i_chose = R.nextInt(2);
		
		int i_a = -1;
		Point a;
		if((i_da_drone == -1 || i_chose == 0) && i_a_truck != -1){
			a = truckOnlyNodes.get(i_a_truck);
			i_a = truckTour.indexOf(a);
		}else{
			a = da.getDrone_node();
		}
		
		int i_i = R.nextInt(truckTour.size()-1);
		//i_k > i_i
		int i_k = R.nextInt(truckTour.size()-i_i-1) +i_i+1;
		
		int it = 0;
		while((i_i == i_a  || i_k == i_a || i_i >= i_k) &&  it != truckTour.size()){
			it++;
			i_i = R.nextInt(truckTour.size()-1);
			//i_k > i_i
			i_k = R.nextInt(truckTour.size()-i_i-1) +i_i+1;
		}
		
		if(it >= truckTour.size()){
			//LOGGER.LOGGER.log(Level.INFO,"Fail to chose point i and point k");
			return;
		}
		
		Point i = truckTour.get(i_i);
		Point k = truckTour.get(i_k);
		//LOGGER.LOGGER.log(Level.INFO,"i = "+i.toString()+" k = "+k.toString()+" a = "+a.toString());
		
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
		if(nDroneLauchFrompi >= nDrone || nDroneRendezvousAtk >= nDrone) {
			//LOGGER.LOGGER.log(Level.INFO,"amount of drone in node higher than k ");
			return;
		}
		
		//if a is truck-only node
		if(i_a != -1){
			Point prev_a = truckTour.get(i_a-1);
			Point next_a = truckTour.get(i_a+1);
			
			double prev_cost = tspd.cost(prev_a,a) + tspd.cost(a,next_a);
			double new_cost = tspd.cost(prev_a,next_a)+ tspd.cost(i,a,k);
			
			if(new_cost < prev_cost){
				truckTour.remove(a);
				if(tspd.checkDroneConstraint(i, a, k, truckTour)){
					DroneDelivery dd = new DroneDelivery(i, a, k);
					droneDeliveries.add(dd);
					//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR improve cost = "+(new_cost-prev_cost));
					return;
				}
				truckTour.add(i_a,a);
				//LOGGER.LOGGER.log(Level.INFO,"(i,a,k) is not satisfy drone constraint");
				return;
			}
			//LOGGER.LOGGER.log(Level.INFO,"relocate a (truck only node) not imporve");
			return;
		}
		//a is drone node
		else{
			if(tspd.checkDroneConstraint(i, a, k, truckTour)){
				double new_cost = tspd.cost(i,a,k); 
				double prev_cost = tspd.cost(da);
				if(new_cost < prev_cost){
					da.setLauch_node(i);
					da.setRendezvous_node(k);
					//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR improve cost = "+(new_cost-prev_cost));
					return;
				}
				//LOGGER.LOGGER.log(Level.INFO,"relocate a (drone node) not imporve");
				return;
			}
			//LOGGER.LOGGER.log(Level.INFO,"(i,a,k) is not satisfy drone constraint");
			return;
		}
	}
	
	public void remove(Tour tspd_tour){
		//LOGGER.LOGGER.log(Level.INFO,"chose remove");
		
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		
		if(droneDeliveries.size() == 0 || droneDeliveries == null){
			//LOGGER.LOGGER.log(Level.INFO,"droneDeliveries size = 0");
			return;
		}
			
		Random R = new Random();
		int i_dj = R.nextInt(droneDeliveries.size());
		DroneDelivery dj = droneDeliveries.get(i_dj);
		Point j = dj.getDrone_node();
		
		int i_k = R.nextInt(truckTour.size()-1)+1;
		Point k = truckTour.get(i_k);
		
		Point prev_k = truckTour.get(i_k-1);
		
		double prev_cost = tspd.cost(dj) + tspd.cost(prev_k,k);
		double new_cost = tspd.cost(prev_k,j) + tspd.cost(j,k);
		
		//LOGGER.LOGGER.log(Level.INFO,"remove drone node "+j.toString()+" insert before point"+k.toString());
		
		if(new_cost < prev_cost){
			truckTour.add(i_k, j);
			droneDeliveries.remove(dj);
			if(tspd.checkConstraint(tspd_tour)){
				//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR improve cost = "+(new_cost-prev_cost));
				return;
			}
			truckTour.remove(j);
			droneDeliveries.add(dj);
			//LOGGER.LOGGER.log(Level.INFO,"remove don't satisfy drone constraint");
			return;
		}
		
		//LOGGER.LOGGER.log(Level.INFO,"this operator not improve solution");
		return;
	}
	
	public void two_exchange(Tour tspd_tour){
		//LOGGER.LOGGER.log(Level.INFO,"chose two_exchange, tspd input = "+tspd_tour.toString());
		
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		
		ArrayList<Point> allPoints = new ArrayList<Point>();
		
		ArrayList<Point> truckTour_copy = new ArrayList<Point>();
		ArrayList<DroneDelivery> droneDeliveries_copy = new ArrayList<DroneDelivery>();
		
		for(int i=0; i<truckTour.size(); i++){
			truckTour_copy.add(truckTour.get(i));
			if(i == 0 || i == truckTour.size()-1)
				continue;
			
			allPoints.add(truckTour.get(i));
		}
		
		for(int i=0; i<droneDeliveries.size(); i++){
			DroneDelivery dd = droneDeliveries.get(i);
			Point lauch_node = dd.getLauch_node();
			Point drone_node = dd.getDrone_node();
			Point rendezvous_node = dd.getRendezvous_node();
			
			allPoints.add(drone_node);
			
			DroneDelivery dd_copy = new DroneDelivery(lauch_node, drone_node, rendezvous_node);
			droneDeliveries_copy.add(dd_copy);
		}
		
		Tour tspd_tour_copy = new Tour(new TruckTour(truckTour_copy), droneDeliveries_copy);
		//LOGGER.LOGGER.log(Level.INFO,"size allPoints = "+allPoints.size());
		
		Random R = new Random();
		
		int i_a = R.nextInt(allPoints.size());
		int i_b = R.nextInt(allPoints.size());
		
		while(i_a == i_b){
			i_a = R.nextInt(allPoints.size());
			i_b = R.nextInt(allPoints.size());
		}
		
		Point a = allPoints.get(i_a);
		Point b = allPoints.get(i_b);
		
		//System.out.println("i_a = "+i_a+" i_b = "+i_b+" Point a = "+a.toString()+" Point b = "+b.toString());
		//LOGGER.LOGGER.log(Level.INFO,"a = "+a.toString()+" b = "+b.toString());
		
		double prev_cost = tspd.cost(tspd_tour);
		
		for(int i=0; i<truckTour_copy.size(); i++){
			Point pi = truckTour_copy.get(i);
			if(pi.equals(a)){
				truckTour_copy.remove(i);
				truckTour_copy.add(i, b);
			}else if(pi.equals(b)){
				truckTour_copy.remove(i);
				truckTour_copy.add(i,a);
			}
		}
		
		for(int i=0; i<droneDeliveries_copy.size(); i++){
			DroneDelivery dd = droneDeliveries_copy.get(i);
			
			Point launch_node = dd.getLauch_node();
			Point drone_node = dd.getDrone_node();
			Point rendezvous_node = dd.getRendezvous_node();
			
			if(launch_node.equals(a)){
				dd.setLauch_node(b);
			}else if(launch_node.equals(b)){
				dd.setLauch_node(a);
			}
			
			if(drone_node.equals(a)){
				dd.setDrone_node(b);
			}else if(drone_node.equals(b)){
				dd.setDrone_node(a);
			}
			
			if(rendezvous_node.equals(a)){
				dd.setRendezvous_node(b);
			}else if(rendezvous_node.equals(b)){
				dd.setRendezvous_node(a);
			}
		}
		
		//LOGGER.LOGGER.log(Level.INFO,"tspd_tour_copy = "+tspd_tour_copy.toString());
		
		if(tspd.checkConstraint(tspd_tour_copy)){
			double new_cost = tspd.cost(tspd_tour_copy);
			if(new_cost < prev_cost){
				tspd_tour.setTD(tspd_tour_copy.getTD());
				tspd_tour.setDD(tspd_tour_copy.getDD());
				//LOGGER.LOGGER.log(Level.INFO,"tspd_tour = "+tspd_tour.toString());
				//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR improve cost = "+(new_cost-prev_cost));
				return;
			}
			//LOGGER.LOGGER.log(Level.INFO,"don't improve cost");
			return;
		}
		//LOGGER.LOGGER.log(Level.INFO,"don't satisfy drone constraint");
		return;
	}
	
	/*
	public Tour two_exchange_launch2drone(Tour tspd_tour){
		LOGGER.LOGGER.log(Level.INFO,"tpsd input = "+ tspd_tour.toString());
		
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		
		Random R = new Random();
		
		int i_db = R.nextInt(droneDeliveries.size());
		DroneDelivery db = droneDeliveries.get(i_db);
		Point b = db.getDrone_node();
		Point b_launch_node = db.getLauch_node();
		Point b_re_node = db.getRendezvous_node();
		
		int i_da = R.nextInt(droneDeliveries.size());
		DroneDelivery da = droneDeliveries.get(i_da);
		
		Point a = da.getLauch_node();
		int i_a = truckTour.indexOf(a);
		while(i_a == 0){
			i_da = R.nextInt(droneDeliveries.size());
			da = droneDeliveries.get(i_da);
		
			a = da.getLauch_node();
			i_a = truckTour.indexOf(a);
		}		
		
		Point a_re_node = da.getRendezvous_node();
		Point a_drone_node = da.getDrone_node();
		
		LOGGER.LOGGER.log(Level.INFO,"a = "+a.toString()+" b = "+b.toString());
	
		Point prev_a = truckTour.get(i_a-1);
		Point next_a = truckTour.get(i_a+1);
		
		double prev_cost = tspd.cost(da)+tspd.cost(db)+tspd.cost(prev_a,a)+tspd.cost(a,next_a);
		double new_cost = tspd.cost(b,a_drone_node,a_re_node) + tspd.cost(b_launch_node,a,b_re_node)+
				tspd.cost(prev_a,b)+tspd.cost(b,next_a);
		
		if(new_cost < prev_cost){
			truckTour.remove(a);
			truckTour.add(i_a,b);
			da.setLauch_node(b);
			db.setDrone_node(a);
			if(tspd.checkConstraint(tspd_tour)){
				LOGGER.LOGGER.log(Level.INFO,"exchange a is launch node and b is drone node \nNEW_TOUR="+tspd_tour.toString());
				return tspd_tour;	
			}
			truckTour.remove(b);
			truckTour.add(i_a,a);
			da.setLauch_node(a);
			db.setDrone_node(b);
			LOGGER.LOGGER.log(Level.INFO,"exchange a is launch node and b is drone node not satisfy drone constraint solution\ntspd-input="+tspd_tour.toString());
			return null;
		}		
		LOGGER.LOGGER.log(Level.INFO,"exchange a is launch node and b is drone node not improve solution\ntspd-input="+tspd_tour.toString());
		return null;
	}

	public Tour two_exchange_rendezvous2drone(Tour tspd_tour){
		LOGGER.LOGGER.log(Level.INFO,"tpsd input = "+ tspd_tour.toString());
		
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		
		Random R = new Random();
	
		int i_db = R.nextInt(droneDeliveries.size());
		DroneDelivery db = droneDeliveries.get(i_db);
		Point b = db.getDrone_node();
		Point b_launch_node = db.getLauch_node();
		Point b_re_node = db.getRendezvous_node();
				
		int i_da = R.nextInt(droneDeliveries.size());
		DroneDelivery da = droneDeliveries.get(i_da);
		
		Point a = da.getRendezvous_node();
		int i_a = truckTour.indexOf(a);
		while(i_a == truckTour.size()-1){
			i_da = R.nextInt(droneDeliveries.size());
			da = droneDeliveries.get(i_da);
			
			a = da.getRendezvous_node();
			i_a = truckTour.indexOf(a);
		}
		
		Point a_launch_node = da.getLauch_node();
		Point a_drone_node = da.getDrone_node();
		
		LOGGER.LOGGER.log(Level.INFO,"a = "+a.toString()+" b = "+b.toString());
		
		Point prev_a = truckTour.get(i_a-1);
		Point next_a = truckTour.get(i_a+1);
		
		double prev_cost = tspd.cost(da)+tspd.cost(db)+tspd.cost(prev_a,a)+tspd.cost(a,next_a);
		double new_cost = tspd.cost(a_launch_node,a_drone_node,b) + tspd.cost(b_launch_node,a,b_re_node)+
				tspd.cost(prev_a,b)+tspd.cost(b,next_a);
		if(new_cost < prev_cost){
			truckTour.remove(a);
			truckTour.add(i_a,b);
			da.setRendezvous_node(b);
			db.setDrone_node(a);
			if(tspd.checkConstraint(tspd_tour)){
				LOGGER.LOGGER.log(Level.INFO,"exchange a is rendezvous node and b is drone node \nNEW_TOUR="+tspd_tour.toString());
				return tspd_tour;	
			}
			truckTour.remove(b);
			truckTour.add(i_a,a);
			da.setRendezvous_node(a);
			db.setDrone_node(b);
			LOGGER.LOGGER.log(Level.INFO,"exchange a is rendezvous node and b is drone node not satisfy drone constraint solution\ntspd-input="+tspd_tour.toString());
			return null;
		}
		LOGGER.LOGGER.log(Level.INFO,"exchange a is rendezvous node and b is drone node not improve solution\ntspd-input="+tspd_tour.toString());
		return null;
	}
	
	public Tour two_exchange_truckonly2drone(Tour tspd_tour){
		LOGGER.LOGGER.log(Level.INFO,"tpsd input = "+ tspd_tour.toString());
		
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tspd_tour);
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		
		Random R = new Random();
		
		int i_oa = R.nextInt(truckOnlyNodes.size());
		Point a = truckOnlyNodes.get(i_oa);
		
		int i_a = truckTour.indexOf(a);
		while(i_a == 0 || i_a == truckTour.size()-1){
			i_oa = R.nextInt(truckOnlyNodes.size());
			a = truckOnlyNodes.get(i_oa);
			
			i_a = truckTour.indexOf(a);
		}
		
		Point prev_a = truckTour.get(i_a-1);
		Point next_a = truckTour.get(i_a+1);
				
		int i_db = R.nextInt(droneDeliveries.size());
		DroneDelivery db = droneDeliveries.get(i_db);
		Point b = db.getDrone_node();
		Point b_launch_node = db.getLauch_node();
		Point b_re_node = db.getRendezvous_node();
		
		LOGGER.LOGGER.log(Level.INFO,"a = "+a.toString()+" b = "+b.toString());
		
		double prev_cost = tspd.cost(db) + tspd.cost(prev_a,a) + tspd.cost(a,next_a);
		double new_cost = tspd.cost(b_launch_node,a,b_re_node) + tspd.cost(prev_a,b) + tspd.cost(b,next_a);
		if(new_cost < prev_cost){
			truckTour.remove(a);
			truckTour.add(i_a,b);
			db.setDrone_node(a);
			if(tspd.checkConstraint(tspd_tour)){
				LOGGER.LOGGER.log(Level.INFO,"exchange a is truck-only node and b is drone node \nNEW_TOUR="+tspd_tour.toString());
				return tspd_tour;
			}
			truckTour.remove(b);
			truckTour.add(i_a,a);
			db.setDrone_node(b);
			LOGGER.LOGGER.log(Level.INFO,"exchange a is truck-only node and b is drone node not satisfy drone constraint solution\ntspd-input="+tspd_tour.toString());
			return null;
		}
		LOGGER.LOGGER.log(Level.INFO,"exchange a is truck-only node and b is drone node not improve solution\ntspd-input="+tspd_tour.toString());
		return null;
	}
	
	public Tour two_exchange_drone2drone(Tour tspd_tour){
		LOGGER.LOGGER.log(Level.INFO,"tpsd input = "+ tspd_tour.toString());
		
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		
		Random R = new Random();
		
		int i_da = R.nextInt(droneDeliveries.size());
		int i_db = R.nextInt(droneDeliveries.size());
		
		int it = 0;
		while(i_da == i_db && it != droneDeliveries.size()){
			i_da = R.nextInt(droneDeliveries.size());
			i_db = R.nextInt(droneDeliveries.size()); 
		}
		
		if(it >= droneDeliveries.size()){
			LOGGER.LOGGER.log(Level.INFO,"Fail to chose point a and point b");
			return null;
		}
		
		DroneDelivery da = droneDeliveries.get(i_da);
		Point a = da.getDrone_node();
		Point a_launch_node = da.getLauch_node();
		Point a_re_node = da.getRendezvous_node();
		
		DroneDelivery db = droneDeliveries.get(i_db);
		Point b = db.getDrone_node();
		Point b_launch_node = db.getLauch_node();
		Point b_re_node = db.getRendezvous_node();
		
		LOGGER.LOGGER.log(Level.INFO,"a = "+a.toString()+" b = "+b.toString());
		
		double prev_cost = tspd.cost(da) + tspd.cost(db);
		
		double new_cost = tspd.cost(a_launch_node,b,a_re_node) + tspd.cost(b_launch_node,a,b_re_node);
		
		if(new_cost < prev_cost){
			 LOGGER.LOGGER.log(Level.INFO,"exchange a is drone node and b is drone node \nNEW_TOUR="+tspd_tour.toString());
			 da.setDrone_node(b);
			 db.setDrone_node(a);
			 return tspd_tour;
		 }
		 LOGGER.LOGGER.log(Level.INFO,"exchange a is drone node and b is drone node not improve solution\ntspd-input="+tspd_tour.toString());
		 return null;
	}
	
	public Tour two_exchange_launch2launch(Tour tspd_tour){
		LOGGER.LOGGER.log(Level.INFO,"tpsd input = "+ tspd_tour.toString());
		
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		
		Random R = new Random();
		
		DroneDelivery da;
		Point a;
		int i_a;
		
		DroneDelivery db;
		Point b;
		int i_b;
		
		do{
			int i_da = R.nextInt(droneDeliveries.size());
			int i_db = R.nextInt(droneDeliveries.size());
			
			int it = 0;
			while(i_da == i_db && it != droneDeliveries.size()){
				i_da = R.nextInt(droneDeliveries.size());
				i_db = R.nextInt(droneDeliveries.size());
				it++;
			}
			
			if(it >= droneDeliveries.size()){
				LOGGER.LOGGER.log(Level.INFO,"Fail to chose point a and point b");
				return null;
			}
			
			da = droneDeliveries.get(i_da);
			a = da.getLauch_node();
			i_a = truckTour.indexOf(a);
			
			db = droneDeliveries.get(i_db);
			b = db.getLauch_node();
			i_b = truckTour.indexOf(b);
			
			if(!(i_a == 0 || i_b == 0))
				break;
			
		}while(true);
		
		Point a_drone_node = da.getDrone_node();
		Point a_re_node = da.getRendezvous_node();
		Point prev_a = truckTour.get(i_a-1);
		Point next_a = truckTour.get(i_a+1);
		
		Point b_drone_node = db.getDrone_node();
		Point b_re_node= db.getRendezvous_node();
		Point prev_b = truckTour.get(i_b-1);
		Point next_b = truckTour.get(i_b+1);
		
		LOGGER.LOGGER.log(Level.INFO,"a = "+a.toString()+" b = "+b.toString());
		double prev_cost = tspd.cost(prev_a,a) + tspd.cost(a,next_a)+tspd.cost(da)
				+tspd.cost(prev_b,b)+tspd.cost(b,next_b)+tspd.cost(db);
		
		double new_cost = tspd.cost(prev_a,b)+tspd.cost(b,next_a)+tspd.cost(b,a_drone_node,a_re_node)+
				+tspd.cost(prev_b,a)+tspd.cost(a,next_b)+tspd.cost(a,b_drone_node,b_re_node);
		
		if(new_cost < prev_cost){
			truckTour.remove(i_a);
			truckTour.add(i_a,b);
			truckTour.remove(i_b);
			truckTour.add(i_b,a);
			da.setLauch_node(b);
			db.setLauch_node(a);
			if(tspd.checkConstraint(tspd_tour)){
				LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR="+tspd_tour.toString());
				return tspd_tour;
			}
			truckTour.remove(i_a);
			truckTour.add(i_a,a);
			truckTour.remove(i_b);
			truckTour.add(i_b,b);
			da.setLauch_node(a);
			db.setRendezvous_node(b);
			LOGGER.LOGGER.log(Level.INFO,"don't satisfy drone constraint, tspd-input"+tspd_tour.toString());
			return null;
		}
		LOGGER.LOGGER.log(Level.INFO,"don't improve solution, tspd-input"+tspd_tour.toString());
		return null;
	}

	public Tour two_exchange_launch2rendezvous(Tour tspd_tour){
		LOGGER.LOGGER.log(Level.INFO,"tpsd input = "+ tspd_tour.toString());
		
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		
		Random R = new Random();
		
		DroneDelivery da;
		Point a;
		int i_a;
		
		DroneDelivery db;
		Point b;
		int i_b;
		
		do{
			int i_da = R.nextInt(droneDeliveries.size());
			int i_db = R.nextInt(droneDeliveries.size());
			
			int it = 0;
			while(i_da == i_db && it != droneDeliveries.size()){
				i_da = R.nextInt(droneDeliveries.size());
				i_db = R.nextInt(droneDeliveries.size());
				it++;
			}
			
			if(it >= droneDeliveries.size()){
				LOGGER.LOGGER.log(Level.INFO,"Fail to chose point a and point b");
				return null;
			}
			
			da = droneDeliveries.get(i_da);
			a = da.getLauch_node();
			i_a = truckTour.indexOf(a);
			
			db = droneDeliveries.get(i_db);
			b = db.getRendezvous_node();
			i_b = truckTour.indexOf(b);
			
			if(!(i_a == 0 || i_b == truckTour.size()-1))
				break;
			
		}while(true);
		
		Point a_drone_node = da.getDrone_node();
		Point a_re_node = da.getRendezvous_node();
		Point prev_a = truckTour.get(i_a-1);
		Point next_a = truckTour.get(i_a+1);
		
		Point b_drone_node = db.getDrone_node();
		Point b_launch_node= db.getRendezvous_node();
		Point prev_b = truckTour.get(i_b-1);
		Point next_b = truckTour.get(i_b+1);
		
		LOGGER.LOGGER.log(Level.INFO,"a = "+a.toString()+" b = "+b.toString());
		double prev_cost = tspd.cost(prev_a,a) + tspd.cost(a,next_a)+tspd.cost(da)
				+tspd.cost(prev_b,b)+tspd.cost(b,next_b)+tspd.cost(db);
		
		double new_cost = tspd.cost(prev_a,b)+tspd.cost(b,next_a)+tspd.cost(b,a_drone_node,a_re_node)+
				+tspd.cost(prev_b,a)+tspd.cost(a,next_b)+tspd.cost(b_launch_node,b_drone_node,a);
		
		if(new_cost < prev_cost){
			truckTour.remove(i_a);
			truckTour.add(i_a,b);
			truckTour.remove(i_b);
			truckTour.add(i_b,a);
			da.setLauch_node(b);
			db.setRendezvous_node(a);
			if(tspd.checkConstraint(tspd_tour)){
				LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR="+tspd_tour.toString());
				return tspd_tour;
			}
			truckTour.remove(i_a);
			truckTour.add(i_a,a);
			truckTour.remove(i_b);
			truckTour.add(i_b,b);
			da.setLauch_node(a);
			db.setRendezvous_node(b);
			LOGGER.LOGGER.log(Level.INFO,"don't satisfy drone constraint, tspd-input"+tspd_tour.toString());
			return null;
		}
		LOGGER.LOGGER.log(Level.INFO,"don't improve solution, tspd-input"+tspd_tour.toString());
		return null;
	}

	public Tour two_exchange_launch2truckOnly(Tour tspd_tour){
		LOGGER.LOGGER.log(Level.INFO,"tpsd input = "+ tspd_tour.toString());
		
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tspd_tour);
		
		Random R = new Random();
		
		DroneDelivery da;
		Point a;
		int i_a;
		
		Point b;
		int i_b;
		
		do{
			int i_da = R.nextInt(droneDeliveries.size());
			int i_ob = R.nextInt(truckOnlyNodes.size());
			
			da = droneDeliveries.get(i_da);
			a = da.getLauch_node();
			i_a = truckTour.indexOf(a);
			
			b = truckOnlyNodes.get(i_ob);
			i_b = truckTour.indexOf(b);
			
			if(!(i_a == 0 || i_b == truckTour.size()-1 || i_b==0))
				break;
			
		}while(true);
		
		Point a_drone_node = da.getDrone_node();
		Point a_re_node = da.getRendezvous_node();
		Point prev_a = truckTour.get(i_a-1);
		Point next_a = truckTour.get(i_a+1);
		
		Point prev_b = truckTour.get(i_b-1);
		Point next_b = truckTour.get(i_b+1);
		
		LOGGER.LOGGER.log(Level.INFO,"a = "+a.toString()+" b = "+b.toString());
		double prev_cost = tspd.cost(prev_a,a) + tspd.cost(a,next_a)+tspd.cost(da)
				+tspd.cost(prev_b,b)+tspd.cost(b,next_b);
		
		double new_cost = tspd.cost(prev_a,b)+tspd.cost(b,next_a)+tspd.cost(b,a_drone_node,a_re_node)+
				+tspd.cost(prev_b,a)+tspd.cost(a,next_b);
		
		if(new_cost < prev_cost){
			truckTour.remove(i_a);
			truckTour.add(i_a,b);
			truckTour.remove(i_b);
			truckTour.add(i_b,a);
			da.setLauch_node(b);
			if(tspd.checkConstraint(tspd_tour)){
				LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR="+tspd_tour.toString());
				return tspd_tour;
			}
			truckTour.remove(i_a);
			truckTour.add(i_a,a);
			truckTour.remove(i_b);
			truckTour.add(i_b,b);
			da.setLauch_node(a);
			LOGGER.LOGGER.log(Level.INFO,"don't satisfy drone constraint, tspd-input"+tspd_tour.toString());
			return null;
		}
		LOGGER.LOGGER.log(Level.INFO,"don't improve solution, tspd-input"+tspd_tour.toString());
		return null;
	}

	public Tour two_exchange_rendezvous2truckOnly(Tour tspd_tour){
		LOGGER.LOGGER.log(Level.INFO,"tpsd input = "+ tspd_tour.toString());
		
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tspd_tour);
		
		Random R = new Random();
		
		DroneDelivery da;
		Point a;
		int i_a;
		
		Point b;
		int i_b;
		
		do{
			int i_da = R.nextInt(droneDeliveries.size());
			int i_ob = R.nextInt(truckOnlyNodes.size());
			
			
			da = droneDeliveries.get(i_da);
			a = da.getRendezvous_node();
			i_a = truckTour.indexOf(a);
			
			b = truckOnlyNodes.get(i_ob);
			i_b = truckTour.indexOf(b);
			
			if(!(i_a == truckTour.size()-1 || i_b == truckTour.size()-1 || i_b == 0))
				break;
			
		}while(true);
		
		Point a_drone_node = da.getDrone_node();
		Point a_launch_node = da.getLauch_node();
		Point prev_a = truckTour.get(i_a-1);
		Point next_a = truckTour.get(i_a+1);
		
		Point prev_b = truckTour.get(i_b-1);
		Point next_b = truckTour.get(i_b+1);
		
		LOGGER.LOGGER.log(Level.INFO,"a = "+a.toString()+" b = "+b.toString());
		double prev_cost = tspd.cost(prev_a,a) + tspd.cost(a,next_a)+tspd.cost(da)
				+tspd.cost(prev_b,b)+tspd.cost(b,next_b);
		
		double new_cost = tspd.cost(prev_a,b)+tspd.cost(b,next_a)+tspd.cost(a_launch_node,a_drone_node,b)+
				+tspd.cost(prev_b,a)+tspd.cost(a,next_b);
		
		if(new_cost < prev_cost){
			truckTour.remove(i_a);
			truckTour.add(i_a,b);
			truckTour.remove(i_b);
			truckTour.add(i_b,a);
			da.setRendezvous_node(b);
			if(tspd.checkConstraint(tspd_tour)){
				LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR="+tspd_tour.toString());
				return tspd_tour;
			}
			truckTour.remove(i_a);
			truckTour.add(i_a,a);
			truckTour.remove(i_b);
			truckTour.add(i_b,b);
			da.setRendezvous_node(a);
			LOGGER.LOGGER.log(Level.INFO,"don't satisfy drone constraint, tspd-input"+tspd_tour.toString());
			return null;
		}
		LOGGER.LOGGER.log(Level.INFO,"don't improve solution, tspd-input"+tspd_tour.toString());
		return null;
	}
	*/
}
