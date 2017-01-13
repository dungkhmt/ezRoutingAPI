package com.kse.ezRoutingAPI.dichungduongdai.service;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripElement;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripInput;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripRequest;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripRoute;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripSolution;
import com.kse.utils.DateTimeUtils;

import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.Direction;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.StepDirection;

public class SharedLongTripService {
	private SharedLongTripInput input;
	
	//Parameter variable
	private static double MAX_VALUE = 10000000;
	private static double MERGE_MAX_DIS = 10.0; //km
	private static double MERGE_MAX_TIME = 900.0; //seconds
	
	//Google map object
	GoogleMapsQuery G;
	
	//Inferred variables 
	private int n; //Number of requests
	SharedLongTripRequest[] requestLst;
	private int[] vhcCapacities; //Vehicle capacities
	private int[][] C; //Connected Matrix, c[i][j] = 1 it means that 
						// requests i and j can go together
	private double[][] d1; //Distance between requests' pickup positions
	private double[][] d2; //Distance between requests' delivery positions
	private double[][] td; //Time distance between requests' pickup points
	private double[] pickupTime; //Relative time of the pickups
	
	//Isolated components
	Vector<Integer> IC[];
	
	public String name(){
		return "DichungDuongDaiService";
	}
	
	public SharedLongTripService(SharedLongTripInput input){
		this.input = input;
		
		//Google map object
		G = new GoogleMapsQuery();
		
		//Initialize dependent variables
		requestLst = input.getRequests();
		
		n = requestLst.length; //Number of requests
		
		vhcCapacities = input.getVehicleCapacities();		
		
		C = new int[n][n]; //Connected Matrix, c[i][j] = 1 it means that 
									// requests i and j can go together
		d1 = new double[n][n]; //Distance between requests' pickup positions
		d2 = new double[n][n]; //Distance between requests' delivery positions
		td = new double[n][n]; //Time distance between requests' pickup points
		pickupTime = new double[n]; //Relative time of the pickups
	}
	
	/**
	 * Pre-processing
	 */
	public void preprocessing(){
		
		//Calculate the matrix C, d and the array of neighbor lists
		int earlestRequestIndex = 0;
		for(int i = 1; i < n; i++){
			if(DateTimeUtils.distance(requestLst[i].getDepartTime(), requestLst[earlestRequestIndex].getDepartTime()) < 0){
				earlestRequestIndex = i;
			}
		}
		for(int i = 0; i < n; i++){
			pickupTime[i] = DateTimeUtils.distance(requestLst[i].getDepartTime(), requestLst[earlestRequestIndex].getDepartTime());
		}
				
		for(int i = 0; i < n-1; i++){			
			for(int j = i + 1; j < n; j++){
				//For distance matrix d
				System.out.println(name() + "::preprocessing, req[" + i + "].pickupPos = " + requestLst[i].getPickupPos() + 
						", req[" + j + "].pickupPos = " + requestLst[j].getPickupPos());
				d1[i][j] = G.computeDistanceHaversine(requestLst[i].getPickupPos(), requestLst[j].getPickupPos());
				d1[i][j] *= 1000 * input.getApproximationDistanceFactor();
				d1[j][i] = d1[i][j];
				
				d2[i][j] = G.computeDistanceHaversine(requestLst[i].getDeliveryPos(), requestLst[j].getDeliveryPos());
				d2[i][j] *= 1000 * input.getApproximationDistanceFactor();
				d2[j][i] = d2[i][j];
				
				td[i][j] = Math.abs(pickupTime[i] - pickupTime[j]);
				td[j][i] = td[i][j];
				
				//For connected matrix
				C[i][j] = 0;
				C[j][i] = 0;				
				if(requestLst[i].isShared() && requestLst[j].isShared()){//Already for sharing
					if(requestLst[i].getItinerary() == requestLst[j].getItinerary()){ //Same itinerary					
						if(d1[i][j] <= input.getForbidenStraightDistance()){ // Two pickup points are not too far to each other						
							if(d2[i][j] <= 2.0 * input.getForbidenStraightDistance()){ // Two delivery points are not too far to each other							
								if(td[i][j] <= input.getForbidenTimeDistance()){//Pickup times of two requests are not too far 
									
									C[i][j] = 1;
									C[j][i] = 1;
	
									//System.out.println("Suitable for sharing: request " + requestLst[i].getTicketCode() + " with request " + requestLst[j].getTicketCode());
								}
							}
						}
					}		
				}				
			}
		}		
	}
	
