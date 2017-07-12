package com.dailyopt.havestplanning.solver.multistepsplitfield;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
import com.dailyopt.havestplanning.model.HavestPlanningCluster;
import com.dailyopt.havestplanning.model.HavestPlanningField;
import com.dailyopt.havestplanning.model.HavestPlanningInput;
import com.dailyopt.havestplanning.model.HavestPlanningSolution;
import com.dailyopt.havestplanning.model.PlantStandard;
import com.dailyopt.havestplanning.solver.MField;
import com.dailyopt.havestplanning.solver.Solver;
import com.dailyopt.havestplanning.utils.DateTimeUtils;
import com.dailyopt.havestplanning.utils.Utility;

public class SolverMultiStepSplitFields extends Solver {
	/*
	 * protected LocalSearchManager mgr; protected VarIntLS[] x; protected
	 * ConstraintSystem S; protected ConditionalSum[] load;
	 */

	// protected int[] w;// quantity of field i
	protected LeveledHavestPlanSolution sol;
	protected ArrayList<LeveledHavestPlanSolution> solutions;

	protected ConstrainedMultiKnapsackSolver S;

	// protected int[] x;// x[i] is the date field i is allocated
	// protected int[] load;// load[i] is the total quantity of fields allocated
	// in date i

	/*
	 * private void stateModel(){ mgr = new LocalSearchManager(); x = new
	 * VarIntLS[fields.length]; w = new int[fields.length]; for(int i = 0; i <
	 * fields.length; i++){ int sl = mDate2Slot.get(fields[i].getmDate()); x[i]
	 * = new VarIntLS(mgr,sl - fields[i].getDeltaDays(), sl +
	 * fields[i].getDeltaDays()); x[i].setValue(sl);
	 * 
	 * w[i] = fields[i].getQuantity(); }
	 * 
	 * S = new ConstraintSystem(mgr); int m = 800; load = new ConditionalSum[m];
	 * for(int i = 0; i < m; i++){ load[i] = new ConditionalSum(x, w, i);
	 * 
	 * IConstraint c1 = new LessOrEqual(input.getMinP(), load[i]); IConstraint
	 * c2 = new LessOrEqual(load[i], input.getMaxP()); IConstraint c = new
	 * IsEqual(load[i], 0); S.post(new OR(c,new AND(c1,c2))); }
	 * 
	 * 
	 * 
	 * mgr.close();
	 * 
	 * for(int i = 0; i < load.length; i++){ log.println("load[" + i + "] = " +
	 * load[i].getValue()); System.out.println("load[" + i + "] = " +
	 * load[i].getValue()); } System.out.println("S = " + S.violations());
	 * 
	 * }
	 */

	public PrintWriter getLog() {
		return log;
	}

	public HavestPlanningInput getInput() {
		return input;
	}

	public void stateModel() {

	}

	public void search() {
		int n = input.getFields().length;
		int[] qtt = new int[n];
		for (int i = 0; i < n; i++)
			qtt[i] = input.getFields()[i].getQuantity();
		int minLoad = input.getMachineSetting().getMinLoad();// 5000;
		int maxLoad = input.getMachineSetting().getMaxLoad();// 6000;

		int m = date_sequence.length;
		int[] preload = new int[m];
		for (int i = 0; i < m; i++)
			preload[i] = 0;
		int[] minDate = new int[n];
		int[] maxDate = new int[n];
		int[] expected_dates = new int[n];
		// int deltaDay = 30;
		for (int i = 0; i < n; i++) {
			MField f = fields[i];
			/*
			 * int sl = mDate2Slot.get(f.getmDate()); minDate[i] = sl -
			 * input.getFields()[i].getDeltaDays(); maxDate[i] = sl +
			 * input.getFields()[i].getDeltaDays();
			 */
			Date d = DateTimeUtils.convertYYYYMMDD2Date(f.getPlant_date());
			int plantStart = mDate2Slot.get(d);
			System.out.println(name() + "::search, field[" + i
					+ "], plantStart = " + plantStart + ", category = "
					+ f.getCategory() + ", plantType = " + f.getPlantType());
			System.out.println(name() + "::search, plantStandard = "
					+ input.getPlantStandard());
			minDate[i] = plantStart
					+ input.getPlantStandard().getMinPeriod(f.getCategory(),
							f.getPlantType());
			maxDate[i] = plantStart
					+ input.getPlantStandard().getMaxPeriod(f.getCategory(),
							f.getPlantType());
			
			expected_dates[i] = plantStart
					+ input.getPlantStandard().getBestPeriod(f.getCategory(),
							f.getPlantType());
			
		}

		S = new ConstrainedMultiKnapsackSolver(this);

		solutions = new ArrayList<LeveledHavestPlanSolution>();

		int nbSteps = 0;
		while (true) {
			// check feasibility for before solving
			boolean feasible = true;
			int nbEmptyFields = 0;
			for(int i = 0; i < qtt.length; i++){
				if(qtt[i] == 0) nbEmptyFields++;
			}
			if(nbEmptyFields == qtt.length) feasible = false;
			
			for (int i = 0; i < qtt.length; i++) {
				boolean ok = false;
				for (int d = minDate[i]; d <= maxDate[i]; d++) {
					if (preload[d] < maxLoad) {
						ok = true;
						break;
					}
				}
				if(!ok){
					feasible = false;
					break;
				}
			}
			if (!feasible)
				break;

			// solve the problem
			LeveledHavestPlanSolution s = S.solve(preload, qtt, minDate,
					maxDate, expected_dates, minLoad, maxLoad);
			solutions.add(s);
			System.out.println(name() + "::search, solutions[" + solutions.size() + "] = " + s.toString());
			System.out.println(name() + "*******************************************************************************");
			// update remaining qtt, preload for next steps
			int[] xd = s.getXd();
			int[] sq = s.getQuantity();

			for (int i = 0; i < n; i++) {
				qtt[i] = qtt[i] - sq[i];
			}

			for (int d = 0; d < m; d++) {
				for (int i = 0; i < n; i++) {
					if (xd[i] == d) {
						preload[d] = preload[d] + sq[i];
					}
				}
			}
			
			//if(solutions.size() > 3){
			//	break;
			//}
		}

	}

