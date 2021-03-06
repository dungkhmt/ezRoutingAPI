package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;

public class TSPDs {
	
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
	

	public ArrayList<DroneDelivery> getP() {
		return P;
	}

	public void setP(ArrayList<DroneDelivery> p) {
		P = p;
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

	public TSPDs(int c1, int c2, double delta, double e, double truckSpeed,
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
		build_P();
	}

	public TSPDs(int c1, int c2, double delta, double e, double truckSpeed,
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
		build_P();
	}

	public void build_distances_array() {
		System.out.println(name() + "::build_distances_array-----------");
		int nPoints = clientPoints.size() + 2;
		distancesDrone = new double[nPoints][nPoints];
		distancesTruck = new double[nPoints][nPoints];
		
		GoogleMapsQuery gmap = new GoogleMapsQuery();

		for (int i = 0; i < clientPoints.size(); i++) {
			Point pi = clientPoints.get(i);
			for (int j = 0; j < clientPoints.size(); j++) {
				Point pj = clientPoints.get(j);
				if (j == i) {
					distancesDrone[pi.getID()][pj.getID()] = 0;
					distancesTruck[pi.getID()][pj.getID()] = 0;
				} else {
					distancesDrone[pi.getID()][pj.getID()] = 
							gmap.computeDistanceHaversine(pi.getLat(), pi.getLng(),	pj.getLat(), pj.getLng());
							//computeEuclice(pi.getLat(), pi.getLng(),	pj.getLat(), pj.getLng());
					double dis = gmap.getDistance(pi.getLat(), pi.getLng(),
							pj.getLat(), pj.getLng());
					if (dis == -1) {
						distancesTruck[pi.getID()][pj.getID()] = gmap
								.getApproximateDistanceMeter(pi.getLat(),
										pi.getLng(), pj.getLat(), pj.getLng()) / 1000;
					} else {
						distancesTruck[pi.getID()][pj.getID()] = dis;
					}
				}
			}
		}

		for (int i = 0; i < clientPoints.size(); i++) {
			Point pi = clientPoints.get(i);
			distancesDrone[startPoint.getID()][pi.getID()] = 
					gmap.computeDistanceHaversine(startPoint.getLat(),	startPoint.getLng(), pi.getLat(), pi.getLng());
					//computeEuclice(startPoint.getLat(),	startPoint.getLng(), pi.getLat(), pi.getLng());
			distancesDrone[endPoint.getID()][pi.getID()] = distancesDrone[startPoint.getID()][pi.getID()];
			double dis = gmap.getDistance(startPoint.getLat(),
					startPoint.getLng(), pi.getLat(), pi.getLng());
			if (dis == -1) {
				distancesTruck[startPoint.getID()][pi.getID()] = 
						gmap.computeDistanceHaversine(startPoint.getLat(),	startPoint.getLng(), pi.getLat(), pi.getLng());
				distancesTruck[endPoint.getID()][pi.getID()] =distancesTruck[startPoint.getID()][pi.getID()];
			} else {
				distancesTruck[startPoint.getID()][pi.getID()] = dis;
				distancesTruck[endPoint.getID()][pi.getID()] =distancesTruck[startPoint.getID()][pi.getID()];
			}
		}
		
		for (int i = 0; i < clientPoints.size(); i++) {
			Point pi = clientPoints.get(i);
			distancesDrone[pi.getID()][startPoint.getID()] = 
					gmap.computeDistanceHaversine( pi.getLat(), pi.getLng(),startPoint.getLat(),startPoint.getLng()); //***
					//computeEuclice(pi.getLat(), pi.getLng(),startPoint.getLat(),startPoint.getLng());
			distancesDrone[pi.getID()][endPoint.getID()] = distancesDrone[pi.getID()][startPoint.getID()];
			double dis = gmap.getDistance( pi.getLat(), pi.getLng(),startPoint.getLat(),
					startPoint.getLng());
			if (dis == -1) {
				distancesTruck[pi.getID()][startPoint.getID()] = 
						gmap
						.computeDistanceHaversine( pi.getLat(), pi.getLng(),startPoint.getLat(),
								startPoint.getLng());
				distancesTruck[pi.getID()][endPoint.getID()] =distancesTruck[pi.getID()][startPoint.getID()];
			} else {
				distancesTruck[pi.getID()][startPoint.getID()] = dis;
				distancesTruck[pi.getID()][endPoint.getID()] =distancesTruck[pi.getID()][startPoint.getID()];
			}
		}
		System.out.println(name() + "::build_distances_array DONE ---------");
		System.out.println("distancesDrone");
		for (int i = 0; i < nPoints; i++) {
			for (int j = 0; j < nPoints; j++) {
				System.out.print(distancesDrone[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println("distancesTruck");
		for (int i = 0; i < nPoints; i++) {
			for (int j = 0; j < nPoints; j++) {
				System.out.print(distancesTruck[i][j] + " ");
			}
			System.out.println();
		}
	}

	public void build_distances_array(Map<String, Double> map) {
		System.out.println(name() + "::build_distances_array-----------");
		int nPoints = clientPoints.size() + 2;
		distancesDrone = new double[nPoints][nPoints];
		distancesTruck = new double[nPoints][nPoints];

		GoogleMapsQuery gmap = new GoogleMapsQuery();

		for (int i = 0; i < clientPoints.size(); i++) {
			Point pi = clientPoints.get(i);
			for (int j = 0; j < clientPoints.size(); j++) {
				Point pj = clientPoints.get(j);
				if (j == i) {
					distancesDrone[pi.getID()][pj.getID()] = 0;
					distancesTruck[pi.getID()][pj.getID()] = 0;
				} else {
					distancesDrone[pi.getID()][pj.getID()] = 
							gmap.computeDistanceHaversine(pi.getLat(), pi.getLng(),pj.getLat(), pj.getLng());
							//computeEuclice(pi.getLat(), pi.getLng(),pj.getLat(), pj.getLng());
					String key = pi.getID() + "_" + pj.getID();
					double dis = map.get(key);

					distancesTruck[pi.getID()][pj.getID()] = dis;

				}
			}
		}

		for (int i = 0; i < clientPoints.size(); i++) {
			Point pi = clientPoints.get(i);
			distancesDrone[startPoint.getID()][pi.getID()] = 
					gmap.computeDistanceHaversine(startPoint.getLat(),startPoint.getLng(), pi.getLat(), pi.getLng());
					//computeEuclice(startPoint.getLat(),startPoint.getLng(), pi.getLat(), pi.getLng());
			String key = startPoint.getID() + "_" + pi.getID();
			double dis = map.get(key);

			distancesTruck[startPoint.getID()][pi.getID()] = dis;
			distancesDrone[endPoint.getID()][pi.getID()] =distancesDrone[startPoint.getID()][pi.getID()];
			distancesTruck[endPoint.getID()][pi.getID()] = distancesTruck[startPoint.getID()][pi.getID()];
		}
		
		for (int i = 0; i < clientPoints.size(); i++) {
			Point pi = clientPoints.get(i);
			distancesDrone[pi.getID()][startPoint.getID()] = 
					gmap.computeDistanceHaversine( pi.getLat(), pi.getLng(),startPoint.getLat(),startPoint.getLng());
					//computeEuclice(pi.getLat(), pi.getLng(),startPoint.getLat(),startPoint.getLng());
			String key =  pi.getID()+ "_" +startPoint.getID();
			double dis = map.get(key);

			distancesTruck[pi.getID()][startPoint.getID()] = dis;
			distancesDrone[pi.getID()][endPoint.getID()] =distancesDrone[pi.getID()][startPoint.getID()];
			distancesTruck[pi.getID()][endPoint.getID()] = distancesTruck[pi.getID()][startPoint.getID()];
		}
		/*System.out.println(name() + "::build_distances_array DONE ---------");
		System.out.println("distancesDrone");
		for (int i = 0; i < nPoints; i++) {
			for (int j = 0; j < nPoints; j++) {
				System.out.print(distancesDrone[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println("distancesTruck");
		for (int i = 0; i < nPoints; i++) {
			for (int j = 0; j < nPoints; j++) {
				System.out.print(distancesTruck[i][j] + " ");
			}
			System.out.println();
		}*/
	}
	public double computeEuclice(double lat1, double long1,
			double lat2, double long2){
		return Math.sqrt((lat1-lat2)*(lat1-lat2)+(long1-long2)*(long1-long2));
	}
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
		//System.out.println("build_P P=" + P.toString());
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

	public double cost(Tour tspd) {
		TruckTour td = tspd.getTD();
		ArrayList<DroneDelivery> dd = tspd.getDD();
		return cost(td) + cost(dd);
	}
	/**
	 * delete droneList.remove(droneList.size() - 1);
	 */
	public boolean checkWaitTime(ArrayList<Point> truckTour,
			ArrayList<DroneDelivery> droneList) {
		// HashMap<Point, Integer> p2i= new HashMap<Point, Integer>();
		// System.out.println(name()+"checkWaitTime current truckTour" +
		// truckTour);
		// System.out.println(name()+"checkWaitTime new droneList" + droneList);
		HashMap<Point, ArrayList<Integer>> p2iDeliveryLaught = new HashMap<Point, ArrayList<Integer>>();
		HashMap<Point, ArrayList<Integer>> p2iDeliveryRendezvous = new HashMap<Point, ArrayList<Integer>>();
		for (int ii = 0; ii < droneList.size(); ii++) {
			if (p2iDeliveryLaught
					.containsKey(droneList.get(ii).getLauch_node())) {
				ArrayList<Integer> tmp = p2iDeliveryLaught.get(droneList
						.get(ii).getLauch_node());
				tmp.add(ii);
				p2iDeliveryLaught.put(droneList.get(ii).getLauch_node(), tmp);
			} else {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				tmp.add(ii);
				p2iDeliveryLaught.put(droneList.get(ii).getLauch_node(), tmp);
			}
			if (p2iDeliveryRendezvous.containsKey(droneList.get(ii)
					.getRendezvous_node())) {// here
				ArrayList<Integer> tmp = p2iDeliveryRendezvous.get(droneList
						.get(ii).getRendezvous_node());
				tmp.add(ii);
				p2iDeliveryRendezvous.put(droneList.get(ii)
						.getRendezvous_node(), tmp);
			} else {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				tmp.add(ii);
				p2iDeliveryRendezvous.put(droneList.get(ii)
						.getRendezvous_node(), tmp);
			}

		}
		// System.out.println(name()+" checkWaitTime p2iDeliveryLaught " +
		// p2iDeliveryLaught);
		// System.out.println(name()+" checkWaitTime p2iDeliveryRendezvous " +
		// p2iDeliveryRendezvous);
		// System.out.println(name()+"size "+truckTour.size());
		double x[] = new double[truckTour.size()];
		x[0] = 0;
		// System.out.println(name()+" checkWaitTime " + truckTour.size() +
		// " ");
		
		for (int ii = 1; ii < truckTour.size(); ii++) {

			x[ii] = x[ii - 1]
					+ d_truck(truckTour.get(ii - 1), truckTour.get(ii)) * 60
					/ truckSpeed;
			// ArrayList<DroneDelivery> ldr= new ArrayList<DroneDelivery>();
			ArrayList<Integer> li = p2iDeliveryRendezvous
					.get(truckTour.get(ii));
			if (li == null || li.size() == 0)
				continue;// here
			double max = 0;
			double min = 1000000000;
			Point vtmax = null;
			Point vtmin = null;
			for (int jj = 0; jj < li.size(); jj++) {
				DroneDelivery dd = droneList.get(li.get(jj));
				
				double time = (d_drone(dd.getLauch_node(), dd.getDrone_node()) + d_drone(
						dd.getDrone_node(), dd.getRendezvous_node()))
						* 60
						/ droneSpeed;
				if (time >= max) {
					max = time;
					vtmax = dd.getLauch_node();
				}
				if (time <= min) {
					min = time;
					vtmin = dd.getLauch_node();
				}
			}
			//System.out.println(name()+" checkWaitTime max ->" + max + " min ->"+min);
			if (x[ii] - x[truckTour.indexOf(vtmax)] > (max + delta)) {
				//droneList.remove(droneList.size() - 1);
				return false;
			} else 
			//System.out.println(name()+" checkWaitTime x[ii] - x[truckTour.indexOf(vtmax)]" + (x[ii] - x[truckTour.indexOf(vtmax)]));
			if ((x[ii] - x[truckTour.indexOf(vtmax)] + delta) < max) {
				//droneList.remove(droneList.size() - 1);
				return false;
			}//System.out.println(name()+" checkWaitTime x[ii] - x[truckTour.indexOf(vtmin)]" + (x[ii] - x[truckTour.indexOf(vtmin)]));
			if ((x[ii] - x[truckTour.indexOf(vtmin)]) > (min + delta)) {
				//droneList.remove(droneList.size() - 1);
				return false;
			} else if (x[ii] - x[truckTour.indexOf(vtmin)] + delta < min) {
				//droneList.remove(droneList.size() - 1);
				return false;
			}
			if (x[ii] - x[truckTour.indexOf(vtmax)] < max) {
				//x[ii] += min - (x[ii] - x[truckTour.indexOf(vtmin)]);
				x[ii] += max - (x[ii] - x[truckTour.indexOf(vtmax)]);
				
			}
		}
		return true;
	}

	public ArrayList<Point> getTruckOnlyNodes(Tour tspd) {
		ArrayList<Point> TD = tspd.getTD().getTruck_tour();
		ArrayList<DroneDelivery> DD = tspd.getDD();

		ArrayList<Point> trunkOnlyNodes = new ArrayList<Point>();

		for (int i = 0; i < TD.size(); i++) {
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
		ArrayList<Point> TD = tspd.getTD().getTruck_tour();
		ArrayList<DroneDelivery> DD = tspd.getDD();

		ArrayList<Point> droneNodes = new ArrayList<Point>();

		for (int j = 0; j < DD.size(); j++) {
			DroneDelivery dd_tmp = DD.get(j);
			droneNodes.add(dd_tmp.getDrone_node());
		}

		return droneNodes;
	}

	public boolean checkConstraint(Tour tour) {
		ArrayList<Point> truckTour = tour.getTD().getTruck_tour();
		ArrayList<DroneDelivery> dronDeliveries = tour.getDD();
		// System.out.println(name()+"checkConstraint current truckTour" +
		// truckTour);
		// System.out.println(name()+"checkConstraint new droneList" +
		// dronDeliveries);
		boolean checkWaitime = checkWaitTime(truckTour, dronDeliveries);
		// System.out.println(name() + "checkWaitime " + checkWaitime);
		boolean checkDroneEndurance = true;
		for (int i = 0; i < dronDeliveries.size(); i++) {
			DroneDelivery dd_tmp = dronDeliveries.get(i);
			double droneEndurance = d_drone(dd_tmp.getLauch_node(),
					dd_tmp.getDrone_node())
					+ d_drone(dd_tmp.getDrone_node(),
							dd_tmp.getRendezvous_node());
			if (droneEndurance > e)
				checkDroneEndurance = false;
		}
		boolean check = checkWaitime && checkDroneEndurance;
		// System.out.println("checkConstraint("+tour.toString()+") -> "+check);
		return check;
	}

	public Point drone(Point start, Point end, Tour tour) {
		ArrayList<Point> listTruckPoint = tour.getTD().getTruck_tour();
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

	public int checkOverQuantityDrone(DroneDelivery de, Tour tour, int K) {
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
				if (d[j] > K)
					return -1;
			}
		}
		for (int j = ltrt.indexOf(de.getLauch_node()); j < ltrt.indexOf(de
				.getRendezvous_node()); j++) {
			if (d[j] > K - 1)
				return 0;
		}
		return 1;
	}

	String name() {
		return "TSPDs:: ";
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