	/**
	 * Find isolated components
	 */
	public void findIsolatedComponent(){
		IC = (Vector<Integer>[]) new Vector[n]; // Storing Isolated Components
		for (int i = 0; i < n; i++) {
			IC[i] = new Vector<Integer>();
		}
		    
		int id[] = new int[n];
		int sz[] = new int[n];
		for(int i = 0; i < n; i++){
			id[i] = i;
			sz[i] = 1;
		}
		
		//Unite these nodes connected to each other
		int rI, rJ;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (C[i][j] == 1) {// Unite two nodes i and j
					rI = i; // Root's ID of i
					while (rI != id[rI]) {
						id[rI] = id[id[rI]];
						rI = id[rI];
					}
					rJ = j;// Root's ID of j
					while (rJ != id[rJ]) {
						id[rJ] = id[id[rJ]];
						rJ = id[rJ];
					}

					if (sz[rI] < sz[rJ]) {
						id[rI] = rJ;
						sz[rJ] += sz[rI];
					} else {
						id[rJ] = rI;
						sz[rI] += sz[rJ];
					}
				}
			}
		}
		
		//Update root nodes
		int rootNode[] = new int[n];
	    for(int i = 0; i < n; i++){
	        rootNode[i] = i;
	        while(rootNode[i] != id[rootNode[i]]){
	            id[rootNode[i]] = id[id[rootNode[i]]];
	            rootNode[i] = id[rootNode[i]];
	        }
	    }
	             
	    for(int i = 0; i < n; i++){
	        IC[rootNode[i]].add(i);
	    }
	}
	/**
	 * Compute solution
	 * @param input
	 * @return
	 */
	public SharedLongTripSolution computeSharedLongTrip(){				
		//Do preprocessing
		preprocessing();
		
		// Computing isolated component
		findIsolatedComponent();	
				
	    //Considering the isolated components	    
	    Vector<SharedLongTripRoute> routeLst = new Vector<SharedLongTripRoute>();
	    for(int i = 0; i < n; i++){
	    	if(IC[i].size() < 3){
	    		if(IC[i].size() == 1){ //Single
	    			SharedLongTripRequest req = requestLst[IC[i].elementAt(0)];
	    			SharedLongTripElement[] elementLst = new SharedLongTripElement[2];
	    			elementLst[0] = new SharedLongTripElement(req.getTicketCode(), req.getDepartTime(), req.getPickupAddress(), req.getPickupPos(), "-", "-",SharedLongTripElement.PICKUP);
	    			elementLst[1] = new SharedLongTripElement(req.getTicketCode(), "", "-", "-", req.getDeliveryAddress(), req.getDeliveryPos(),SharedLongTripElement.DELIVERY);
	    			
	    			int nbPeople = req.getNumberPassengers();
	    			String taxiType = "";
	    			for(int j = 1; j < vhcCapacities.length; j++){
	    				if(nbPeople <= vhcCapacities[j]){
	    					taxiType = Integer.toString(vhcCapacities[j]);
	    					break;
	    				}
	    			}
	    			
	    			SharedLongTripRoute route = new SharedLongTripRoute(elementLst, taxiType, nbPeople, 1);
	    			routeLst.add(route);
	    		}else if(IC[i].size() == 2){
	    			SharedLongTripRequest req1 =  requestLst[IC[i].elementAt(0)];
	    			SharedLongTripRequest req2 =  requestLst[IC[i].elementAt(1)];
	    			SharedLongTripElement[] elementLst = new SharedLongTripElement[4];
	    			
	    			if(DateTimeUtils.distance(req1.getDepartTime(), req2.getDepartTime()) < 0){
	    				elementLst[0] = new SharedLongTripElement(req1.getTicketCode(), req1.getDepartTime(), req1.getPickupAddress(), req1.getPickupPos(), "-", "-",SharedLongTripElement.PICKUP);
	    				elementLst[1] = new SharedLongTripElement(req2.getTicketCode(), req2.getDepartTime(), req2.getPickupAddress(), req2.getPickupPos(), "-", "-",SharedLongTripElement.PICKUP);
	    			}else{
	    				elementLst[0] = new SharedLongTripElement(req2.getTicketCode(), req2.getDepartTime(), req2.getPickupAddress(), req2.getPickupPos(), "-", "-",SharedLongTripElement.PICKUP);
	    				elementLst[1] = new SharedLongTripElement(req1.getTicketCode(), req1.getDepartTime(), req1.getPickupAddress(), req1.getPickupPos(), "-", "-",SharedLongTripElement.PICKUP);	    				
	    			}
	    			
	    			double distance1 = G.getDistance(elementLst[1].getPickupPosition(), req1.getDeliveryPos());
	    			double distance2 = G.getDistance(elementLst[1].getPickupPosition(), req2.getDeliveryPos());
	    			
	    			if(distance1 < distance2){
	    				elementLst[2] = new SharedLongTripElement(req1.getTicketCode(), "-", "-", "-", req1.getDeliveryAddress(), req1.getDeliveryPos(),SharedLongTripElement.DELIVERY);
	    				elementLst[3] = new SharedLongTripElement(req2.getTicketCode(), "-", "-", "-", req2.getDeliveryAddress(), req2.getDeliveryPos(), SharedLongTripElement.DELIVERY);
	    			}else{
	    				elementLst[2] = new SharedLongTripElement(req2.getTicketCode(), "-", "-", "-", req2.getDeliveryAddress(), req2.getDeliveryPos(),SharedLongTripElement.DELIVERY);
	    				elementLst[3] = new SharedLongTripElement(req1.getTicketCode(), "-", "-", "-", req1.getDeliveryAddress(), req1.getDeliveryPos(),SharedLongTripElement.DELIVERY);
	    			}
	    			
	    			int nbPeople = req1.getNumberPassengers() + req2.getNumberPassengers();
	    			String taxiType = "";
	    			for(int j = 1; j < vhcCapacities.length; j++){
	    				if(nbPeople <= vhcCapacities[j]){
	    					taxiType = Integer.toString(vhcCapacities[j]);
	    					break;
	    				}
	    			}
	    			SharedLongTripRoute route = new SharedLongTripRoute(elementLst, taxiType, nbPeople, 2);
	    			routeLst.add(route);
	    		}
	    	}else{ //Local search for isolated components with more than 3 requests
	    		int m = IC[i].size();

	    		int [] bestSol = new int[m]; //For the best solution found so far
	    		int [] routeBestSol = new int[m]; //routeBestSol[i] states that the route index of request sol[i]
	    		double [] bsTimeLB = new double[m]; //Lower bound time for the arrival of best solution
	    		double [] bsTimeUB = new double[m]; //Upper bound time for the arrival of best solution
	    		double bestObj = 0;
    		
	    		//Tabu search
	    		TabuSearchForPickupSide(input, d1, pickupTime, IC, i, m, bestSol, routeBestSol, bsTimeLB, bsTimeUB);
	    		
	    		System.out.println("Best objective: " + bestObj);
	    		System.out.print("Best Solution : ");
	    		for(int r = 0; r < m; r++){
	    			System.out.print(bestSol[r] + ", ");
	    		}System.out.println();
	    		System.out.print("Route Index : ");
	    		for(int r = 0; r < m; r++){
	    			System.out.print(routeBestSol[r] + ", ");
	    		}System.out.println();

	    		
	    		//Making routes from the solution found by tabu search
	    		int nbRoute = routeBestSol[m-1];
	    		for(int r = 1; r <= nbRoute; r++){
	    			Vector<SharedLongTripRequest> temRequestLst = new Vector<SharedLongTripRequest>();
	    			for(int j = 0; j < m; j++){
	    				if(routeBestSol[j] == r){
	    					temRequestLst.add(requestLst[bestSol[j]]);
	    				}
	    			}
	    			int k = temRequestLst.size();
	    			
	    			//Pickup
	    			SharedLongTripElement[] elementLst = new SharedLongTripElement[2*k];
	    			for(int j = 0; j < k; j++){
	    				elementLst[j] = new SharedLongTripElement(temRequestLst.elementAt(j).getTicketCode(), temRequestLst.elementAt(j).getDepartTime(),
	    						temRequestLst.elementAt(j).getPickupAddress(), temRequestLst.elementAt(j).getPickupPos(), "-", "-",SharedLongTripElement.PICKUP);
	    			}
	    			
	    			int [] visited = new int[k];
	    			for(int j = 0; j < k; j++){
	    				visited[j] = 0;
	    			}
	    			String lastLatLng = elementLst[k-1].getPickupPosition();
	    			
	    			//Delivery	    			
	    			for(int j = 0; j < k; j++){
	    				
	    				double dist = 10000000;
	    				int index = -1;
	    				for(int p = 0; p < k; p++){
	    					if(visited[p] == 0){
	    						//double d = G.getDistance(lastLatLng, temRequestLst.elementAt(p).getDeliveryPos()); 
	    						double d = G.computeDistanceHaversine(lastLatLng, temRequestLst.elementAt(p).getDeliveryPos());
	    						System.out.println("lastLatLng: " + lastLatLng + " to " + temRequestLst.elementAt(p).getDeliveryPos() + " => d:  " + d);
	    						if(d < dist){
	    							dist = d;
	    							index = p; 
	    						}
	    					}
	    				}
	    				System.out.println("index: " + index);
	    				if(index != -1){
	    					visited[index] = 1;
		    				lastLatLng = temRequestLst.elementAt(index).getDeliveryPos();	    				
		    				
		    				elementLst[k+j] = new SharedLongTripElement(temRequestLst.elementAt(index).getTicketCode(), "-", "-", "-", 
		    						temRequestLst.elementAt(index).getDeliveryAddress(), temRequestLst.elementAt(index).getDeliveryPos(),SharedLongTripElement.DELIVERY);
		    				
		    				System.out.println("test: " + G.getDistance("20.6608254,106.3276864", "20.4204865,106.3905338"));
	    				}
	    				
	    			}
	    			int nbPeople = 0;
	    			for(int j = 0; j < k; j++){
	    				nbPeople += temRequestLst.elementAt(j).getNumberPassengers();
	    			}
	    			String taxiType = "";
	    			for(int j = 1; j < vhcCapacities.length; j++){
	    				if(nbPeople <= vhcCapacities[j]){
	    					taxiType = Integer.toString(vhcCapacities[j]);
	    					break;
	    				}
	    			}
	    			
	    			
		    		
	    			//Make a route
	    			SharedLongTripRoute route = new SharedLongTripRoute(elementLst, taxiType, nbPeople, k);
	    			routeLst.add(route);	
	    		}
	    	}
	    }
	    
	    System.out.println("Number of vehicles before posprocessing " + routeLst.size());
	    
	    //Post-processing
	    postprocessing2(routeLst);
	    
	    System.out.println("Number of vehicles after posprocessing " + routeLst.size());
	    
	    SharedLongTripRoute[] routeArray = new SharedLongTripRoute[routeLst.size()];
	    for(int i = 0; i < routeLst.size(); i++){
	    	routeArray[i] = routeLst.elementAt(i);
	    }
	    SharedLongTripSolution solution = new SharedLongTripSolution(n, routeArray);
	    
	    //Printing solutions
	    solution.print();	  
	    
		return solution;
	}
	
	/**
	 * Copy from to solution 1 to solution 2 using for TabuSearchForPickupSide
	 * @param sol1
	 * @param routeSol1
	 * @param s1TimeLB
	 * @param s1TimeUB
	 * @param sol2
	 * @param routeSol2
	 * @param s2TimeLB
	 * @param s2TimeUB
	 */	
	public void copySolution(int [] sol1, int [] routeSol1, double [] s1TimeLB, double [] s1TimeUB,
			int [] sol2, int [] routeSol2, double [] s2TimeLB, double [] s2TimeUB){
		for(int i = 0; i < sol1.length; i++){
			sol2[i] = sol1[i];
			routeSol2[i] = routeSol1[i];
			s2TimeLB[i] = s1TimeLB[i];
			s2TimeUB[i] = s2TimeUB[i];
		}
	}
	
	/**
	 * Tabu search for pickup side	
	 * @param input
	 * @param d1
	 * @param pickupTime
	 * @param IC
	 * @param i
	 * @param m
	 * @param bestSol
	 * @param routeBestSol
	 * @param bsTimeLB
	 * @param bsTimeUB
	 */
	public void TabuSearchForPickupSide(SharedLongTripInput input, double[][] d1, double[] pickupTime, Vector<Integer> IC[], int i, int m, int [] bestSol, int [] routeBestSol, double [] bsTimeLB, double [] bsTimeUB){
		int maxIt = 1000;
		int it = 1;
		int stable = 0;
		
		int [] curSol = new int[m]; //For the current solution
		int [] routeCurSol = new int[m];
		double [] csTimeLB = new double[m]; //Lower bound time for the arrival of the current solution
		double [] csTimeUB =  new double[m]; //Upper bound time for the arrival of the current solution
		
		int [] tempSol = new int[m]; //For the current solution
		int [] routeTempSol = new int[m];
		double [] tsTimeLB = new double[m]; //Lower bound time for the arrival of the current solution
		double [] tsTimeUB =  new double[m]; //Upper bound time for the arrival of the current solution
		
		int restartTime = 0; //After a time, we take a random walk
		int[][] tabuList = new int[m][m];
		int tabuLength = m;
		
		//Generate randomly initial solution	    		
		for(int r = 0; r < m; r++){
			curSol[r] = IC[i].elementAt(r);
		}
		
		double curObj = computeRoutes(input, m, pickupTime, d1, curSol, routeCurSol, csTimeLB, csTimeUB);
		double bestObj = curObj;
		
		while(it < maxIt){	    			
			it++;
			stable++;
			
			int req1 = -1;
			int req2 = -1;
			//Take a rand walk if a long time no improvement
			if(stable > (int)(m*m/2 + m)){
				restartTime++;
				for(int r = 0; r < m; r++){
	    			curSol[r] = IC[i].elementAt(r);
	    		}
				Random rand = new Random();
				for(int t = 0; t < m*m; t++){
					int index1 = rand.nextInt(m);
					int index2 = rand.nextInt(m);
					if(index1 != index2){
						int tempVar = curSol[index1];
						curSol[index1] = curSol[index2];
						curSol[index2] = tempVar;
					}
				}
				//Update tabu
				for(int r1 = 0; r1 < m - 1; r1++){
    				for(int r2 = r1+1; r2 < m; r2++){
    					tabuList[r1][r2] = 0;
    				}
    			}
				
			}else{
				double tempBestObj = m * MAX_VALUE;		    			
    			
    			for(int r1 = 0; r1 < m-1; r1++){
    				for(int r2 = r1+1; r2 < m; r2++){		    					
    					if(tabuList[r1][r2] == 0 && tabuList[r2][r1] == 0){
    						for(int r = 0; r < m; r++){
	    	    				tempSol[r] = curSol[r];
	    	    			}	    					
	    					tempSol[r1] = curSol[r2];
	    					tempSol[r2] = curSol[r1];
	    					
	    					//Compute routes for the temporary sequence
	    					double tempObj = computeRoutes(input, m, pickupTime, d1, tempSol, routeTempSol, tsTimeLB, tsTimeUB);
	    					if(tempObj < tempBestObj){
	    						tempBestObj = tempObj;
	    						req1 = r1;
	    						req2 = r2;    						
	    					} 
    					}		    					
    				}
    			}
			}
			
			//Move	& update tabu    		
			if(req1 > -1 && req2 > -1){
				//Move
				int tempVar = curSol[req1];
    			curSol[req1] = curSol[req2];
    			curSol[req2] = tempVar;
    			
    			//Update tabu
    			for(int r1 = 0; r1 < m - 1; r1++){
    				for(int r2 = r1+1; r2 < m; r2++){
    					if(tabuList[r1][r2] > 0){
    						tabuList[r1][r2]--;
    					}
    				}
    			}
    			if(req1 < req2){
    				tabuList[req1][req2] = tabuLength;	
    			}else{
    				tabuList[req2][req1] = tabuLength;
    			}		    		
			}	    			
			curObj = computeRoutes(input, m, pickupTime, d1, curSol, routeCurSol, csTimeLB, csTimeUB);
			
			//Check best solution so far
			if(curObj < bestObj){
				bestObj = curObj;
				copySolution(curSol, routeCurSol, csTimeLB, csTimeUB, 
						bestSol, routeBestSol, bsTimeLB, bsTimeUB);
				
				stable = 0;
			}
		}
	}
	
	
	/**
	 * Compute routes for a sequence using for TabuSearchForPickupSide
	 * @param input
	 * @param m
	 * @param pickupTime
	 * @param d1
	 * @param curSol
	 * @param routeCurSol
	 * @param csTimeLB
	 * @param csTimeUB
	 * @return
	 */
	public double computeRoutes(SharedLongTripInput input, int m, double[] pickupTime, double[][] d1, 
			int [] curSol, int [] routeCurSol, double [] csTimeLB, double [] csTimeUB){
		
		
		int routeIndex = 1;
		double cumulativeLength = 0;
		routeCurSol[0] = routeIndex;
		csTimeLB[0] = pickupTime[curSol[0]] - input.getDeltaRequestTime();
		csTimeUB[0] = pickupTime[curSol[0]] + input.getDeltaRequestTime();
		
		for(int r = 1; r < m; r++){ //Calculation of routeCurSol
			cumulativeLength += d1[curSol[r]][curSol[r-1]];
			
			double travelTime = d1[curSol[r]][curSol[r-1]]/input.getStdSpeed();
			double lb = Math.max(pickupTime[curSol[r]] - input.getDeltaRequestTime(),
					csTimeLB[r-1] + travelTime);
			double ub = Math.min(pickupTime[curSol[r]] + input.getDeltaRequestTime(),
					csTimeUB[r-1] + travelTime);
			if(cumulativeLength > input.getForbidenStraightDistance() && lb > ub){
				routeIndex++;
				cumulativeLength = d1[curSol[r]][curSol[r-1]];
				csTimeLB[r] = pickupTime[curSol[r]] - input.getDeltaRequestTime();
				csTimeUB[r] = pickupTime[curSol[r]] + input.getDeltaRequestTime();
			}else{
				csTimeLB[r] = lb;
				csTimeUB[r] = ub;
			}
			routeCurSol[r] = routeIndex;
		}
		
		//Print something
		/*
		System.out.print("Sequence ");
		for(int j = 0; j < m; j++){
			System.out.print(input.getRequests()[curSol[j]].getTicketCode() + ", ");	
		}System.out.println();
		
		System.out.print("Lower bound of Pickup time ");
		for(int j = 0; j < m; j++){
			System.out.print(csTimeLB[j] + ", ");	
		}System.out.println();
		
		System.out.print("Upper bound of Pickup time ");
		for(int j = 0; j < m; j++){
			System.out.print(csTimeUB[j] + ", ");	
		}System.out.println();
		
		System.out.print("Route index of request ");
		for(int j = 0; j < m; j++){
			System.out.print(routeCurSol[j] + ", ");	
		}System.out.println();
		*/
		
		double result = routeCurSol[m-1] * MAX_VALUE;
		for(int i = 0; i < m-1; i++){
			if(routeCurSol[i] == routeCurSol[i+1]){
				result += d1[curSol[i]][curSol[i+1]];
			}
		}

		return result;
	}
	
	/**
	 * Post-processing with hope of merging some routes
	 * @param routeLst
	 */
	public void postprocessing(Vector<SharedLongTripRoute> routeLst){
		System.out.println("n: " + n);
		//Finding possible merge
		boolean[][] possibleMerge = new boolean[n][n];
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				possibleMerge[i][j] = false;
			}
		}

		
		boolean [] singleRequest = new boolean[n];
		String[] ticketCodeLst = new String[n];
		for(int i = 0; i < n; i++){
			singleRequest[i] = false;
		}
		
		for(int i = 0; i < n; i++){	
			ticketCodeLst[i] = input.getRequests()[i].getTicketCode();
		}
		
		for(int i = 0; i < n; i++){
			for(SharedLongTripRoute sltr : routeLst){	
				if(sltr.getRouteElements().length == 2){
					if(ticketCodeLst[i].equals(sltr.getRouteElements()[0].getTicketCode() )){
						singleRequest[i] = true;
						break;
					}				
				}			
			}
		}
		
		System.out.println("List of single request");
		for(int i = 0; i < n; i++){
			if(singleRequest[i]){
				System.out.println(ticketCodeLst[i]);
			}
		}
		
		boolean[] consideredList = new boolean[n];
		for(int i = 0; i < n; i++){
			consideredList[i] = false;
		}
		for(int i = 0; i < n; i++){
			if(singleRequest[i]){
				consideredList[i] = true;
				if(input.getRequests()[i].getDirectItineraries() == null || input.getRequests()[i].getDirectItineraries().length == 0)
					continue;
				
				String itinerary1 = input.getRequests()[i].getDirectItineraries()[0];
				String[] iti1 = itinerary1.split(";");
				Vector<Double> latLst1 = new Vector<Double>();
				Vector<Double> lonLst1 = new Vector<Double>();				
				for(int j = 0; j < iti1.length; j++){
					String[] elements1 = iti1[j].split(",");
					latLst1.add(Double.parseDouble(elements1[0]));
					lonLst1.add(Double.parseDouble(elements1[1]));
				}
 				
				for(int j = 0; j < n; j++){
					if(consideredList[j] == false){
				
						if(input.getRequests()[j].getDirectItineraries() == null || input.getRequests()[j].getDirectItineraries().length == 0)
							continue;
						
						String itinerary2 = input.getRequests()[j].getDirectItineraries()[0];					
						String[] iti2 = itinerary2.split(";");
						Vector<Double> latLst2 = new Vector<Double>();
						Vector<Double> lonLst2 = new Vector<Double>();
						
						for(int k = 0; k < iti2.length; k++){
							String[] elements2 = iti2[k].split(",");
							latLst2.add(Double.parseDouble(elements2[0]));
							lonLst2.add(Double.parseDouble(elements2[1]));
						}
						
						
						//A merge is acceptable since we have two merges:						
						boolean firstMerge = false;
						boolean secondMerge = false;
						
						//Check for a possible first merge											
						double timeDelta = Math.abs(DateTimeUtils.distance(input.getRequests()[i].getDepartTime(), input.getRequests()[j].getDepartTime()));
						
						//case 1: pickup 1 -> pickup 2 
						double cumulativeDistace = 0.0;
						for(int k = 0; k < latLst2.size(); k++){
							double dis = G.computeDistanceHaversine(latLst1.firstElement(), lonLst1.firstElement(), latLst2.elementAt(k), lonLst2.elementAt(k));

							if(dis < MERGE_MAX_DIS){ //Check for distance condition
								
																
								if(timeDelta - cumulativeDistace * 1000.0/input.getStableSpeed() < MERGE_MAX_TIME &&
										timeDelta - cumulativeDistace * 1000.0/input.getStableSpeed() > -MERGE_MAX_TIME){ //Check for pickup time condition
									firstMerge = true;
									
									break;
								}								
							}
							
							if(k < latLst2.size() - 1){
								cumulativeDistace += G.computeDistanceHaversine(latLst2.elementAt(k), lonLst2.elementAt(k), latLst2.elementAt(k+1), lonLst2.elementAt(k+1));
							}
						}
						
						//Case 2: pickup 2 -> pickup 1
						if(firstMerge == false){
							cumulativeDistace = 0.0;
							for(int k = 0; k < latLst1.size(); k++){
								double dis = G.computeDistanceHaversine(latLst2.firstElement(), lonLst2.firstElement(), latLst1.elementAt(k), lonLst1.elementAt(k));

								if(dis < MERGE_MAX_DIS){									
									if(timeDelta - cumulativeDistace * 1000.0/input.getStableSpeed() < MERGE_MAX_TIME &&
											timeDelta - cumulativeDistace * 1000.0/input.getStableSpeed() > -MERGE_MAX_TIME){
										firstMerge = true;
										break;
									}								
								}
								
								if(k < latLst1.size() - 1){
									cumulativeDistace += G.computeDistanceHaversine(latLst1.elementAt(k), lonLst1.elementAt(k), latLst1.elementAt(k+1), lonLst1.elementAt(k+1));
								}
							}
						}
						
						//Check for the second merge if needed
						if(firstMerge){									
							//Case 1: Delivery 1 -> Delivery 2
							for(int k = 0; k < latLst2.size(); k++){
								double dis = G.computeDistanceHaversine(latLst1.lastElement(), lonLst1.lastElement(), latLst2.elementAt(k), lonLst2.elementAt(k));
								if(dis < MERGE_MAX_DIS){
									secondMerge = true;
									break;
								}
							}
							if(secondMerge == false){
								//Case 2: Delivery 2 -> Delivery 1
								for(int k = 0; k < latLst1.size(); k++){
									double dis = G.computeDistanceHaversine(latLst2.lastElement(), lonLst2.lastElement(), latLst1.elementAt(k), lonLst1.elementAt(k));
									if(dis < MERGE_MAX_DIS){
										secondMerge = true;
										break;
									}
								}
							}
						}
						
						//Check a possible merge
						if(firstMerge && secondMerge){
							possibleMerge[i][j] = true;
							System.out.println("There is a merge between " + input.getRequests()[i].getTicketCode() + " and " + input.getRequests()[j].getTicketCode());
						}
					}
				}
			}
		}
		
		
		
		//Do Merging
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				if(possibleMerge[i][j]){
					
					//Checking request[j] is in route with more than 3 requests or not
					boolean insertable = true;
					for(SharedLongTripRoute sltr : routeLst){	
						if(sltr.getRouteElements().length >= 6){
							for(SharedLongTripElement slte : sltr.getRouteElements()){
								if(input.getRequests()[j].getTicketCode().equals(slte.getTicketCode() )){
									insertable = false;
									break;
								}	
							}										
						}			
					}
					
					if(insertable){
																		
						//Copy and delete route containing request j
						int index = -1;
						SharedLongTripRoute editRoute = new SharedLongTripRoute();
						for(int k = 0; k < routeLst.size(); k++){
							for(SharedLongTripElement slte : routeLst.elementAt(k).getRouteElements()){
								if(input.getRequests()[j].getTicketCode().equals( slte.getTicketCode() )){
									index = k;
									break;
								}
							}
						}
						
						if(index >= 0){
							editRoute = routeLst.elementAt(index);								
							routeLst.remove(index);											
							
							//Insert pickup elements							
							SharedLongTripElement[] elementLst = new SharedLongTripElement[editRoute.getNbRequests() * 2 + 2];
							for(int p = 0; p < editRoute.getNbRequests(); p++){
								elementLst[p] = editRoute.getRouteElements()[p];
							}
							SharedLongTripElement newElement = new SharedLongTripElement(requestLst[i].getTicketCode(), 
									requestLst[j].getDepartTime(), requestLst[j].getPickupAddress(), requestLst[j].getPickupPos(), "-", "-",SharedLongTripElement.PICKUP);
							elementLst[editRoute.getNbRequests()] = newElement;		
		
							//Sorting in order of depart time
							SharedLongTripElement tempElement = new SharedLongTripElement();
							for(int p = 0; p < editRoute.getNbRequests(); p++){
								for(int q = p + 1; q < editRoute.getNbRequests() + 1; q++){
									double t1 = DateTimeUtils.dateTime2Int(elementLst[p].getDepartTime());
									double t2 = DateTimeUtils.dateTime2Int(elementLst[q].getDepartTime());
									if(t1 > t2){
										tempElement = elementLst[p];
										elementLst[p] = elementLst[q];
										elementLst[q] = tempElement;												
									}
								}
							}
							
							//insert delivery elements
							for(int p = editRoute.getNbRequests()+1; p < 2*editRoute.getNbRequests()+1; p++){
								elementLst[p] = editRoute.getRouteElements()[p-1];
							}
							SharedLongTripElement newElement2 = new SharedLongTripElement(requestLst[i].getTicketCode(), 
									"-", "-", "-", requestLst[i].getDeliveryAddress(), requestLst[i].getDeliveryPos(), SharedLongTripElement.DELIVERY);
							elementLst[2 * editRoute.getNbRequests() + 1] = newElement2;
							
							//Sorting						
							for(int p = editRoute.getNbRequests() +1; p < 2* editRoute.getNbRequests()+1; p++){
								for(int q = p + 1; q < 2*editRoute.getNbRequests() + 2; q++){
									
									double d1 = G.computeDistanceHaversine(elementLst[editRoute.getNbRequests()].getPickupPosition(), elementLst[p].getDeliveryPosition());
									double d2 = G.computeDistanceHaversine(elementLst[editRoute.getNbRequests()].getPickupPosition(), elementLst[q].getDeliveryPosition());
									if(d1 > d2){
										tempElement = elementLst[p];
										elementLst[p] = elementLst[q];
										elementLst[q] = tempElement;												
									}
								}
							}
							
							int peopleNb = editRoute.getNbPeople() + requestLst[i].getNumberPassengers();
							String taxiType = "";
			    			for(int p = 1; p < vhcCapacities.length; p++){
			    				if(peopleNb <= vhcCapacities[p]){
			    					taxiType = Integer.toString(vhcCapacities[p]);
			    					break;
			    				}
			    			} 
			
			    			SharedLongTripRoute route = new SharedLongTripRoute(elementLst, taxiType, peopleNb, editRoute.getNbRequests() + 1);
			    			
			    		
			    			
			    			routeLst.add(route);
			    			
			    			
			    			
			    			//Delete route containing request i
							index = -1;						
							for(int k = 0; k < routeLst.size(); k++){
								if(routeLst.elementAt(k).getRouteElements().length == 2){
									for(SharedLongTripElement slte : routeLst.elementAt(k).getRouteElements()){
										if(input.getRequests()[i].getTicketCode().equals( slte.getTicketCode() )){
											index = k;
											break;
										}
									}
								}								
							}
							if(index >= 0){
								routeLst.remove(index);	
								
								
							}else{
								System.out.println("Error1");
							}
							
							
						}else{
							System.out.println("Error2");
						}
					}
				}
			}
		}
		
	}
	
	/**
	 * Post-processing with hope of merging some routes
	 * @param routeLst
	 */
	public void postprocessing2(Vector<SharedLongTripRoute> routeLst){
		System.out.println("n: " + n);
		//Finding possible merge
		boolean[][] possibleMerge = new boolean[n][n];
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				possibleMerge[i][j] = false;
			}
		}

		
		boolean [] singleRequest = new boolean[n];
		String[] ticketCodeLst = new String[n];
		for(int i = 0; i < n; i++){
			singleRequest[i] = false;
		}
		
		for(int i = 0; i < n; i++){	
			ticketCodeLst[i] = input.getRequests()[i].getTicketCode();
		}
		
		for(int i = 0; i < n; i++){
			for(SharedLongTripRoute sltr : routeLst){	
				if(sltr.getRouteElements().length == 2){
					if(ticketCodeLst[i].equals(sltr.getRouteElements()[0].getTicketCode() )){
						//Check for a shared request
						for(int j = 0; j < input.getRequests().length; j++){
							if(ticketCodeLst[i].equals(input.getRequests()[j].getTicketCode())){
								if(input.getRequests()[j].isShared()){
									singleRequest[i] = true;
									break;
								}
							}
						}
						
						if(singleRequest[i]){
							break;
						}
					}				
				}			
			}
		}
		
		System.out.println("List of single request");
		for(int i = 0; i < n; i++){
			if(singleRequest[i]){
				System.out.println(ticketCodeLst[i]);
			}
		}
		
		boolean[] consideredList = new boolean[n];
		for(int i = 0; i < n; i++){
			consideredList[i] = false;
		}
		for(int i = 0; i < n; i++){
			if(singleRequest[i]){
				consideredList[i] = true;
				if(input.getRequests()[i].getDirectItineraries() == null || input.getRequests()[i].getDirectItineraries().length == 0)
					continue;
								
 				
				for(int j = 0; j < n; j++){
					if(consideredList[j] == false){
				
						if(input.getRequests()[j].getDirectItineraries() == null || input.getRequests()[j].getDirectItineraries().length == 0)
							continue;
						
						for(int ind1 = 0; ind1 < input.getRequests()[i].getDirectItineraries().length; ind1++){
							for(int ind2 = 0; ind2 < input.getRequests()[j].getDirectItineraries().length; ind2++){
								String itinerary1 = input.getRequests()[i].getDirectItineraries()[ind1];
								String[] iti1 = itinerary1.split(";");
								Vector<Double> latLst1 = new Vector<Double>();
								Vector<Double> lonLst1 = new Vector<Double>();				
								for(int k = 0; k < iti1.length; k++){
									String[] elements1 = iti1[k].split(",");
									latLst1.add(Double.parseDouble(elements1[0]));
									lonLst1.add(Double.parseDouble(elements1[1]));
								}
								
								String itinerary2 = input.getRequests()[j].getDirectItineraries()[ind2];					
								String[] iti2 = itinerary2.split(";");
								Vector<Double> latLst2 = new Vector<Double>();
								Vector<Double> lonLst2 = new Vector<Double>();
								
								for(int k = 0; k < iti2.length; k++){
									String[] elements2 = iti2[k].split(",");
									latLst2.add(Double.parseDouble(elements2[0]));
									lonLst2.add(Double.parseDouble(elements2[1]));
								}
								
								
								//A merge is acceptable since we have two merges:						
								boolean firstMerge = false;
								boolean secondMerge = false;
								
								//Check for a possible first merge
								double timeDelta = Math.abs(DateTimeUtils.distance(input.getRequests()[i].getDepartTime(), input.getRequests()[j].getDepartTime()));
								
								//case 1: pickup 1 -> pickup 2 
								double cumulativeDistace = 0.0;
								for(int k = 0; k < latLst2.size(); k++){
									double dis = G.computeDistanceHaversine(latLst1.firstElement(), lonLst1.firstElement(), latLst2.elementAt(k), lonLst2.elementAt(k));

									if(dis < MERGE_MAX_DIS){ //Check for distance condition
										
																		
										if(timeDelta - cumulativeDistace * 1000.0/input.getStableSpeed() < MERGE_MAX_TIME &&
												timeDelta - cumulativeDistace * 1000.0/input.getStableSpeed() > -MERGE_MAX_TIME){ //Check for pickup time condition
											firstMerge = true;
											
											break;
										}								
									}
									
									if(k < latLst2.size() - 1){
										cumulativeDistace += G.computeDistanceHaversine(latLst2.elementAt(k), lonLst2.elementAt(k), latLst2.elementAt(k+1), lonLst2.elementAt(k+1));
									}
								}
								
								//Case 2: pickup 2 -> pickup 1
								if(firstMerge == false){
									cumulativeDistace = 0.0;
									for(int k = 0; k < latLst1.size(); k++){
										double dis = G.computeDistanceHaversine(latLst2.firstElement(), lonLst2.firstElement(), latLst1.elementAt(k), lonLst1.elementAt(k));

										if(dis < MERGE_MAX_DIS){									
											if(timeDelta - cumulativeDistace * 1000.0/input.getStableSpeed() < MERGE_MAX_TIME &&
													timeDelta - cumulativeDistace * 1000.0/input.getStableSpeed() > -MERGE_MAX_TIME){
												firstMerge = true;
												break;
											}								
										}
										
										if(k < latLst1.size() - 1){
											cumulativeDistace += G.computeDistanceHaversine(latLst1.elementAt(k), lonLst1.elementAt(k), latLst1.elementAt(k+1), lonLst1.elementAt(k+1));
										}
									}
								}
								
								//Check for the second merge if needed
								if(firstMerge){									
									//Case 1: Delivery 1 -> Delivery 2
									for(int k = 0; k < latLst2.size(); k++){
										double dis = G.computeDistanceHaversine(latLst1.lastElement(), lonLst1.lastElement(), latLst2.elementAt(k), lonLst2.elementAt(k));
										if(dis < MERGE_MAX_DIS){
											secondMerge = true;
											break;
										}
									}
									if(secondMerge == false){
										//Case 2: Delivery 2 -> Delivery 1
										for(int k = 0; k < latLst1.size(); k++){
											double dis = G.computeDistanceHaversine(latLst2.lastElement(), lonLst2.lastElement(), latLst1.elementAt(k), lonLst1.elementAt(k));
											if(dis < MERGE_MAX_DIS){
												secondMerge = true;
												break;
											}
										}
									}
								}
								
								//Check a possible merge
								if(firstMerge && secondMerge){
									possibleMerge[i][j] = true;
									System.out.println("There is a merge between " + input.getRequests()[i].getTicketCode() + " and " + input.getRequests()[j].getTicketCode());
								}
							}
						}
					}
				}
			}
		}
		
		
		
		//Do Merging
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				if(possibleMerge[i][j]){
					
					//Checking request[j] is in route with more than 3 requests or not
					boolean insertable = true;
					for(SharedLongTripRoute sltr : routeLst){	
						if(sltr.getRouteElements().length >= 6){
							for(SharedLongTripElement slte : sltr.getRouteElements()){
								if(input.getRequests()[j].getTicketCode().equals(slte.getTicketCode() )){
									insertable = false;
									break;
								}	
							}										
						}			
					}
					
					if(insertable){
																		
						//Copy and delete route containing request j
						int index = -1;
						SharedLongTripRoute editRoute = new SharedLongTripRoute();
						for(int k = 0; k < routeLst.size(); k++){
							for(SharedLongTripElement slte : routeLst.elementAt(k).getRouteElements()){
								if(input.getRequests()[j].getTicketCode().equals( slte.getTicketCode() )){
									index = k;
									break;
								}
							}
						}
						
						if(index >= 0){
							editRoute = routeLst.elementAt(index);								
							routeLst.remove(index);											
							
							//Insert pickup elements							
							SharedLongTripElement[] elementLst = new SharedLongTripElement[editRoute.getNbRequests() * 2 + 2];
							for(int p = 0; p < editRoute.getNbRequests(); p++){
								elementLst[p] = editRoute.getRouteElements()[p];
							}
							SharedLongTripElement newElement = new SharedLongTripElement(requestLst[i].getTicketCode(), 
									requestLst[j].getDepartTime(), requestLst[j].getPickupAddress(), requestLst[j].getPickupPos(), "-", "-",SharedLongTripElement.PICKUP);
							elementLst[editRoute.getNbRequests()] = newElement;		
		
							//Sorting in order of depart time
							SharedLongTripElement tempElement = new SharedLongTripElement();
							for(int p = 0; p < editRoute.getNbRequests(); p++){
								for(int q = p + 1; q < editRoute.getNbRequests() + 1; q++){
									double t1 = DateTimeUtils.dateTime2Int(elementLst[p].getDepartTime());
									double t2 = DateTimeUtils.dateTime2Int(elementLst[q].getDepartTime());
									if(t1 > t2){
										tempElement = elementLst[p];
										elementLst[p] = elementLst[q];
										elementLst[q] = tempElement;												
									}
								}
							}
							
							//insert delivery elements
							for(int p = editRoute.getNbRequests()+1; p < 2*editRoute.getNbRequests()+1; p++){
								elementLst[p] = editRoute.getRouteElements()[p-1];
							}
							SharedLongTripElement newElement2 = new SharedLongTripElement(requestLst[i].getTicketCode(), 
									"-", "-", "-", requestLst[i].getDeliveryAddress(), requestLst[i].getDeliveryPos(), SharedLongTripElement.DELIVERY);
							elementLst[2 * editRoute.getNbRequests() + 1] = newElement2;
							
							//Sorting						
							for(int p = editRoute.getNbRequests() +1; p < 2* editRoute.getNbRequests()+1; p++){
								for(int q = p + 1; q < 2*editRoute.getNbRequests() + 2; q++){
									
									double d1 = G.computeDistanceHaversine(elementLst[editRoute.getNbRequests()].getPickupPosition(), elementLst[p].getDeliveryPosition());
									double d2 = G.computeDistanceHaversine(elementLst[editRoute.getNbRequests()].getPickupPosition(), elementLst[q].getDeliveryPosition());
									if(d1 > d2){
										tempElement = elementLst[p];
										elementLst[p] = elementLst[q];
										elementLst[q] = tempElement;												
									}
								}
							}
							
							int peopleNb = editRoute.getNbPeople() + requestLst[i].getNumberPassengers();
							String taxiType = "";
			    			for(int p = 1; p < vhcCapacities.length; p++){
			    				if(peopleNb <= vhcCapacities[p]){
			    					taxiType = Integer.toString(vhcCapacities[p]);
			    					break;
			    				}
			    			} 
			
			    			SharedLongTripRoute route = new SharedLongTripRoute(elementLst, taxiType, peopleNb, editRoute.getNbRequests() + 1);
			    			
			    		
			    			
			    			routeLst.add(route);
			    			
			    			
			    			
			    			//Delete route containing request i
							index = -1;						
							for(int k = 0; k < routeLst.size(); k++){
								if(routeLst.elementAt(k).getRouteElements().length == 2){
									for(SharedLongTripElement slte : routeLst.elementAt(k).getRouteElements()){
										if(input.getRequests()[i].getTicketCode().equals( slte.getTicketCode() )){
											index = k;
											break;
										}
									}
								}								
							}
							if(index >= 0){
								routeLst.remove(index);	
								
								
							}else{
								System.out.println("Error1");
							}
							
							
						}else{
							System.out.println("Error2");
						}
					}
				}
			}
		}
		
	}
	
	
}
