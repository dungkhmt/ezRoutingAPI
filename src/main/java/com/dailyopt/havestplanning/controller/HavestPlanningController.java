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
import com.dailyopt.havestplanning.model.FieldCodeList;
import com.dailyopt.havestplanning.model.FieldList;
import com.dailyopt.havestplanning.model.FieldSolutionList;
import com.dailyopt.havestplanning.model.HavestPlanningInput;
import com.dailyopt.havestplanning.model.HavestPlanningSolution;
import com.dailyopt.havestplanning.model.MachineSetting;
import com.dailyopt.havestplanning.model.PlantStandard;
import com.dailyopt.havestplanning.model.ReturnAddFields;
import com.dailyopt.havestplanning.model.ReturnFields;
import com.dailyopt.havestplanning.model.ReturnMachineSetting;
import com.dailyopt.havestplanning.model.ReturnPlantStandard;
import com.dailyopt.havestplanning.model.ReturnSetPlantStandard;
import com.dailyopt.havestplanning.model.ReturnStart;
import com.dailyopt.havestplanning.solver.Solver;
import com.dailyopt.havestplanning.solver.multistepsplitfield.SolutionChecker;
import com.dailyopt.havestplanning.solver.multistepsplitfield.SolverMultiStepSplitFields;
import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.Gson;

@RestController
public class HavestPlanningController {
	public static String ROOT = "C:/ezRoutingAPIROOT/havestplanning";

	public String name() {
		return "HavestPlanningController";
	}

	@RequestMapping(value = "/havest-plan", method = RequestMethod.POST)
	public HavestPlanningSolution computeHavestPlanningSolution(
			HttpServletRequest request, @RequestBody HavestPlanningInput input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");
		System.out.println(name() + "::computeVRP3DSolution, path = " + path);

		// Solver solver = new Solver();
		SolverMultiStepSplitFields solver = new SolverMultiStepSplitFields();

		if (input.getPlantStandard() == null)
			input.initDefaultPlantStandard();

		return solver.solve(input);
	}

	@RequestMapping(value = "/havest-plan/start", method = RequestMethod.POST)
	public ReturnStart start(HttpServletRequest request,
			@RequestBody HavestPlanningInput input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		return null;
	}

	@RequestMapping(value = "/havest-plan/get-fields", method = RequestMethod.POST)
	public ReturnFields getFields(HttpServletRequest request) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/fields.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			FieldList FL = gson.fromJson(new FileReader(fieldFilename),
					FieldList.class);

			System.out.println(name() + "::getFields, fieldList.sz = "
					+ FL.getFields().length);



			return new ReturnFields(FL.getFields().length,
					"successful", FL);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/get-plant-standard", method = RequestMethod.POST)
	public ReturnPlantStandard getPlantStandard(HttpServletRequest request) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/plant-standard.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			PlantStandard ps = gson.fromJson(new FileReader(fieldFilename),
					PlantStandard.class);

			System.out.println(name() + "::getPlantStandard, ps = " + ps.toString());



			return new ReturnPlantStandard("successful", ps);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/get-machine-setting", method = RequestMethod.POST)
	public ReturnMachineSetting getMachineSetting(HttpServletRequest request) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/machine-setting.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			MachineSetting ms = gson.fromJson(new FileReader(fieldFilename),
					MachineSetting.class);

			System.out.println(name() + "::getMachineSetting, machine setting = "
					+ ms.toString());



			return new ReturnMachineSetting("successful", ms);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/get-solution", method = RequestMethod.POST)
	public HavestPlanningSolution getSolution(HttpServletRequest request) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/harvest-plan-solution.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			HavestPlanningSolution sol = gson.fromJson(new FileReader(fieldFilename),
					HavestPlanningSolution.class);

			System.out.println(name() + "::getSolution, solution = "
					+ sol.toString());



