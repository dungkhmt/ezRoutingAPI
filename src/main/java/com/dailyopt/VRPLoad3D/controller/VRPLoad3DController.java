package com.dailyopt.VRPLoad3D.controller;

import com.dailyopt.VRPLoad3D.model.*;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dailyopt.VRPLoad3D.service.RoutingLoad3DSolver;

import javax.servlet.http.HttpServletRequest;

@RestController
public class VRPLoad3DController {
	String ROOT_DIR = "C:/ezRoutingAPIRoot/";

	public String name() {
		return "VRPLoad3DController";
	}

/*
	@RequestMapping(value = "/vrp-load3d", method = RequestMethod.POST)
	public RoutingLoad3DSolution computeVRPLoad3DSolution(
			HttpServletRequest request, @RequestBody RoutingLoad3DInput input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");
		System.out.println(name() + "::computeVRP3DSolution, path = "
				+ path);
		
		RoutingLoad3DSolver solver = new RoutingLoad3DSolver();
		return solver.solve(input);

	}
*/
}