	public HavestPlanningSolution solve(HavestPlanningInput input) {
		initLog();

		this.input = input;
		// this.DURATION = input.getGrowthDuration();
		analyze();
		mapDates();

		System.out.println(name() + "::solve date_sequence = "
				+ date_sequence.length + " ...");
		for (int i = 0; i < date_sequence.length; i++) {
			Date d = date_sequence[i];
			ArrayList<Integer> L = mDate2ListFields.get(d);
			if (L == null)
				L = new ArrayList<Integer>();
			if (L.size() > 0) {
				// System.out.print("date " + i + "(" +
				// DateTimeUtils.date2YYYYMMDD(d) + "), sz = " + L.size() +
				// " : ");
				log.print("date " + i + "(" + DateTimeUtils.date2YYYYMMDD(d)
						+ "), sz = " + L.size() + ", total = "
						+ mDate2Quantity.get(d) + " : ");
				// for(int j: L){
				// log.print(j + "(" + fields[j].getQuantity() + ") ");
				// System.out.print(j + " ");
				// }
				// System.out.println();
				log.println();
			}
		}

		//
		stateModel();

		search();

		finalize();
		System.out.println("finished, number of levels = " + solutions.size());

		ArrayList<HavestPlanningCluster> cluster = new ArrayList<HavestPlanningCluster>();
		int quality = 0;

		HashMap<Integer, HavestPlanningCluster> mDate2Cluster = new HashMap<Integer, HavestPlanningCluster>();
		HashMap<Integer, Integer> mDate2Quantity = new HashMap<Integer, Integer>();
		
		for (int k = 0; k < solutions.size(); k++) {
			sol = solutions.get(k);
			
			int[] xd = sol.getXd();
			int[] sq = sol.getQuantity();

			for (int i = 0; i < date_sequence.length; i++) {
				ArrayList<Integer> F = new ArrayList<Integer>();
				for (int j = 0; j < xd.length; j++)
					if (xd[j] == i)
						F.add(j);

				if (F.size() > 0) {
					// Date currentDate =
					// Utility.next(date_sequence[i],DURATION);
					Date currentDate = date_sequence[i];

					HavestPlanningField[] HPF = new HavestPlanningField[F
							.size()];
					int qtt = 0;
					for (int j = 0; j < F.size(); j++) {
						int fid = F.get(j);
						// MField f = fields[fid];
						Field f = input.getFields()[fid];
						// MField mf = fields[fid];
						// int sl = mDate2Slot.get(f.getmDate());
						// Date expected_havest_date =
						// Utility.next(f.getmDate(),DURATION);
						int bestDate = getBestHavestDate(f);
						Date expected_havest_date = date_sequence[bestDate];

						String expected_havest_date_str = DateTimeUtils
								.date2YYYYMMDD(expected_havest_date);
						Date d = date_sequence[xd[fid]];
						// d = Utility.next(d,DURATION);
						int days_late = Utility.distance(expected_havest_date,
								d);
						HPF[j] = new HavestPlanningField(f,
								expected_havest_date_str, sq[fid], days_late);

						int period = xd[fid]
								- mDate2Slot
										.get(DateTimeUtils
												.convertYYYYMMDD2Date(f
														.getPlant_date()));

						// quality += Utility.eval(input.getQualityFunction(),
						// days_late);
						quality += sq[fid]
								* input.getPlantStandard().evaluateQuality(
										f.getCategory(), f.getPlantType(),
										period);

						qtt += sq[fid];//f.getQuantity();
					}
					String date = DateTimeUtils.date2YYYYMMDD(currentDate);
					HavestPlanningCluster Ck = new HavestPlanningCluster(date,
							HPF, qtt, F.size());
					
					HavestPlanningCluster Ci = mDate2Cluster.get(i);
					if(Ci == null){
						cluster.add(Ck);
						mDate2Cluster.put(i, Ck);
					}else{
						// concatenate  the fields in Ck and Ci
						Ci.concate(Ck);
					}
				}
			}
		}

		HavestPlanningCluster[] a_cluster = new HavestPlanningCluster[cluster
				.size()];
		for (int i = 0; i < cluster.size(); i++)
			a_cluster[i] = cluster.get(i);

		HavestPlanningSolution solution = new HavestPlanningSolution(a_cluster,
				quality);
		return solution;
	}

	public String name() {
		return "SolverMultiStepSplitFields";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
