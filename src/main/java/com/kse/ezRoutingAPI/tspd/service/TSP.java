package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOnePointMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove3Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove4Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove5Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove6Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove7Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove8Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.INeighborhoodExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.search.GenericLocalSearch;

import com.kse.ezRoutingAPI.tspd.model.Point;

public class TSP {
	private Point startPoint;
	private ArrayList<Point> clientPoints;
	private Point endPoint;
	private double distances_matrix[][];
	ArcWeightsManager awm=null;

	
	public Point getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}

	public ArrayList<Point> getClientPoints() {
		return clientPoints;
	}

	public void setClientPoints(ArrayList<Point> clientPoints) {
		this.clientPoints = clientPoints;
	}

	public Point getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(Point endPoint) {
		this.endPoint = endPoint;
	}

	public double[][] getDistances_matrix() {
		return distances_matrix;
	}

	public void setDistances_matrix(double[][] distances_matrix) {
		this.distances_matrix = distances_matrix;
	}

	public TSP(Point sPoint, ArrayList<Point> cPoint, Point ePoint){
		this.startPoint = sPoint;
		this.clientPoints = cPoint;
		this.endPoint = ePoint;
	}
	
	public ArrayList<Point> randomGenerator(){
		ArrayList<Point> tour = new ArrayList<Point>();
		tour.add(startPoint);
		
		int nClientPoint = clientPoints.size();
				
		Collections.shuffle(clientPoints);
		
		for(int i=0; i<nClientPoint; i++){
			tour.add(clientPoints.get(i));
		}
		
		tour.add(endPoint);
		
		return tour;
	}
	
	public ArrayList<Point> kNearest(){
		ArrayList<Point> tour = new ArrayList<Point>();
		
		Random R = new Random();
		int k = R.nextInt(2)+2;
		
		tour.add(startPoint);
		
		boolean[] visited  = new boolean[clientPoints.size()];
		for(int i=0; i<clientPoints.size(); i++){
			visited[i] = false;
		}
		
			
		int nClientPoint = 0;
		
		while(nClientPoint != clientPoints.size()){
			Point last = tour.get(tour.size()-1);
			//System.out.println("last = "+last.toString());
			double[] minCost = new double[k];
			int[] minIndex = new int[k];
			
			for(int i=0; i<k; i++){
				minCost[i] = Double.MAX_VALUE;
				minIndex[i] = -1;
			}
			
			for(int i=0; i<clientPoints.size(); i++){
				if(!visited[i]){
					Point pi = clientPoints.get(i);
					for(int ik = 0; ik < k; ik ++){
						if(distances_matrix[last.getID()][pi.getID()] < minCost[ik]){
							for(int jk = k-1; jk > ik; jk--){
								minCost[jk] = minCost[jk-1];
								minIndex[jk] = minIndex[jk-1];
							}
							minCost[ik] = distances_matrix[last.getID()][pi.getID()];
							minIndex[ik] = i;
							break;
						}
					}
				}
			}
			ArrayList<Integer> iNearests = new ArrayList<Integer>();
			for(int i=0; i<k; i++){
				if(minIndex[i] != -1){
					iNearests.add(minIndex[i]);
				}
			}
			//System.out.println("nearests = "+iNearests.toString());
			int in = R.nextInt(iNearests.size());
			int iNearest = iNearests.get(in);
			Point nearest = clientPoints.get(iNearest);
			
			//System.out.println("nearest = "+nearest.toString());
			
			tour.add(nearest);
			nClientPoint++;
			visited[iNearest] = true;
		}
		
		tour.add(endPoint);
		return tour;
	}

	public ArrayList<Point> kCheapest(){
		ArrayList<Point> tour = new ArrayList<Point>();
		Random R = new Random();
		
		int k = R.nextInt(2)+2;
		
		tour.add(startPoint);
		tour.add(endPoint);
			
		
		Collections.shuffle(clientPoints);
		
		for(int i=0; i<clientPoints.size(); i++){
			Point pi = clientPoints.get(i);
			//System.out.println("pi = "+pi.toString());
			
			double[] minCost = new double[k];
			int[] minIndex = new int[k];
			for(int ik = 0; ik < k; ik++){
				minCost[ik] = Double.MAX_VALUE;
				minIndex[ik] = -1;
			}
			
			for(int ir = 1; ir < tour.size() ; ir++){
				Point pj = tour.get(ir);
				Point pk = tour.get(ir-1);
				double cost = distances_matrix[pj.getID()][pi.getID()] + distances_matrix[pi.getID()][pk.getID()]
						- distances_matrix[pj.getID()][pk.getID()];
					
				for(int ik = 0; ik < k; ik ++){
					if(cost < minCost[ik]){
						for(int jk = k-1; jk > ik; jk--){
							minCost[jk] = minCost[jk-1];
							minIndex[jk] = minIndex[jk-1];
						}
						minCost[ik] = cost;
						minIndex[ik] = ir;
						break;
					}
				}	
			}
			
			ArrayList<Integer> iInserts = new ArrayList<Integer>();
			for(int ik = 0; ik < k ; ik++){
				if(minIndex[ik] != -1){
					iInserts.add(minIndex[ik]);
				}
			}
			//System.out.println("iInserts = "+iInserts.toString());
			int iInsert = R.nextInt(iInserts.size());
			//System.out.println("iInsert = "+iInserts.get(iInsert));
			tour.add(iInserts.get(iInsert),pi);
		}
		
		return tour;
	}
	
	public ArrayList<Point> greedyInit(){
		ArrayList<Point> tour = new ArrayList<Point>();
		
		tour.add(startPoint);
		
		boolean[] visited  = new boolean[clientPoints.size()];
		for(int i=0; i<clientPoints.size(); i++){
			visited[i] = false;
		}
		
			
		int nClientPoint = 0;			
		while(nClientPoint != clientPoints.size()){
			Point last = tour.get(tour.size()-1);
			
			double minCost = Double.MAX_VALUE;
			int minIndex = -1;
			for(int i=0; i<clientPoints.size(); i++){
				if(!visited[i]){
					Point pi = clientPoints.get(i);
					if(distances_matrix[last.getID()][pi.getID()] < minCost){
						minCost = distances_matrix[last.getID()][pi.getID()];
						minIndex = i;
					}
				}
			}
			Point nearest = clientPoints.get(minIndex);
			tour.add(nearest);
			nClientPoint++;
			visited[minIndex] = true;
		}
		
		tour.add(endPoint);
		return tour;
	}
	
	public void initLs(ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point> allPoints,ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point> startPoints,ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point> lclientPoint,ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point> endPoints,HashMap<localsearch.domainspecific.vehiclerouting.vrp.entities.Point, Point> hash){
		localsearch.domainspecific.vehiclerouting.vrp.entities.Point p=new localsearch.domainspecific.vehiclerouting.vrp.entities.Point(startPoint.getID(), startPoint.getLat(), startPoint.getLng());
		hash.put(p, startPoint);
		startPoints.add(p);
		allPoints.add(p);
		
		p=new localsearch.domainspecific.vehiclerouting.vrp.entities.Point(endPoint.getID(), endPoint.getLat(), endPoint.getLng());
		hash.put(p, endPoint);
		endPoints.add(p);
		allPoints.add(p);
		
		for(int i=0;i<clientPoints.size();i++){
			p=new localsearch.domainspecific.vehiclerouting.vrp.entities.Point(clientPoints.get(i).getID(), clientPoints.get(i).getLat(), clientPoints.get(i).getLng());
			hash.put(p, clientPoints.get(i));
			lclientPoint.add(p);
			allPoints.add(p);
		}
		//allPoints.add(startPoints.get(0));
		//allPoints.addAll(lclientPoint);
		//allPoints.add(endPoints.get(0));
		//GoogleMapsQuery gmq = new GoogleMapsQuery();
		awm=new ArcWeightsManager(allPoints);
		
		for(localsearch.domainspecific.vehiclerouting.vrp.entities.Point p1:allPoints){
			for(localsearch.domainspecific.vehiclerouting.vrp.entities.Point p2:allPoints){
				//double x=gmq.getDistance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
				double x = distances_matrix[hash.get(p1).getID()][hash.get(p2).getID()];
				//if (x==-1) x=gmq.getApproximateDistanceMeter(p1.getX(), p1.getY(), p2.getX(), p2.getY());
				awm.setWeight(p1, p2, x);
			}
		}	
	}
	
	public ArrayList<Point> lsInitTSP(){
		HashMap<localsearch.domainspecific.vehiclerouting.vrp.entities.Point, Point> mapPP= new HashMap<localsearch.domainspecific.vehiclerouting.vrp.entities.Point, Point>();
		ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point> allPoints = new ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point>();
		ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point> startPoints = new ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point>();
		ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point> endPoints = new ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point>();
		ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point> clientPoints = new ArrayList<localsearch.domainspecific.vehiclerouting.vrp.entities.Point>();
		awm=null;
		initLs(allPoints,startPoints,clientPoints,endPoints,mapPP);
//		System.out.println(name());
//		System.out.println(allPoints.toString());
//		System.out.println(clientPoints.toString());
//		System.out.println(startPoints.toString());
//		System.out.println(endPoints.toString());
		VRManager mgr=new VRManager();
		VarRoutesVR XR=new VarRoutesVR(mgr);
		XR.addRoute(startPoints.get(0),endPoints.get(0));
		for(int i = 0; i < clientPoints.size(); i++)
			XR.addClientPoint(clientPoints.get(i));
		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		IFunctionVR cost = new AccumulatedEdgeWeightsOnPathVR(awe, XR.endPoint(1));
		LexMultiFunctions F = new LexMultiFunctions();
		F.add(cost);
		mgr.close();
//		mgr.performAddOnePoint(clientPoints.get(0),startPoints.get(0));
//		localsearch.domainspecific.vehiclerouting.vrp.entities.Point pOld=clientPoints.get(0);
//		for(int i=1;i<clientPoints.size();i++){
//			mgr.performAddOnePoint(clientPoints.get(i), pOld);
//			pOld=clientPoints.get(i);
//		}
		XR.setRandom();
		ArrayList<INeighborhoodExplorer> NE = new ArrayList<INeighborhoodExplorer>();
		NE.add(new GreedyOnePointMoveExplorer(XR, F));
		NE.add(new GreedyThreeOptMove1Explorer(XR, F));
		NE.add(new GreedyThreeOptMove2Explorer(XR, F));
		NE.add(new GreedyThreeOptMove3Explorer(XR, F));
		NE.add(new GreedyThreeOptMove4Explorer(XR, F));
		NE.add(new GreedyThreeOptMove5Explorer(XR, F));
		NE.add(new GreedyThreeOptMove6Explorer(XR, F));
		NE.add(new GreedyThreeOptMove7Explorer(XR, F));
		NE.add(new GreedyThreeOptMove8Explorer(XR, F));
		
		GenericLocalSearch gs = new GenericLocalSearch(mgr);
		gs.setNeighborhoodExplorer(NE);
		gs.setObjectiveFunction(F);
		gs.setMaxStable(50);
		
		gs.search(300, 10);
		localsearch.domainspecific.vehiclerouting.vrp.entities.Point p=XR.getStartingPointOfRoute(1);
		ArrayList<Point> lSol= new ArrayList<Point>();
		lSol.add(mapPP.get(p));
		while(true){
			p=XR.next(p);
			lSol.add(mapPP.get(p));
			if(p==XR.getTerminatingPointOfRoute(1)) break;
		}
		return lSol;
	}
	String name(){
		return "TSP::";
	}
}
