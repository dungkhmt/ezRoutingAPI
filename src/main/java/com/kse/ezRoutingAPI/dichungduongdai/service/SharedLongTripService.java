package com.kse.ezRoutingAPI.dichungduongdai.service;

import java.util.Random;
import java.util.Vector;

import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripElement;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripInput;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripRequest;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripRoute;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripSolution;
import com.kse.utils.DateTimeUtils;

import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.GoogleMapsQuery;

public class SharedLongTripService {
	private static double MAX_VALUE = 10000000;
	public String name(){
		return "DichungDuongDaiService";
	}
	
	public SharedLongTripSolution computeSharedLongTrip(SharedLongTripInput input){
		GoogleMapsQuery G = new GoogleMapsQuery();
		
		SharedLongTripRequest[] requestLst = input.getRequests();
		int n = requestLst.length; //Number of requests
		
		int[] vhcCapacities = input.getVehicleCapacities();
		
		
		int[][] C = new int[n][n]; //Connected Matrix, c[i][j] = 1 it means that 
									// requests i and j can go together
		double[][] d1 = new double[n][n]; //Distance between requests' pickup positions
		double[][] d2 = new double[n][n]; //Distance between requests' delivery positions
		double[][] td = new double[n][n]; //Time distance between requests' pickup points
		double[] pickupTime = new double[n]; //Relative time of the pickups
		
		
		
		Vector<Integer> neighborRequests[] = (Vector<Integer>[]) new Vector[n]; //store neighbor requests of a request
		for(int i = 0; i < n; i++){
			neighborRequests[i] = new Vector<Integer>();		
		}
		//Calculate the matrix C, d and the array of neighbor lists
		int earlestRequestIndex = 0;
		for(int i = 1; i < n; i++){
			if(DateTimeUtils.distance(requestLst[i].getDepartDateTime(), requestLst[earlestRequestIndex].getDepartDateTime()) < 0){
				earlestRequestIndex = i;
			}
		}
		for(int i = 0; i < n; i++){
			pickupTime[i] = DateTimeUtils.distance(requestLst[i].getDepartDateTime(), requestLst[earlestRequestIndex].getDepartDateTime());
		}
				
		for(int i = 0; i < n-1; i++){			
			for(int j = i + 1; j < n; j++){
				//For distance matrix d
				d1[i][j] = G.computeDistanceHaversine(requestLst[i].getPickupLatLng(), requestLst[j].getPickupLatLng());
				d1[i][j] *= 1000 * input.getApproximationDistanceFactor();
				d1[j][i] = d1[i][j];
				
				d2[i][j] = G.computeDistanceHaversine(requestLst[i].getDeliveryLatLng(), requestLst[j].getDeliveryLatLng());
				d2[i][j] *= 1000 * input.getApproximationDistanceFactor();
				d2[j][i] = d2[i][j];
				
				td[i][j] = Math.abs(pickupTime[i] - pickupTime[j]);
				td[j][i] = td[i][j];
				
				//For connected matrix
				C[i][j] = 0;
				C[j][i] = 0;				
				if(requestLst[i].isSharing() && requestLst[j].isSharing()){//Already for sharing
					if(requestLst[i].getItinerary() == requestLst[j].getItinerary()){ //Same itinerary					
						if(d1[i][j] <= input.getForbidenStraightDistance()){ // Two pickup points are not too far to each other						
							if(d2[i][j] <= 2.0 * input.getForbidenStraightDistance()){ // Two delivery points are not too far to each other							
								if(td[i][j] <= input.getForbidenTimeDistance()){//Pickup times of two requests are not too far 
									
									C[i][j] = 1;
									C[j][i] = 1;
									
									neighborRequests[i].add(j);
									neighborRequests[j].add(i);
									
									//System.out.println("Suitable for sharing: request " + requestLst[i].getTicketCode() + " with request " + requestLst[j].getTicketCode());
								}
							}
						}
					}		
				}				
			}
		}		
		
		// Computing isolated component
		Vector<Integer> IC[] = (Vector<Integer>[]) new Vector[n]; // Storing Isolated Components
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
	   
	    //Considering the isolated components
	    int count = 0;
	    
	    Vector<SharedLongTripRoute> routeLst = new Vector<SharedLongTripRoute>();
	    for(int i = 0; i < n; i++){
	    	if(IC[i].size() < 3){
	    		if(IC[i].size() == 1){ //Single
	    			SharedLongTripRequest req = requestLst[IC[i].elementAt(0)];
	    			SharedLongTripElement[] elementLst = new SharedLongTripElement[2];
	    			elementLst[0] = new SharedLongTripElement(req.getTicketCode(), req.getDepartDateTime(), req.getPickupAddress(), req.getPickupLatLng(), "-", "-");
	    			elementLst[1] = new SharedLongTripElement(req.getTicketCode(), "", "-", "-", req.getDeliveryAddress(), req.getDeliveryLatLng());
	    			
	    			int nbPeople = req.getNbPassengers();
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
	    			
	    			if(DateTimeUtils.distance(req1.getDepartDateTime(), req2.getDepartDateTime()) < 0){
	    				elementLst[0] = new SharedLongTripElement(req1.getTicketCode(), req1.getDepartDateTime(), req1.getPickupAddress(), req1.getPickupLatLng(), "-", "-");
	    				elementLst[1] = new SharedLongTripElement(req2.getTicketCode(), req2.getDepartDateTime(), req2.getPickupAddress(), req2.getPickupLatLng(), "-", "-");
	    			}else{
	    				elementLst[0] = new SharedLongTripElement(req2.getTicketCode(), req2.getDepartDateTime(), req2.getPickupAddress(), req2.getPickupLatLng(), "-", "-");
	    				elementLst[1] = new SharedLongTripElement(req1.getTicketCode(), req1.getDepartDateTime(), req1.getPickupAddress(), req1.getPickupLatLng(), "-", "-");	    				
	    			}
	    			
	    			double distance1 = G.getDistance(elementLst[1].getPickupPosition(), req1.getDeliveryLatLng());
	    			double distance2 = G.getDistance(elementLst[1].getPickupPosition(), req2.getDeliveryLatLng());
	    			
	    			if(distance1 < distance2){
	    				elementLst[2] = new SharedLongTripElement(req1.getTicketCode(), "-", "-", "-", req1.getDeliveryAddress(), req1.getDeliveryLatLng());
	    				elementLst[3] = new SharedLongTripElement(req2.getTicketCode(), "-", "-", "-", req2.getDeliveryAddress(), req2.getDeliveryLatLng());
	    			}else{
	    				elementLst[2] = new SharedLongTripElement(req2.getTicketCode(), "-", "-", "-", req2.getDeliveryAddress(), req2.getDeliveryLatLng());
	    				elementLst[3] = new SharedLongTripElement(req1.getTicketCode(), "-", "-", "-", req1.getDeliveryAddress(), req1.getDeliveryLatLng());
	    			}
	    			
	    			int nbPeople = req1.getNbPassengers() + req2.getNbPassengers();
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
	    		
	    		int maxIt = 1000;
	    		int it = 1;
	    		int stable = 0;
	    		int [] bestSol = new int[m]; //For the best solution found so far
	    		int [] routeBestSol = new int[m]; //routeBestSol[i] states that the route index of request sol[i]
	    		double [] bsTimeLB = new double[m]; //Lower bound time for the arrival of best solution
	    		double [] bsTimeUB = new double[m]; //Upper bound time for the arrival of best solution
	    		
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
	    			
	    			SharedLongTripElement[] elementLst = new SharedLongTripElement[2*k];
	    			for(int j = 0; j < k; j++){
	    				elementLst[j] = new SharedLongTripElement(temRequestLst.elementAt(j).getTicketCode(), temRequestLst.elementAt(j).getDepartDateTime(),
	    						temRequestLst.elementAt(j).getPickupAddress(), temRequestLst.elementAt(j).getPickupLatLng(), "-", "-");
	    			}
	    			
	    			int [] visited = new int[k];
	    			String lastLatLng = elementLst[k-1].getPickupPosition();
	    				    			
	    			for(int j = 0; j < k; j++){
	    				double dist = 10000000;
	    				int index = -1;
	    				for(int p = 0; p < k; p++){
	    					if(visited[p] == 0){
	    						double d =G.getDistance(temRequestLst.elementAt(p).getDeliveryLatLng(), lastLatLng); 
	    						if(d < dist){
	    							dist = d;
	    							index = p; 
	    						}
	    					}
	    				}
	    				
	    				visited[index] = 1;
	    				lastLatLng = temRequestLst.elementAt(index).getDeliveryLatLng();	    				
	    				
	    				elementLst[k+j] = new SharedLongTripElement(temRequestLst.elementAt(index).getTicketCode(), "-", "-", "-", 
	    						temRequestLst.elementAt(index).getDeliveryAddress(), temRequestLst.elementAt(index).getDeliveryLatLng());
	    				
	    			}
	    			int nbPeople = 0;
	    			for(int j = 0; j < k; j++){
	    				nbPeople += temRequestLst.elementAt(j).getNbPassengers();
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
	    SharedLongTripRoute[] routeArray = new SharedLongTripRoute[routeLst.size()];
	    for(int i = 0; i < routeLst.size(); i++){
	    	routeArray[i] = routeLst.elementAt(i);
	    }
	    SharedLongTripSolution solution = new SharedLongTripSolution(n, routeArray);
	    
		return solution;
	}
	
	//Copy from to solution 1 to solution 2
	public void copySolution(int [] sol1, int [] routeSol1, double [] s1TimeLB, double [] s1TimeUB,
			int [] sol2, int [] routeSol2, double [] s2TimeLB, double [] s2TimeUB){
		for(int i = 0; i < sol1.length; i++){
			sol2[i] = sol1[i];
			routeSol2[i] = routeSol1[i];
			s2TimeLB[i] = s1TimeLB[i];
			s2TimeUB[i] = s2TimeUB[i];
		}
	}
	
	//Compute routes for a sequence
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
}
