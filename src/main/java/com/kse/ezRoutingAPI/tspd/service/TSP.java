package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyCrossExchangeMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOnePointMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOrOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOrOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove3Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove4Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove5Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove6Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove7Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove8Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove3Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove4Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove5Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove6Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove7Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove8Explorer;
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
		boolean visted[] = new boolean[nClientPoint];
		for(int i=0; i<nClientPoint; i++){
			visted[i] = false;
		}
 		Random rand = new Random();
		
		for(int i=0; i<nClientPoint; i++){
			int iPoint = rand.nextInt(nClientPoint);
			while(visted[iPoint]){
				iPoint = rand.nextInt(nClientPoint);
			}
			tour.add(clientPoints.get(iPoint));
			visted[iPoint] = true;
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
