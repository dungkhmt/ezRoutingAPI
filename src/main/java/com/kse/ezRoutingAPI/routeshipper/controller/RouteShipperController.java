package com.kse.ezRoutingAPI.routeshipper.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.kse.ezRoutingAPI.routeshipper.model.ShipInput;
import com.kse.ezRoutingAPI.routeshipper.model.ShipRoute;
import com.kse.ezRoutingAPI.routeshipper.service.ShipRouteService;

public class RouteShipperController {
	@RequestMapping(value = "/ship-route", method = RequestMethod.POST)
	public ShipRoute computeRoute(@RequestBody ShipInput input){
		ShipRouteService service = new ShipRouteService();
		return service.computeRoute(input);
	}
}
