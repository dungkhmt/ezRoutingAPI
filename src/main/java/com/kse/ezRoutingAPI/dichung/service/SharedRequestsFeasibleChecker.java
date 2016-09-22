package com.kse.ezRoutingAPI.dichung.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kse.ezRoutingAPI.dichung.model.SharedTaxiRequest;
import com.kse.utils.DateTimeUtils;
import com.kse.utils.traveltimeestimator.HanoiTravelTimeEstimator;

/*
 * start by dungkhmt@gmail.com
 * start date: 28/08/2016
 * check if a set of requests can be shared in a same vehicle
 */

public class SharedRequestsFeasibleChecker {
	public SharedTaxiRequest[] requests;
	public double[][] distances;// distances[i][j] is the distance from request i to request j
	public long[] earlyTime;
	public long[] lateTime;
	public HashMap<SharedTaxiRequest, Double> extraDistances;// extraDistance[i] is the extra distance allowed of each request when sharing
	public HashMap<SharedTaxiRequest, Double> distanceOfRequest;// distanceOfRequest[i] is the shortest distance from pickup to delivery of request i
	public int N;
	public int maxWaitTime;// total travel time of the sequence cannot exceed maxWaitTime
	public int maxSharedDistance;// distance between two consecutive requests cannot exceed maxSharedDistance
	
	// data structure for TRY permutations of a[0,...n-1]
	private int[] a;// input idx
	private int[] x;
	private int[] sol;
	private int n;
	private boolean[] marked;
	private long startTime;
	
	public boolean feasible;
	
	private HanoiTravelTimeEstimator travelTimeEstimator;
	
	public SharedRequestsFeasibleChecker(SharedTaxiRequest[] requests,
			int maxWaitTime,
			double[][] distances,
			HashMap<SharedTaxiRequest, Double> extraDistances,
			HashMap<SharedTaxiRequest, Double> distanceOfRequest){
	
		travelTimeEstimator = new HanoiTravelTimeEstimator();
		
		this.requests = requests;
		this.maxWaitTime = maxWaitTime;
		this.distances = distances;
		this.extraDistances = extraDistances;
		this.distanceOfRequest = distanceOfRequest;
		N = requests.length;
		earlyTime = new long[N];
		lateTime = new long[N];
		for(int i = 0; i < N; i++){
			earlyTime[i] = DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupDateTime());
			lateTime[i] = DateTimeUtils.dateTime2Int(requests[i].getLatePickupDateTime());
		}
	}
	
	private void processSolution(){
		// sequence of request is requests[a[x[0]]], requests[a[x[1]]],..., requests[a[x[n-1]]]
		int total = 0;
		boolean ok = true;
		long mint = earlyTime[a[x[0]]];
		long maxt = lateTime[a[x[0]]];
		
		for(int i = 0; i < n-1; i++){
			int I = a[x[i]];
			int J = a[x[i+1]];
			int t = travelTimeEstimator.estimateTravelTime(requests[I].getEarlyPickupDateTime(), distances[I][J]); 
			// consider requests I and J
			//ok  = ok & (earlyTime[J] <= lateTime[I] + t && 
			//		 earlyTime[I] + t <= lateTime[J]);
			total += t;
			long etJ = earlyTime[J] - total;
			long ltJ = lateTime[J] - total;
			if(mint < etJ) mint = etJ;
			if(maxt > ltJ) maxt = ltJ;
		}
		if(mint > maxt){
			return;
		}
		
		
		double d = distanceOfRequest.get(requests[a[x[n-1]]]);
		for(int i = n-1; i > 0; i--){
			int I = a[x[i]];
			int J = a[x[i-1]];
			d = d + distances[J][I];
			if(d >= distanceOfRequest.get(requests[J]) + extraDistances.get(requests[J])){
				ok = false; break;
			}
			if(distances[J][I] > maxSharedDistance){
				ok = false; break;
			}
		}
		ok = ok & (total < maxWaitTime);
		if(ok) feasible = true;
		
		startTime = mint;// start departure as soon as possible
		for(int i = 0; i < x.length; i++)
			sol[i] = a[x[i]];
	}
	
	
	private void TRY(int k){
		if(feasible) return;
		
		for(int i = 0; i < n; i++){
			if(!marked[i]){
				x[k] = i; marked[i] = true;
				if(k == n-1){
					processSolution();
				}else{
					TRY(k+1);
				}
			}
		}
	}
	
	public int[] getSolution(){
		return sol;
	}
	public boolean checkFeasibleSharing(ArrayList<Integer> ReqIdx){
		n = ReqIdx.size();
		a = new int[ReqIdx.size()];
		for(int i = 0; i < n; i++)
			a[i] = ReqIdx.get(i);
		x = new int[n];
		sol = new int[n];
		marked = new boolean[n];
		for(int i = 0; i < n; i++) marked[i] = false;
		feasible = false;
		startTime = -1;
		TRY(0);
		return feasible;
	}
	public boolean checkFeasibleSharing(int r1, int r2){
		ArrayList<Integer> reqIdx = new ArrayList<Integer>();
		reqIdx.add(r1);
		reqIdx.add(r2);
		return checkFeasibleSharing(reqIdx);
	}
	public boolean checkFeasibleSharing(int r1, int r2, int r3){
		ArrayList<Integer> reqIdx = new ArrayList<Integer>();
		reqIdx.add(r1);
		reqIdx.add(r2);
		reqIdx.add(r3);
		return checkFeasibleSharing(reqIdx);
	}
	
}
