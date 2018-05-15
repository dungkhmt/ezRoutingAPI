package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.GRASP_Arc;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;
import com.kse.utils.LOGGER;

public class GRASP {
	private TSPD tspd;
	//int nTSP;
	int nDrone;
	
	public GRASP(TSPD tspd, int nDrone){
		this.tspd = tspd;
		this.nDrone = nDrone;
	}
	
	double startTime;
	double timeLimit = 30000;
	public Tour solve(){
		Tour solution_tour = null;
		double bestObjectiveValue = Double.MAX_VALUE;
		TSP tsp = new TSP(tspd.getStartPoint(), tspd.getClientPoints(), tspd.getEndPoint());
		tsp.setDistances_matrix(tspd.getDistancesTruck());
		
		int iteration = 0;
		//nTSP = 500;
		
		startTime = System.currentTimeMillis();
		while((System.currentTimeMillis() - startTime) < timeLimit){
			iteration++;
			//ArrayList<Point> tour = tsp.randomGenerator();
			//ArrayList<Point> tour = tsp.lsInitTSP();
			//ArrayList<Point> tour = tsp.greedyInit();
			ArrayList<Point> tour = tsp.kNearest();
			
			//LOGGER.LOGGER.log(Level.INFO,"it "+iteration+" init tsp_tour cost = "+tspd.cost(0,tour.size()-1,tour));
			
			//startTime = System.currentTimeMillis();
			Tour tspdSolution = split_algorithm(tour);
			//System.out.println("split done");
			
			//LOGGER.LOGGER.log(Level.INFO,"tspd after using split cost = "+tspd.cost(tspdSolution));
			
			//timeLimit = timeLimit - (System.currentTimeMillis()-startTime);
			local_search(tspdSolution);
			//System.out.println("local_search done");
			//local_search_greedy(tspdSolution);
			//LOGGER.LOGGER.log(Level.INFO,"tspd after local_search cost = "+tspd.cost(tspdSolution));
			
			if(tspd.cost(tspdSolution) < bestObjectiveValue){
				solution_tour = tspdSolution;
				bestObjectiveValue = tspd.cost(tspdSolution);
				//iteration = 0;
				//LOGGER.LOGGER.log(Level.INFO,"find bestTour cost = "+tspd.cost(solution_tour)+"    bestObjectiveValue "+bestObjectiveValue);
			}
			//System.out.println("iter "+iteration+" done");
		}
		//LOGGER.LOGGER.log(Level.INFO,"bestTour cost = "+tspd.cost(solution_tour)+"    bestObjectiveValue "+bestObjectiveValue);
		System.out.println("total iter run: "+iteration);
		solution_tour.setTotalCost(tspd.cost(solution_tour));
		return solution_tour;
		//return tspdSolution;
	}
	
	private Point[] P;
	private ArrayList<DroneDelivery> T;
	
