package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;
import java.util.Collections;
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
	
	private int nRemovalOperators = 2;
	private int nInsertionOperators = 1;
	
	private int lower_removal = 1;
	private int upper_removal = 10;
	private double temperature = 200;
	private double cooling_rate = 0.9995;
	private int sigma1 = 1;
	private int sigma2 = 3;
	private int sigma3 = 5;
	private int nw = 100;
	private double rp = 0.1;
	
	public ALNS(TSPD tspd){
		this.tspd = tspd;
		//this.nDrone = nDrone;
	}
	
	public Tour init(){
		TSP tsp = new TSP(tspd.getStartPoint(), tspd.getClientPoints(), tspd.getEndPoint());
		tsp.setDistances_matrix(tspd.getDistancesTruck());
		
		//ArrayList<Point> tsp_solution = tsp.lsInitTSP();
		//ArrayList<Point> tsp_solution = tsp.randomGenerator();
		ArrayList<Point> tsp_solution = tsp.greedyInit();
		
		LOGGER.LOGGER.log(Level.INFO,"tsp init = "+tsp_solution.toString());
		
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
			
			//int i_selected_removal = get_operator(ptd);
			int i_selected_removal = 0;
			wd[i_selected_removal]++;
			
			switch(i_selected_removal){
				case 0: random_removal(); break;
				case 1: max_saving_removal(); break;
			}
			
			int i_selected_insertion = get_operator(pti);
			wi[i_selected_insertion]++;
			switch(i_selected_insertion){
				case 0: greedy_insertion(); break;
			}
			
			double new_cost = tspd.cost(tour);
			
			//LOGGER.LOGGER.log(Level.INFO, "it "+it+" new_cost = "+tspd.cost(tour)+" new_rejectPoint = "+rejectedPoint.size());
			
			if(new_cost < current_cost){
				//current_solution = tour;
				if(new_cost < best_cost){
					sd[i_selected_removal] += sigma1;
					si[i_selected_insertion] += sigma1;
					
					best_cost = new_cost;
					best_solution = copySolution(tour);
					LOGGER.LOGGER.log(Level.INFO, "it "+it+" find best best_cost = "+best_cost +" rejectedPoint = "+rejectedPoint.size());
				}else{
					sd[i_selected_removal] += sigma2;
					si[i_selected_insertion] += sigma2;
				}
			}else{
				sd[i_selected_removal] += sigma3;
				si[i_selected_insertion] += sigma3;
				
				double v = Math.exp(-(new_cost - current_cost)/temperature);
				double r = Math.random();
				if(r >= v){
					//LOGGER.LOGGER.log(Level.INFO,"r >=v back to prev solution");
					tour = current_solution;
					rejectedPoint = current_rejectPoint;
				}
			}
			
			temperature = cooling_rate * temperature;
			
			if(it % nw == 0){
				for(int i=0; i<nInsertionOperators; i++){
					pti[i] = Math.max(0, pti[i]*(1-rp) + rp*si[i]/wi[i]);
					//wi[i] = 1;
					//si[i] = 0;
				}
				
				for(int i=0; i<nRemovalOperators; i++){
					ptd[i] = Math.max(0, ptd[i]*(1-rp) + rp*sd[i]/wd[i]);
					//wd[i] = 1;
					//sd[i] = 0;
				}
			}
		}
		best_solution.setTotalCost(tspd.cost(best_solution));
		int nClients = best_solution.getTD().getTruck_tour().size()+ best_solution.getDD().size();
		LOGGER.LOGGER.log(Level.INFO,"search done best cost = "+tspd.cost(best_solution)+ " nPoint = "+nClients);;
		return best_solution;
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
		
		for(int i=0; i<nRemove; i++){
			Point pi = clientPoints.get(i);
			//LOGGER.LOGGER.log(Level.INFO," chose "+pi.toString());
			
			if(!tspd.evaluateRemove(pi, tour))
				continue;
			
			for(int j=0; j<truckTour.size(); j++){
				Point pj = truckTour.get(j);
				if(pj.equals(pi)){
					//LOGGER.LOGGER.log(Level.INFO," pi is truck node");
					truckTour.remove(pj);
					rejectedPoint.add(pi);
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
				
				for(int j=0; j<droneDeliveries.size(); j++){
					DroneDelivery dj = droneDeliveries.get(j);
					
					Point ldj = dj.getLauch_node();
					Point ddj = dj.getDrone_node();
					Point rdj = dj.getRendezvous_node();
					
					if(ldj.equals(pi) || rdj.equals(pi)){
						rejectedPoint.add(ddj);
						droneDeliveries.remove(dj);
						//LOGGER.LOGGER.log(Level.INFO," pi is drone Node");
						j--;
					}
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
				i--;
			}else{
				DroneDelivery new_dd = new DroneDelivery(best_pi,pj,best_pk);
				//LOGGER.LOGGER.log(Level.INFO,"new DroneDelivery( "+best_pi.toString()+", "+pj.toString()+", "+best_pk.toString());
				droneDeliveries.add(new_dd);
				rejectedPoint.remove(pj);
				i--;
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
