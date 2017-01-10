package com.kse.ezRoutingAPI.dichungduongdai.model;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.*; 

public class SharedLongTripRequest {
	private String ticketCode;
	private String pickupAddress;
	private String pickupLatLng;
	private String deliveryAddress;
	private String deliveryLatLng;
	private String departTime;
	private int numberPassengers;
	private boolean shared;
	private boolean oneway;
	private int price;
	
	private String Itinerary;// chunkName 
	
	private String[] directItineraries;
	
	




	public SharedLongTripRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	public int getPrice() {
		return price;
	}


	public void setPrice(int price) {
		this.price = price;
	}




	


	public void print(){
		System.out.println("Information of a request ");
		System.out.println("Ticket code: " + ticketCode);
		System.out.println("Itinerary: ");
		System.out.println("Pickup address: " + pickupAddress);
		System.out.println("Delivery address: " + deliveryAddress);
		System.out.println("Departure time: " + departTime);
		System.out.println("Number of passengers: " + numberPassengers);
		System.out.println("Price: " + price);
	}

	public String getItinerary() {
		return Itinerary;
	}
	public void setItinerary(String itinerary) {
		Itinerary = itinerary;
	}
	public String getTicketCode() {
		return ticketCode;
	}
	public void setTicketCode(String ticketCode) {
		this.ticketCode = ticketCode;
	}
	public String getPickupAddress() {
		return pickupAddress;
	}
	public void setPickupAddress(String pickupAddress) {
		this.pickupAddress = pickupAddress;
	}
	public String getPickupLatLng() {
		return pickupLatLng;
	}
	public void setPickupLatLng(String pickupLatLng) {
		this.pickupLatLng = pickupLatLng;
	}
	public String getDeliveryAddress() {
		return deliveryAddress;
	}
	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}
	public String getDeliveryLatLng() {
		return deliveryLatLng;
	}
	public void setDeliveryLatLng(String deliveryLatLng) {
		this.deliveryLatLng = deliveryLatLng;
	}
	public String getDepartTime() {
		return departTime;
	}
	public void setDepartTime(String departTime) {
		this.departTime = departTime;
	}
	public int getNumberPassengers() {
		return numberPassengers;
	}


	public SharedLongTripRequest(String ticketCode, String pickupAddress,
			String pickupLatLng, String deliveryAddress, String deliveryLatLng,
			String departTime, int numberPassengers, boolean shared,
			boolean oneway, int price, String itinerary,
			String[] directItineraries) {
		super();
		this.ticketCode = ticketCode;
		this.pickupAddress = pickupAddress;
		this.pickupLatLng = pickupLatLng;
		this.deliveryAddress = deliveryAddress;
		this.deliveryLatLng = deliveryLatLng;
		this.departTime = departTime;
		this.numberPassengers = numberPassengers;
		this.shared = shared;
		this.oneway = oneway;
		this.price = price;
		Itinerary = itinerary;
		this.directItineraries = directItineraries;
	}


	public void setNumberPassengers(int numberPassengers) {
		this.numberPassengers = numberPassengers;
	}


	public boolean isShared() {
		return shared;
	}


	public void setShared(boolean shared) {
		this.shared = shared;
	}


	public boolean isOneway() {
		return oneway;
	}
	public void setOneway(boolean oneway) {
		this.oneway = oneway;
	}


	public String[] getDirectItineraries() {
		return directItineraries;
	}


	public void setDirectItineraries(String[] directItineraries) {
		this.directItineraries = directItineraries;
	}
	
	
	
}
