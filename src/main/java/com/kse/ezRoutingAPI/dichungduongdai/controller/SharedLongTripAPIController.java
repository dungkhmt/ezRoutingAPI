package com.kse.ezRoutingAPI.dichungduongdai.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripInput;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripSolution;
import com.kse.ezRoutingAPI.dichungduongdai.service.SharedLongTripService;




@RestController
public class SharedLongTripAPIController {
	String ROOT_DIR = "C:/ezRoutingAPIRoot/";
	public String name() {
		return "DichungDuongDaiAPIController";
	}

	public void writeDichungDuongDaiRequest(SharedLongTripInput input, SharedLongTripSolution sol){
		// write requests and solution to file for logging
		
	}
	
	@RequestMapping(value = "/shared-long-trip-plan-dichung", method = RequestMethod.POST)
	public SharedLongTripSolution computeSharedTaxiSolution(HttpServletRequest request,
			@RequestBody SharedLongTripInput input) {
		System.out.println("Enter the method in the controller");
		SharedLongTripService dichungDuongdaiService = new SharedLongTripService(input);
		System.out.println("Create a service");
		
		
		SharedLongTripSolution sol = dichungDuongdaiService.computeSharedLongTrip();
		
		writeDichungDuongDaiRequest(input, sol);
		
		return sol;
		
	}
}
