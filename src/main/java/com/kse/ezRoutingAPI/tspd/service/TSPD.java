package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;
import com.kse.utils.LOGGER;

public class TSPD {
	private int C1;// cost per unit of trunk
	private int C2; // cost per unit of drone
	private double delta;
	private double e;
	private double truckSpeed; // Speed of truck
	private double droneSpeed; // Speed of drone
	private Point startPoint;
	private ArrayList<Point> clientPoints;
	private Point endPoint;
	private ArrayList<DroneDelivery> P;
	private double distancesDrone[][];
	private double distancesTruck[][];
	private Map<Integer, Boolean> allowDrone;
	private int nDrone;
	private double maxTruckDistance;
	private double maxDroneDistance;
	
	
	public int getnDrone() {
		return nDrone;
	}

	public void setnDrone(int nDrone) {
		this.nDrone = nDrone;
	}

	public double getMaxTruckDistance() {
		return maxTruckDistance;
	}

	public void setMaxTruckDistance(double maxTruckDistance) {
		this.maxTruckDistance = maxTruckDistance;
	}

	public double getMaxDroneDistance() {
		return maxDroneDistance;
	}

	public void setMaxDroneDistance(double maxDroneDistance) {
		this.maxDroneDistance = maxDroneDistance;
	}

	public int getC1() {
		return C1;
	}

	public void setC1(int c1) {
		C1 = c1;
	}

	public int getC2() {
		return C2;
	}

