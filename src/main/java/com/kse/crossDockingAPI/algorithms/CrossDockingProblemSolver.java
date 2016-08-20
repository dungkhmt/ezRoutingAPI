package com.kse.crossDockingAPI.algorithms;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.io.IOException;

public class CrossDockingProblemSolver {
	private String fileName;
	
	// Problem attributes
	private int M; // Number of origins
	private int N; // Number of destinations
	private int I; // Number of strip doors
	private int J; // Number of stack doors
	
	private int[][] w; // Number of trips required by the material handling
						// equipment to move items originating from m to the
						// cross-dock door where freight destined for n is
						// being consolidated
	private int[][] d; //Distance between strip door i and stack door j
	private int[] s; // Volume of goods from origin m
	private int[] S; // Capacity of strip door i
	private int[] r; // Demand from destination n
	private int[] R; // Capacity of stack door j
	

	//Best-known solution during the search
	public int[] bx = new int[M];
	public int[] by = new int[N];
	public int bVal = 0;

	
	/*
	 * Constructor without any parameters
	 */
	public CrossDockingProblemSolver(String _fileName){
		fileName = _fileName;
	}
	
	public CrossDockingProblemSolver(
			long nbInVehicles, long nbOutVehicles, int[] demandInVehicles, int [] demandOutVehicles,
			long nbInDoors, long nbOutDoors, int[] capacityInDoors, int[] capacityOutDoors,
			int[][] C, int[][] T)
	{
		//Origin + Destination
		M = (int)nbInVehicles;
		N = (int)nbOutVehicles;
		
		s = new int[M];
		for(int i = 0; i < demandInVehicles.length; i++){
			s[i] = demandInVehicles[i];
		}
		r = new int[N];
		for(int i = 0; i < demandOutVehicles.length; i++){
			r[i] = demandOutVehicles[i];
		}
			
		//Strip door + Stack door
		I = (int)nbInDoors;
		J = (int)nbOutDoors;
		
		S = new int[I];
		for(int i = 0; i < capacityInDoors.length; i++){
			S[i] = capacityInDoors[i];
		}
		R = new int[J];
		for(int i = 0; i < capacityOutDoors.length; i++){
			R[i] = capacityOutDoors[i];
		}
		
		//Distance
		d = new int[I][J];
		for(int i = 0; i < I; i++){
			for(int j = 0; j < J; j++){
				d[i][j] = C[i][j];
			}
		}
		
		//Trips
		w = new int[M][N];
		for(int i = 0; i < M; i++){
			for(int j = 0; j < N; j++){
				w[i][j] = T[i][j];
			}
		}
	}
	
