package com.kse.ezRoutingAPI.tspd.controller;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kse.ezRoutingAPI.tspd.model.Point;
import com.kse.ezRoutingAPI.tspd.model.TSPDRequest;
import com.kse.ezRoutingAPI.tspd.model.TSPDRequestwithDistance;
import com.kse.ezRoutingAPI.tspd.model.TSPDSolution;
import com.kse.ezRoutingAPI.tspd.model.Tour;
import com.kse.ezRoutingAPI.tspd.service.GRASP;
import com.kse.ezRoutingAPI.tspd.service.GRASPkDrone;
import com.kse.ezRoutingAPI.tspd.service.TSPD;
import com.kse.ezRoutingAPI.tspd.service.TSPD_LS;
import com.kse.ezRoutingAPI.tspd.service.TSPDs;
import com.kse.ezRoutingAPI.tspd.service.TSPDs_LS;

@RestController
public class TSPwithDroneController {
	// TSPDSolution tspdSol=null;
	@RequestMapping(value = "/tsp-with-drone", method = RequestMethod.POST)
	public TSPDSolution computeTSPwithDroneProblem(HttpServletRequest request,
			@RequestBody TSPDRequest input) {
		// System.out.println(name()+"computeTSPwithDroneProblem::request");
		// System.out.println(input.toString());
		TSPDSolution tspdSol;
		Point startPoint = input.getListPoints()[0];
		startPoint.setID(0);
		Point endPoint = new Point(input.getListPoints().length,
				startPoint.getLat(), startPoint.getLng());
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		for (int i = 1; i < input.getListPoints().length; i++) {
			Point clientPoint = input.getListPoints()[i];
			clientPoint.setID(i);
			clientPoints.add(clientPoint);
		}

		Tour[] tours = new Tour[2];
		TSPD tspd = new TSPD(input.getTruckCost(), input.getDroneCost(),
				input.getDelta(), input.getEndurance(), input.getTruckSpeed(),
				input.getDroneSpeed(), startPoint, clientPoints, endPoint);
		// System.out.println(name()+"computeTSPwithDroneProblem::tspd"+tspd.toString());

		GRASP grasp = new GRASP(tspd);
		tours[1] = grasp.solve();

		TSPD_LS tspls = new TSPD_LS(tspd);
		tours[0] = tspls.solve();

		// System.out.println("TSPD_LS solution = "+tours[1].toString()+"  cost = "+tspd.cost(tours[1]));
		// System.out.println("GRASP solution = "+tours[0].toString()+"    cost = "+tspd.cost(tours[0]));

		tspdSol = new TSPDSolution(tours, input.getTruckSpeed(),
				input.getDroneSpeed(), input.getTruckCost(),
				input.getDroneCost(), input.getDelta(), input.getEndurance());

		return tspdSol;
	}

	@RequestMapping(value = "/tsp-with-kdrone", method = RequestMethod.POST)
	public TSPDSolution computeTSPwithKDroneProblem(HttpServletRequest request,
			@RequestBody TSPDRequest input) {
		System.out.println(name() + "computeTSPwithKDroneProblem::request");
		System.out.println(input.toString());
		TSPDSolution tspdSol;
		Point startPoint = input.getListPoints()[0];
		startPoint.setID(0);
		Point endPoint = new Point(input.getListPoints().length,
				startPoint.getLat(), startPoint.getLng());
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		for (int i = 1; i < input.getListPoints().length; i++) {
			Point clientPoint = input.getListPoints()[i];
			clientPoint.setID(i);
			clientPoints.add(clientPoint);
		}
		Tour[] tours = new Tour[2];
		TSPDs tspds = new TSPDs(input.getTruckCost(), input.getDroneCost(),
				input.getDelta(), input.getEndurance(), input.getTruckSpeed(),
				input.getDroneSpeed(), startPoint, clientPoints, endPoint);
		System.out.println(name() + "computeTSPwithKDroneProblem::tspkd"
				+ tspds.toString());
		TSPD tspd = new TSPD(input.getTruckCost(), input.getDroneCost(),
				input.getDelta(), input.getEndurance(), input.getTruckSpeed(),
				input.getDroneSpeed(), startPoint, clientPoints, endPoint);
		GRASPkDrone grasp = new GRASPkDrone(tspd);
		tours[1] = grasp.solve();
		TSPDs_LS tspls = new TSPDs_LS(tspds,4,4);
		tours[0] = tspls.solve();

		System.out.println("TSPD_LS solution = " + tours[0].toString()
				+ "  cost = " + tspds.cost(tours[0]));
		// System.out.println("GRASP solution = "+tours[0].toString()+"    cost = "+tspds.cost(tours[0]));

		tspdSol = new TSPDSolution(tours, input.getTruckSpeed(),
				input.getDroneSpeed(), input.getTruckCost(),
				input.getDroneCost(), input.getDelta(), input.getEndurance());
		return tspdSol;
	}

