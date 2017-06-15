package com.dailyopt.havestplanning.model;

public class HavestPlanningSolution {
	private HavestPlanningCluster[] clusters;
	private double quality;
	public HavestPlanningCluster[] getClusters() {
		return clusters;
	}
	public void setClusters(HavestPlanningCluster[] clusters) {
		this.clusters = clusters;
	}
	public double getQuality() {
		return quality;
	}
	public void setQuality(double quality) {
		this.quality = quality;
	}
	public HavestPlanningSolution(HavestPlanningCluster[] clusters,
			double quality) {
		super();
		this.clusters = clusters;
		this.quality = quality;
	}
	public HavestPlanningSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
