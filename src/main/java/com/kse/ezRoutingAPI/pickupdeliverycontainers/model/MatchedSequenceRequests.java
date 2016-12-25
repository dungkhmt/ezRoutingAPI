package com.kse.ezRoutingAPI.pickupdeliverycontainers.model;

import java.util.ArrayList;

public class MatchedSequenceRequests {
	private ArrayList<PickupDeliveryRequest> seq;
	private int quantity;
	public ArrayList<PickupDeliveryRequest> getSeq() {
		return seq;
	}
	public void setSeq(ArrayList<PickupDeliveryRequest> seq) {
		this.seq = seq;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public MatchedSequenceRequests(ArrayList<PickupDeliveryRequest> seq,
			int quantity) {
		super();
		this.seq = seq;
		this.quantity = quantity;
	}
	
}	