	public void readProblemInstanceFromFile(){
		try{
			// Open the file
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			//Line 1: #M
			br.readLine();
			
			//Line 2: M
			if ((strLine = br.readLine()) != null){
				M = Integer.parseInt(strLine);
			}
			
			//Line 3: #N
			br.readLine();
			
			//Line 4: M
			if ((strLine = br.readLine()) != null){
				N = Integer.parseInt(strLine);
			}
			
			//Line 5: #I
			br.readLine();
			
			//Line 6: I
			if ((strLine = br.readLine()) != null){
				I = Integer.parseInt(strLine);
			}
			
			//Line 7: #N
			br.readLine();
			
			//Line 8: J
			if ((strLine = br.readLine()) != null){
				J = Integer.parseInt(strLine);
			}
			
			//Line 9: #w
			br.readLine();
			
			//Line 10 -- 9 + M
			w = new int[M][N];
			for(int i = 0; i < M; i++){
				if ((strLine = br.readLine()) != null){
					String[] elements = strLine.split("\\s");
					for(int j = 0; j < N; j++){
						w[i][j] = Integer.parseInt(elements[j]);
					}
					//System.out.println("Size of strings : " + elements.length);
				}
			}
			
			//Line 9 + M : #d
			br.readLine();
			
			//Line 10 + M + 1 -- 10 + M + I
			d = new int[I][J];
			for(int i = 0; i < I; i++){
				if ((strLine = br.readLine()) != null){
					String[] elements = strLine.split("\\s");
					for(int j = 0; j < J; j++){
						d[i][j] = Integer.parseInt(elements[j]);
					}
					//System.out.println("Size of strings : " + elements.length);
				}
			}
			
			//Line 10 + M + I + 1: #s
			br.readLine();
			
			//Line 10 + M + I + 2 
			s = new int[M];
			if ((strLine = br.readLine()) != null){
				String[] elements = strLine.split("\\s");
				for(int j = 0; j < M; j++){
					s[j] = Integer.parseInt(elements[j]);
				}
				//System.out.println("Size of strings : " + elements.length);
			}
			
			//Line 10 + M + I + 3: #s
			br.readLine();
			
			//Line 10 + M + I + 4 
			S = new int[I];
			if ((strLine = br.readLine()) != null){
				String[] elements = strLine.split("\\s");
				for(int j = 0; j < I; j++){
					S[j] = Integer.parseInt(elements[j]);
				}
				//System.out.println("Size of strings : " + elements.length);
			}
			
			//Line 10 + M + I + 5: #s
			br.readLine();
			
			//Line 10 + M + I + 6 
			r = new int[N];
			if ((strLine = br.readLine()) != null){
				String[] elements = strLine.split("\\s");
				for(int j = 0; j < N; j++){
					r[j] = Integer.parseInt(elements[j]);
				}
				//System.out.println("Size of strings : " + elements.length);
			}
			
			//Line 10 + M + I + 7: #s
			br.readLine();
			
			//Line 10 + M + I + 8 
			R = new int[J];
			if ((strLine = br.readLine()) != null){
				String[] elements = strLine.split("\\s");
				for(int j = 0; j < J; j++){
					R[j] = Integer.parseInt(elements[j]);
				}
				//System.out.println("Size of strings : " + elements.length);
			}
			
			
			
			
			//Close the input stream
			br.close();
		}catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void printOriginGoodVolumn(){
		System.out.println("Origin Good Volumn");
		for(int i = 0; i < M; i++){
			System.out.print(s[i] + " ");
		}
		System.out.println();
	}
	
	public void printDestinationDemand(){
		System.out.println("Demand of destinations");
		for(int i = 0; i < N; i++){
			System.out.print(r[i] + " ");
		}
		System.out.println();
	}
	
	public void printCapacityOfStripDoor(){
		System.out.println("Capacities of trip doors");
		for(int i = 0; i < I; i++){
			System.out.print(S[i] + " ");
		}
		System.out.println();
	}
	
	public void printCapacityOfStackDoor(){
		System.out.println("Capacities of stack doors");
		for(int i = 0; i < J; i++){
			System.out.print(R[i] + " ");
		}
		System.out.println();
	}
	/*
	 * Print flow matrix
	 */
	public void printFlowMatrix(){
		System.out.println("Printing the flow matrix");
		for(int i = 0; i < M; i++){
			for(int j = 0; j < N; j++){
				System.out.println("[" + i + "," + j + "] = " + w[i][j]);
			}
		}
	};
	
	/*
	 * Generate randomly a solution
	 */
	void generateRandomlySolution(int[] x, int[] y){
		Random rn = new Random();
		
		//Assigning each origin to a strip door
		int[] stpCpctRemain = new int[I]; //Capacity remain of the strip doors
		for(int i = 0; i < I; i++){
			stpCpctRemain[i] = S[i];
		}
		
		for(int i = 0; i < M; i++){
			//System.out.println("Assigning the origin " + i);
			boolean assigned = false;
			int counter = 0;
			while(!assigned && counter <= 10 * I){
				counter++;
				
				int stripDoor = rn.nextInt(I);
				//System.out.println("Taking strip door " + stripDoor);
				if(stpCpctRemain[stripDoor] >= s[i]){
					//Assign this origin to the that strip door
					x[i] = stripDoor;
					assigned = true;
					stpCpctRemain[stripDoor] -= s[i];
				}
			}
			
			//If it can not find a strip door for an origin, restarting whole the assignment 
			if(!assigned){
				i = -1;
				for(int j = 0; j < I; j++){
					stpCpctRemain[j] = S[j];
				}
			}
		}
		
		
		//Assigning each destination to a stack door
		int[] stkCpctRemain = new int[J]; //Capacity remain of the strip doors
		for(int i = 0; i < J; i++){
			stkCpctRemain[i] = R[i];
		}
		
		for(int i = 0; i < N; i++){
			//System.out.println("Assigning the destination " + i);
			boolean assigned = false;
			int counter = 0;
			while(!assigned && counter <= 10 * J){
				counter++;
				
				int stackDoor = rn.nextInt(J);
				if(stkCpctRemain[stackDoor] >= r[i]){
					//Assign this origin to the that strip door
					y[i]= stackDoor;
					assigned = true;
					stkCpctRemain[stackDoor] -= r[i];
				}
			}
			
			//If it can not find a stack door for a destination, restarting whole the assignment 
			if(!assigned){
				i = -1;
				for(int j = 0; j < J; j++){
					stkCpctRemain[j] = R[j];
				}
			}
		}	
	}
	
	/*
	 * Printing a solution
	 */
	public  void printSolution(int[] x,  int[] y, int val){
		for(int i = 0; i < M; i++){
			System.out.println("Origin " + i + " assigned to strip door " + x[i]);
			
		}
		
		for(int i = 0; i < N; i++){
			System.out.println("Destination " + i + " assigned to stack door " + y[i]);
		}
		
		System.out.println("Solution value : " + val);
	}
	
	/*
	 * Copy solution: solution2 = solutions1
	 */
	public void copySolution(int[] x1, int[] y1, int[] rmStpCap1, int[] rmStkCap1, 
		int[] x2, int[] y2,  int[] rmStpCap2, int[] rmStkCap2){
		for(int i = 0; i < M; i++){
			x2[i] = x1[i];
		}
		
		for(int i = 0; i < N; i++){
			y2[i] = y1[i];
		}
		
		
		for(int i = 0; i < I; i++){
			rmStpCap2[i] = rmStpCap1[i];
		}
		
		for(int i = 0; i < J; i++){
			rmStkCap2[i] = rmStkCap1[i];
		}
	}
	
	/*
	 * Compute value of a solution
	 */
	public int computeSolValue(int[] x, int[] y){
		int solValue = 0;
		for(int i = 0; i < M; i++){
			for(int j = 0; j < N; j++){
				if(w[i][j] > 0){
					solValue += d[x[i]][y[j]] * w[i][j];
				}
			}
		}
		
		return solValue;
	}
	
	public void computeRemainingCapacities(int[] x, int[] y, int[] stp, int[] stk){
		for(int i = 0; i < I; i++){
			stp[i] = S[i];
		}
		
		for(int j = 0; j < J; j++){
			stk[j] = R[j];
		}
		
		for(int i = 0; i < M; i++){
			stp[x[i]] -= s[i];
		}
		
		for(int i = 0; i < N; i++){
			stk[y[i]] -= r[i];
		}
	}
	
	/*
	 * Tabu and constraint-based local Search 
	 */
	public void TabuConstraintBasedLocalSearch(){
		// Algorithm parameters
		int maxIt = 2000000; // Maximum of iterations
		int[] xtabu = new int[M]; // Tabu for x 
		int[] ytabu = new int[N]; //Tabu for y
		int tbl = 1; // Tabu length

		int restartFreq = 10000; //Frequency of restart
		int stable = 0; 
		int stableLimit = 800; //Limit of stable 
				
		//A solution
		int[] x = new int[M];
		int[] y = new int[N];
		int val = 0;
		int[] rmStpCap = new int[I];
		int[] rmStkCap = new int[J];
		
		generateRandomlySolution(x, y); //Generate randomly a solution
		computeRemainingCapacities(x, y, rmStpCap, rmStkCap);	
		val = computeSolValue(x, y);
		
		System.out.println("Randomly generated solution: ");
		printSolution(x, y, val);
		System.out.println("\n Remaining capacities of strip doors");
		for(int i = 0; i < rmStpCap.length; i++){
			System.out.print(" " + rmStpCap[i]);
		}
		System.out.println("\n Remaining capacities of stack doors");
		for(int i = 0; i < rmStkCap.length; i++){
			System.out.print(" " + rmStkCap[i]);
		}
		System.out.println();
		
		//Best-known solution during the search
		//int[] bx = new int[M];
		//int[] by = new int[N];
		//int bVal = 0;
		bx = new int[M];
		by = new int[N];
		int[] bRmStpCap = new int[I];
		int[] bRmStkCap = new int[J];
		
		//Copy the current solution to the best solution
		copySolution(x, y, rmStpCap, rmStkCap, bx, by, bRmStpCap, bRmStkCap);
		bVal = val;
		
		Random rn = new Random();
		
		//Starting the search
		int it = 0;
		while(it < maxIt){
			
			//System.out.println("\n it :" + it);
			if(rn.nextInt(2) == 0 ){ //Working on x variable
				int origin = -1;
				int stripDoor = -1;	
				int ori1 = -1;
				int ori2 = -1;
				int maxReduce = 100000;
				
				//Searching for a good neighbor
				for(int i = 0; i < M; i++){ //Consider each origin
					if(xtabu[i] == 0){ //This variable is not in tabu
						//System.out.println("Considering origin : " + i);
						int dis = 0;
						for(int j = 0; j < N; j++){
							if(w[i][j] > 0){
								
								dis += d[x[i]][y[j]] * w[i][j];
							}
						}
						
						for(int j = 0; j < I; j++){
							if(j != x[i] && rmStpCap[j] >= s[i]){ //Trying move to strip door j
								//System.out.println("Assigning to " + j);
								int delta = 0;
								delta -= dis;
								
								for(int p = 0; p < N; p++){
									if(w[i][p] >0){
										delta += d[j][y[p]] * w[i][p];
									}
								}
								
								if(maxReduce > delta){
									origin = i;
									stripDoor = j;
									maxReduce = delta ;
								}
							}
						}
						
						//System.out.println("max reduce " + maxReduce);
						
						if(maxReduce < 0){
							break;
						}
					}					
				}
				
				//If there does not exist better solution in the first neighborhood, 
				//finding in the second neighborhood (swapping)
				if(maxReduce >= 0 ){
					for(int i  = 0; i < M; i++){
						if(xtabu[i] == 0){
							for(int j = 0; j < M; j++){
								if(ytabu[j] == 0 && i != j){
									if(rmStpCap[x[i]] + s[i] - s[j] >= 0 &&
											rmStpCap[x[j]] + s[j] - s[i] >= 0 ){
										int delta = 0;
										for(int p = 0; p < N; p++){
											if(w[i][p] > 0){
												delta += w[i][p] * (d[x[j]][y[p]] - d[x[i]][y[p]]);
											}
											
											if(w[j][p] > 0){
												delta += w[j][p] * (d[x[i]][y[p]] - d[x[j]][y[p]]);
											}
										}
										
										if(maxReduce > delta){
											ori1 = i;
											ori2 = j;
											maxReduce = delta;
	
										}
										
										
										if(maxReduce < 0){
											i = M;
											j = M;
											break;
										}
									}
								}
							}
						}
					}
				}
				
				//Moving 
				if(origin != -1){ 
					if(ori1 == -1 && ori2 == -1){
						//System.out.println("Moving with decrease of " + maxReduce);
						rmStpCap[x[origin]] += s[origin];
						x[origin] = stripDoor;
						val += maxReduce;
						rmStpCap[x[origin]] -= s[origin];
						
						//Update tabu
						for(int i = 0; i < M; i++){
							if(xtabu[i] > 0){
								xtabu[i] -= 1;
							}
						}
						xtabu[origin] = tbl;
						
						//System.out.println("val " + val);
					}else{
						
						rmStpCap[x[ori1]] += s[ori1] - s[ori2];
						rmStpCap[x[ori2]] += s[ori2] - s[ori1];
						int temps = x[ori1];
						x[ori1] = x[ori2];
						x[ori2] = temps;
						val += maxReduce;
						

						//Update tabu
						for(int i = 0; i < M; i++){
							if(xtabu[i] > 0){
								xtabu[i] -= 1;
							}
						}
						xtabu[ori1] = tbl;
						xtabu[ori2] = tbl;
						
					}
					
				}
			}else {
				
				int destination = -1;
				int des1 = -1;
				int des2 = -1;
				int stackDoor = -1;				
				int maxReduce = 100000;
				
				//Searching for a good neighbor
				for(int i = 0; i < N; i++){ //Consider each destination
					if(ytabu[i] == 0){ //This variable is not in tabu
						//System.out.println("Considering destination : " + i);
						int dis = 0;
						for(int j = 0; j < M; j++){
							if(w[j][i] > 0){
								
								dis += d[x[j]][y[i]] * w[j][i];
							}
						}
						
						for(int j = 0; j < J; j++){
							if(j != y[i] && rmStkCap[j] >= r[i]){ //Trying move to stack door j
								//System.out.println("Assigning to " + j);
								int delta = 0;
								delta -= dis;
								
								for(int p = 0; p < M; p++){
									if(w[p][i] >0){
										delta += d[x[p]][j] * w[p][i];
									}
								}
								
								if(maxReduce > delta){
									destination = i;
									stackDoor = j;
									maxReduce = delta ;
								}
							}
						}
						
						//System.out.println("max reduce " + maxReduce);
						
						if(maxReduce < 0){
							break;
						}
					}					
				}
				
				//If there does not exist better solution in the first neighborhood, 
				//finding in the second neighborhood (swapping)
				if(maxReduce >= 0){
					for(int i  = 0; i < N; i++){
						if(xtabu[i] == 0){
							for(int j = 0; j < N; j++){
								if(ytabu[j] == 0 && i != j){
									if(rmStkCap[y[i]] + r[i] - r[j] >= 0 &&
											rmStkCap[y[j]] + r[j] - r[i] >= 0 ){
										int delta = 0;
										for(int p = 0; p < M; p++){
											if(w[p][i] > 0){
												delta += w[p][i] * (d[x[p]][y[j]] - d[x[p]][y[i]]);
											}
											
											if(w[p][j] > 0){
												delta += w[p][j] * (d[x[p]][y[i]] - d[x[p]][y[j]]);
											}
										}
										
										if(maxReduce > delta){
											des1 = i;
											des2 = j;
											maxReduce = delta;
											
											
										}
										
										
										if(maxReduce < 0){
											i = N;
											j = N;
											break;
										}
									}
								}
							}
						}
					}
				}
				
				//Moving 
				if(destination != -1){ 
					if(des1 == -1 && des2 == -1){
						//System.out.println("Moving with decrease of " + maxReduce);
						rmStkCap[y[destination]] += r[destination];
						y[destination] = stackDoor;
						val += maxReduce;
						rmStkCap[y[destination]] -= r[destination];
						
						//Update tabu
						for(int i = 0; i < N; i++){
							if(ytabu[i] > 0){
								ytabu[i] -= 1;
							}
						}
						ytabu[destination] = tbl;
						
						//System.out.println("val " + val);
					}else {
						rmStkCap[y[des1]] += r[des1] - r[des2];
						rmStkCap[y[des2]] += r[des2] - r[des1];
						int temps = y[des1];
						y[des1] = y[des2];
						y[des2] = temps;
						val += maxReduce;
						

						//Update tabu
						for(int i = 0; i < N; i++){
							if(ytabu[i] > 0){
								ytabu[i] -= 1;
							}
						}
						ytabu[des1] = tbl;
						ytabu[des2] = tbl;
					}					
				}
			}
			
			//Checking for updating the best solution
			if(bVal > val){
				System.out.println("Updating the best known solution at iteration " + it);
				copySolution(x, y, rmStpCap, rmStkCap, bx, by, bRmStpCap, bRmStkCap);
				bVal = val;
				System.out.println(" with value " + bVal + ", test " + computeSolValue(bx, by));
				
				stable = 0;
			}else if(stable == stableLimit){
				//System.out.println("Restoring");
				copySolution(bx, by, bRmStpCap, bRmStkCap, x, y, rmStpCap, rmStkCap);
				stable = 0;
			}else{
				stable++;
			}
			
			//Restart
			if(it % restartFreq == 0){
				//System.out.println("Restarting");
				generateRandomlySolution(x, y); //Generate randomly a solution
				computeRemainingCapacities(x, y, rmStpCap, rmStkCap);	
				val = computeSolValue(x, y);
			}
			 
			it++;
		}
		
		System.out.println("\n Best solution with value " + bVal);
		System.out.println("\nBest-known solution: ");
		printSolution(bx, by, bVal);
		computeRemainingCapacities(bx, by, rmStpCap, rmStkCap);	
		System.out.println("\n Remaining capacities of strip doors");
		for(int i = 0; i < rmStpCap.length; i++){
			System.out.print(" " + rmStpCap[i]);
		}
		System.out.println("\n Remaining capacities of stack doors");
		for(int i = 0; i < rmStkCap.length; i++){
			System.out.print(" " + rmStkCap[i]);
		}
		System.out.println();
	}
	
	
	
	/*
	 * Main function
	 */	
	public static void main(String[] args) {
		
		
		CrossDockingProblemSolver crossDocking = new CrossDockingProblemSolver("D:/kde-solution/Logistics/code/Logistic/src/main/java/vn/webapp/modules/crossdockingsystem/algorithms/test.txt");
		crossDocking.readProblemInstanceFromFile();
		//crossDocking.printFlowMatrix();
		crossDocking.TabuConstraintBasedLocalSearch();
		
	}
	
}
