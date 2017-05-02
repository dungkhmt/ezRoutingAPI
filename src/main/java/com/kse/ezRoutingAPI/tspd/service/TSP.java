package com.kse.ezRoutingAPI.tspd.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import com.kse.ezRoutingAPI.tspd.model.Point;

public class TSP {
	private Point startPoint;
	private ArrayList<Point> clientPoints;
	private Point endPoint;
	private double distances_matrix[][];
	
	public Point getStartPoint() {
		return startPoint;
	}

	public void setStartPoint(Point startPoint) {
		this.startPoint = startPoint;
	}

	public ArrayList<Point> getClientPoints() {
		return clientPoints;
	}

	public void setClientPoints(ArrayList<Point> clientPoints) {
		this.clientPoints = clientPoints;
	}

	public Point getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(Point endPoint) {
		this.endPoint = endPoint;
	}

	public double[][] getDistances_matrix() {
		return distances_matrix;
	}

	public void setDistances_matrix(double[][] distances_matrix) {
		this.distances_matrix = distances_matrix;
	}

	public TSP(Point sPoint, ArrayList<Point> cPoint, Point ePoint){
		this.startPoint = sPoint;
		this.clientPoints = cPoint;
		this.endPoint = ePoint;
	}
	
	public ArrayList<Point> randomGenerator(){
		ArrayList<Point> tour = new ArrayList<Point>();
		tour.add(startPoint);
		
		int nClientPoint = clientPoints.size();
		boolean visted[] = new boolean[nClientPoint];
		for(int i=0; i<nClientPoint; i++){
			visted[i] = false;
		}
 		Random rand = new Random();
		
		for(int i=0; i<nClientPoint; i++){
			int iPoint = rand.nextInt(nClientPoint);
			while(visted[iPoint]){
				iPoint = rand.nextInt(nClientPoint);
			}
			tour.add(clientPoints.get(iPoint));
			visted[iPoint] = true;
		}
		
		tour.add(endPoint);
		
		return tour;
	}

	/*
	public ArrayList<Point> localSearchGenerator(){
		
	}*/
	
}
