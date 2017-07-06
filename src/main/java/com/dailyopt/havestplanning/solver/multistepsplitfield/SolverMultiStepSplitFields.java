package com.dailyopt.havestplanning.solver.multistepsplitfield;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import localsearch.constraints.basic.AND;
import localsearch.constraints.basic.IsEqual;
import localsearch.constraints.basic.LessOrEqual;
import localsearch.constraints.basic.OR;
import localsearch.functions.conditionalsum.ConditionalSum;
import localsearch.model.ConstraintSystem;
import localsearch.model.IConstraint;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;

import org.apache.poi.ss.usermodel.DateUtil;

import com.dailyopt.havestplanning.model.Field;
import com.dailyopt.havestplanning.model.HavestPlanningInput;
import com.dailyopt.havestplanning.model.HavestPlanningSolution;
import com.dailyopt.havestplanning.solver.MField;
import com.dailyopt.havestplanning.solver.Solver;
import com.dailyopt.havestplanning.utils.DateTimeUtils;

public class SolverMultiStepSplitFields extends Solver{
	/*
	protected LocalSearchManager mgr;
	protected VarIntLS[] x;
	protected ConstraintSystem S;
	protected ConditionalSum[] load;
	*/
	
	protected int[] w;// quantity of field i
	
	//protected int[] x;// x[i] is the date field i is allocated
	//protected int[] load;// load[i] is the total quantity of fields allocated in date i
	
	
	/*
	private void stateModel(){
		mgr = new LocalSearchManager();
		x = new VarIntLS[fields.length];
		w = new int[fields.length];
		for(int i = 0; i < fields.length; i++){
			int sl = mDate2Slot.get(fields[i].getmDate());
			x[i] = new VarIntLS(mgr,sl - fields[i].getDeltaDays(), sl + fields[i].getDeltaDays());
			x[i].setValue(sl);
			
			w[i] = fields[i].getQuantity();
		}
		
		S = new ConstraintSystem(mgr);
		int m = 800;
		load = new ConditionalSum[m];
		for(int i = 0; i < m; i++){
			load[i] = new ConditionalSum(x, w, i);
			
			IConstraint c1 = new LessOrEqual(input.getMinP(), load[i]);
			IConstraint c2 = new LessOrEqual(load[i], input.getMaxP());
			IConstraint c = new IsEqual(load[i], 0);
			S.post(new OR(c,new AND(c1,c2)));
		}
		
		
		
		mgr.close();
		
		for(int i = 0; i < load.length; i++){
			log.println("load[" + i + "] = " + load[i].getValue());
			System.out.println("load[" + i + "] = " + load[i].getValue());
		}
		System.out.println("S = " + S.violations());
		
	}
	*/
	
	public PrintWriter getLog(){
		return log;
	}
	public HavestPlanningInput getInput(){
		return input;
	}
	public void stateModel(){
		
	}
	
	public void search(){
		int n = input.getFields().length;
		int[] qtt = new int[n];
		for(int i = 0; i < n; i++) qtt[i] = input.getFields()[i].getQuantity();
		int minLoad = 5000;
		int maxLoad = 6000;
		
		int m = date_sequence.length;
		int[] preload = new int[m];
		for(int i = 0; i < m; i++) preload[i]= 0;
		int[] minDate = new int[n];
		int[] maxDate = new int[n];
		
		int deltaDay = 30;
		for(int i = 0; i < n; i++){
			MField f = fields[i];
			int sl = mDate2Slot.get(f.getmDate());
			minDate[i] = sl - input.getFields()[i].getDeltaDays();
			maxDate[i] = sl + input.getFields()[i].getDeltaDays();
		}
		
		ConstrainedMultiKnapsackSolver S = new ConstrainedMultiKnapsackSolver(this);
		S.solve(preload, qtt, minDate, maxDate, minLoad, maxLoad);
		
	}
	public HavestPlanningSolution solve(HavestPlanningInput input){
		initLog();
		
		this.input = input;
		this.DURATION = input.getGrowthDuration();
		analyze();
		mapDates();
		
		System.out.println(name() + "::solve date_sequence = " + date_sequence.length + " ...");
		for(int i = 0; i < date_sequence.length; i++){
			Date d = date_sequence[i];
			ArrayList<Integer> L = mDate2ListFields.get(d);
			if(L == null) L = new ArrayList<Integer>();
			if(L.size() > 0){
			//System.out.print("date " + i + "(" + DateTimeUtils.date2YYYYMMDD(d) + "), sz = " + L.size() + " : ");
			log.print("date " + i + "(" + DateTimeUtils.date2YYYYMMDD(d) + "), sz = " + L.size() + ", total = " + mDate2Quantity.get(d) + " : ");
			//for(int j: L){
				//log.print(j + "(" + fields[j].getQuantity() + ") ");
				//System.out.print(j + " ");
			//}
			//System.out.println();
			log.println();
			}
		}
		
		// 
		stateModel();
		
		search();
		
		finalize();
		System.out.println("finished");
		return null;
	}
	
	public String name(){
		return "SolverMultiStepSplitFields";
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
