package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;
import com.kse.utils.LOGGER;

public class ALNS {
	
	private TSPD tspd;
	//private int nTSP;
	//private int nDrone;
	
	private ArrayList<Point> rejectedPoint;
	private Tour tour;
	
	private int nRemovalOperators = 7;
	private int nInsertionOperators = 6;
	
	private int lower_removal;
	private int upper_removal;
	private double temperature = 200;
	private double cooling_rate = 0.9995;
	private int sigma1 = 5;
	private int sigma2 = 3;
	private int sigma3 = 1;
	private int nw = 100;
	private double rp = 0.1;
	private int maxNbRROp = 20;
	private double shaw1 = 0.5;
	private double shaw2 = 0.5;
	
	private HashMap<Point, Integer> nbUsedByRRop;
	private HashMap<Point, Double> historicalCost;
	private HashMap<Point, Integer> nbRemoved;
	
	public ALNS(TSPD tspd){
		this.tspd = tspd;
		//this.nDrone = nDrone;
		lower_removal = 2*tspd.getClientPoints().size()/100;
		upper_removal = 25*tspd.getClientPoints().size()/100;
	}
	
	public Tour init(){
		TSP tsp = new TSP(tspd.getStartPoint(), tspd.getClientPoints(), tspd.getEndPoint());
		tsp.setDistances_matrix(tspd.getDistancesTruck());
		
		//ArrayList<Point> tsp_solution = tsp.lsInitTSP();
		//ArrayList<Point> tsp_solution = tsp.randomGenerator();
		//ArrayList<Point> tsp_solution = tsp.greedyInit();
		//ArrayList<Point> tsp_solution = tsp.kNearest(5);
		ArrayList<Point> tsp_solution = tsp.kCheapest();
		
		//LOGGER.LOGGER.log(Level.INFO,"tsp init = "+tsp_solution.toString());
		//System.out.println("tsp init = "+tsp_solution.toString());
		/*
		 * init historical cost (served historical removal operator)
		 * and number removed of each point
		 */
		nbRemoved = new HashMap<Point, Integer>();
		historicalCost = new HashMap<Point, Double>();
		for(int i=1; i<tsp_solution.size()-1; i++){
			Point pi = tsp_solution.get(i);
			
			nbRemoved.put(pi, 0);
			Point prev_pi = tsp_solution.get(i-1);
			Point next_pi = tsp_solution.get(i+1);
			
			double cost = tspd.cost(prev_pi,pi) + tspd.cost(pi,next_pi);
			
			historicalCost.put(pi,cost);
		}
		
		TruckTour init_truckTour = new TruckTour(tsp_solution);
		ArrayList<DroneDelivery> init_DD = new ArrayList<DroneDelivery>();
		
		tour  = new Tour(init_truckTour, init_DD);
		rejectedPoint = new ArrayList<Point>();
		
		return tour;
		//LOGGER.LOGGER.log(Level.INFO,"init done");
	}
	
