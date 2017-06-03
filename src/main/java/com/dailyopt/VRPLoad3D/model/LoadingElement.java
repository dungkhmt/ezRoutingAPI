package com.dailyopt.VRPLoad3D.model;

public class LoadingElement {
	private Item item;
	private int posWidth;
	private int posLength;
	private int posHeight;
	private String description;
	public Item getItem() {
		return item;
	}
	public void setItem(Item item) {
		this.item = item;
	}
	public int getPosWidth() {
		return posWidth;
	}
	public void setPosWidth(int posWidth) {
		this.posWidth = posWidth;
	}
	public int getPosLength() {
		return posLength;
	}
	public void setPosLength(int posLength) {
		this.posLength = posLength;
	}
	public int getPosHeight() {
		return posHeight;
	}
	public void setPosHeight(int posHeight) {
		this.posHeight = posHeight;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public LoadingElement(Item item, int posWidth, int posLength,
			int posHeight, String description) {
		super();
		this.item = item;
		this.posWidth = posWidth;
		this.posLength = posLength;
		this.posHeight = posHeight;
		this.description = description;
	}
	public LoadingElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
