package com.kse.ezRoutingAPI.tspd.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.TSPDRequest;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.service.GRASP;
import com.kse.ezRoutingAPI.tspd.service.TSPD;

@RestController
public class TSPwithDroneController {

	@RequestMapping(value="/tsp-with-drone", method= RequestMethod.POST)
	public Tour computeTSPwithDroneProblem(HttpServletRequest request,@RequestBody TSPDRequest input){
		//System.out.println(name()+"computeTSPwithDroneProblem::request");
		//System.out.println(input.toString());
		Point startPoint = input.getListPoints()[0];
		startPoint.setID(0);
		Point endPoint = new Point(input.getListPoints().length, 0.0, 0.0);
		
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		for(int i=1; i<input.getListPoints().length; i++){
			Point clientPoint = input.getListPoints()[i];
			clientPoint.setID(i);
			clientPoints.add(clientPoint);
		}
		
		TSPD tspd = new TSPD(50, 2, 5, 13, startPoint, clientPoints, endPoint);
		GRASP grasp = new GRASP(tspd);
		Tour tour = grasp.solve();
		return tour;
	}
	
	public String name(){
		return "TSPwithDroneController::";
	}
}
