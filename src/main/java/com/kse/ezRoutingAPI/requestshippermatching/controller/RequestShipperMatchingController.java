package com.kse.ezRoutingAPI.requestshippermatching.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.util.SystemOutLogger;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperInput;
import com.kse.ezRoutingAPI.requestshippermatching.model.RequestShipperMatchingSolution;
import com.kse.ezRoutingAPI.requestshippermatching.service.RequestShipperMatchingService;
@RestController
public class RequestShipperMatchingController {
	String ROOT_DIR = "C:/ezRoutingAPIRoot/";
	public String name() {
		return "RequestShipperMatchingController";
	}

	@RequestMapping(value = "/request-shipper-matching", method = RequestMethod.POST)
	public RequestShipperMatchingSolution computeRequestShipperMatchingSolution(HttpServletRequest request,
			@RequestBody RequestShipperInput input){
		System.out.println(input);
		RequestShipperMatchingService service = new RequestShipperMatchingService();
		
		RequestShipperMatchingSolution sol = service.computeRequestShipperMatchingSolution(input);
		
		return sol;
		
	}
}
