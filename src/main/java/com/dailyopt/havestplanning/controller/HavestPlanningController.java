package com.dailyopt.havestplanning.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dailyopt.VRPLoad3D.model.RoutingLoad3DInput;
import com.dailyopt.VRPLoad3D.model.RoutingLoad3DSolution;
import com.dailyopt.VRPLoad3D.service.RoutingLoad3DSolver;
import com.dailyopt.havestplanning.model.HavestPlanningInput;
import com.dailyopt.havestplanning.model.HavestPlanningSolution;
import com.dailyopt.havestplanning.solver.Solver;


@RestController
public class HavestPlanningController {
	public String name(){
		return "HavestPlanningController";
	}
	@RequestMapping(value = "/havest-plan", method = RequestMethod.POST)
	public HavestPlanningSolution computeHavestPlanningSolution(
			HttpServletRequest request, @RequestBody HavestPlanningInput input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");
		System.out.println(name() + "::computeVRP3DSolution, path = "
				+ path);
		
		Solver solver = new Solver();
		return solver.solve(input);

	}

}
