package com.kse.ezRoutingAPI.tspd.service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.GRASP_Arc;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.TrunkTour;

public class GRASP {
	private Tour solution_tour;
	private TSPD tspd;
	int nTSP;
	public Tour solve(){
		solution_tour = null;
		double bestObjectiveValue = Double.MAX_VALUE;
		TSP tsp = new TSP(tspd.getStartPoint(), tspd.getClientPoints(), tspd.getEndPoint());
		int iteration = 0;
		nTSP = tspd.getClientPoints().size();
		
		while(iteration < nTSP){
			iteration++;
			ArrayList<Point> tour = tsp.randomGenerator();
			Tour tspdSolution = split_algorithm(tour);
			tspdSolution = local_search(tspdSolution);
			if(tspd.cost(tspdSolution) < bestObjectiveValue){
				solution_tour = tspdSolution;
				bestObjectiveValue = tspd.cost(tspdSolution);
				iteration = 0;
			}
		}
		
		return solution_tour;
	}
	
	private Point[] P;
	private ArrayList<DroneDelivery> T;
	
	public Tour split_algorithm(ArrayList<Point> tsp_tour){
		build_graph(tsp_tour);
		Point pj = tsp_tour.get(nTSP+1);
		ArrayList<Point> Sa = new ArrayList<Point>();
		
		Point pi = new Point(nTSP+2,0,0);
		while(!pi.equals(tsp_tour.get(0))){
			pi = P[pj.getID()];
			Sa.add(pi);
			pj = pi;
		}
		Collections.reverse(Sa);
		
		ArrayList<Point> St = new ArrayList<Point>();
		ArrayList<DroneDelivery> Sd = new ArrayList<DroneDelivery>();
		
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
		
		Point currentPosition = tsp_tour.get(0);
		int index = 0;
		while(index != (nTSP+1)){
			for(int i=0; i<Sd.size(); i++){
				DroneDelivery tmp_dd = Sd.get(i);
				if(currentPosition.equals(tmp_dd.getLauch_node())){
					while(!currentPosition.equals(tmp_dd.getRendezvous_node())){
						if(!currentPosition.equals(tmp_dd.getDrone_node())){
							St.add(currentPosition);
						}
						index++;
						currentPosition = tsp_tour.get(index);
					}
					break;
				}
			}
			St.add(currentPosition);
			index++;
			currentPosition = tsp_tour.get(index);
		}
		
		TrunkTour trunk_tour = new TrunkTour(St);
		Tour tour = new Tour(trunk_tour, Sd);
		
		return tour;
	}
	
	public void build_graph(ArrayList<Point> tsp_tour){
		ArrayList<GRASP_Arc> arcs = new ArrayList<GRASP_Arc>();
		T = new ArrayList<DroneDelivery>();
		
		for(int i=0; i<tsp_tour.size()-1; i++){
			Point pi = tsp_tour.get(i);
			Point pk = tsp_tour.get(i+1);	
			GRASP_Arc arc = new GRASP_Arc(pi, pk, tspd.cost(pi, pk));
			arcs.add(arc);
		}
		
		for(int i=0; i<tsp_tour.size()-2; i++){
			for(int k=i+2; k<tsp_tour.size(); k++){
				double minValue = Double.MAX_VALUE;
				int minIndex = nTSP+3;
				Point pi = tsp_tour.get(i);
				Point pk = tsp_tour.get(k);
				
				for(int j=i+1; j<k; j++){
					Point pj = tsp_tour.get(j);
				
					if(tspd.isDroneDelivery(pi, pj, pk) && tspd.checkWaitTime(pi, pj, pk)){
						double d1 = tspd.d_truck(tsp_tour.get(j-1), tsp_tour.get(j+1));
						double d2 = tspd.d_truck(tsp_tour.get(j-1), tsp_tour.get(j));
						double d3 = tspd.d_truck(tsp_tour.get(j), tsp_tour.get(j+1));
						
						double cost = tspd.cost(i, k, tsp_tour) + tspd.getC1()*(d1-d2-d3) + tspd.cost(pi,pj,pk);
						
						if(cost < minValue){
							minValue = cost;
							minIndex = j;
						}
					}
				}
				
				GRASP_Arc arc = new GRASP_Arc(pi, pk, minValue);
				arcs.add(arc);
				if(minIndex != nTSP+3){
					DroneDelivery dd = new DroneDelivery(pi,tsp_tour.get(minIndex),pk);
					T.add(dd);
				}
			}
		}
		
		P = new Point[nTSP+2];
		double V[] = new double[nTSP+2];
		V[0] = 0;
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
	}
	
	public Tour local_search(Tour tspdSolution){
		
	}
	
	public ArrayList<Point> relocate(Tour tspd, Point a, Point b){
		
		
		
	}
	
	public ArrayList<Point> getTrunkOnlyNodes(Tour tspd){
		ArrayList<Point> TD = tspd.getTD().getTrunk_tour();
		ArrayList<DroneDelivery> DD = tspd.getDD();
		
		ArrayList<Point> trunkOnlyNodes = new ArrayList<Point>();
		
		for(int i=0; i<TD.size(); i++){
			Point 
		}
	}
}
