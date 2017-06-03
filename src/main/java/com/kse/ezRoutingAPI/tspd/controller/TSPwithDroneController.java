package com.kse.ezRoutingAPI.tspd.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.TSPDRequest;
import com.kse.ezRoutingAPI.tspd.model.TSPDSolution;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.service.GRASP;
import com.kse.ezRoutingAPI.tspd.service.GRASPkDrone;
import com.kse.ezRoutingAPI.tspd.service.TSPD;
import com.kse.ezRoutingAPI.tspd.service.TSPD_LS;

@RestController
public class TSPwithDroneController {

	TSPD tspd = null;
	
	@RequestMapping(value="/tsp-with-drone", method= RequestMethod.POST)
	public TSPDSolution computeTSPwithDroneProblem(HttpServletRequest request,@RequestBody TSPDRequest input){
//		System.out.println(name()+"computeTSPwithDroneProblem::request");
//		System.out.println(input.toString());
		TSPDSolution tspdSol;
		Point startPoint = input.getListPoints()[0];
		startPoint.setID(0);
		Point endPoint = new Point(input.getListPoints().length, startPoint.getLat(), startPoint.getLng());
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		for(int i=1; i<input.getListPoints().length; i++){
			Point clientPoint = input.getListPoints()[i];
			clientPoint.setID(i);
			clientPoints.add(clientPoint);
		}
		
		Tour[] tours = new Tour[2];
		if(tspd == null){
			tspd = new TSPD(input.getTruckCost(), input.getDroneCost(), input.getDelta(), input.getEndurance(),input.getTruckSpeed(),input.getDroneSpeed(),startPoint, clientPoints, endPoint);
		}
		//System.out.println(name()+"computeTSPwithDroneProblem::tspd"+tspd.toString());

		
		//GRASP grasp = new GRASP(tspd);
		GRASPkDrone grasp = new GRASPkDrone(tspd);
		tours[0] = grasp.solve();
		
//		TSPD_LS tspls= new TSPD_LS(tspd);
//		tours[1] = tspls.solve();
		
//		System.out.println("TSPD_LS solution = "+tours[1].toString()+"  cost = "+tspd.cost(tours[1]));
//		System.out.println("GRASP solution = "+tours[0].toString()+"    cost = "+tspd.cost(tours[0]));
		
		tspdSol= new TSPDSolution(tours, input.getTruckSpeed(), input.getDroneSpeed(), input.getTruckCost(), input.getDroneCost(), input.getDelta(), input.getEndurance());

		return tspdSol;
	}
	
	public String name(){
		return "TSPwithDroneController::";
	}
}
