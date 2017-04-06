package com.kse.ezRoutingAPI.tspd.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.TSPDRequest;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.service.GRASP;
import com.kse.ezRoutingAPI.tspd.service.TSPD;
import com.kse.ezRoutingAPI.tspd.service.TSPD_LS;

@RestController
public class TSPwithDroneController {

	@RequestMapping(value="/tsp-with-drone/{id}", method= RequestMethod.POST)
	public Tour computeTSPwithDroneProblem(HttpServletRequest request,@RequestBody TSPDRequest input,@PathVariable("id") String alg){
		System.out.println(name()+"computeTSPwithDroneProblem::request");
		System.out.println(input.toString());
		Point startPoint = input.getListPoints()[0];
		startPoint.setID(0);
		Point endPoint = new Point(input.getListPoints().length, startPoint.getLat(), startPoint.getLng());
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		for(int i=1; i<input.getListPoints().length; i++){
			Point clientPoint = input.getListPoints()[i];
			clientPoint.setID(i);
			clientPoints.add(clientPoint);
		}
		
		Tour tour;
		TSPD tspd = new TSPD(input.getTruckCost(), input.getDroneCost(), input.getDelta(), input.getEndurance(),input.getTruckSpeed(),input.getDroneSpeed(),startPoint, clientPoints, endPoint);
		if(alg.equals("tspd-ls")){
			//GRASP grasp = new GRASP(tspd);
			//Tour tour = grasp.solve();
			TSPD_LS tspls= new TSPD_LS(tspd);
			tour = tspls.solve();
		}else{
			GRASP grasp = new GRASP(tspd);
			tour = grasp.solve();
		}
		return tour;
	}
	
	public String name(){
		return "TSPwithDroneController::";
	}
}
