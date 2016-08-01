package com.kse.ezRoutingAPI.model;

public class Route {
	int len;
	int[] sequence;
	
	
	public Route() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Route(int len, int[] sequence) {
		super();
		this.len = len;
		this.sequence = sequence;
	}
	public int getLen() {
		return len;
	}
	public void setLen(int len) {
		this.len = len;
	}
	public int[] getSequence() {
		return sequence;
	}
	public void setSequence(int[] sequence) {
		this.sequence = sequence;
	}
}
