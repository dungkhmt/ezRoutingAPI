package com.kse.ezRoutingAPI.dichung.service;

import java.util.ArrayList;

public class SharedRoute {
	public ArrayList<Integer> route;// sequence of indices of requests of the shared route
	public SharedRoute(ArrayList<Integer> route){
		this.route = route;
	}
	public SharedRoute(){
		route = new ArrayList<Integer>();
	}
	public int get(int i){
		return route.get(i);
	}
	public void add(int e){
		route.add(e);
	}
	public int size(){
		return route.size();
	}
	public int getLast(){
		return route.get(route.size()-1);
	}
}