	@RequestMapping(value = "/tsp-with-drone-input-distance", method = RequestMethod.POST)
	public TSPDSolution computeTSPwithDroneProblemInputDistance(
			HttpServletRequest request,
			@RequestBody TSPDRequestwithDistance input) {
		// System.out.println(name()+"computeTSPwithDroneProblem::request");
		// System.out.println(input.toString());
		TSPDSolution tspdSol;
		Point startPoint = input.getListPoints()[0];
		startPoint.setID(0);
		Point endPoint = new Point(input.getListPoints().length,
				startPoint.getLat(), startPoint.getLng());

		ArrayList<Point> clientPoints = new ArrayList<Point>();

		for (int i = 1; i < input.getListPoints().length; i++) {
			Point clientPoint = input.getListPoints()[i];
			clientPoint.setID(i);
			clientPoints.add(clientPoint);
		}
		Map<String, Double> map = input.getMap();
		for (int i = 0; i < clientPoints.size(); i++) {
			String key = endPoint.getID() + "_" + clientPoints.get(i).getID();
			String keyStart = startPoint.getID() + "_"
					+ clientPoints.get(i).getID();
			map.put(key, map.get(keyStart));
			key = clientPoints.get(i).getID() + "_" + endPoint.getID();
			keyStart = clientPoints.get(i).getID() + "_" + startPoint.getID();
			map.put(key, map.get(keyStart));
		}
		map.put(startPoint.getID() + "_" + endPoint.getID(), 0.0);
		map.put(endPoint.getID() + "_" + startPoint.getID(), 0.0);
		System.out.println(map);
		Tour[] tours = new Tour[2];
		TSPD tspd = new TSPD(input.getTruckCost(), input.getDroneCost(),
				input.getDelta(), input.getEndurance(), input.getTruckSpeed(),
				input.getDroneSpeed(), startPoint, clientPoints, endPoint, map);
		// System.out.println(name()+"computeTSPwithDroneProblem::tspd"+tspd.toString());

		GRASP grasp = new GRASP(tspd);
		tours[1] = grasp.solve();

		TSPD_LS tspls = new TSPD_LS(tspd);
		tours[0] = tspls.solve();

		// System.out.println("TSPD_LS solution = "+tours[1].toString()+"  cost = "+tspd.cost(tours[1]));
		// System.out.println("GRASP solution = "+tours[0].toString()+"    cost = "+tspd.cost(tours[0]));

		tspdSol = new TSPDSolution(tours, input.getTruckSpeed(),
				input.getDroneSpeed(), input.getTruckCost(),
				input.getDroneCost(), input.getDelta(), input.getEndurance());

		return tspdSol;
	}

	@RequestMapping(value = "/tsp-with-kdrone-input-distance", method = RequestMethod.POST)
	public TSPDSolution computeTSPwithKDroneProblemInputDistance(
			HttpServletRequest request,
			@RequestBody TSPDRequestwithDistance input) {
		System.out.println(name() + "computeTSPwithKDroneProblem::request");
		System.out.println(input.toString());
		TSPDSolution tspdSol;
		Point startPoint = input.getListPoints()[0];
		startPoint.setID(0);
		Point endPoint = new Point(input.getListPoints().length,
				startPoint.getLat(), startPoint.getLng());
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		for (int i = 1; i < input.getListPoints().length; i++) {
			Point clientPoint = input.getListPoints()[i];
			clientPoint.setID(i);
			clientPoints.add(clientPoint);
		}
		Map<String, Double> map = input.getMap();
		for (int i = 0; i < clientPoints.size(); i++) {
			String key = endPoint.getID() + "_" + clientPoints.get(i).getID();
			String keyStart = startPoint.getID() + "_"
					+ clientPoints.get(i).getID();
			map.put(key, map.get(keyStart));
			key = clientPoints.get(i).getID() + "_" + endPoint.getID();
			keyStart = clientPoints.get(i).getID() + "_" + startPoint.getID();
			map.put(key, map.get(keyStart));
		}
		map.put(startPoint.getID() + "_" + endPoint.getID(), 0.0);
		map.put(endPoint.getID() + "_" + startPoint.getID(), 0.0);
		System.out.println(map);
		Tour[] tours = new Tour[2];
		TSPDs tspds = new TSPDs(input.getTruckCost(), input.getDroneCost(),
				input.getDelta(), input.getEndurance(), input.getTruckSpeed(),
				input.getDroneSpeed(), startPoint, clientPoints, endPoint, map);
		System.out.println(name() + "computeTSPwithKDroneProblem::tspkd"
				+ tspds.toString());
		TSPD tspd = new TSPD(input.getTruckCost(), input.getDroneCost(),
				input.getDelta(), input.getEndurance(), input.getTruckSpeed(),
				input.getDroneSpeed(), startPoint, clientPoints, endPoint, map);
		GRASPkDrone grasp = new GRASPkDrone(tspd);
		tours[1] = grasp.solve();
		TSPDs_LS tspls = new TSPDs_LS(tspds,4,4);
		tours[0] = tspls.solve();

		System.out.println("TSPD_LS solution = " + tours[0].toString()
				+ "  cost = " + tspds.cost(tours[0]));
		// System.out.println("GRASP solution = "+tours[0].toString()+"    cost = "+tspds.cost(tours[0]));

		tspdSol = new TSPDSolution(tours, input.getTruckSpeed(),
				input.getDroneSpeed(), input.getTruckCost(),
				input.getDroneCost(), input.getDelta(), input.getEndurance());
		return tspdSol;
	}
	