	public Tour search(int maxIter, double timeLimit){
		//insertion operators selection probabilities
		double[] pti = new double[nInsertionOperators];
		//removal operators selection probabilities
		double[] ptd = new double[nRemovalOperators];
		
		//wi - number of times used during last nw iteration
		int[] wi = new int[nInsertionOperators];
		int[] wd = new int[nRemovalOperators];
		
		//pi_i - score of operator
		int[] si = new int[nInsertionOperators];
		int[] sd = new int[nRemovalOperators];
		
		
		//init probabilites
		for(int i=0; i<nInsertionOperators; i++){
			pti[i] = 1.0/nInsertionOperators;
			wi[i] = 1;
			si[i] = 0;
		}
		for(int i=0; i<nRemovalOperators; i++){
			ptd[i] = 1.0/nRemovalOperators;
			wd[i] = 1;
			sd[i] = 0;
		}
		
		double best_cost = tspd.cost(tour);
		Tour best_solution = copySolution(tour);
		
		//LOGGER.LOGGER.log(Level.INFO,"start search best_cost = "+best_cost);
		
		//Init number used by random and for-bidden random operator of each point
		reset_number_used_byRROp();
		
		int it = 0;
		double start_search_time = System.currentTimeMillis();
		while((System.currentTimeMillis()-start_search_time) < timeLimit && it++ < maxIter){
			
			Tour current_solution = copySolution(tour);
			double current_cost = tspd.cost(tour);
			ArrayList<Point> current_rejectPoint = new ArrayList<Point>();
			for(int i=0; i<rejectedPoint.size(); i++){
				current_rejectPoint.add(rejectedPoint.get(i));
			}
			
			//LOGGER.LOGGER.log(Level.INFO, "it "+it+" current_cost ="+current_cost+" current_rejectPoint = "+current_rejectPoint.size());
			
			int i_selected_removal = get_operator(ptd);
			//int i_selected_removal = 5;
			//LOGGER.LOGGER.log(Level.INFO,"it "+ it+ " select removal "+i_selected_removal);
			wd[i_selected_removal]++;
			
			switch(i_selected_removal){
				case 0: random_removal(); break;
				case 1: forbidden_random_removal(); break;
				case 2: max_saving_removal(); break;
				case 3: worst_distance_removal(); break;
				case 4: historical_removal(); break;
				case 5: shaw_removal(); break;
				case 6: tabu_removal(); break;
			}
			
			int i_selected_insertion = get_operator(pti);
			//int i_selected_insertion = 0;
			//LOGGER.LOGGER.log(Level.INFO,"it "+ it+ " select insertion "+i_selected_insertion);
			wi[i_selected_insertion]++;
			switch(i_selected_insertion){
				case 0: greedy_insertion(); break;
				case 1: second_best_insertion(); break;
				case 2: greedy_noise_insertion(); break;
				case 3: second_best_noise_insertion(); break;
				case 4: regret_n_insertion(2); break;
				case 5: regret_n_insertion(3); break;
			}
			
			double new_cost = tspd.cost(tour);
			
			//LOGGER.LOGGER.log(Level.INFO, "it "+it+" new_cost = "+tspd.cost(tour)+" new_rejectPoint = "+rejectedPoint.size());
			
			if(rejectedPoint.size() == 0 && new_cost < current_cost){
				//current_solution = tour;
				if(new_cost < best_cost){
					sd[i_selected_removal] += sigma1;
					si[i_selected_insertion] += sigma1;
					
					best_cost = new_cost;
					best_solution = copySolution(tour);
					//LOGGER.LOGGER.log(Level.INFO, "it "+it+" find best best_cost = "+best_cost +" rejectedPoint = "+rejectedPoint.size());
				}else{
					sd[i_selected_removal] += sigma2;
					si[i_selected_insertion] += sigma2;
				}
			}else{
				sd[i_selected_removal] += sigma3;
				si[i_selected_insertion] += sigma3;
				
				double v = Math.exp(-(new_cost - current_cost)/temperature);
				double r = Math.random();
				
				if(rejectedPoint.size() > 0 || r >= v){
					//LOGGER.LOGGER.log(Level.INFO,"r >=v back to prev solution");
					tour = current_solution;
					rejectedPoint = current_rejectPoint;
				}
			}
			
			temperature = cooling_rate * temperature;
			
			if(it % nw == 0){
				reset_number_used_byRROp();
				String nbi = "nbUsedInseriton = [";
				String nbd = "nbUsedRemoval = [";
				for(int i=0; i<nInsertionOperators; i++){
					nbi += (wi[i]+", ");
					pti[i] = Math.max(0, pti[i]*(1-rp) + rp*si[i]/wi[i]);
					wi[i] = 1;
					si[i] = 0;
				}
				
				for(int i=0; i<nRemovalOperators; i++){
					nbd += (wd[i]+", ");
					ptd[i] = Math.max(0, ptd[i]*(1-rp) + rp*sd[i]/wd[i]);
					wd[i] = 1;
					sd[i] = 0;
				}
				//LOGGER.LOGGER.log(Level.INFO,"after "+it+" iter\n"+nbi+"\n"+nbd);
			}
		}
		best_solution.setTotalCost(tspd.cost(best_solution));
		//int nClients = best_solution.getTD().getTruck_tour().size()+ best_solution.getDD().size();
		//LOGGER.LOGGER.log(Level.INFO,"search done best cost = "+tspd.cost(best_solution)+ " nPoint = "+nClients);;
		return best_solution;
	}

	private void reset_number_used_byRROp(){
		ArrayList<Point> clientsPoint = tspd.getClientPoints();
		
		nbUsedByRRop = new HashMap<Point, Integer>();
		
		for(int i=0; i<clientsPoint.size(); i++){
			nbUsedByRRop.put(clientsPoint.get(i), 0);
		}
	}
	
	private void random_removal(){
		Random R = new Random();
		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
		
		//LOGGER.LOGGER.log(Level.INFO,"nRemove = "+nRemove);
		
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		
		for(int i=1; i<truckTour.size()-1; i++){
			clientPoints.add(truckTour.get(i));
		}
		
		for(int i=0; i<droneDeliveries.size(); i++){
			clientPoints.add(droneDeliveries.get(i).getDrone_node());
		}
		
		Collections.shuffle(clientPoints);
		
		for(int i=0; i<clientPoints.size() && i<nRemove; i++){
			Point pi = clientPoints.get(i);
			//LOGGER.LOGGER.log(Level.INFO," chose "+pi.toString());
			
			if(!tspd.evaluateRemove(pi, tour))
				continue;
			
			for(int j=1; j<truckTour.size()-1; j++){
				Point pj = truckTour.get(j);
				if(pj.equals(pi)){
					//LOGGER.LOGGER.log(Level.INFO," pi is truck node");
					truckTour.remove(pj);
					rejectedPoint.add(pi);
					nbUsedByRRop.put(pj, (nbUsedByRRop.get(pj)+1));
					nbRemoved.put(pj, nbRemoved.get(pj)+1);
					break;
				}
			}
			
			for(int j=0; j<droneDeliveries.size(); j++){
				DroneDelivery dj = droneDeliveries.get(j);
				
				Point ldj = dj.getLauch_node();
				Point ddj = dj.getDrone_node();
				Point rdj = dj.getRendezvous_node();
				
				if(ldj.equals(pi) || ddj.equals(pi) || rdj.equals(pi)){
					rejectedPoint.add(ddj);
					nbUsedByRRop.put(ddj, (nbUsedByRRop.get(ddj)+1));
					nbRemoved.put(ddj, nbRemoved.get(ddj)+1);
					droneDeliveries.remove(dj);
					//LOGGER.LOGGER.log(Level.INFO," pi is drone Node");
					j--;
				}
			}
		}
		//LOGGER.LOGGER.log(Level.INFO," after remove "+tour.toString());
	}
	
