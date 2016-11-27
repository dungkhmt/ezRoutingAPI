package com.kse.ezRoutingAPI.dichungduongdai.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kse.ezRoutingAPI.dichungduongdai.model.Input;
import com.kse.ezRoutingAPI.dichungduongdai.model.SharedLongTripSolution;
import com.kse.ezRoutingAPI.dichungduongdai.service.DichungDuongDaiService;


@RestController
public class DichungDuongDaiAPIController {
	String ROOT_DIR = "C:/ezRoutingAPIRoot/";
	public String name() {
		return "DichungDuongDaiAPIController";
	}

	public void writeDichungDuongDaiRequest(Input input, SharedLongTripSolution sol){
		// write requests and solution to file for logging
		
	}
	
	@RequestMapping(value = "/shared-long-trip-plan-dichung", method = RequestMethod.POST)
	public SharedLongTripSolution computeSharedTaxiSolution(HttpServletRequest request,
			@RequestBody Input input) {
		DichungDuongDaiService dichungDuongdaiService = new DichungDuongDaiService();
		
		SharedLongTripSolution sol = dichungDuongdaiService.computeSharedLongTrip(input);
		
		writeDichungDuongDaiRequest(input, sol);
		
		return sol;
	}
}
