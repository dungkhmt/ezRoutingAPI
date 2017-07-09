package com.dailyopt.havestplanning.controller;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.dailyopt.VRPLoad3D.model.RoutingLoad3DInput;
import com.dailyopt.VRPLoad3D.model.RoutingLoad3DSolution;
import com.dailyopt.VRPLoad3D.service.RoutingLoad3DSolver;
import com.dailyopt.havestplanning.model.Field;
import com.dailyopt.havestplanning.model.FieldList;
import com.dailyopt.havestplanning.model.HavestPlanningInput;
import com.dailyopt.havestplanning.model.HavestPlanningSolution;
import com.dailyopt.havestplanning.model.MachineSetting;
import com.dailyopt.havestplanning.model.PlantStandard;
import com.dailyopt.havestplanning.model.ReturnAddFields;
import com.dailyopt.havestplanning.model.ReturnSetPlantStandard;
import com.dailyopt.havestplanning.model.ReturnStart;
import com.dailyopt.havestplanning.solver.Solver;
import com.dailyopt.havestplanning.solver.multistepsplitfield.SolverMultiStepSplitFields;
import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.Gson;


@RestController
public class HavestPlanningController {
	public static String ROOT = "C:/ezRoutingAPIROOT/havestplanning";
	
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
		
		//Solver solver = new Solver();
		SolverMultiStepSplitFields solver = new SolverMultiStepSplitFields();
		
		if(input.getPlantStandard() == null)
			input.initDefaultPlantStandard();
		
		return solver.solve(input);
	}
	
	@RequestMapping(value = "/havest-plan/start", method = RequestMethod.POST)
	public ReturnStart start(
			HttpServletRequest request, @RequestBody HavestPlanningInput input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");
		
		return null;
	}
	@RequestMapping(value = "/havest-plan/add-fields", method = RequestMethod.POST)
	public FieldList addFields(
			HttpServletRequest request, @RequestBody FieldList fieldList) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");
		
		//path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/fields.json";
		try{
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(path));
			JSONObject jsonObject = (JSONObject)obj;
			JSONArray jarr = (JSONArray)jsonObject.get("fields");
			ArrayList<Field> L = new ArrayList<Field>();
			Iterator it = jarr.iterator();
			while(it.hasNext()){
				JSONObject o = (JSONObject)it.next();
				String code = (String)o.get("code");
				String districtCode = (String)o.get("districtCode");
				String ownerCode = (String)o.get("ownerCode");
				double area = (double)o.get("area");
				
				//private Date date;// optimal havesting date
				String plant_date = (String)o.get("plant_date");
				
				long l_quantity = (long)o.get("quantity");
				
				int quantity = (int)l_quantity;
				
				String category = (String)o.get("category");
				String plantType = (String)o.get("plantType");
				
				long l_deltaDays = (long)o.get("deltaDays");
				int deltaDays = (int)l_deltaDays;
				
				Field f = new Field(code, districtCode,ownerCode,area,plant_date,quantity,category, plantType,deltaDays);
				System.out.println(f);
				L.add(f);
			}
			System.out.println(name() + "::addFields, fieldList.sz = " + fieldList);
			
			Field[] F = new Field[L.size() + fieldList.getFields().length];
			int idx = -1;
			for(int i = 0; i < L.size(); i++){
				idx++;
				F[idx] = L.get(i);
			}
			for(int i = 0; i < fieldList.getFields().length; i++){
				idx++;
				F[idx] = fieldList.getFields()[i];
			}
			
			FieldList newFieldList = new FieldList(F);
			
			Gson gson = new Gson();
			PrintWriter out = new PrintWriter(path);
			out.print(gson.toJson(newFieldList));
			out.close();
			
			//return new ReturnAddFields(newFieldList.getFields().length);
			return newFieldList;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	@RequestMapping(value = "/havest-plan/set-fields", method = RequestMethod.POST)
	public FieldList setFields(
			HttpServletRequest request, @RequestBody FieldList input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");
		
		//path = "C:/ezRoutingAPIROOT/havestplanning/plant-standard.json";
		path = ROOT + "/fields.json";
		
		for(int i = 0; i < input.getFields().length; i++){
			Field f = input.getFields()[i];
			if(f.getCategory() == null || f.getCategory().equals("")) f.setCategory("-");
			if(f.getPlantType() == null || f.getPlantType().equals("")) f.setPlantType("-");
		}
		try{
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();
			
			return input;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/set-plant-standard", method = RequestMethod.POST)
	public ReturnSetPlantStandard setPlantStandard(
			HttpServletRequest request, @RequestBody PlantStandard input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");
		
		//path = "C:/ezRoutingAPIROOT/havestplanning/plant-standard.json";
		path = ROOT + "/plant-standard.json";
		
		try{
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();
			
			return new ReturnSetPlantStandard(input.getPlantStandards().length);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	@RequestMapping(value = "/havest-plan/set-machine", method = RequestMethod.POST)
	public MachineSetting setMachine(
			HttpServletRequest request, @RequestBody MachineSetting input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");
		
		//path = "C:/ezRoutingAPIROOT/havestplanning/machine-setting.json";
		path = ROOT + "/machine-setting.json";
		
		try{
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();
			
			return input;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/compute", method = RequestMethod.POST)
	public HavestPlanningSolution compute(
			HttpServletRequest request
			//, @RequestBody HavestPlanningInput input
			) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");
		
		String fieldFilename = ROOT + "/fields.json";
		String setPlatStandardFilename = ROOT + "/plant-standard.json";
		String machineSettingFilename = ROOT + "/machine-setting.json";
		
		Gson gson = new Gson();
		try{
			FieldList fieldList = gson.fromJson(new FileReader(fieldFilename), FieldList.class);
			PlantStandard ps = gson.fromJson(new FileReader(setPlatStandardFilename),PlantStandard.class);
			MachineSetting ms = gson.fromJson(new FileReader(machineSettingFilename), MachineSetting.class);
			
			HavestPlanningInput input = new HavestPlanningInput(fieldList.getFields(),ps,ms);
			
			
			SolverMultiStepSplitFields solver = new SolverMultiStepSplitFields();
			
			if(input.getPlantStandard() == null)
				input.initDefaultPlantStandard();
			
			return solver.solve(input);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

}