	private void forbidden_random_removal(){
		Random R = new Random();
		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
		
		//LOGGER.LOGGER.log(Level.INFO,"nRemove = "+nRemove);
		
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		
		for(int i=1; i<truckTour.size()-1; i++){
			clientPoints.add(truckTour.get(i));
		}
		
		for(int i=0; i<droneDeliveries.size(); i++){
			clientPoints.add(droneDeliveries.get(i).getDrone_node());
		}
		
		Collections.shuffle(clientPoints);
		
		for(int i=0; i<clientPoints.size() && i<nRemove; i++){
			Point pi = clientPoints.get(i);
			//LOGGER.LOGGER.log(Level.INFO," chose "+pi.toString());
			if(nbUsedByRRop.get(pi) > maxNbRROp)
				continue;
			
			if(!tspd.evaluateRemove(pi, tour))
				continue;
			
			for(int j=0; j<truckTour.size(); j++){
				Point pj = truckTour.get(j);
				if(pj.equals(pi)){
					//LOGGER.LOGGER.log(Level.INFO," pi is truck node");
					truckTour.remove(pj);
					rejectedPoint.add(pi);
					nbRemoved.put(pi, nbRemoved.get(pi)+1);
					nbUsedByRRop.put(pj, (nbUsedByRRop.get(pj)+1));
					break;
				}
			}
			
			for(int j=0; j<droneDeliveries.size(); j++){
				DroneDelivery dj = droneDeliveries.get(j);
				
				Point ldj = dj.getLauch_node();
				Point ddj = dj.getDrone_node();
				Point rdj = dj.getRendezvous_node();
				
				if(ldj.equals(pi) || ddj.equals(pi) || rdj.equals(pi)){
					rejectedPoint.add(ddj);
					nbRemoved.put(ddj, nbRemoved.get(ddj)+1);
					nbUsedByRRop.put(ddj, (nbUsedByRRop.get(ddj)+1));
					droneDeliveries.remove(dj);
					//LOGGER.LOGGER.log(Level.INFO," pi is drone Node");
					j--;
				}
			}
		}
		//LOGGER.LOGGER.log(Level.INFO," after remove "+tour.toString());
	}
	
	private void max_saving_removal(){
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		Random R = new Random();
		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
		
		int iNRemove = 0;
		while(iNRemove++ != nRemove){
			
			double max_saving = Double.MIN_VALUE;
			int i_removed = -1;
			for(int i=1; i<truckTour.size()-1; i++){
				Point pi = truckTour.get(i);
				if(tspd.evaluateRemove(pi, tour)){
					Point prev_pi = truckTour.get(i-1);
					Point next_pi = truckTour.get(i+1);
					
					double cost = tspd.cost(prev_pi,next_pi) - (tspd.cost(prev_pi,pi)+tspd.cost(pi,next_pi));
					if(cost < max_saving){
						max_saving = cost;
						i_removed = i;
					}
				}
			}
			
			if(i_removed != -1){
				Point pi = truckTour.get(i_removed);
				truckTour.remove(i_removed);
				rejectedPoint.add(pi);
				nbRemoved.put(pi, nbRemoved.get(pi)+1);
				
				for(int j=0; j<droneDeliveries.size(); j++){
					DroneDelivery dj = droneDeliveries.get(j);
					
					Point ldj = dj.getLauch_node();
					Point ddj = dj.getDrone_node();
					Point rdj = dj.getRendezvous_node();
					
					if(ldj.equals(pi) || rdj.equals(pi)){
						rejectedPoint.add(ddj);
						nbRemoved.put(ddj, nbRemoved.get(ddj)+1);
						droneDeliveries.remove(dj);
						//LOGGER.LOGGER.log(Level.INFO," pi is drone Node");
						j--;
					}
				}
			}
		}
	}
	
