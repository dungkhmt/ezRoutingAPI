package com.kse.ezRoutingAPI.pickupdeliverycontainers.controller;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kse.ezRoutingAPI.dichung.model.GlobalSharedTaxiInput;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.PickupDeliveryInput;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.PickupDeliverySolution;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.service.PickupDeliveryContainerService;
import com.kse.utils.DateTimeUtils;


@RestController
public class PickupDeliveryContainersController {
	String ROOT_DIR = "C:/ezRoutingAPIRoot/";
	
	public String name(){
		return "PickupDeliveryContainersController";
	}
	
	public void writeRequest(PickupDeliveryInput input){
		try{
			DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
			Date date = new Date();
			
			//System.out.println(dateFormat.format(date)); //2014/08/06 15:59:48
			String dt = dateFormat.format(date);
			String[] s  = dt.split(":"); 
			
			String dir = ROOT_DIR + "/" + DateTimeUtils.currentDate();
			File f = new File(dir);
			if(!f.exists()){
				f.mkdir();
			}
			
			//String fn = ROOT_DIR + "dichungairport-requests-" + s[0] + s[1] + s[2] + "-" + s[3] + s[4] + s[5] + ".txt";
			String fn = dir + "/pickup-delivery-container-requests-" + s[0] + s[1] + s[2] + "-" + s[3] + s[4] + s[5] + ".txt";
			System.out.println(name() + "::writeRequest to file " + fn);
			PrintWriter out = new PrintWriter(fn);
			
			ObjectMapper mapper = new ObjectMapper();
			String jsoninput = mapper.writeValueAsString(input);
			
			//out.println("input: JSON");
			out.println(jsoninput);
			
	
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@RequestMapping(value = "/pickup-delivery-containers-plan", method = RequestMethod.POST)
	public PickupDeliverySolution computePickupDeliveryContainerSolution(
			@RequestBody PickupDeliveryInput input) {
		System.out.println(name() + "::computePickupDeliveryContainerSolution");
		
		writeRequest(input);
		
		PickupDeliveryContainerService service = new PickupDeliveryContainerService();
		return service.computePickupDeliveryContainerSolution(input);
		
	}
}