			return sol;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/add-fields", method = RequestMethod.POST)
	public ReturnAddFields addFields(HttpServletRequest request,
			@RequestBody FieldList fieldList) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/fields.json";
		try {
			/*
			 * JSONParser parser = new JSONParser(); Object obj =
			 * parser.parse(new FileReader(path)); JSONObject jsonObject =
			 * (JSONObject)obj; JSONArray jarr =
			 * (JSONArray)jsonObject.get("fields"); ArrayList<Field> L = new
			 * ArrayList<Field>(); Iterator it = jarr.iterator();
			 * while(it.hasNext()){ JSONObject o = (JSONObject)it.next(); String
			 * code = (String)o.get("code"); String districtCode =
			 * (String)o.get("districtCode"); String ownerCode =
			 * (String)o.get("ownerCode"); double area = (double)o.get("area");
			 * 
			 * //private Date date;// optimal havesting date String plant_date =
			 * (String)o.get("plant_date");
			 * 
			 * long l_quantity = (long)o.get("quantity");
			 * 
			 * int quantity = (int)l_quantity;
			 * 
			 * String category = (String)o.get("category"); String plantType =
			 * (String)o.get("plantType");
			 * 
			 * long l_deltaDays = (long)o.get("deltaDays"); int deltaDays =
			 * (int)l_deltaDays;
			 * 
			 * Field f = new Field(code,
			 * districtCode,ownerCode,area,plant_date,quantity,category,
			 * plantType,deltaDays); System.out.println(f); L.add(f); }
			 */

			String fieldFilename = path;
			Gson gson = new Gson();

			FieldList FL = gson.fromJson(new FileReader(fieldFilename),
					FieldList.class);

			System.out.println(name() + "::addFields, fieldList.sz = "
					+ fieldList);

			// check duplication of field code
			boolean duplication = false;
			String codes = "";
			for (int j = 0; j < fieldList.getFields().length; j++) {
				Field f = fieldList.getFields()[j];
				if (FL.getFields() != null)
					for (int i = 0; i < FL.getFields().length; i++) {
						if (f.getCode().equals(FL.getFields()[i].getCode())) {
							duplication = true;
							codes += f.getCode() + ", ";
							break;
						}
					}
			}
			if (duplication) {
				return new ReturnAddFields(FL.getFields().length,
						"duplicated fields " + codes, FL);
			}

			Field[] F = null;

			if (FL.getFields() == null) {
				F = fieldList.getFields();
			}else if(fieldList.getFields() == null){
				F = FL.getFields();
			}else {
				F = new Field[FL.getFields().length
						+ fieldList.getFields().length];
				int idx = -1;
				for (int i = 0; i < FL.getFields().length; i++) {
					idx++;
					F[idx] = FL.getFields()[i];
				}
				for (int i = 0; i < fieldList.getFields().length; i++) {
					idx++;
					F[idx] = fieldList.getFields()[i];
				}
			}
			FieldList newFieldList = new FieldList(F);

			PrintWriter out = new PrintWriter(path);
			out.print(gson.toJson(newFieldList));
			out.close();

			return new ReturnAddFields(newFieldList.getFields().length,
					"successful", newFieldList);
			// return newFieldList;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/remove-fields", method = RequestMethod.POST)
	public ReturnAddFields removeFields(HttpServletRequest request,
			@RequestBody FieldCodeList fieldList) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/fields.json";
		path = ROOT + "/fields.json";
		try {
			String fieldFilename = path;
			Gson gson = new Gson();

			FieldList FL = gson.fromJson(new FileReader(fieldFilename),
					FieldList.class);

			System.out.println(name() + "::addFields, fieldList.sz = "
					+ fieldList);
			
			ArrayList<Field> l_fields = new ArrayList<Field>();

			// check duplication of field code
			if(FL.getFields() != null)for (int i = 0; i < FL.getFields().length; i++) {
				Field f = FL.getFields()[i];
				boolean exists = false;
				for (int j = 0; j < fieldList.getFields().length; j++) {
					if (fieldList.getFields()[j].getCode().equals(f.getCode())) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					l_fields.add(f);
				}
			}

			Field[] arr_fields = new Field[l_fields.size()];
			for (int i = 0; i < l_fields.size(); i++)
				arr_fields[i] = l_fields.get(i);
			FieldList newFieldList = new FieldList(arr_fields);

			PrintWriter out = new PrintWriter(path);
			out.print(gson.toJson(newFieldList));
			out.close();

			return new ReturnAddFields(newFieldList.getFields().length,
					"successful", newFieldList);
			// return newFieldList;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/set-fields", method = RequestMethod.POST)
	public FieldList setFields(HttpServletRequest request,
			@RequestBody FieldList input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/plant-standard.json";
		path = ROOT + "/fields.json";

		if (input.getFields() == null) {
			try {
				PrintWriter out = new PrintWriter(path);
				out.print("{}");
				out.close();
				return input;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		for (int i = 0; i < input.getFields().length; i++) {
			Field f = input.getFields()[i];
			if (f.getCategory() == null || f.getCategory().equals(""))
				f.setCategory("-");
			if (f.getPlantType() == null || f.getPlantType().equals(""))
				f.setPlantType("-");
		}
		try {
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();

			return input;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/set-plant-standard", method = RequestMethod.POST)
	public ReturnSetPlantStandard setPlantStandard(HttpServletRequest request,
			@RequestBody PlantStandard input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/plant-standard.json";
		path = ROOT + "/plant-standard.json";

		try {
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();

			return new ReturnSetPlantStandard(input.getPlantStandards().length);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/set-machine", method = RequestMethod.POST)
	public MachineSetting setMachine(HttpServletRequest request,
			@RequestBody MachineSetting input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		// path = "C:/ezRoutingAPIROOT/havestplanning/machine-setting.json";
		path = ROOT + "/machine-setting.json";

		try {
			PrintWriter out = new PrintWriter(path);
			Gson gson = new Gson();
			out.print(gson.toJson(input));
			out.close();

			return input;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/compute", method = RequestMethod.POST)
	public HavestPlanningSolution compute(HttpServletRequest request
	// , @RequestBody HavestPlanningInput input
	) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		String fieldFilename = ROOT + "/fields.json";
		String setPlatStandardFilename = ROOT + "/plant-standard.json";
		String machineSettingFilename = ROOT + "/machine-setting.json";

		Gson gson = new Gson();
		try {
			FieldList fieldList = gson.fromJson(new FileReader(fieldFilename),
					FieldList.class);
			PlantStandard ps = gson.fromJson(new FileReader(
					setPlatStandardFilename), PlantStandard.class);
			MachineSetting ms = gson.fromJson(new FileReader(
					machineSettingFilename), MachineSetting.class);

			HavestPlanningInput input = new HavestPlanningInput(
					fieldList.getFields(), ps, ms);

			SolverMultiStepSplitFields solver = new SolverMultiStepSplitFields();

			if (input.getPlantStandard() == null)
				input.initDefaultPlantStandard();

			HavestPlanningSolution sol = solver.solve(input);
			String json = gson.toJson(sol);
			System.out.println(name() + "::compute, RETURN " + json);
			
			
			path = ROOT + "/harvest-plan-solution.json";

			try {
				PrintWriter out = new PrintWriter(path);
				out.print(gson.toJson(sol));
				out.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			HavestPlanningSolution ret_sol = new HavestPlanningSolution(sol.getQuality(), 
					sol.getDescription(), sol.getNumberOfFieldsInPlan(), 
					sol.getNumberOfDatesInPlan(), sol.getNumberOfDatesInPlantStandard(), 
					sol.getInitMinQuantityDay(), sol.getInitMaxQuantityDay(), 
					sol.getComputedMinQuantityDay(), sol.getComputedMaxQuantityDay(), 
					sol.getNumberFieldsNotPlanned(), sol.getQuantityNotPlanned(), 
					sol.getQuantityPlanned(), sol.getTotalQuantity(), sol.getNumberOfLevels(), 
					sol.getNumberOfDaysHarvestExact(), sol.getNumberOfDaysPlanned(), 
					sol.getNumberOfFieldsCompleted(), sol.getMaxDaysLate(), sol.getMaxDaysEarly(), 
					sol.getNumberOfDaysOverLoad(), 
					sol.getNumberOfDaysUnderLoad());
			
			return ret_sol;
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/havest-plan/check-solution", method = RequestMethod.POST)
	public HavestPlanningSolution checkSolution(HttpServletRequest request,
			@RequestBody FieldSolutionList input_solution) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");

		String fieldFilename = ROOT + "/fields.json";
		String setPlatStandardFilename = ROOT + "/plant-standard.json";
		String machineSettingFilename = ROOT + "/machine-setting.json";

		Gson gson = new Gson();
		try {
			FieldList fieldList = gson.fromJson(new FileReader(fieldFilename),
					FieldList.class);
			PlantStandard ps = gson.fromJson(new FileReader(
					setPlatStandardFilename), PlantStandard.class);
			MachineSetting ms = gson.fromJson(new FileReader(
					machineSettingFilename), MachineSetting.class);

			// HavestPlanningInput input = new
			// HavestPlanningInput(fieldList.getFields(),ps,ms);
			HavestPlanningInput input = new HavestPlanningInput(
					input_solution.getFields(), ps, ms);

			SolutionChecker checker = new SolutionChecker();

			return checker.checkSolution(input, input_solution);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