	private void worst_distance_removal(){
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		//LOGGER.LOGGER.log(Level.INFO, "tour = "+tour.toString());
		
		Random R = new Random();
		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
		int iNRemove = 0;
		
		while(iNRemove++ != nRemove){
			double maxCost = Double.MIN_VALUE;
			Point best_pi = null;
			
			for(int i=1; i<truckTour.size()-1; i++){
				Point pi = truckTour.get(i);
				Point prev_pi = truckTour.get(i-1);
				Point next_pi = truckTour.get(i+1);
				
				double cost = tspd.cost(prev_pi, pi) + tspd.cost(pi,next_pi);
				if(cost > maxCost){
					if(tspd.evaluateRemove(pi, tour)){
						maxCost = cost;
						best_pi = pi;
					}
				}
			}
			
			if(best_pi != null){
				truckTour.remove(best_pi);
				//LOGGER.LOGGER.log(Level.INFO,"remove " + best_pi.toString());
				rejectedPoint.add(best_pi);
				nbRemoved.put(best_pi, nbRemoved.get(best_pi)+1);
				for(int j=0; j<droneDeliveries.size(); j++){
					DroneDelivery dj = droneDeliveries.get(j);
					
					Point ldj = dj.getLauch_node();
					Point ddj = dj.getDrone_node();
					Point rdj = dj.getRendezvous_node();
					
					if(ldj.equals(best_pi) || rdj.equals(best_pi)){
						rejectedPoint.add(ddj);
						nbRemoved.put(ddj, nbRemoved.get(ddj)+1);
						droneDeliveries.remove(dj);
						//LOGGER.LOGGER.log(Level.INFO," pi is drone Node");
						j--;
					}
				}
			}
		}
		//LOGGER.LOGGER.log(Level.INFO, "new_tour = "+tour.toString());
	}

	private void historical_removal(){
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		//LOGGER.LOGGER.log(Level.INFO, "tour = "+tour.toString());
		Random R = new Random();
		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
		int iNRemove = 0;
		
		while(iNRemove++ != nRemove){
			double maxCost = 0;
			Point best_pi = null;
			
			for(int i=1; i<truckTour.size()-1; i++){
				Point pi = truckTour.get(i);
				Point prev_pi = truckTour.get(i-1);
				Point next_pi = truckTour.get(i+1);
				
				double cost = tspd.cost(prev_pi,pi) + tspd.cost(pi,next_pi);
				double historical_cost = historicalCost.get(pi);
				
				if(cost - historical_cost >= maxCost){
					if(tspd.evaluateRemove(pi, tour)){
						maxCost = cost - historical_cost;
						best_pi = pi;
					}
				}
			}
			
			DroneDelivery best_dd = null;
			for(int i=0; i<droneDeliveries.size(); i++){
				Point di = droneDeliveries.get(i).getDrone_node();
				double cost = tspd.cost(droneDeliveries.get(i));
				double historical_cost = historicalCost.get(di);
				
				if(cost - historical_cost > maxCost){
					maxCost = cost - historical_cost;
					best_dd = droneDeliveries.get(i);
				}
			}
			
			if(best_dd != null){
				//LOGGER.LOGGER.log(Level.INFO, "remove "+best_dd.toString());
				droneDeliveries.remove(best_dd);
				rejectedPoint.add(best_dd.getDrone_node());
				nbRemoved.put(best_dd.getDrone_node(), nbRemoved.get(best_dd.getDrone_node())+1);
			}else{
				if(best_pi != null){
					rejectedPoint.add(best_pi);
					//LOGGER.LOGGER.log(Level.INFO, "remove "+best_pi.toString());
					nbRemoved.put(best_pi, nbRemoved.get(best_pi)+1);
					truckTour.remove(best_pi);
					for(int j=0; j<droneDeliveries.size(); j++){
						DroneDelivery dj = droneDeliveries.get(j);
						
						Point ldj = dj.getLauch_node();
						Point ddj = dj.getDrone_node();
						Point rdj = dj.getRendezvous_node();
						
						if(ldj.equals(best_pi) || ddj.equals(best_pi) || rdj.equals(best_pi)){
							rejectedPoint.add(ddj);
							nbRemoved.put(ddj, nbRemoved.get(ddj)+1);
							droneDeliveries.remove(dj);
							//LOGGER.LOGGER.log(Level.INFO," pi is drone Node");
							j--;
						}
					}
				}
			}
		}
		//LOGGER.LOGGER.log(Level.INFO, "new_tour = "+tour.toString());
	}

