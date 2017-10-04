package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.NeighborHood;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;

public class TSPDs_LS2 {
	int customers;
	Tour tour;
	TSPDs tspds;
	TSP tsp;

	public TSPDs_LS2(TSPDs tspkd) {
		this.tspds = tspkd;
	}

	void printDArr(double[][] arr, int m, int n) {
		System.out.println("***********************************");
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++)
				System.out.print(arr[i][j] + " ");
			System.out.println();
		}
		System.out.println(">***********************************");
	}

	public void init() {

		tsp = new TSP(tspds.getStartPoint(), tspds.getClientPoints(),
				tspds.getEndPoint());
		tsp.setDistances_matrix(tspds.getDistancesTruck());
		double x[][] = tspds.getDistancesDrone();

		// System.out.println(tsp.lsInitTSP());
		TruckTour truckTour = new TruckTour(tsp.lsInitTSP());
		ArrayList<DroneDelivery> droneTours = new ArrayList<DroneDelivery>();
		tour = new Tour(truckTour, droneTours);
		// printDArr(x, tspds.getClientPoints().size()+2 ,
		// tspds.getClientPoints().size()+2);
		x = tspds.getDistancesTruck();
		// printDArr(x, tspds.getClientPoints().size()+2 ,
		// tspds.getClientPoints().size()+2);
	}

	public Tour solve() {
		init();
		ArrayList<Point> customerPoints = tspds.getClientPoints();
		System.out.println(name() + " solve init " + tour.getTD().toString());
		System.out.println(name() + " solve init " + tour.getDD().toString());
		boolean d[] = new boolean[tour.getTD().getTruck_tour().size() + 1];

		for (int i = 0; i < d.length; i++)
			d[i] = true;
		while (true) {
			ArrayList<Point> truckTourList = tour.getTD().getTruck_tour();
			double globalMaxSavings = 0;
			DronesNeighborHood dnhGlobal = new DronesNeighborHood(0, 0);
			for (int ik = 1; ik <= tspds.getK(); ik++) {
				for (int i = 1; i < truckTourList.size() - (ik + 1); i++) {
					System.out.println(name()+" solve truckTourSize1 "+truckTourList.size());
					System.out.println(name()+" solve i="+i+" ik= "+ik);
					boolean xd = true;
					for (int j = i; j < i + ik; j++) {
						if (d[truckTourList.get(j).getID()] == false) {
							xd = false;
							break;
						}
					}
					if (xd == false)
						continue;
					ArrayList<Point> wllbeDrone = new ArrayList<Point>();
					double savings = -tspds.cost(truckTourList.get(i-1),
							truckTourList.get(i + ik ));
					for (int ii = 0; ii < ik; ii++) {
						savings = +tspds.cost(truckTourList.get(i + ii-1),
								truckTourList.get(i + ii ));
					}
					for (int ii = 0; ii < ik; ii++) {
						wllbeDrone.add(truckTourList.get(i));
						truckTourList.remove(i);
					}
					TruckTour t = new TruckTour(truckTourList);
					tour.setTD(t);
					DronesNeighborHood dnh = new DronesNeighborHood(ik, i);
					double maxSavings = 0;
					int isBreak = 0;
					System.out.println(name()+" solve truckTourSize "+truckTourList.size());
					for (int ii = 0; ii < wllbeDrone.size(); ii++) {
						dnh.addADroneDelivery(null);
						double localMaxSaving=0;
						for (int jj = 0; jj < truckTourList.size() - 1; jj++)
							for (int kk = jj + 1; kk < truckTourList.size(); kk++) {
								double cost = caculRelocateAsDrone(
										wllbeDrone.get(ii), jj, kk, savings);
								if (cost > localMaxSaving) {
									dnh.setDroneLast(new DroneDelivery(
											truckTourList.get(jj), wllbeDrone
													.get(ii), truckTourList
													.get(kk)));
									dnh.setSavings(cost);
									localMaxSaving = cost;
								}
							}
						DroneDelivery de = dnh.getDroneDeliveryLast();
						if (de == null) {
							dnh.removeLastDroneDelivery();
							isBreak = 1;
							// add check break
							break;
						}
						maxSavings=maxSavings+localMaxSaving;
						ArrayList<DroneDelivery> lde = tour.getDD();
						lde.add(de);
						tour.setDD(lde);
					}
					System.out.println(name()+" solve maxSavings"+maxSavings);
					System.out.println(name()+" sovle ik "+ik);
					System.out.println(name()+" sovle break "+isBreak);
					if (globalMaxSavings < maxSavings && isBreak != 1) {
						globalMaxSavings = maxSavings;
						System.out.println(name()+" solve dnhGlobal "+dnhGlobal);
						dnhGlobal = dnh;
					}
					// here
					for (int ii = 0; ii < ik; ii++) {
						// wllbeDrone.add(truckTourList.get(i+1));
						truckTourList.add(i + ii, wllbeDrone.get(ii));
					}
					// System.out.println(name()+" solve 2"+truckTourList.toString());
					t = new TruckTour(truckTourList);
					tour.setTD(t);
					ArrayList<DroneDelivery> lde = tour.getDD();
					for (int ii = 0; ii < dnh.getLde().size(); ii++) {
						lde.remove(lde.size() - 1);
					}
					tour.setDD(lde);
				}
			}
			System.out.println(name()+" solve globalMaxSavings"+globalMaxSavings);
			System.out.println(name()+" solve dnhGlobal "+dnhGlobal);
			if (globalMaxSavings <= 0)
				break;
			for (int ii = 0; ii < dnhGlobal.getIk(); ii++) {
				// wllbeDrone.add(truckTourList.get(dnhGlobal.getTruckPointIndex()+ii+1));
				truckTourList.remove(dnhGlobal.getTruckPointIndex());
			}
			System.out.println(name() + " solve 2 " + truckTourList.toString());
			TruckTour t = new TruckTour(truckTourList);
			tour.setTD(t);
			ArrayList<DroneDelivery> lde = tour.getDD();
			System.out.println(name() + " solve 2 dronedelivery "
					+ dnhGlobal.getLde());
			for (int i = 0; i < dnhGlobal.getLde().size(); i++) {
				DroneDelivery de = dnhGlobal.getLde().get(i);
				d[de.getDrone_node().getID()] = false;
				d[de.getRendezvous_node().getID()] = false;
				d[de.getLauch_node().getID()] = false;
				lde.add(dnhGlobal.getLde().get(i));
			}
			tour.setDD(lde);
			System.out.println(name() + " solve " + tour.getTD().toString());
			System.out.println(name() + " solve " + tour.getDD().toString());
			System.out.println(name()
					+ " solve *******************************");
		}
		System.out.println(name()
				+ " solve "
				+ tspds.checkWaitTime(tour.getTD().getTruck_tour(),
						tour.getDD()));
		return tour;
	}

	public double caculRelocateAsDrone(Point j, int laught, int revouz,
			double savings) {
		double sol = -1000;
		ArrayList<Point> truckPoint = tour.getTD().getTruck_tour();
		System.out.println(name()+"truckTour:: "+truckPoint);
		DroneDelivery de = new DroneDelivery(truckPoint.get(laught), j,
				truckPoint.get(revouz));
		if (tspds.checkOverQuantityDrone(de, tour, tspds.getK()) != 1)
			return sol;
		ArrayList<DroneDelivery> lde = tour.getDD();
		lde.add(de);
		tour.setDD(lde);
		if (tspds.checkConstraint(tour)) {
			System.out.println(name()+" checkConstraint "+ true);
			double delta = tspds.cost(truckPoint.get(laught), j,
					truckPoint.get(revouz));
			sol = savings - delta;
		}
		lde.remove(de);
		tour.setDD(lde);
		System.out.println(name()+" caculRelocateAsDrone "+sol);
		return sol;
	}

	String name() {
		return "TSPDs_LS:: ";
	}
}