	@RequestMapping(value = "/tsp-with-drone-tspdls-input-distance", method = RequestMethod.POST)
	public TSPDSolution computeTSPwithKDroneTSPDLSInputDistance(
			HttpServletRequest request,
			@RequestBody TSPDRequestwithDistance input) {
		System.out.println(name() + "computeTSPwithKDroneTSPDLSInputDistance::request");
		//System.out.println(input.toString());
		TSPDSolution tspdSol;
		Point startPoint = input.getListPoints()[0];
		startPoint.setID(0);
		Point endPoint = new Point(input.getListPoints().length,
				startPoint.getLat(), startPoint.getLng());
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		for (int i = 1; i < input.getListPoints().length; i++) {
			Point clientPoint = input.getListPoints()[i];
			clientPoint.setID(i);
			clientPoints.add(clientPoint);
		}
		Map<String, Double> map = input.getMap();
		for (int i = 0; i < clientPoints.size(); i++) {
			String key = endPoint.getID() + "_" + clientPoints.get(i).getID();
			String keyStart = startPoint.getID() + "_"
					+ clientPoints.get(i).getID();
			map.put(key, map.get(keyStart));
			key = clientPoints.get(i).getID() + "_" + endPoint.getID();
			keyStart = clientPoints.get(i).getID() + "_" + startPoint.getID();
			map.put(key, map.get(keyStart));
		}
		map.put(startPoint.getID() + "_" + endPoint.getID(), 0.0);
		map.put(endPoint.getID() + "_" + startPoint.getID(), 0.0);
		//System.out.println(map);
		Tour[] tours = new Tour[4];
		TSPDs tspds = new TSPDs(input.getTruckCost(), input.getDroneCost(),
				input.getDelta(), input.getEndurance(), input.getTruckSpeed(),
				input.getDroneSpeed(), startPoint, clientPoints, endPoint, map);
		System.out.println(name() + "computeTSPwithKDroneProblem::tspkd");
		TSPD tspd = new TSPD(input.getTruckCost(), input.getDroneCost(),
				input.getDelta(), input.getEndurance(), input.getTruckSpeed(),
				input.getDroneSpeed(), startPoint, clientPoints, endPoint, map);
		TSPD_LS tspdls1 = new TSPD_LS(tspd);
		long startTime = System.currentTimeMillis();
		System.out.println(name() + "computeTSPwithKDroneProblem::tspkd 1");
		tours[0] = tspdls1.solve();
		tours[0].setTotalTime(  System.currentTimeMillis()- startTime);
		startTime = System.currentTimeMillis();
		TSPDs_LS tspls = new TSPDs_LS(tspds,2,7);
		System.out.println(name() + "computeTSPwithKDroneProblem::tspkd 2");
		tours[1] = tspls.solve();
		tours[1].setTotalTime(  System.currentTimeMillis()- startTime);
		startTime = System.currentTimeMillis();
		tspls = new TSPDs_LS(tspds,3,7);
		System.out.println(name() + "computeTSPwithKDroneProblem::tspkd 3");
		tours[2] = tspls.solve();
		tours[2].setTotalTime(  System.currentTimeMillis()- startTime);
		startTime = System.currentTimeMillis();
		tspls = new TSPDs_LS(tspds,4,7);
		System.out.println(name() + "computeTSPwithKDroneProblem::tspkd 4");
		tours[3] = tspls.solve();
		tours[3].setTotalTime(  System.currentTimeMillis()- startTime);
		tspdSol = new TSPDSolution(tours, input.getTruckSpeed(),
				input.getDroneSpeed(), input.getTruckCost(),
				input.getDroneCost(), input.getDelta(), input.getEndurance());
		return tspdSol;
	}
	
	public String name() {
		return "TSPwithDroneController::";
	}
}