	private void shaw_removal(){
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		ArrayList<Point> truckOnlyNodes = tspd.getTruckOnlyNodes(tour);
		ArrayList<Point> droneNodes = tspd.getDroneNodes(tour);
		
		//LOGGER.LOGGER.log(Level.INFO, "tour = "+tour.toString());
		
		Random R = new Random();
		//System.out.println("up = "+upper_removal+" low = "+lower_removal);
		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
		
		if(truckTour.size() <= 2)
			return;
		
		int i_r1 = R.nextInt(truckTour.size()-2) + 1;
		Point r1 = truckTour.get(i_r1);
		int it_same = 1;
		while(!tspd.evaluateRemove(r1, tour) && it_same++ < truckTour.size()*10){
			i_r1 = R.nextInt(truckTour.size()-2) + 1;
			r1 = truckTour.get(i_r1);
		}
		
		if(it_same >= truckTour.size()*10)
			return;
		
		truckTour.remove(i_r1);
		rejectedPoint.add(r1);
		nbRemoved.put(r1, nbRemoved.get(r1)+1);
		for(int j=0; j<droneDeliveries.size(); j++){
			DroneDelivery dj = droneDeliveries.get(j);
			Point ldj = dj.getLauch_node();
			Point ddj = dj.getDrone_node();
			Point rdj = dj.getRendezvous_node();
			
			if(ldj.equals(r1) || ddj.equals(r1) || rdj.equals(r1)){
				rejectedPoint.add(ddj);
				nbRemoved.put(ddj, nbRemoved.get(ddj)+1);
				droneDeliveries.remove(dj);
				j--;
			}
		}
		
		int iNRemove = 0;
		while(iNRemove++ != nRemove){
			
			boolean r1isTruckOnlyNode = truckOnlyNodes.contains(r1);
			boolean r1isDroneNode = droneNodes.contains(r1);
			
			double minCost = Double.MAX_VALUE;
			Point r2 = null;
		
			for(int i=1; i<truckTour.size()-1; i++){
				Point pi = truckTour.get(i);
				if(pi.equals(r1))
					continue;
				
				int l_r1_pi = 0;
				if(r1isDroneNode){
					l_r1_pi = 1;
				}else{
					if(truckOnlyNodes.contains(pi) && r1isTruckOnlyNode){
						l_r1_pi = -2;
					}else{
						l_r1_pi = -1;
					}
				}
				
				double cost = shaw1*tspd.d_truck(r1, pi) + shaw2*l_r1_pi;
				if(cost < minCost){
					if(tspd.evaluateRemove(pi, tour)){
						minCost = cost;
						r2 = pi;
					}
				}
			}
			
			DroneDelivery dd_r2 = null;
			for(int i=0; i<droneDeliveries.size(); i++){
				DroneDelivery dd = droneDeliveries.get(i);
				Point di = dd.getDrone_node();
				
				int l_r1_di = 0;
				if(r1isDroneNode){
					l_r1_di = 2;
				}else{
					l_r1_di = 1;
				}
				
				double cost = shaw1*tspd.d_drone(r1, di) + shaw2*l_r1_di;
				if(cost < minCost){
					minCost = cost;
					dd_r2 = dd;
					r2 = di;
				}
			}
			
			if(dd_r2 != null){
				droneDeliveries.remove(dd_r2);
				//LOGGER.LOGGER.log(Level.INFO, "remove "+dd_r2.toString());
				rejectedPoint.add(dd_r2.getDrone_node());
				nbRemoved.put(dd_r2.getDrone_node(), nbRemoved.get(dd_r2.getDrone_node())+1);
				r1 = r2;
				
			}else{
				if(r2 != null){
					rejectedPoint.add(r2);
					//LOGGER.LOGGER.log(Level.INFO, "remove r2 = "+r2.toString());
					nbRemoved.put(r2, nbRemoved.get(r2)+1);
					truckTour.remove(r2);
					for(int j=0; j<droneDeliveries.size(); j++){
						DroneDelivery dj = droneDeliveries.get(j);
						//LOGGER.LOGGER.log(Level.INFO,"dj = "+dj.toString());
						Point ldj = dj.getLauch_node();
						Point ddj = dj.getDrone_node();
						Point rdj = dj.getRendezvous_node();
						
						if(ldj.equals(r2) || ddj.equals(r2) || rdj.equals(r2)){
							rejectedPoint.add(ddj);
							nbRemoved.put(ddj, nbRemoved.get(ddj)+1);
							droneDeliveries.remove(dj);
							//LOGGER.LOGGER.log(Level.INFO,"remove "+dj.toString());
							j--;
						}
					}
					r1 = r2;
				}
			}
		}
		//LOGGER.LOGGER.log(Level.INFO,"new_tour = "+tour.toString());
	}

