package com.kse.crossDockingAPI.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kse.crossDockingAPI.algorithms.*;
import com.kse.crossDockingAPI.model.CrossDockingSystemInput;
import com.kse.crossDockingAPI.model.CrossDockingSystemSolution;


@RestController
public class CrossDockingSystem{
	
	public String name(){
		return "CrossDockingSystem";
	}
	

	@RequestMapping(value = "/solve-cross-docking-system", method = RequestMethod.POST)	
	public CrossDockingSystemSolution solveCrossDockingSystem(@RequestBody CrossDockingSystemInput input){
		
		System.out.println(name() + "::solveCrossDockingSystem, input = " + input);
		int[][] C;
		int[][] T;
		int[] capacityInDoors;
		int[] capacityOutDoors;
		int[] demandInVehicles;
		int[] demandOutVehicles;
		try{
			
			long nbInDoors = input.getInDoorNum();
			long nbOutDoors = input.getOutDoorNum();
			long nbInVehicles = input.getInVehicleNum();
			long nbOutVehicles = input.getOutVehicleNum();
			
			C = new int[(int)nbInDoors][(int)nbOutDoors];			
			for(int i  = 0; i < input.getCosts().length; i++){
				int inDoor = input.getCosts()[i].getFromDoor();
				int outDoor = input.getCosts()[i].getToDoor();
				C[inDoor][outDoor] = (int) input.getCosts()[i].getCost();
			}
			
			T = new int[(int)nbInVehicles][(int)nbOutVehicles];
			for(int i = 0; i < input.getTrips().length; i++){
				int inVhc = input.getTrips()[i].getFromVehicle();
				int outVhc = input.getTrips()[i].getToVehicle();
				T[inVhc][outVhc] = input.getTrips()[i].getTime();
			}
			capacityInDoors = new int[(int)nbInDoors];
			capacityInDoors = input.getInDoorCapacities();
			
			capacityOutDoors = new int[(int)nbOutDoors];
			capacityOutDoors = input.getOutDoorCapacities();
			
			demandInVehicles = new int[(int)nbInVehicles];
			demandInVehicles = input.getInVehicleDemands();
			
			demandOutVehicles = new int[(int)nbOutVehicles];
			demandOutVehicles = input.getOutVehicleDemands();
			
		
			
			for(int i = 0; i < nbInDoors; i++){
				for(int j = 0; j < nbOutDoors; j++)
					System.out.print(C[i][j] + " ");
				System.out.println();
			}
			
			for(int i = 0; i < nbInVehicles; i++){
				for(int j = 0; j < nbOutVehicles; j++)
					System.out.print(T[i][j] + " ");
				System.out.println();
			}
			System.out.println("capacity indoor:");
			for(int i = 0; i < capacityInDoors.length; i++)
				System.out.print(capacityInDoors[i] + " ");
			System.out.println();
			System.out.println("capacity outdoor:");
			for(int i = 0; i < capacityOutDoors.length; i++)
				System.out.print(capacityOutDoors[i] + " ");
			System.out.println();
			
			System.out.println("demand-in-vehicles:");
			for(int i = 0; i < demandInVehicles.length; i++)
				System.out.print(demandInVehicles[i] + " ");
			System.out.println();
			
			System.out.println("demand-out-vehicles:");
			for(int i = 0; i < demandOutVehicles.length; i++)
				System.out.print(demandOutVehicles[i] + " ");
			System.out.println();
			
			
			CrossDockingProblemSolver solve = new CrossDockingProblemSolver(nbInVehicles, nbOutVehicles, demandInVehicles, demandOutVehicles, nbInDoors, nbOutDoors, capacityInDoors, capacityOutDoors, C, T);
			solve.TabuConstraintBasedLocalSearch();
			
			/*
			solve.printOriginGoodVolumn();
			solve.printCapacityOfStripDoor();
			solve.printDestinationDemand();
			solve.printCapacityOfStackDoor();
			*/
			CrossDockingSystemSolution cdsSolution = new CrossDockingSystemSolution(0, solve.bVal, solve.bx, solve.by);
				
			return cdsSolution;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}		
}
