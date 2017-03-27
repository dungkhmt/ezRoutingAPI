/**
 * 
 */
package com.kse.ezRoutingAPI.tspd.model;

/**
 * @author T420
 *
 */
public class NeighborHood {
	boolean isDroneNode;
	double maxSavings;
	Point ni;
	Point nj;
	Point nk;
	public boolean isDroneNode() {
		return isDroneNode;
	}
	public void setDroneNode(boolean isDroneNode) {
		this.isDroneNode = isDroneNode;
	}
	public double getMaxSavings() {
		return maxSavings;
	}
	public void setMaxSavings(double maxSavings) {
		this.maxSavings = maxSavings;
	}
	
	public NeighborHood(boolean isDroneNode, double maxSavings, int ni, int nj,
			int nk) {
		super();
		this.isDroneNode = isDroneNode;
		this.maxSavings = maxSavings;
		
		}
	public NeighborHood(boolean isDroneNode, double maxSavings, Point ni,
			Point nj, Point nk) {
		super();
		this.isDroneNode = isDroneNode;
		this.maxSavings = maxSavings;
		this.ni = ni;
		this.nj = nj;
		this.nk = nk;
	}
	public Point getNi() {
		return ni;
	}
	public void setNi(Point ni) {
		this.ni = ni;
	}
	public Point getNj() {
		return nj;
	}
	public void setNj(Point nj) {
		this.nj = nj;
	}
	public Point getNk() {
		return nk;
	}
	public void setNk(Point nk) {
		this.nk = nk;
	}
	
	
}