	public Tour split_algorithm(ArrayList<Point> tsp_tour){
		
		build_graph(tsp_tour);
		//LOGGER.LOGGER.log(Level.INFO,"T="+T.toString());
		//LOGGER.LOGGER.log(Level.INFO,"GRASP::split_alogrithm:: start");
		
//		String s_p = "P = [";
//		for(int i=0; i<P.length; i++){
//			s_p += (P[i].toString()+", ");
//		}
//		s_p += ("]");
//		
//		LOGGER.LOGGER.log(Level.INFO, s_p);
		
		ArrayList<Point> St = new ArrayList<Point>();
		ArrayList<DroneDelivery> Sd = new ArrayList<DroneDelivery>();
		
		Point pi = tsp_tour.get(tsp_tour.size()-1);
		while(!pi.equals(tsp_tour.get(0))){

			St.add(pi);
			
			int i = tsp_tour.indexOf(pi);
			Point nearest_pi = P[pi.getID()];
			int i_pj = tsp_tour.indexOf(nearest_pi);
			
			//LOGGER.LOGGER.log(Level.INFO," pi = "+pi.toString()+" nearest_pi = "+nearest_pi.toString());
			if(i - i_pj > 1){
				ArrayList<Point> droneNodes = new ArrayList<Point>();
				for(int j=0; j<T.size(); j++){
					DroneDelivery tmp_dd = T.get(j);
					if(tmp_dd.getLauch_node().equals(nearest_pi) && tmp_dd.getRendezvous_node().equals(pi)){
						Point droneNode = tmp_dd.getDrone_node();
						droneNodes.add(droneNode);
						Sd.add(tmp_dd);
						//LOGGER.LOGGER.log(Level.INFO,"add new dd = ["+nearest_pi.toString()+", "+pk.toString()+", "+pi.toString()+"]");
						//LOGGER.LOGGER.log(Level.INFO,"nDroneFromNode["+i_pj+"] = "+nDroneFromNode[i_pj]+" nDroneToNode["+i+"] = "+nDroneToNode[i]);
					}
				}
				
				for(int j = i-1; j > i_pj; j--){
					Point tpi = tsp_tour.get(j);
					if(droneNodes.indexOf(tpi) == -1){
						St.add(tpi);
					}
				}
			}

			pi = nearest_pi;
		}
		
		St.add(tsp_tour.get(0));
		Collections.reverse(St);
		
		TruckTour truck_tour = new TruckTour(St);
		Tour tour = new Tour(truck_tour, Sd);
		//LOGGER.LOGGER.log(Level.INFO,"tour="+tour.toString());
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
		int nBestRemove = 1;
		for(int i=0; i<tsp_tour.size()-2; i++){
			for(int k=i+2; k<tsp_tour.size(); k++){
				Point pi = tsp_tour.get(i);
				Point pk = tsp_tour.get(k);
				//LOGGER.LOGGER.log(Level.INFO,"pi="+pi.toString());
				//LOGGER.LOGGER.log(Level.INFO,"pk="+pk.toString());
				double prev_cost = tspd.cost(i,k,tsp_tour);
				
				int[] minIndex = new int[nBestRemove];
				double[] minCost = new double[nBestRemove];
				for(int iMinIndex = 0; iMinIndex < nBestRemove; iMinIndex++){
					minIndex[iMinIndex] = -1;
					minCost[iMinIndex] = Double.MAX_VALUE;
				}
				
				for(int j=i+1; j<k; j++){
					Point pj = tsp_tour.get(j);
					
					//check Drone constraint
					if(tspd.checkDroneEndurance(pi, pj, pk)){
						Point prev_pj = tsp_tour.get(j-1);
						Point next_pj = tsp_tour.get(j+1);
						double cost = prev_cost - tspd.cost(prev_pj,pj) - tspd.cost(pj,next_pj) 
								+ tspd.cost(prev_pj,next_pj) + tspd.cost(pi,pj,pk);	
						for(int iMinIndex = 0; iMinIndex < nBestRemove; iMinIndex++){
							if(minCost[iMinIndex] > cost){
								for(int jMinIndex = nBestRemove-1; jMinIndex > iMinIndex ; jMinIndex--){
									minCost[jMinIndex] = minCost[jMinIndex-1];
									minIndex[jMinIndex] = minIndex[jMinIndex-1];
								}
								minCost[iMinIndex] = cost;
								minIndex[iMinIndex] = j;
								break;
							}
						}
					}			
				}
				
				List<Integer> lstMinIndex = new ArrayList<Integer>();
				for(int iMinIndex=0; iMinIndex<minIndex.length; iMinIndex++){
					if(minIndex[iMinIndex] != -1){
						lstMinIndex.add(minIndex[iMinIndex]);
					}
				}
				
				double minValue = Double.MAX_VALUE;
				List<Integer> lstIndexDroneNode = new ArrayList<Integer>();
				
				for(int iDrone = 1; iDrone <= nDrone; iDrone++){
					List<List<Integer>> lstIndexDrone = combination(lstMinIndex, iDrone);
					for(int ilst = 0; ilst < lstIndexDrone.size(); ilst++){
						double cost = tspd.evaluateCost(i, lstIndexDrone.get(ilst), k, tsp_tour);
						if(cost < minValue){
							minValue = cost;
							lstIndexDroneNode = lstIndexDrone.get(ilst);
						}
					}
				}
				 
				
				GRASP_Arc arc = new GRASP_Arc(pi, pk, minValue);
				arcs.add(arc);
//				LOGGER.LOGGER.log(Level.INFO,"add arc "+arc.toString());
//				LOGGER.LOGGER.log(Level.INFO,"minValue = "+minValue+"    minIndex="+minIndex.toString());
				if(minValue != Double.MAX_VALUE){
					for(int iMinIndex = 0; iMinIndex < lstIndexDroneNode.size(); iMinIndex++){
						//if(lstIndexDroneNode[iMinIndex] != -1){
							DroneDelivery dd = new DroneDelivery(pi,tsp_tour.get(lstIndexDroneNode.get(iMinIndex)),pk);
							T.add(dd);
						//}
					}
				}
			}
		}
		
		//LOGGER.LOGGER.log(Level.INFO,"compute cost done");
		
		P = new Point[tsp_tour.size()];
		double V[] = new double[tsp_tour.size()];
		V[0] = 0;
		for(int i=1; i<tsp_tour.size(); i++){
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
		
		//int maxIter = 300000;
		//int it = 0;
		
//		Random R = new Random();
		
//		int nOperator = 4;
//		int[] nChoseOperator = new int[nOperator];
//		for(int i=0; i<nOperator; i++){
//			nChoseOperator[i] = 0;
//		}
		
//		double current_cost = tspd.cost(tspdSolution);
//		double new_cost = Double.MIN_VALUE;
		//while(it++ < maxIter || new_cost < current_cost){
		while(true){
			//LOGGER.LOGGER.log(Level.INFO,"iter "+it);
			/*int i = R.nextInt(4);
			nChoseOperator[i]++;
			current_cost = tspd.cost(tspdSolution);
			switch(i){
				case 0: {
					//LOGGER.LOGGER.log(Level.INFO,"prev_cost = "+tspd.cost(tspdSolution));
					relocate_T(tspdSolution);
					//LOGGER.LOGGER.log(Level.INFO,"new_cost = "+tspd.cost(tspdSolution));
					//new_cost = tspd.cost(tspdSolution);
					break;
				}
				case 1: {
					//LOGGER.LOGGER.log(Level.INFO,"prev_cost = "+tspd.cost(tspdSolution));
					relocate_D(tspdSolution);
					//new_cost = tspd.cost(tspdSolution);
					//LOGGER.LOGGER.log(Level.INFO,"new_cost = "+tspd.cost(tspdSolution));
					break;
				}
				case 2: {
					//LOGGER.LOGGER.log(Level.INFO,"prev_cost = "+tspd.cost(tspdSolution));
					remove(tspdSolution);
					//new_cost = tspd.cost(tspdSolution);
					//LOGGER.LOGGER.log(Level.INFO,"new_cost = "+tspd.cost(tspdSolution));
					break;
				}
				case 3: {
					//LOGGER.LOGGER.log(Level.INFO,"prev_cost = "+tspd.cost(tspdSolution));
					two_exchange(tspdSolution);
					//new_cost = tspd.cost(tspdSolution);
					//LOGGER.LOGGER.log(Level.INFO,"new_cost = "+tspd.cost(tspdSolution));
					break;
				}
			}*/
			int it = 0;
			while(it++ < 1000 && !relocate_T(tspdSolution) &&
					!relocate_D(tspdSolution) && 
					!remove(tspdSolution) && 
					!two_exchange(tspdSolution)){}
			//LOGGER.LOGGER.log(Level.INFO,"It = "+it);	
			//System.out.println("It = "+it);
			if(it >= 1000 || (System.currentTimeMillis() - startTime) >= timeLimit){
				break;
			}
		}
		
//		LOGGER.LOGGER.log(Level.INFO,"number chosen operator: relocate_T="+nChoseOperator[0]+" relocate_D="
//		+nChoseOperator[1]+" remove="+nChoseOperator[2]+" two_exchange="+nChoseOperator[3]);
	}
	
	public void local_search_greedy(Tour tspdSolution){
		while(greedy_relocate_T(tspdSolution) ||
				greedy_relocate_D(tspdSolution) || 
				greedy_remove(tspdSolution) || 
				greedy_two_exchange(tspdSolution)){}
	}
	
	public boolean relocate_T(Tour tspd_tour){
		//LOGGER.LOGGER.log(Level.INFO,"chose relocate_T, tspd input = "+tspd_tour.toString()+"\ncost = "+tspd.cost(tspd_tour));
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tspd_tour);
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		
		if(truckOnlyNodes.size() == 0 || truckOnlyNodes == null){
			//LOGGER.LOGGER.log(Level.INFO,"truckOnlyNodes size = 0");
			return false;
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
			return false;
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
				//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR cost="+tspd.cost(tspd_tour));
				//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR improve cost = "+(new_cost-prev_cost));
				return true;
			}
			//reverse input 
			truckTour.remove(iInsert);
			truckTour.add(pRelocation,a);
			//LOGGER.LOGGER.log(Level.INFO,"checkConstraint false");
			return false;
		}	
		//LOGGER.LOGGER.log(Level.INFO,"apply this operation not improve");
		return false;
	}
	
	public boolean relocate_D(Tour tspd_tour){
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
			return false;
		}
		
		Point i = truckTour.get(i_i);
		Point k = truckTour.get(i_k);
		//LOGGER.LOGGER.log(Level.INFO,"i = "+i.toString()+" k = "+k.toString()+" a = "+a.toString());
		
		//check how many drone launch from point i and not rendezvous at point before point k
		
		int nDroneFlying = tspd.countDroneFlying(i_i, i_k, tspd_tour);
		if(nDroneFlying >= nDrone)
			return false;
		
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
					//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR cost = "+tspd.cost(tspd_tour));
					return true;
				}
				truckTour.add(i_a,a);
				//LOGGER.LOGGER.log(Level.INFO,"(i,a,k) is not satisfy drone constraint");
				return false;
			}
			//LOGGER.LOGGER.log(Level.INFO,"relocate a (truck only node) not imporve");
			return false;
		}
		//a is drone node
		else{
			if(tspd.checkDroneConstraint(i, a, k, truckTour)){
				double new_cost = tspd.cost(i,a,k); 
				double prev_cost = tspd.cost(da);
				if(new_cost < prev_cost){
					da.setLauch_node(i);
					da.setRendezvous_node(k);
					//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR cost = "+tspd.cost(tspd_tour));
					return true;
				}
				//LOGGER.LOGGER.log(Level.INFO,"relocate a (drone node) not imporve");
				return false;
			}
			//LOGGER.LOGGER.log(Level.INFO,"(i,a,k) is not satisfy drone constraint");
			return false;
		}
	}
	
	public boolean remove(Tour tspd_tour){
		//LOGGER.LOGGER.log(Level.INFO,"chose remove");
		
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		
		if(droneDeliveries.size() == 0 || droneDeliveries == null){
			//LOGGER.LOGGER.log(Level.INFO,"droneDeliveries size = 0");
			return false;
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
				//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR cost = "+tspd.cost(tspd_tour));
				return true;
			}
			truckTour.remove(j);
			droneDeliveries.add(dj);
			//LOGGER.LOGGER.log(Level.INFO,"remove don't satisfy drone constraint");
			return false;
		}
		
		//LOGGER.LOGGER.log(Level.INFO,"this operator not improve solution");
		return false;
	}
	
	public boolean two_exchange(Tour tspd_tour){
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
				//LOGGER.LOGGER.log(Level.INFO,"NEW_TOUR cost = "+tspd.cost(tspd_tour));
				return true;
			}
			//LOGGER.LOGGER.log(Level.INFO,"don't improve cost");
			return false;
		}
		//LOGGER.LOGGER.log(Level.INFO,"don't satisfy drone constraint");
		return false;
	}

	public boolean greedy_relocate_T(Tour tspd_tour){
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tspd_tour);
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		
		double maxsaving = 0;
		Point pa = null;
		Point pb = null;
		
		for(int i=0; i<truckOnlyNodes.size(); i++){
			Point pi = truckOnlyNodes.get(i);
			int ii = truckTour.indexOf(pi);
			Point prev_pi = truckTour.get(ii-1);
			Point next_pi = truckTour.get(ii+1);
			
			for(int j=1; j<truckTour.size(); j++){
				Point pj = truckTour.get(j);
				if(pi.equals(pj))
					continue;
				Point prev_pj = truckTour.get(j-1);
				
				double prev_cost = tspd.cost(prev_pi, pi) + tspd.cost(pi, next_pi) + tspd.cost(prev_pj, pj);
				double new_cost = tspd.cost(prev_pi,next_pi) + tspd.cost(prev_pj,pi)+ tspd.cost(pi,pj);
				double saving = prev_cost - new_cost;
				
				if(saving > maxsaving){
					truckTour.remove(ii);
					int ij = truckTour.indexOf(pj);
					truckTour.add(ij,pi);
					if(tspd.checkConstraint(tspd_tour)){
						pa = pi;
						pb = pj;
						maxsaving = saving;
					}
					truckTour.remove(ij);
					truckTour.add(ii,pi);
				}	
			}
		}
		
		if(maxsaving != 0){
			int ia = truckTour.indexOf(pa);
			truckTour.remove(ia);
			int ib = truckTour.indexOf(pb);
			truckTour.add(ib,pa);
			//LOGGER.LOGGER.log(Level.INFO,"relocate("+pa.toString()+", "+pb.toString()+") maxsaving = "+maxsaving);
			return true;
		}
		return false;
	}
	
	public boolean greedy_relocate_D(Tour tspd_tour){
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tspd_tour);
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		
		double maxsaving = 0;
		Point best_pa = null;
		Point best_pi = null;
		Point best_pk = null;
		
		for(int a=0; a<truckOnlyNodes.size(); a++){
			Point pa = truckOnlyNodes.get(a);
			int ia = truckTour.indexOf(pa);
			Point prev_a = truckTour.get(ia-1);
			Point next_a = truckTour.get(ia+1);
			
			double prev_cost = tspd.cost(prev_a,pa) + tspd.cost(pa,next_a);
			double new_truck_cost = tspd.cost(prev_a,next_a);
			
			truckTour.remove(ia);
			
			for(int i=0; i<truckTour.size()-1; i++){
				Point pi = truckTour.get(i);
				
				for(int k=i+1; k<truckTour.size(); k++){
					int nDroneFlying = tspd.countDroneFlying(i, k, tspd_tour);
					if(nDroneFlying >= nDrone)
						continue;
					
					Point pk = truckTour.get(k);
					
					double new_cost = new_truck_cost + tspd.cost(pi,pa,pk);
					
					double saving = prev_cost - new_cost;
					
					if(saving > maxsaving){
						if(tspd.checkDroneConstraint(pi, pa, pk, truckTour)){
							best_pa = pa;
							best_pi = pi;
							best_pk = pk;
							maxsaving = saving;
						}
					}
				}
			}
			
			truckTour.add(ia,pa);
		}
		
		DroneDelivery best_dd = null;
		
		for(int a=0; a<droneDeliveries.size(); a++){
			DroneDelivery da = droneDeliveries.get(a);
			Point pa = da.getDrone_node();
			
			double prev_cost = tspd.cost(da);
			
			for(int i=0; i<truckTour.size()-1; i++){
				Point pi = truckTour.get(i);
				for(int k=i+1; k<truckTour.size(); k++){
					int nDroneFlying = tspd.countDroneFlying(i, k, tspd_tour);
					if(nDroneFlying >= nDrone)
						continue;

					Point pk = truckTour.get(k);
					if(tspd.checkDroneConstraint(pi, pa, pk, truckTour)){
						double new_cost = tspd.cost(pi,pa,pk);
						
						double saving = prev_cost - new_cost;
						if(saving > maxsaving){
							best_pi = pi;
							best_pk = pk;
							best_pa = pa;
							best_dd = da;
							saving = maxsaving;
						}
					}
				}
			}
		}
		
		if(maxsaving != 0){
			if(best_dd != null){
				best_dd.setLauch_node(best_pi);
				best_dd.setRendezvous_node(best_pk);
				//LOGGER.LOGGER.log(Level.INFO,"maxsaving = "+maxsaving);
				return true;
			}
			truckTour.remove(best_pa);
			DroneDelivery new_da = new DroneDelivery(best_pi, best_pa, best_pk);
			droneDeliveries.add(new_da);
			//LOGGER.LOGGER.log(Level.INFO,"maxsaving (swap truck to drone)= "+maxsaving);
			return true;
		}
		return false;
	}

	public boolean greedy_remove(Tour tspd_tour){
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		
		double maxsaving = 0;
		DroneDelivery best_dj = null;
		int best_k = -1;
		
		for(int j=0; j<droneDeliveries.size(); j++){
			DroneDelivery dj = droneDeliveries.get(j);
			Point pj = dj.getDrone_node();
			
			for(int k=1; k<truckTour.size(); k++){
				Point pk = truckTour.get(k);
				Point prev_pk = truckTour.get(k-1);
				
				double prev_cost = tspd.cost(dj) + tspd.cost(prev_pk,pk);
				double new_cost = tspd.cost(prev_pk, pj) + tspd.cost(pj,pk);
				double saving = prev_cost - new_cost;
				
				if(saving > maxsaving){
					truckTour.add(k,pj);
					droneDeliveries.remove(dj);
					if(tspd.checkConstraint(tspd_tour)){
						best_k = k;
						best_dj = dj;
						maxsaving = saving;
					}
					truckTour.remove(pj);
					droneDeliveries.add(dj);
				}
			}
		}
		
		if(maxsaving != 0){
			truckTour.add(best_k,best_dj.getDrone_node());
			droneDeliveries.remove(best_dj);
			//LOGGER.LOGGER.log(Level.INFO,"maxsaving = "+maxsaving);
			return true;
		}
		return false;
	}

	public boolean greedy_two_exchange(Tour tspd_tour){
		ArrayList<Point> truckTour = tspd_tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tspd_tour.getDD();
		
		ArrayList<Point> allPoints = new ArrayList<Point>();
		
		for(int i=1; i<truckTour.size()-1; i++){
			allPoints.add(truckTour.get(i));
		}
		
		for(int i=0; i<droneDeliveries.size(); i++){
			DroneDelivery dd = droneDeliveries.get(i);
			allPoints.add(dd.getDrone_node());
		}
		
		double prev_cost = tspd.cost(tspd_tour);
		TruckTour best_TD = null;
		ArrayList<DroneDelivery> best_DD = null;
		double maxsaving = 0;
		
		for(int a=0; a < allPoints.size()-1; a++){
			Point pa = allPoints.get(a);
			for(int b = a+1; b < allPoints.size(); b++){
				Point pb = allPoints.get(b);
				
				Tour tspd_tour_copy = copySolution(tspd_tour);
				ArrayList<Point> truckTour_copy = tspd_tour_copy.getTD().getTruck_tour();
				ArrayList<DroneDelivery> droneDeliveries_copy = tspd_tour_copy.getDD();
				
				for(int i=0; i<truckTour_copy.size(); i++){
					Point pi = truckTour_copy.get(i);
					if(pi.equals(pa)){
						truckTour_copy.remove(i);
						truckTour_copy.add(i, pb);
					}else if(pi.equals(pb)){
						truckTour_copy.remove(i);
						truckTour_copy.add(i,pa);
					}
				}
				
				for(int i=0; i<droneDeliveries_copy.size(); i++){
					DroneDelivery dd = droneDeliveries_copy.get(i);
					
					Point launch_node = dd.getLauch_node();
					Point drone_node = dd.getDrone_node();
					Point rendezvous_node = dd.getRendezvous_node();
					
					if(launch_node.equals(pa)){
						dd.setLauch_node(pb);
					}else if(launch_node.equals(pb)){
						dd.setLauch_node(pa);
					}
					
					if(drone_node.equals(pa)){
						dd.setDrone_node(pb);
					}else if(drone_node.equals(pb)){
						dd.setDrone_node(pa);
					}
					
					if(rendezvous_node.equals(pa)){
						dd.setRendezvous_node(pb);
					}else if(rendezvous_node.equals(pb)){
						dd.setRendezvous_node(pa);
					}
				}
				
				//LOGGER.LOGGER.log(Level.INFO,"tspd_tour_copy = "+tspd_tour_copy.toString());
				
				if(tspd.checkConstraint(tspd_tour_copy)){
					double new_cost = tspd.cost(tspd_tour_copy);
					double saving = prev_cost - new_cost;
					if(saving > maxsaving){
						best_TD = tspd_tour_copy.getTD();
						best_DD = tspd_tour_copy.getDD();
						maxsaving = saving;
					}
				
				}
			}
		}
		
		if(maxsaving != 0){
			tspd_tour.setTD(best_TD);
			tspd_tour.setDD(best_DD);
			//LOGGER.LOGGER.log(Level.INFO,"maxsaving = "+maxsaving);
			return true;
		}
		return false;
	}
	
	public Tour copySolution(Tour old){
		ArrayList<Point> truckTour = old.getTD().getTruck_tour();
		ArrayList<DroneDelivery> dd = old.getDD();
		
		ArrayList<Point> new_truckTour = new ArrayList<Point>();
		ArrayList<DroneDelivery> new_dd = new ArrayList<DroneDelivery>();
		
		for(int i=0; i<truckTour.size(); i++){
			new_truckTour.add(truckTour.get(i));
		}
		
		for(int i=0; i<dd.size(); i++){
			new_dd.add(dd.get(i));
		}
		
		return new Tour(new TruckTour(new_truckTour), new_dd);
	}
}