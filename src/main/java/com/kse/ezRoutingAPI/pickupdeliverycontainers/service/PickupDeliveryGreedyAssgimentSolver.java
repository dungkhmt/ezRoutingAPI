package com.kse.ezRoutingAPI.pickupdeliverycontainers.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;

import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.MatchedSequenceRequests;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.PickupDeliveryRequest;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.Truck;
import com.kse.utils.DateTimeUtils;

public class PickupDeliveryGreedyAssgimentSolver {
	private PickupDeliveryRequest[] req;
	private Truck[] trucks;
	private boolean feasible;

	private HashMap<MatchedSequenceRequests, Truck> truckOf;// truckOf[i] is the truck assigned to the request i
	
	public PickupDeliveryGreedyAssgimentSolver(PickupDeliveryRequest[] req, Truck[] trucks){
		this.req= req;
		this.trucks=trucks;
	}
	
	public ArrayList<MatchedSequenceRequests> combineRequest(
			double maxDistance, int maxWaitTime) {
		int n = req.length;
		boolean[][] a = new boolean[n][n];// a[i][j] = true: request i can be
											// followed by request j
											// w.r.t maxDistance and maxWaitTime

		GoogleMapsQuery G = new GoogleMapsQuery();
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				a[i][j] = false;
		for (int i = 0; i < n; i++) {
			PickupDeliveryRequest ri = req[i];
			for (int j = 0; j < n; j++) {
				PickupDeliveryRequest rj = req[j];
				double d = G.computeDistanceHaversine(ri.getDeliveryLatLng(),
						rj.getPickupLatLng());
				long t = DateTimeUtils.distance(rj.getPickupDateTime(),
						ri.getDeliveryDateTime());
				a[i][j] = d <= maxDistance && 0 <= t && t <= maxWaitTime;
				
				System.out.println(name() + "::combineRequests, d[" + ri.getRequestCode() + "," + rj.getRequestCode() + "] = " + d
						+ ", t = " + t + ", maxDistance = " + maxDistance + ", maxWaitTime = " + maxWaitTime + ", a = " + a[i][j]);
			}
		}

		int[] inDeg = new int[n];// in-degree of requests
		int[] outDeg = new int[n];
		for (int i = 0; i < n; i++) {
			inDeg[i] = 0;
			outDeg[i] = 0;
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (a[i][j]) {
					inDeg[j]++;
					outDeg[i]++;
				}
			}
		}

		ArrayList<MatchedSequenceRequests> L = new ArrayList<MatchedSequenceRequests>();

		boolean[] marked = new boolean[n];
		for (int i = 0; i < n; i++)
			marked[i] = false;

		for (int i = 0; i < n; i++) {
			if (inDeg[i] == 0 && outDeg[i] == 0) {// cannot match with others
				ArrayList<PickupDeliveryRequest> seq = new ArrayList<PickupDeliveryRequest>();
				seq.add(req[i]);
				MatchedSequenceRequests msr = new MatchedSequenceRequests(seq,
						req[i].getQuantity());
				L.add(msr);
				marked[i] = true;
			}
		}

		HashSet<Integer> S = new HashSet<Integer>();
		for (int i = 0; i < n; i++)
			if (!marked[i])
				S.add(i);

		while (S.size() > 0) {
			ArrayList<PickupDeliveryRequest> seq = new ArrayList<PickupDeliveryRequest>();
			int q = 0;

			int s = -1;// select a first request having no incoming arcs (j such
						// that a[j][s] = true)
						// ì no such request, then pick randomly
			for (int i : S) {
				s = i;
				if (inDeg[i] == 0) {
					s = i;
					break;
				}
			}
			seq.add(req[s]);
			q = req[s].getQuantity();
			marked[s] = true;
			S.remove(s);

			System.out.println(name() + "::combineRequests, start req[" + s + " = " + req[s].getRequestCode());
			
			// iteratively connect with other following requests
			// among possible following requests, choose the one that closest to
			// req[s] in term of quantity
			while (S.size() > 0) {
				int t = -1;
				int minD = Integer.MAX_VALUE;
				for (int j : S)
					if (a[s][j]) {
						if (Math.abs(q - req[j].getQuantity()) < minD) {
							minD = Math.abs(q - req[j].getQuantity());
							t = j;
						}
					}

				if (t > -1) {// engage request t to seq
					System.out.println(name() + "::combineRequest, engage req[" + t + "] = " + req[t].getRequestCode());
					seq.add(req[t]);
					if(q < req[t].getQuantity()) q = req[t].getQuantity();
					S.remove(t);
					marked[t] = true;
					s = t;
				} else {
					break;
				}
			}
			MatchedSequenceRequests msr = new MatchedSequenceRequests(seq, q);
			L.add(msr);
		}

		return L;

	}
	public boolean feasibleSolution(){
		return feasible;
	}
	public String name(){
		return "PickupDeliveryGreedyAssignment";
	}
	public void assignTrucks(double maxDistance, int maxWaitTime){
		ArrayList<MatchedSequenceRequests> MSR = combineRequest(maxDistance, maxWaitTime);
		
		MatchedSequenceRequests[] L = new MatchedSequenceRequests[MSR.size()];
		for(int i = 0; i < MSR.size(); i++) L[i] = MSR.get(i);
		
		// sort L in an increasing order of quantity
		for(int i = 0; i < L.length-1; i++)
			for(int j = i+1; j < L.length; j++)
				if(L[i].getQuantity() > L[j].getQuantity()){
					MatchedSequenceRequests tmp = L[i]; L[i] = L[j]; L[j] = tmp;
				}
		
		System.out.println(name() + "::assignTrucks, trucks.sz = " + trucks.length);
		
		//HashMap<MatchedSequenceRequests, Integer> mMatchedRequests2Truck = new HashMap<MatchedSequenceRequests, Integer>();
		truckOf = new HashMap<MatchedSequenceRequests, Truck>();
		boolean[] assigned = new boolean[trucks.length];
		for(int j = 0; j < trucks.length; j++) assigned[j] = false;
		// assignment
		for(int i= 0; i < L.length; i++){
			// find min-capacity truck that match L[i]
			int sel_truck = -1;
			int minC = Integer.MAX_VALUE;
			System.out.println(name() + "::assignTrucks, L[" + i + "] = " + L[i].getSeq().size() + ", quantity = " + L[i].getQuantity());
			for(int j = 0; j < trucks.length; j++){
				if(trucks[j].getCapacity() >= L[i].getQuantity() && !assigned[j]){
					if(minC > trucks[j].getCapacity()){
						minC = trucks[j].getCapacity();
						sel_truck = j;
					}
				}
			}
			if(sel_truck == -1){
				// Cannot assign trucks
				feasible = false; return;
			}else{
				//mMatchedRequests2Truck.put(L[i], sel_truck);
				truckOf.put(L[i], trucks[sel_truck]);
				assigned[sel_truck] = true;
			}
			
		}
		
		// establish solution
		feasible = true;
	}

	public HashMap<MatchedSequenceRequests, Truck> getTruckOf(){
		return truckOf;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
