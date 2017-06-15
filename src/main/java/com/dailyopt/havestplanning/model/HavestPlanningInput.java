package com.dailyopt.havestplanning.model;

public class HavestPlanningInput {
	private Field[] fields;
	private QualityFunction qualityFunction;
	private int minP;
	private int maxP;
	
	private int clusterDuration; 
	
	
	public int getClusterDuration() {
		return clusterDuration;
	}
	public void setClusterDuration(int clusterDuration) {
		this.clusterDuration = clusterDuration;
	}
	public Field[] getFields() {
		return fields;
	}
	public void setFields(Field[] fields) {
		this.fields = fields;
	}
	public int getMinP() {
		return minP;
	}
	public void setMinP(int minP) {
		this.minP = minP;
	}
	public int getMaxP() {
		return maxP;
	}
	public void setMaxP(int maxP) {
		this.maxP = maxP;
	}
	public QualityFunction getQualityFunction() {
		return qualityFunction;
	}
	public void setQualityFunction(QualityFunction qualityFunction) {
		this.qualityFunction = qualityFunction;
	}
	public HavestPlanningInput(Field[] fields, int minP, int maxP,
			QualityFunction qualityFunction, int clusterDuration) {
		super();
		this.fields = fields;
		this.minP = minP;
		this.maxP = maxP;
		this.qualityFunction = qualityFunction;
		this.clusterDuration = clusterDuration;
	}
	public HavestPlanningInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
