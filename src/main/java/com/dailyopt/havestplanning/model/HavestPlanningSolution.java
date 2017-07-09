package com.dailyopt.havestplanning.model;

public class HavestPlanningSolution {
	private double quality;
	private String description;
	private HavestPlanningCluster[] clusters;
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
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
	
	
	public HavestPlanningSolution(double quality, String description,
			HavestPlanningCluster[] clusters) {
		super();
		this.quality = quality;
		this.description = description;
		this.clusters = clusters;
	}
	public HavestPlanningSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