	private void tabu_removal(){
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		Random R = new Random();
		int nRemove = R.nextInt(upper_removal-lower_removal+1) + lower_removal;
		
		List<Map.Entry<Point, Integer>> list =
                new LinkedList<Map.Entry<Point, Integer>>(nbRemoved.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Point, Integer>>() {
            public int compare(Map.Entry<Point, Integer> o1,
                               Map.Entry<Point, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        
        int i_nRemove = 0;
        for(Map.Entry<Point, Integer> entry : list){
        	if(i_nRemove++ == nRemove)
        		break;
        	
        	Point pi = entry.getKey();
        	if(!tspd.evaluateRemove(pi, tour))
				continue;
			
			for(int j=0; j<truckTour.size(); j++){
				Point pj = truckTour.get(j);
				if(pj.equals(pi)){
					//LOGGER.LOGGER.log(Level.INFO," pi is truck node");
					truckTour.remove(pj);
					rejectedPoint.add(pi);
					nbRemoved.put(pj, nbRemoved.get(pj)+1);
					break;
				}
			}
			
			for(int j=0; j<droneDeliveries.size(); j++){
				DroneDelivery dj = droneDeliveries.get(j);
				
				Point ldj = dj.getLauch_node();
				Point ddj = dj.getDrone_node();
				Point rdj = dj.getRendezvous_node();
				
				if(ldj.equals(pi) || ddj.equals(pi) || rdj.equals(pi)){
					rejectedPoint.add(ddj);
					nbRemoved.put(ddj, nbRemoved.get(ddj)+1);
					droneDeliveries.remove(dj);
					//LOGGER.LOGGER.log(Level.INFO," pi is drone Node");
					j--;
				}
			}
        }
	}
	
	private void greedy_insertion(){
		
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		for(int i=0; i<rejectedPoint.size(); i++){
			Point pj = rejectedPoint.get(i);
			double best_cost = Double.MAX_VALUE;
			Point best_pi = null;
			Point best_pk = null;
			
			for(int j=0; j<truckTour.size()-1; j++){
				Point pi = truckTour.get(j);
				for(int k = j+1; k<truckTour.size(); k++){
					Point pk = truckTour.get(k);
					double cost = tspd.evaluateInsertasDrone(j, pj, k, tour);
					if(cost < best_cost){
						best_cost = cost;
						best_pi = pi;
						best_pk = pk;
					}
				}
			}
			
			int best_i_truck = -1;
			for(int j=1; j<truckTour.size(); j++){
				Point pi = truckTour.get(j);
				double cost = tspd.evaluateInsertasTruck(pi, pj, tour);
				if(cost < best_cost){
					best_cost = cost;
					best_i_truck = j;
				}
			}
			
			if(best_i_truck != -1){
				truckTour.add(best_i_truck,pj);
				//LOGGER.LOGGER.log(Level.INFO,"insert "+pj.toString()+" before "+truckTour.get(best_i_truck));
				rejectedPoint.remove(pj);
				
				/*
				 * Update historical cost
				 */
				Point historical_prev = truckTour.get(best_i_truck-1);
				Point historical_next = truckTour.get(best_i_truck+1);
				
				double historical_new_cost = tspd.cost(historical_prev,pj) + tspd.cost(pj,historical_next); 
				double historical_best_cost = historicalCost.get(pj);
				if(historical_new_cost < historical_best_cost){
					historicalCost.put(pj, historical_new_cost);
				}
				
				i--;
			}else{
				if(best_pi != null && best_pk != null){
					DroneDelivery new_dd = new DroneDelivery(best_pi,pj,best_pk);
					//LOGGER.LOGGER.log(Level.INFO,"new DroneDelivery( "+best_pi.toString()+", "+pj.toString()+", "+best_pk.toString());
					droneDeliveries.add(new_dd);
					rejectedPoint.remove(pj);
					
					/*
					 * Update historical cost
					 */
					double historical_new_cost = tspd.cost(new_dd); 
					double historical_best_cost = historicalCost.get(pj);
					if(historical_new_cost < historical_best_cost){
						historicalCost.put(pj, historical_new_cost);
					}
					
					i--;
				}
			}
		}
		//LOGGER.LOGGER.log(Level.INFO," after insert "+tour.toString());
	}
	
	private void second_best_insertion(){
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		for(int i=0; i<rejectedPoint.size(); i++){
			Point pj = rejectedPoint.get(i);
			
			double best_cost = Double.MAX_VALUE;
			double second_best_cost = Double.MAX_VALUE;
			Point second_best_pi = null;
			Point second_best_pk = null;
			
			for(int j=0; j<truckTour.size()-1; j++){
				Point pi = truckTour.get(j);
				for(int k = j+1; k<truckTour.size(); k++){
					Point pk = truckTour.get(k);
					double cost = tspd.evaluateInsertasDrone(j, pj, k, tour);
					if(cost <= best_cost){
						second_best_cost = best_cost;
						best_cost = cost;
					}else{
						if(cost < second_best_cost){
							second_best_cost = cost;
							second_best_pi = pi;
							second_best_pk = pk;
						}
					}
				}
			}
			
			int best_i_truck = -1;
			for(int j=1; j<truckTour.size(); j++){
				Point pi = truckTour.get(j);
				double cost = tspd.evaluateInsertasTruck(pi, pj, tour);
				if(cost <= best_cost){
					second_best_cost = best_cost;
					best_cost = cost;
				}else{
					if(cost < second_best_cost){
						second_best_cost = cost;
						best_i_truck = j;
					}
				}
			}
			
			if(best_i_truck != -1){
				truckTour.add(best_i_truck,pj);
				//LOGGER.LOGGER.log(Level.INFO,"insert "+pj.toString()+" before "+truckTour.get(best_i_truck));
				rejectedPoint.remove(pj);
				
				/*
				 * Update historical cost
				 */
				Point historical_prev = truckTour.get(best_i_truck-1);
				Point historical_next = truckTour.get(best_i_truck+1);
				
				double historical_new_cost = tspd.cost(historical_prev,pj) + tspd.cost(pj,historical_next); 
				double historical_best_cost = historicalCost.get(pj);
				if(historical_new_cost < historical_best_cost){
					historicalCost.put(pj, historical_new_cost);
				}
				
				i--;
			}else{
				if(second_best_pi != null && second_best_pk != null){
					DroneDelivery new_dd = new DroneDelivery(second_best_pi,pj,second_best_pk);
					//LOGGER.LOGGER.log(Level.INFO,"new DroneDelivery( "+best_pi.toString()+", "+pj.toString()+", "+best_pk.toString());
					droneDeliveries.add(new_dd);
					rejectedPoint.remove(pj);
					
					/*
					 * Update historical cost
					 */
					double historical_new_cost = tspd.cost(new_dd); 
					double historical_best_cost = historicalCost.get(pj);
					if(historical_new_cost < historical_best_cost){
						historicalCost.put(pj, historical_new_cost);
					}
					
					i--;
				}
			}
		}
		//LOGGER.LOGGER.log(Level.INFO," after insert "+tour.toString());
	}
	
	private void greedy_noise_insertion(){
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		for(int i=0; i<rejectedPoint.size(); i++){
			Point pj = rejectedPoint.get(i);
			double best_cost = Double.MAX_VALUE;
			Point best_pi = null;
			Point best_pk = null;
			
			for(int j=0; j<truckTour.size()-1; j++){
				Point pi = truckTour.get(j);
				for(int k = j+1; k<truckTour.size(); k++){
					Point pk = truckTour.get(k);
					double actual_cost = tspd.evaluateInsertasDrone(j, pj, k, tour);
					double r = Math.random()*2 - 1;
					double cost = actual_cost + tspd.getMaxDroneDistance()*tspd.getC2()*0.1*r;
					if(cost < best_cost){
						best_cost = cost;
						best_pi = pi;
						best_pk = pk;
					}
				}
			}
			
			int best_i_truck = -1;
			for(int j=1; j<truckTour.size(); j++){
				Point pi = truckTour.get(j);
				double actual_cost = tspd.evaluateInsertasTruck(pi, pj, tour);
				double r = Math.random()*2 - 1;
				double cost = actual_cost + tspd.getMaxTruckDistance()*tspd.getC1()*0.1*r;
				
				if(cost < best_cost){
					best_cost = cost;
					best_i_truck = j;
				}
			}
			
			if(best_i_truck != -1){
				truckTour.add(best_i_truck,pj);
				//LOGGER.LOGGER.log(Level.INFO,"insert "+pj.toString()+" before "+truckTour.get(best_i_truck));
				rejectedPoint.remove(pj);
				
				/*
				 * Update historical cost
				 */
				Point historical_prev = truckTour.get(best_i_truck-1);
				Point historical_next = truckTour.get(best_i_truck+1);
				
				double historical_new_cost = tspd.cost(historical_prev,pj) + tspd.cost(pj,historical_next); 
				double historical_best_cost = historicalCost.get(pj);
				if(historical_new_cost < historical_best_cost){
					historicalCost.put(pj, historical_new_cost);
				}
				
				i--;
			}else{
				if(best_pi != null && best_pk != null){
					DroneDelivery new_dd = new DroneDelivery(best_pi,pj,best_pk);
					//LOGGER.LOGGER.log(Level.INFO,"new DroneDelivery( "+best_pi.toString()+", "+pj.toString()+", "+best_pk.toString());
					droneDeliveries.add(new_dd);
					rejectedPoint.remove(pj);
					
					/*
					 * Update historical cost
					 */
					double historical_new_cost = tspd.cost(new_dd); 
					double historical_best_cost = historicalCost.get(pj);
					if(historical_new_cost < historical_best_cost){
						historicalCost.put(pj, historical_new_cost);
					}
					
					i--;
				}
			}
		}
		//LOGGER.LOGGER.log(Level.INFO," after insert "+tour.toString());
	}

	private void second_best_noise_insertion(){
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		for(int i=0; i<rejectedPoint.size(); i++){
			Point pj = rejectedPoint.get(i);
			
			double best_cost = Double.MAX_VALUE;
			double second_best_cost = Double.MAX_VALUE;
			Point second_best_pi = null;
			Point second_best_pk = null;
			
			for(int j=0; j<truckTour.size()-1; j++){
				Point pi = truckTour.get(j);
				for(int k = j+1; k<truckTour.size(); k++){
					Point pk = truckTour.get(k);
					double actual_cost = tspd.evaluateInsertasDrone(j, pj, k, tour);
					double r = Math.random()*2 - 1;
					double cost = actual_cost + tspd.getMaxDroneDistance()*tspd.getC2()*0.1*r;
					
					if(cost <= best_cost){
						second_best_cost = best_cost;
						best_cost = cost;
					}else{
						if(cost < second_best_cost){
							second_best_cost = cost;
							second_best_pi = pi;
							second_best_pk = pk;
						}
					}
				}
			}
			
			int best_i_truck = -1;
			for(int j=1; j<truckTour.size(); j++){
				Point pi = truckTour.get(j);
				double actual_cost = tspd.evaluateInsertasTruck(pi, pj, tour);
				double r = Math.random()*2 - 1;
				double cost = actual_cost + tspd.getMaxTruckDistance()*tspd.getC1()*0.1*r;
				
				if(cost <= best_cost){
					second_best_cost = best_cost;
					best_cost = cost;
				}else{
					if(cost < second_best_cost){
						second_best_cost = cost;
						best_i_truck = j;
					}
				}
			}
			
			if(best_i_truck != -1){
				truckTour.add(best_i_truck,pj);
				//LOGGER.LOGGER.log(Level.INFO,"insert "+pj.toString()+" before "+truckTour.get(best_i_truck));
				rejectedPoint.remove(pj);
				
				/*
				 * Update historical cost
				 */
				Point historical_prev = truckTour.get(best_i_truck-1);
				Point historical_next = truckTour.get(best_i_truck+1);
				
				double historical_new_cost = tspd.cost(historical_prev,pj) + tspd.cost(pj,historical_next); 
				double historical_best_cost = historicalCost.get(pj);
				if(historical_new_cost < historical_best_cost){
					historicalCost.put(pj, historical_new_cost);
				}
				
				i--;
			}else{
				if(second_best_pi != null && second_best_pk != null){
					DroneDelivery new_dd = new DroneDelivery(second_best_pi,pj,second_best_pk);
					//LOGGER.LOGGER.log(Level.INFO,"new DroneDelivery( "+best_pi.toString()+", "+pj.toString()+", "+best_pk.toString());
					droneDeliveries.add(new_dd);
					rejectedPoint.remove(pj);
					
					/*
					 * Update historical cost
					 */
					double historical_new_cost = tspd.cost(new_dd); 
					double historical_best_cost = historicalCost.get(pj);
					if(historical_new_cost < historical_best_cost){
						historicalCost.put(pj, historical_new_cost);
					}
					
					i--;
				}
			}
		}
		//LOGGER.LOGGER.log(Level.INFO," after insert "+tour.toString());
	}
	
	private void regret_n_insertion(int n){
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		for(int i=0; i<rejectedPoint.size(); i++){
			Point pj = rejectedPoint.get(i);
			
			Point best_pi = null;
			Point best_pk = null;
			double[] n_best_cost = new double[n];
			double best_regret_value = Double.MIN_VALUE;
			for(int it=0; it<n; it++){
				n_best_cost[it] = Double.MAX_VALUE;
			}
			
			for(int j=0; j<truckTour.size()-1; j++){
				Point pi = truckTour.get(j);
				for(int k = j+1; k<truckTour.size(); k++){
					Point pk = truckTour.get(k);
					double cost = tspd.evaluateInsertasDrone(j, pj, k, tour);
					for(int it=0; it<n; it++){
						if(n_best_cost[it] > cost){
							for(int it2 = n-1; it2 > it; it2--){
								n_best_cost[it2] = n_best_cost[it2-1];
							}
							n_best_cost[it] = cost;
							break;
						}
					}
					double regret_value = 0;
					for(int it=1; it<n; it++){
						regret_value += (n_best_cost[it] - n_best_cost[0]);
					}
					if(regret_value > best_regret_value){
						best_regret_value = regret_value;
						best_pi = pi;
						best_pk = pk;
					}
				}
			}
			
			int best_i_truck = -1;
			for(int j=1; j<truckTour.size(); j++){
				Point pi = truckTour.get(j);
				double cost = tspd.evaluateInsertasTruck(pi, pj, tour);
				for(int it=0; it<n; it++){
					if(n_best_cost[it] > cost){
						for(int it2 = n-1; it2 > it; it2--){
							n_best_cost[it2] = n_best_cost[it2-1];
						}
						n_best_cost[it] = cost;
						break;
					}
				}
				double regret_value = 0;
				for(int it=1; it<n; it++){
					regret_value += (n_best_cost[it] - n_best_cost[0]);
				}
				if(regret_value > best_regret_value){
					best_regret_value = regret_value;
					best_i_truck = j;
				}
			}
			
			if(best_i_truck != -1){
				truckTour.add(best_i_truck,pj);
				//LOGGER.LOGGER.log(Level.INFO,"insert "+pj.toString()+" before "+truckTour.get(best_i_truck));
				rejectedPoint.remove(pj);
				
				/*
				 * Update historical cost
				 */
				Point historical_prev = truckTour.get(best_i_truck-1);
				Point historical_next = truckTour.get(best_i_truck+1);
				
				double historical_new_cost = tspd.cost(historical_prev,pj) + tspd.cost(pj,historical_next); 
				double historical_best_cost = historicalCost.get(pj);
				if(historical_new_cost < historical_best_cost){
					historicalCost.put(pj, historical_new_cost);
				}
				
				i--;
			}else{
				if(best_pi != null && best_pk != null){
					DroneDelivery new_dd = new DroneDelivery(best_pi,pj,best_pk);
					//LOGGER.LOGGER.log(Level.INFO,"new DroneDelivery( "+best_pi.toString()+", "+pj.toString()+", "+best_pk.toString());
					droneDeliveries.add(new_dd);
					rejectedPoint.remove(pj);
					
					/*
					 * Update historical cost
					 */
					double historical_new_cost = tspd.cost(new_dd); 
					double historical_best_cost = historicalCost.get(pj);
					if(historical_new_cost < historical_best_cost){
						historicalCost.put(pj, historical_new_cost);
					}
					
					i--;
				}
			}
		}
		//LOGGER.LOGGER.log(Level.INFO," after insert "+tour.toString());
	}
	
	private Tour copySolution(Tour old){
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
	
	//roulette-wheel mechanism
 	private int get_operator(double[] p){
 		
 		int n = p.length;
		double[] s = new double[n];
		s[0] = 0+p[0];
		
		for(int i=1; i<n; i++){
			s[i] = s[i-1]+p[i]; 
		}
		
		double r = s[n-1]*Math.random();
		
		if(r>=0 && r <= s[0])
			return 0;
		
		for(int i=1; i<n; i++){
			if(r>s[i-1] && r<=s[i])
				return i;
		}
		return -1;
	}	
}