	public void setC2(int c2) {
		C2 = c2;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public double getE() {
		return e;
	}

	public void setE(double e) {
		this.e = e;
	}

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

	public double getTruckSpeed() {
		return truckSpeed;
	}

	public void setTruckSpeed(double truckSpeed) {
		this.truckSpeed = truckSpeed;
	}

	public double getDroneSpeed() {
		return droneSpeed;
	}

	public void setDroneSpeed(double droneSpeed) {
		this.droneSpeed = droneSpeed;
	}

	public double[][] getDistancesDrone() {
		return distancesDrone;
	}

	public void setDistancesDrone(double[][] distancesDrone) {
		this.distancesDrone = distancesDrone;
	}

	public double[][] getDistancesTruck() {
		return distancesTruck;
	}

	public void setDistancesTruck(double[][] distancesTruck) {
		this.distancesTruck = distancesTruck;
	}

	public TSPD(int c1, int c2, double delta, double e, double truckSpeed,
			double droneSpeed, Point startPoint, ArrayList<Point> clientPoints,
			Point endPoint, Map<String, Double> map) {
		super();
		C1 = c1;
		C2 = c2;
		this.delta = delta;
		this.e = e;
		this.truckSpeed = truckSpeed;
		this.droneSpeed = droneSpeed;
		this.startPoint = startPoint;
		this.clientPoints = clientPoints;
		this.endPoint = endPoint;
		
		build_distances_array(map);
		//build_P();
	}
	
	public TSPD(int c1, int c2, double delta, double e, double truckSpeed,
			double droneSpeed, Point startPoint, ArrayList<Point> clientPoints,
			Point endPoint, Map<String, Double> map, Map<Integer, Boolean> allowDrone, int nDrone) {
		super();
		C1 = c1;
		C2 = c2;
		this.delta = delta;
		this.e = e;
		this.truckSpeed = truckSpeed;
		this.droneSpeed = droneSpeed;
		this.startPoint = startPoint;
		this.clientPoints = clientPoints;
		this.endPoint = endPoint;
		this.allowDrone = allowDrone;
		this.nDrone = nDrone;
		
		build_distances_array(map);
		//build_P();
	}

	public TSPD(int c1, int c2, double delta, double e, double truckSpeed,
			double droneSpeed, Point startPoint, ArrayList<Point> clientPoints,
			Point endPoint) {
		super();
		C1 = c1;
		C2 = c2;
		this.delta = delta;
		this.e = e;
		this.truckSpeed = truckSpeed;
		this.droneSpeed = droneSpeed;
		this.startPoint = startPoint;
		this.clientPoints = clientPoints;
		this.endPoint = endPoint;

		build_distances_array();
		//build_P();
	}

	public void build_distances_array(Map<String, Double> map) {
		int nPoints = clientPoints.size() + 2;
		distancesDrone = new double[nPoints][nPoints];
		distancesTruck = new double[nPoints][nPoints];
		ArrayList<Point> allPoints = new ArrayList<Point>();
		allPoints.add(startPoint);
		allPoints.addAll(clientPoints);
		allPoints.add(endPoint);

		//GoogleMapsQuery gmap = new GoogleMapsQuery();
		maxDroneDistance = Double.MIN_VALUE;
		maxTruckDistance = Double.MIN_VALUE;
		
		for (int i = 0; i < allPoints.size(); i++) {
			Point pi = allPoints.get(i);
			distancesTruck[pi.getID()][pi.getID()] = 0;
			distancesDrone[pi.getID()][pi.getID()] = 0;
			for (int j = i + 1; j < allPoints.size(); j++) {
				Point pj = allPoints.get(j);
				double droneDis = //gmap.computeDistanceHaversine(pi.getLat(), pi.getLng(),pj.getLat(), pj.getLng());
						computeEuclice(pi.getLat(), pi.getLng(),pj.getLat(), pj.getLng());
				distancesDrone[pi.getID()][pj.getID()] = droneDis;
				if(droneDis > maxDroneDistance){
					maxDroneDistance = droneDis;
				}
						
				String key = pi.getID() + "_" + pj.getID();
				//System.out.println("Key = "+ key);
				double dis = map.get(key);
				if(dis > maxTruckDistance){
					maxTruckDistance = dis;
				}
				distancesTruck[pi.getID()][pj.getID()] = dis;
				distancesDrone[pj.getID()][pi.getID()] = distancesDrone[pi
						.getID()][pj.getID()];
				key = pj.getID() + "_" + pi.getID();
				dis = map.get(key);
				if(dis > maxTruckDistance){
					maxTruckDistance = dis;
				}
				distancesTruck[pj.getID()][pi.getID()] = dis;
			}
		}
		/*
		 * System.out.println(name()+"::build_distances_array DONE ---------");
		 * System.out.println("distancesDrone"); for(int i=0; i<nPoints ; i++){
		 * for(int j=0; j<nPoints; j++){
		 * System.out.print(distancesDrone[i][j]+" "); } System.out.println(); }
		 * System.out.println("distancesTruck"); for(int i=0; i<nPoints ; i++){
		 * for(int j=0; j<nPoints; j++){
		 * System.out.print(distancesTruck[i][j]+" "); } System.out.println(); }
		 */
	}
	
	public double computeEuclice(double lat1, double long1,
			double lat2, double long2){
		return Math.sqrt((lat1-lat2)*(lat1-lat2)+(long1-long2)*(long1-long2));
	}
	
	public void build_distances_array() {
		// System.out.println(name()+"::build_distances_array-----------");
		int nPoints = clientPoints.size() + 2;
		distancesDrone = new double[nPoints][nPoints];
		distancesTruck = new double[nPoints][nPoints];
		ArrayList<Point> allPoints = new ArrayList<Point>();
		allPoints.add(startPoint);
		allPoints.addAll(clientPoints);
		allPoints.add(endPoint);

		GoogleMapsQuery gmap = new GoogleMapsQuery();

		for (int i = 0; i < allPoints.size(); i++) {
			Point pi = allPoints.get(i);

			for (int j = 0; j < allPoints.size(); j++)
				if (i == j) {
					distancesTruck[pi.getID()][pi.getID()] = 0;
					distancesDrone[pi.getID()][pi.getID()] = 0;
				} else {
					Point pj = allPoints.get(j);
					distancesDrone[pi.getID()][pj.getID()] = gmap
							.computeDistanceHaversine(pi.getLat(), pi.getLng(),
									pj.getLat(), pj.getLng());
					double dis = gmap.getDistance(pi.getLat(), pi.getLng(),
							pj.getLat(), pj.getLng());
					if (dis == -1) {
						distancesTruck[pi.getID()][pj.getID()] = gmap
								.getApproximateDistanceMeter(pi.getLat(),
										pi.getLng(), pj.getLat(), pj.getLng()) / 1000;
					} else {
						distancesTruck[pi.getID()][pj.getID()] = dis;
					}
					distancesDrone[pj.getID()][pi.getID()] = distancesDrone[pi
							.getID()][pj.getID()];
					distancesTruck[pj.getID()][pi.getID()] = distancesTruck[pi
							.getID()][pj.getID()];
				}
		}

		// System.out.println(name()+"::build_distances_array DONE ---------");
		// System.out.println("distancesDrone");
		// for(int i=0; i<nPoints ; i++){
		// for(int j=0; j<nPoints; j++){
		// System.out.print(distancesDrone[i][j]+" ");
		// }
		// System.out.println();
		// }
		// System.out.println("distancesTruck");
		// for(int i=0; i<nPoints ; i++){
		// for(int j=0; j<nPoints; j++){
		// System.out.print(distancesTruck[i][j]+" ");
		// }
		// System.out.println();
		// }
	}
	
	/*
	public void build_P() {
		P = new ArrayList<DroneDelivery>();
		for (int i = 0; i < clientPoints.size() - 2; i++) {
			Point pi = clientPoints.get(i);
			for (int j = 0; j < clientPoints.size() - 1; j++) {
				if (i == j)
					continue;

				Point pj = clientPoints.get(j);
				for (int k = 0; k < clientPoints.size(); k++) {
					if (j == k || i == k)
						continue;

					Point pk = clientPoints.get(k);
					double dDrone = d_drone(pi, pj) + d_drone(pj, pk);
					if (dDrone <= e
							&& Math.abs(d_truck(pi, pk) / truckSpeed - dDrone
									/ droneSpeed) * 60 <= delta) {
						P.add(new DroneDelivery(pi, pj, pk));
					}
				}
			}
		}

		for (int i = 0; i < clientPoints.size() - 1; i++) {
			Point pi = clientPoints.get(i);
			for (int j = 0; j < clientPoints.size(); j++) {
				if (i == j)
					continue;

				Point pj = clientPoints.get(j);

				double dsDrone = d_drone(startPoint, pi) + d_drone(pi, pj);
				double deDrone = d_drone(pi, pj) + d_drone(pj, endPoint);

				if (dsDrone <= e
						&& Math.abs(d_truck(startPoint, pj) / truckSpeed
								- dsDrone / droneSpeed) * 60 <= delta) {
					P.add(new DroneDelivery(startPoint, pi, pj));
				}

				if (deDrone <= e
						&& Math.abs(d_truck(pi, endPoint) / truckSpeed
								- deDrone / droneSpeed) * 60 <= delta) {
					P.add(new DroneDelivery(pi, pj, endPoint));
				}
			}
		}
		// System.out.println("build_P P="+P.toString());
	}
	
	public boolean inP(Point i, Point j, Point k) {
		for (int in = 0; in < P.size(); in++) {
			DroneDelivery dd = P.get(in);
			if (dd.getLauch_node().equals(i) && dd.getDrone_node().equals(j)
					&& dd.getRendezvous_node().equals(k)) {
				return true;
			}
		}
		return false;
	}
	*/
	
	public double d_drone(Point i, Point j) {
		// System.out.println()
		return distancesDrone[i.getID()][j.getID()];
	}

	public double d_truck(Point i, Point j) {
		return distancesTruck[i.getID()][j.getID()];
	}

	public double cost(Point i, Point j) {
		return C1 * d_truck(i, j);
	}

	public double cost(TruckTour td) {
		ArrayList<Point> point_tour = td.getTruck_tour();
		double cost = 0;
		for (int i = 0; i < point_tour.size() - 1; i++) {
			cost += C1 * d_truck(point_tour.get(i), point_tour.get(i + 1));
		}
		return cost;
	}

	public double cost(DroneDelivery dd) {
		return C2
				* (d_drone(dd.getLauch_node(), dd.getDrone_node()) + d_drone(
						dd.getDrone_node(), dd.getRendezvous_node()));
	}

	public double cost(ArrayList<DroneDelivery> dd) {
		double cost = 0;
		for (int i = 0; i < dd.size(); i++) {
			cost += cost(dd.get(i));
		}
		return cost;
	}

	public double cost(int i, int k, ArrayList<Point> s) {
		double cost = 0;
		for (int j = i; j < k; j++) {
			cost += C1 * d_truck(s.get(j), s.get(j + 1));
		}
		return cost;
	}

	public double cost(Point i, Point j, Point k) {
		return C2 * (d_drone(i, j) + d_drone(j, k));
	}
	
	public double cost(Point i, List<Point> list_drones, Point k) {
		double cost = 0;
		for(int in = 0; in < list_drones.size(); in++){
			cost += cost(i, list_drones.get(in), k);
		}
		return cost;
	}
	
	public double cost(Tour tspd) {
		TruckTour td = tspd.getTD();
		ArrayList<DroneDelivery> dd = tspd.getDD();
		return cost(td) + cost(dd);
	}
	
	//compute cost from i to k if move list_drones to drone
	public double cost(int i, int k, List<Point> list_drones, ArrayList<Point> tsp){
		
		ArrayList<Point> tsp_copy = new ArrayList<Point>();
		for(int in=0; in<tsp.size(); in++){
			tsp_copy.add(tsp.get(in));
		}
		
		double cost = 0.0;
		//System.out.println("TSPD:: evaluate cost  ::");
		//System.out.println("input tsp = "+tsp.toString());
		//System.out.println("tsp_copy = "+tsp_copy.toString());
		
		Point pi = tsp.get(i);
		Point pk = tsp.get(k);
		
		//System.out.println("i="+i+" pi="+pi.toString()+"  k="+k+" pk="+pk.toString()+"  listdrones="+list_drones.toString()+"tsp_tour="+tsp.toString());
		
		
		//int[] idx = new int[list_drones.size()];
		
		for(int j=0; j<list_drones.size(); j++){
			Point pj = list_drones.get(j);
			//int id=tsp.indexOf(pj);
			//idx[j] = id;
			tsp_copy.remove(pj);
		}
		
		int k_new = tsp_copy.indexOf(pk);
		
		//System.out.println("tsp_copy(check)= "+tsp_copy.toString());
		if(checkDroneConstraint(pi, list_drones, pk, tsp_copy)){
			cost += (cost(i, k_new, tsp_copy)+ cost(pi, list_drones, pk));
			//System.out.println("cost = "+cost+" output tsp = "+tsp.toString());
			return cost;
		}else{
			//System.out.println("output tsp = "+tsp.toString());
			return Double.MAX_VALUE;
		}
	}
	
	public boolean checkDroneWaitTime(Point pi, Point pj, Point pk, ArrayList<Point> truckTour){
		
		//System.out.println(pj.getID());
		if(!allowDrone.get(pj.getID()))
			return false; 
		
		int iLaunchNode = truckTour.indexOf(pi);
		int irendezvousNode = truckTour.indexOf(pk);
		// System.out.println("TSPD::checkWaitTime("+i.getID()+","+j.getID()+","+k.getID()+")::index_i="+iLaunchNode+"  index_k="+irendezvousNode);
		// System.out.println("TSPD::checkWaitTime::trunckTour: "+truckTour.toString());
		double distanceTruck = 0;
		// System.out.println(name()+"size "+truckTour.size());
		
		for (int in = iLaunchNode; in < irendezvousNode; in++) {
			// System.out.println(name()+"in "+in);
			distanceTruck += d_truck(truckTour.get(in), truckTour.get(in + 1));
		}
		
		return (Math.abs(distanceTruck / truckSpeed
				- (d_drone(pi, pj) + d_drone(pj, pk)) / droneSpeed) * 60 <= delta);
	}
	
	public boolean checkDroneEndurance(Point pi, Point pj, Point pk){
		System.out.println("pj = "+pj.getID());
		if(!allowDrone.get(pj.getID()))
			return false;
		
		return (d_drone(pi, pj) + d_drone(pj, pk)) <= e;
	}
	
	public boolean checkDroneConstraint(Point pi, Point pj, Point pk, ArrayList<Point> truckTour){
		return (checkDroneWaitTime(pi, pj, pk, truckTour) && checkDroneEndurance(pi, pj, pk));
	}
	
	public boolean checkDroneConstraint(Point pi, List<Point> list_drones, Point pk,
			ArrayList<Point> truckTour) {
		
		boolean check = true;
		for(int i=0; i<list_drones.size(); i++){
			Point pj = list_drones.get(i);
			check = check && checkDroneConstraint(pi, pj, pk, truckTour);
		}
		
		return check;
	}
	
	public boolean checkConstraint(Tour tour) {
		// System.out.print(b);
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> dronDeliveries = tour.getDD();

		boolean checkWaitime = true;
		boolean checkDroneEndurance = true;
		for (int i = 0; i < dronDeliveries.size(); i++) {
			DroneDelivery dd_tmp = dronDeliveries.get(i);
			if (!checkDroneWaitTime(dd_tmp.getLauch_node(), dd_tmp.getDrone_node(),
					dd_tmp.getRendezvous_node(), truckTour)) {
				checkWaitime = false;
			}
			if(!checkDroneEndurance(dd_tmp.getLauch_node(), dd_tmp.getDrone_node(), dd_tmp.getRendezvous_node())){
				checkDroneEndurance = false;
			}
		}
		boolean check = checkWaitime && checkDroneEndurance;
		// System.out.println("checkConstraint("+tour.toString()+") -> "+check);
		return check;
	}
	
	public int countDroneFlying(int i, int j, Tour tour){
		
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		int nDroneFlying = 0;
		
		for(int it = 0; it < droneDeliveries.size(); it++){
			DroneDelivery dd = droneDeliveries.get(it);
			Point li = dd.getLauch_node();
			Point ri = dd.getRendezvous_node();
			
			int i_li = truckTour.indexOf(li);
			int i_ri = truckTour.indexOf(ri);
			
			if(i_li >= j || i >= i_ri)
				continue;
				
			nDroneFlying++;
		}
		
		return nDroneFlying;
	}
	
	public boolean evaluateDroneWaitime(int i, int j, int k, ArrayList<Point> truckTour){
		
		Point pi = truckTour.get(i);
		Point pj = truckTour.get(j);
		Point pk = truckTour.get(k);
		
		double truck_distance = 0;
		
		for(int index=i; index<k; index++){
			truck_distance += d_truck(truckTour.get(index), truckTour.get(index+1));
		}
		
		Point prev_pj = truckTour.get(j-1);
		Point next_pj = truckTour.get(j+1);
		
		truck_distance = truck_distance - d_truck(prev_pj, pj) - d_truck(pj, next_pj) + d_truck(prev_pj, next_pj);
		
		return (Math.abs(truck_distance / truckSpeed
				- (d_drone(pi, pj) + d_drone(pj, pk)) / droneSpeed) * 60 <= delta);
	}
	
	public double evaluateCost(int i, List<Integer> lj, int k, ArrayList<Point> truckTour){
		
		ArrayList<Point> truckSubTour = new ArrayList<Point>();
		for(int it = i; it <= k; it++){
			truckSubTour.add(truckTour.get(it));
		}
		
		for(int it=0; it < lj.size(); it++){
			if(lj.get(it) != -1){
				Point pj = truckTour.get(lj.get(it));
				truckSubTour.remove(pj);
			}
		}
		
		double truck_distance = 0;
		double truck_cost = 0;
		for(int it = 0; it < truckSubTour.size()-1; it++){
			truck_distance += d_truck(truckSubTour.get(it), truckSubTour.get(it+1));
			truck_cost += cost(truckSubTour.get(it), truckSubTour.get(it+1));
		}
		double truck_time = truck_distance/truckSpeed;
		
		Point pi = truckTour.get(i);
		Point pk = truckTour.get(k);
		boolean check = true;
		
		double drone_cost = 0;
		for(int it = 0; it < lj.size(); it++){
			if(lj.get(it) != -1){
				Point pj = truckTour.get(lj.get(it));
				double drone_time = (d_drone(pi, pj) + d_drone(pj, pk)) / droneSpeed;
				drone_cost += cost(pi,pj,pk); 
				check = check && ((Math.abs(truck_time - drone_time)*60) <= delta);
			}
		}
		
		if(check)
			return (truck_cost + drone_cost);
		
		return Double.MAX_VALUE;
	}
	
	public boolean evaluateRemove(Point pi, Tour tour){
		//LOGGER.LOGGER.log(Level.INFO,"pi = "+pi.toString()+ " tour = "+tour.toString());
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
	
		int i_pi = truckTour.indexOf(pi);
		
		if(i_pi == -1) 
			return true;
		
		for(int i=0; i<droneDeliveries.size(); i++){
			DroneDelivery di = droneDeliveries.get(i);
			//LOGGER.LOGGER.log(Level.INFO, di.toString());
			Point li = di.getLauch_node();
			Point ri = di.getRendezvous_node();
			int i_li = truckTour.indexOf(li);
			int i_ri = truckTour.indexOf(ri);
			
			if(i_li < i_pi && i_ri > i_pi){
				double truck_distance = 0;
				
				for(int index=i_li; index<i_ri; index++){
					truck_distance += d_truck(truckTour.get(index), truckTour.get(index+1));
				}
				
				Point prev_pi = truckTour.get(i_pi-1);
				Point next_pi = truckTour.get(i_pi+1);
				
				truck_distance = truck_distance - d_truck(prev_pi, pi) - d_truck(pi, next_pi) + d_truck(prev_pi, next_pi);
			
				double truck_time = truck_distance/truckSpeed;
				double drone_time = (d_drone(li, di.getDrone_node()) + d_drone(di.getDrone_node(), ri)) / droneSpeed;
				if((truck_time - drone_time)*60 > delta)
					return false;
			}
		}
		
		return true;
	}

	public double evaluateInsertasDrone(int i, Point pj, int k, Tour tour){
		
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		Point pi = truckTour.get(i);
		Point pk = truckTour.get(k);
		
		if(!allowDrone.get(pj.getID()))
			return Double.MAX_VALUE;
		
		if(!checkDroneWaitTime(pi, pj, pk, tour.getTD().getTruck_tour()))
			return Double.MAX_VALUE;
		
		if(!checkDroneEndurance(pi, pj, pk))
			return Double.MAX_VALUE;
		
		int nDroneflying = countDroneFlying(i, k, tour);
		if(nDroneflying >= nDrone)
			return Double.MAX_VALUE;
		
		return cost(pi,pj,pk);
		
	}

	public double evaluateInsertasTruck(Point pi, Point pj, Tour tour){
		
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> droneDeliveries = tour.getDD();
		
		int i_pi = truckTour.indexOf(pi);
		Point prev_pi = truckTour.get(i_pi-1);
		
		boolean check = true;
		
		for(int i=0; i<droneDeliveries.size(); i++){
			DroneDelivery di = droneDeliveries.get(i);
			Point li = di.getLauch_node();
			Point ri = di.getRendezvous_node();
			int i_li = truckTour.indexOf(li);
			int i_ri = truckTour.indexOf(ri);
			
			if(i_li < i_pi && i_ri >= i_pi){
				double truck_distance = 0;
				
				for(int index=i_li; index<i_ri; index++){
					truck_distance += d_truck(truckTour.get(index), truckTour.get(index+1));
				}
				
				truck_distance = truck_distance - d_truck(prev_pi, pi) + d_truck(prev_pi, pj) + d_truck(pj, pi);
				
				double truck_time = truck_distance/truckSpeed;
				double drone_time = (d_drone(li, di.getDrone_node()) + d_drone(di.getDrone_node(), ri)) / droneSpeed;
				
				check = check && ((truck_time - drone_time)*60 <= delta);
			}
		}
		
		if(check)
			return cost(prev_pi, pj) + cost(pj, pi) - cost(prev_pi, pi);
		
		return Double.MAX_VALUE;
		
	}
	
	/*
	public boolean isDroneDelivery(Point i, Point j, Point k,
			ArrayList<Point> trucktour) {
		for (int index = 0; index < P.size(); index++) {
			DroneDelivery tmp_dd = P.get(index);
			boolean check = tmp_dd.getLauch_node().equals(i)
					&& tmp_dd.getDrone_node().equals(j)
					&& tmp_dd.getRendezvous_node().equals(k);
			if (check && checkWaitTime(i, j, k, trucktour)) {
				return true;
			}
		}
		return false;
	}
	*/
	public ArrayList<Point> getTruckOnlyNodes(Tour tspd) {
		ArrayList<Point> TD = tspd.getTD().getTruck_tour();
		ArrayList<DroneDelivery> DD = tspd.getDD();

		ArrayList<Point> trunkOnlyNodes = new ArrayList<Point>();

		for (int i = 1; i < TD.size()-1; i++) {
			Point pi = TD.get(i);
			int check = 0;
			for (int j = 0; j < DD.size(); j++) {
				DroneDelivery dd_tmp = DD.get(j);
				if (dd_tmp.checkDroneDeliveryPoint(pi)) {
					check++;
				}
			}
			if (check == 0) {
				trunkOnlyNodes.add(pi);
			}
		}

		return trunkOnlyNodes;
	}

	public ArrayList<Point> getDroneNodes(Tour tspd) {
		
		ArrayList<DroneDelivery> DD = tspd.getDD();

		ArrayList<Point> droneNodes = new ArrayList<Point>();

		for (int j = 0; j < DD.size(); j++) {
			DroneDelivery dd_tmp = DD.get(j);
			droneNodes.add(dd_tmp.getDrone_node());
		}

		return droneNodes;
	}
	
	public Point drone(Point start, Point end, Tour tour) {
		ArrayList<DroneDelivery> listDroneDeli = tour.getDD();
		for (int i = 0; i < listDroneDeli.size(); i++) {
			DroneDelivery d = listDroneDeli.get(i);
			if (d.getLauch_node().equals(startPoint)
					|| d.getRendezvous_node().equals(end)) {
				return d.getDrone_node();
			}
		}
		return null;
	}

	public int checkNotOverLapDroneDelivery(DroneDelivery de, Tour tour) {
		int d[] = new int[tour.getTD().getTruck_tour().size()];
		for (int i = 0; i < d.length - 1; i++) {
			d[i] = 0;
		}
		ArrayList<DroneDelivery> lde = tour.getDD();
		ArrayList<Point> ltrt = tour.getTD().getTruck_tour();
		for (int i = 0; i < lde.size(); i++) {
			DroneDelivery ide = lde.get(i);
			for (int j = ltrt.indexOf(ide.getLauch_node()); j < ltrt
					.indexOf(ide.getRendezvous_node()); j++) {
				d[j] += 1;
				if (d[j] > 1)
					return -1;
			}
		}
		for (int j = ltrt.indexOf(de.getLauch_node()); j < ltrt.indexOf(de
				.getRendezvous_node()); j++) {
			if (d[j] > 0)
				return 0;
		}
		return 1;
	}

	String name() {
		return "TSPD:: ";
	}

	@Override
	public String toString() {
		return "TSPD [C1=" + C1 + ", C2=" + C2 + ", delta=" + delta + ", e="
				+ e + ", truckSpeed=" + truckSpeed + ", droneSpeed="
				+ droneSpeed + ", startPoint=" + startPoint + ", clientPoints="
				+ clientPoints + ", endPoint=" + endPoint + ", P=" + P
				+ ", distancesDrone=" + Arrays.toString(distancesDrone)
				+ ", distancesTruck=" + Arrays.toString(distancesTruck) + "]";
	}

}