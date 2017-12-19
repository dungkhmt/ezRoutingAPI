package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import com.kse.ezRoutingAPI.tspd.model.DroneDelivery;
import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.model.TruckTour;

public class TSPDs_LS {
	int customers;
	Tour tour;
	TSPDs tspds;
	//TSP tsp;
	int maxRangeMove;
	private int K;
	Map<Integer, Boolean> allowDrone;

	public int getK() {
		return K;
	}

	public void setK(int k) {
		K = k;
	}

	public TSPDs_LS(TSPDs tspkd, int numOfDrone, int maxRangeMove,
			Map<Integer, Boolean> map) {
		this.tspds = tspkd;
		this.K = numOfDrone;
		this.maxRangeMove = maxRangeMove;
		this.allowDrone = map;
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

	public void init(ArrayList<Point> truckPointTourPoint) {

	/*	tsp = new TSP(tspds.getStartPoint(), tspds.getClientPoints(),
				tspds.getEndPoint());
		tsp.setDistances_matrix(tspds.getDistancesTruck());
		double x[][] = tspds.getDistancesDrone();

		// System.out.println(tsp.lsInitTSP());
		TruckTour truckTour = new TruckTour(tsp.lsInitTSP());*/
		ArrayList<Point> arr= new ArrayList<Point>();
		arr.addAll(truckPointTourPoint);
		TruckTour truckTour = new TruckTour(arr);
		ArrayList<DroneDelivery> droneTours = new ArrayList<DroneDelivery>();
		tour = new Tour(truckTour, droneTours);
		// printDArr(x, tspds.getClientPoints().size()+2 ,
		// tspds.getClientPoints().size()+2);
		//x = tspds.getDistancesTruck();
		// printDArr(x, tspds.getClientPoints().size()+2 ,
		// tspds.getClientPoints().size()+2);
	}

	public Tour solve(ArrayList<Point> truckPointTourPoint) {
		init(truckPointTourPoint);
		System.out.println(allowDrone.toString());
		int d[] = new int[tour.getTD().getTruck_tour().size() + 1];

		for (int i = 0; i < d.length; i++)
			d[i] = 0;
		while (true) {
			// int movePoint = maxRangeMove;
			ArrayList<Point> truckTourList = tour.getTD().getTruck_tour();
			double maxkPointRelocationSavings = 0;
			DronesNeighborHood dnhGlobal = new DronesNeighborHood(0, 0);
			for (int ik = 1; ik <= maxRangeMove; ik++) {
				for (int i = 1; i < truckTourList.size() - (ik + 1); i++) {
					boolean xd = true;
					for (int j = i; j < i + ik; j++) {
						if (d[truckTourList.get(j).getID()] != 0
								|| !allowDrone
										.get(truckTourList.get(j).getID())) {
							xd = false;
							break;
						}
					}
					if (xd == false)
						continue;
					ArrayList<Point> wllbeDrone = new ArrayList<Point>();
					double savings = -tspds.cost(truckTourList.get(i - 1),
							truckTourList.get(i + ik));
					for (int ii = 0; ii < ik + 1; ii++) {
						savings += tspds.cost(truckTourList.get(i + ii - 1),
								truckTourList.get(i + ii));
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

					for (int ii = 0; ii < wllbeDrone.size(); ii++) {
						dnh.addADroneDelivery(null);
						double localMaxSaving = -1000000000.0;
						for (int jj = 0; jj < truckTourList.size() - 1; jj++)
							for (int kk = jj + 1; kk < truckTourList.size(); kk++) {
								double cost = caculRelocateAsDrone(
										wllbeDrone.get(ii), jj, kk, 0);
								if (cost > localMaxSaving) {// here
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
							break;
						}
						maxSavings = maxSavings + localMaxSaving;
						ArrayList<DroneDelivery> lde = tour.getDD();
						lde.add(de);
						tour.setDD(lde);
					}
					//maxSavings =( maxSavings+savings) / ik;
					maxSavings =maxSavings+savings;
					if (maxkPointRelocationSavings < maxSavings && isBreak != 1) {
						maxkPointRelocationSavings = maxSavings;

						dnhGlobal = dnh;
					}
					for (int ii = 0; ii < ik; ii++) {
						truckTourList.add(i + ii, wllbeDrone.get(ii));
					}
					t = new TruckTour(truckTourList);
					tour.setTD(t);
					ArrayList<DroneDelivery> lde = tour.getDD();
					for (int ii = 0; ii < dnh.getLde().size(); ii++) {
						lde.remove(lde.size() - 1);
					}
					tour.setDD(lde);
				}
			}
			ArrayList<DroneDelivery> lde = tour.getDD();
			/**
			 * swap truck drone
			 */
			double maxSwapTruckDrone = 0;
			int vtDD = -1;
			int vtTD = -1;
			for (int i = 0; i < lde.size(); i++)
				for (int j = 1; j < truckTourList.size() - 1; j++)
					if (d[truckTourList.get(j).getID()] == 0
							&& allowDrone.get(truckTourList.get(j).getID())) {
						DroneDelivery olddd = lde.get(i);
						double savings = tspds.cost(olddd)
								- tspds.cost(olddd.getLauch_node(),
										truckTourList.get(j),
										olddd.getRendezvous_node())
								+ tspds.cost(truckTourList.get(j - 1),
										truckTourList.get(j))
								+ tspds.cost(truckTourList.get(j),
										truckTourList.get(j + 1))
								- tspds.cost(truckTourList.get(j - 1),
										olddd.getDrone_node())
								- tspds.cost(olddd.getDrone_node(),
										truckTourList.get(j + 1));
						savings=Math.round(savings*1000000)/1000000.0d;
						Point truckPoint = olddd.getDrone_node();
						Point drone = truckTourList.get(j);
						DroneDelivery dd = new DroneDelivery(
								olddd.getLauch_node(), drone,
								olddd.getRendezvous_node());
						lde.set(i, dd);
						truckTourList.set(j, truckPoint);
						TruckTour t = new TruckTour(truckTourList);
						tour.setTD(t);
						tour.setDD(lde);
						if (!tspds.checkConstraint(tour)) {
							savings = -10000000;
						}
						truckTourList.set(j, drone);
						lde.set(i, olddd);
						tour.setTD(t);
						tour.setDD(lde);
						if (maxSwapTruckDrone < savings) {
							maxSwapTruckDrone = savings;
							vtDD = i;
							vtTD = j;
						}
					}
			/**
			 * swap 2 truck
			 */
			double maxSwap2Truck = 0;
			int vtsT1 = -1;
			int vtsT2 = -1;
			for (int i = 1; i < truckTourList.size() - 2; i++)
				if (d[truckTourList.get(i).getID()] == 0)
					for (int j = i + 1; j < truckTourList.size() - 1; j++)
						if (d[truckTourList.get(j).getID()] == 0) {
							double savings = 0;
							if ((j - i) != 1)
								savings = tspds.cost(truckTourList.get(i - 1),
										truckTourList.get(i))
										+ tspds.cost(truckTourList.get(i),
												truckTourList.get(i + 1))
										+ tspds.cost(truckTourList.get(j - 1),
												truckTourList.get(j))
										+ tspds.cost(truckTourList.get(j),
												truckTourList.get(j + 1))
										- (tspds.cost(truckTourList.get(i - 1),
												truckTourList.get(j))
												+ tspds.cost(truckTourList
														.get(j), truckTourList
														.get(i + 1))
												+ tspds.cost(truckTourList
														.get(j - 1),
														truckTourList.get(i)) + tspds
													.cost(truckTourList.get(i),
															truckTourList
																	.get(j + 1)));
							else
								savings = tspds.cost(truckTourList.get(i - 1),
										truckTourList.get(i))
										+ tspds.cost(truckTourList.get(i),
												truckTourList.get(j))
										+ tspds.cost(truckTourList.get(j),
												truckTourList.get(j + 1))
										- (tspds.cost(truckTourList.get(i - 1),
												truckTourList.get(j))
												+ tspds.cost(
														truckTourList.get(j),
														truckTourList.get(i)) + tspds
													.cost(truckTourList.get(i),
															truckTourList
																	.get(j + 1)));
							savings=Math.round(savings*1000000)/1000000.0d;
							Point tmp = truckTourList.get(i);
							truckTourList.set(i, truckTourList.get(j));
							truckTourList.set(j, tmp);
							TruckTour t = new TruckTour(truckTourList);
							tour.setTD(t);
							if (!tspds.checkConstraint(tour)) {
								savings = -10000000;
							}
							tmp = truckTourList.get(i);
							truckTourList.set(i, truckTourList.get(j));
							truckTourList.set(j, tmp);
							t = new TruckTour(truckTourList);
							tour.setTD(t);
							if (maxSwap2Truck < savings) {
								maxSwap2Truck = savings;
								vtsT1 = i;
								vtsT2 = j;
							}
						}
			/**
			 * swap 2 drone
			 */
			double maxSwap2Drone = 0;
			int swap2Dronevt1 = -1;
			int swap2Dronevt2 = -1;
			for (int i = 0; i < lde.size() - 1; i++)
				for (int j = i + 1; j < lde.size(); j++) {
					DroneDelivery dd1 = lde.get(i);
					DroneDelivery dd2 = lde.get(j);
					DroneDelivery dd1new = new DroneDelivery(
							dd1.getLauch_node(), dd2.getDrone_node(),
							dd1.getRendezvous_node());
					DroneDelivery dd2new = new DroneDelivery(
							dd2.getLauch_node(), dd1.getDrone_node(),
							dd2.getRendezvous_node());

					double savings = tspds.cost(dd1) + tspds.cost(dd2)
							- tspds.cost(dd1new) - tspds.cost(dd2new);
					savings=Math.round(savings*1000000)/1000000.0d;
					lde.set(i, dd1new);
					lde.set(j, dd2new);
					tour.setDD(lde);
					if (!tspds.checkConstraint(tour)) {
						savings = -10000000;
					}
					lde.set(i, dd1);
					lde.set(j, dd2);
					tour.setDD(lde);
					if (maxSwap2Drone < savings) {
						maxSwap2Drone = savings;
						swap2Dronevt1 = i;
						swap2Dronevt2 = j;
					}
				}
			/**
			 * truck relocation
			 */
			double maxTruckRelocation = 0;
			int vtTruckRelocation = -1;
			int vtTruckRelocation1 = -1;
			for (int i = 1; i < truckTourList.size() - 1; i++)
				if (d[truckTourList.get(i).getID()] == 0)
					for (int j = 1; j < truckTourList.size(); j++)
						if (i != j && i + 1 != j) {
							double savings = tspds.cost(
									truckTourList.get(i - 1),
									truckTourList.get(i))
									+ tspds.cost(truckTourList.get(i),
											truckTourList.get(i + 1))
									+ tspds.cost(truckTourList.get(j - 1),
											truckTourList.get(j))
									- (tspds.cost(truckTourList.get(i - 1),
											truckTourList.get(i + 1))
											+ tspds.cost(
													truckTourList.get(j - 1),
													truckTourList.get(i)) + tspds
												.cost(truckTourList.get(i),
														truckTourList
																.get(j)));
							savings=Math.round(savings*1000000)/1000000.0d;
							Point p = truckTourList.remove(i);
							truckTourList.add(j, p);
							TruckTour t = new TruckTour(truckTourList);
							tour.setTD(t);
							if (!tspds.checkConstraint(tour)) {
								savings = -1000000;
							}
							p = truckTourList.remove(j);
							truckTourList.add(i, p);
							t = new TruckTour(truckTourList);
							tour.setTD(t);
							if (maxTruckRelocation < savings) {
								maxTruckRelocation = savings;
								vtTruckRelocation1 = j;
								vtTruckRelocation=i;
							}
						}
			/**
			 * Drone relocation
			 */
			double maxDroneRelocation = 0;
			int vtMaxDroneRelocation=-1;
			int vtMaxDroneRelocation1 = -1;
			int vtMaxDroneRelocation2 = -1;
			for (int i = 0; i < lde.size(); i++)
				for (int j = 0; j < truckTourList.size() - 1; j++)
					for (int jj = j + 1; jj < truckTourList.size(); jj++) {
						DroneDelivery dd = lde.get(i);
						double savings = tspds.cost(dd)
								- tspds.cost(truckTourList.get(j),
										dd.getDrone_node(),
										truckTourList.get(jj));
						DroneDelivery dd2 = new DroneDelivery(
								truckTourList.get(j), dd.getDrone_node(),
								truckTourList.get(jj));
						savings=Math.round(savings*1000000)/1000000.0d;
						if (tspds.checkOverQuantityDrone(dd2, tour, getK()) != 1) {
							savings = -10000000;
							continue;
						}
						lde.set(i, dd2);
						tour.setDD(lde);
						if (!tspds.checkConstraint(tour)) {
							savings = -100000;
						}
						lde.set(i, dd);
						tour.setDD(lde);
						if (savings > maxDroneRelocation) {
							maxDroneRelocation = savings;
							vtMaxDroneRelocation=i;
							vtMaxDroneRelocation1 = j;
							vtMaxDroneRelocation2 = jj;
						}
					}
			/**
			 * Drone removal
			 */
			double maxDroneRemoval = 0;
			int vtMaxDroneRemoval=-1;
			int vtMaxDroneRemoval1 = -1;
			for (int i = 0; i < lde.size(); i++)
				for (int j = 1; j < truckTourList.size(); j++) {
					DroneDelivery dd = lde.get(i);
					double savings = tspds.cost(dd)
							+ tspds.cost(truckTourList.get(j - 1),
									truckTourList.get(j))
							- (tspds.cost(truckTourList.get(j - 1),
									dd.getDrone_node()) + tspds.cost(
									dd.getDrone_node(), truckTourList.get(j)));
					savings=Math.round(savings*1000000)/1000000.0d;
					lde.remove(i);
					truckTourList.add(j, dd.getDrone_node());
					TruckTour t = new TruckTour(truckTourList);
					tour.setDD(lde);
					tour.setTD(t);
					if (!tspds.checkConstraint(tour)) {
						savings = -10000000;
					}
					lde.add(i, dd);
					truckTourList.remove(j);
					t = new TruckTour(truckTourList);
					tour.setDD(lde);
					tour.setTD(t);
					if (maxDroneRemoval < savings) {
						maxDroneRemoval = savings;
						vtMaxDroneRemoval=i;
						vtMaxDroneRemoval1 = j;
					}
				}
			if (maxkPointRelocationSavings <= 0 && maxSwapTruckDrone <= 0
					&& maxSwap2Truck <= 0 && maxDroneRelocation <= 0
					&& maxSwap2Drone <= 0 && maxDroneRemoval <= 0
					&& maxTruckRelocation <= 0)// here
				break;
			double globalMax = maxkPointRelocationSavings;
			int vt = 1;
			if (globalMax < maxSwapTruckDrone) {
				globalMax = maxSwapTruckDrone;
				vt = 2;
			}
			if(globalMax<maxSwap2Truck){
				globalMax=maxSwap2Truck;
				vt=3;
			}
			if(globalMax<maxDroneRelocation){
				globalMax=maxDroneRelocation;
				vt=4;
			}
			if(globalMax<maxSwap2Drone){
				globalMax=maxSwap2Drone;
				vt=5;
			}
			if(globalMax<maxDroneRemoval){
				globalMax=maxDroneRemoval;
				vt=6;
			}
			if(globalMax<maxTruckRelocation){
				globalMax=maxTruckRelocation;
				vt=7;
			}
			if (vt==1) {
				/*
				 * if (globalMaxSavings <= 0 )// here break;
				 */
				for (int ii = 0; ii < dnhGlobal.getIk(); ii++) {
					// wllbeDrone.add(truckTourList.get(dnhGlobal.getTruckPointIndex()+ii+1));
					truckTourList.remove(dnhGlobal.getTruckPointIndex());
				}

				TruckTour t = new TruckTour(truckTourList);
				tour.setTD(t);
				System.out.println("Drone relocation "
						+ dnhGlobal.getLde().size() + " drone");

				for (int i = 0; i < dnhGlobal.getLde().size(); i++) {
					DroneDelivery de = dnhGlobal.getLde().get(i);
					d[de.getDrone_node().getID()] += 1;
					d[de.getRendezvous_node().getID()] += 1;
					d[de.getLauch_node().getID()] += 1;
					lde.add(dnhGlobal.getLde().get(i));
				}
				tour.setDD(lde);
				System.out.println(name() + tour);
			} else if (vt==2) {
				System.out.println("Swap drone truck point");
				DroneDelivery dd = lde.get(vtDD);
				d[dd.getDrone_node().getID()] -= 1;
				Point truckPoint = dd.getDrone_node();
				Point drone = truckTourList.get(vtTD);
				dd.setDrone_node(drone);
				lde.set(vtDD, dd);
				truckTourList.set(vtTD, truckPoint);
				TruckTour t = new TruckTour(truckTourList);
				d[dd.getDrone_node().getID()] += 1;
				tour.setTD(t);
				tour.setDD(lde);
				System.out.println(name() + tour);
			} else if(vt==3) {
				System.out.println("Swap two truck point");
				Point tmp = truckTourList.get(vtsT1);
				truckTourList.set(vtsT1, truckTourList.get(vtsT2));
				truckTourList.set(vtsT2, tmp);
				TruckTour t = new TruckTour(truckTourList);
				tour.setTD(t);
				System.out.println(name() + tour);
			} else if(vt==4){
				System.out.println("Drone relocation new position");
				DroneDelivery dd= lde.get(vtMaxDroneRelocation);
				lde.remove(vtMaxDroneRelocation);
				d[dd.getLauch_node().getID()]--;
				d[dd.getRendezvous_node().getID()]--;
				DroneDelivery ddnew = new DroneDelivery(truckTourList.get(vtMaxDroneRelocation1), dd.getDrone_node(), truckTourList.get(vtMaxDroneRelocation2));
				d[ddnew.getLauch_node().getID()]++;
				d[ddnew.getRendezvous_node().getID()]++;
				lde.add(vtMaxDroneRelocation, ddnew);
				tour.setDD(lde);
				System.out.println(name() + tour);
			}else if(vt==5){
				System.out.println("Swap 2 drone relocation");
				DroneDelivery dd1 = lde.get(swap2Dronevt1);
				DroneDelivery dd2 = lde.get(swap2Dronevt2);
				DroneDelivery dd1new = new DroneDelivery(
						dd1.getLauch_node(), dd2.getDrone_node(),
						dd1.getRendezvous_node());
				DroneDelivery dd2new = new DroneDelivery(
						dd2.getLauch_node(), dd1.getDrone_node(),
						dd2.getRendezvous_node());
				lde.set(swap2Dronevt1, dd1new);
				lde.set(swap2Dronevt2, dd2new);
				tour.setDD(lde);
				System.out.println(name() + tour);
			}else if(vt==6){
				System.out.println("Drone removal");
				DroneDelivery dd = lde.get(vtMaxDroneRemoval);
				d[dd.getLauch_node().getID()]--;
				d[dd.getRendezvous_node().getID()]--;
				d[dd.getDrone_node().getID()]--;
				lde.remove(vtMaxDroneRemoval);
				truckTourList.add(vtMaxDroneRemoval1, dd.getDrone_node());
				TruckTour t = new TruckTour(truckTourList);
				tour.setDD(lde);
				tour.setTD(t);
				System.out.println(name() + tour);
			}else if(vt==7){
				System.out.println("Truck relocation");
				Point p = truckTourList.remove(vtTruckRelocation);
				truckTourList.add(vtTruckRelocation1, p);
				TruckTour t = new TruckTour(truckTourList);
				tour.setTD(t);
				System.out.println(name() + tour);
			}
		}
		tour.setTotalCost(tspds.cost(tour));
		return tour;
	}

	public double caculRelocateAsDrone(Point j, int laught, int revouz,
			double savings) {
		double sol = -1000000;// here
		ArrayList<Point> truckPoint = tour.getTD().getTruck_tour();
		DroneDelivery de = new DroneDelivery(truckPoint.get(laught), j,
				truckPoint.get(revouz));
		if (tspds.checkOverQuantityDrone(de, tour, getK()) != 1)
			return sol;
		ArrayList<DroneDelivery> lde = tour.getDD();
		lde.add(de);
		tour.setDD(lde);
		if (tspds.checkConstraint(tour)) {
			double delta = tspds.cost(truckPoint.get(laught), j,
					truckPoint.get(revouz));
			sol = savings - delta;
		}
		lde.remove(de);
		tour.setDD(lde);
		return sol;
	}

	String name() {
		return "TSPDs_LS:: ";
	}
}
