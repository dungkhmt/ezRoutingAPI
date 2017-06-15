package com.dailyopt.havestplanning.solver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;









import com.dailyopt.havestplanning.model.Field;
import com.dailyopt.havestplanning.model.HavestPlanningCluster;
import com.dailyopt.havestplanning.model.HavestPlanningField;
import com.dailyopt.havestplanning.model.HavestPlanningInput;
import com.dailyopt.havestplanning.model.FieldClusterIndices;
import com.dailyopt.havestplanning.model.FieldCluster;
import com.dailyopt.havestplanning.model.HavestPlanningSolution;
import com.dailyopt.havestplanning.utils.DateTimeUtils;
import com.dailyopt.havestplanning.utils.Utility;


public class Solver {
	private int DURATION = 355;
	private HavestPlanningInput input;
	private HashMap<Date, Integer> mDate2Slot;
	private HashMap<Date, Integer> mDate2Quantity;
	private HashMap<Date, ArrayList<Integer>> mDate2ListFields;
	private Date[] dates;
	private MField[] fields;
	private int[] expected_slot;
	
	private Date[] date_sequence;
	
	ArrayList<FieldClusterIndices> clusterIndices;
	HashMap<FieldCluster, Integer> mCluster2Slot;
	HashMap<Integer, FieldCluster> mSlot2Cluster;
	int startSlot;
	int endSlot;
	
	public String name(){
		return "api.solver.solve";
	}
	public void analyze(){
		fields = new MField[input.getFields().length];
		for(int i = 0; i < fields.length; i++){
			Field F = input.getFields()[i];
			Date mDate = DateTimeUtils.convertYYYYMMDD2Date(F.getPlant_date());
			
			fields[i] = new MField(F.getCode(), F.getDistrictCode(), 
					F.getOwnerCode(), F.getArea(), F.getPlant_date(), F.getQuantity(), 
					F.getDeltaDays(), mDate);
			
		}
		
		mDate2Quantity = new HashMap<Date, Integer>();
		mDate2ListFields = new HashMap<Date, ArrayList<Integer>>();

		for(int i = 0; i < fields.length; i++){
			if(mDate2Quantity.get(fields[i].getmDate()) == null){
				mDate2Quantity.put(fields[i].getmDate(), fields[i].getQuantity());
				
				mDate2ListFields.put(fields[i].getmDate(), new ArrayList<Integer>());
				mDate2ListFields.get(fields[i].getmDate()).add(i);
			}else{
				int q = mDate2Quantity.get(fields[i].getmDate());
				q += fields[i].getQuantity();
				mDate2Quantity.put(fields[i].getmDate(),q);
				mDate2ListFields.get(fields[i].getmDate()).add(i);
			}
		}
		dates = new Date[mDate2Quantity.keySet().size()];
		int idx = -1;
		for(Date d: mDate2Quantity.keySet()){
			idx++;
			dates[idx] = d;
			//System.out.println(d.toString() + " : " + mDate2Quantity.get(d));
		}
		
		for(int i = 0; i < dates.length; i++)
			for(int j = i+1; j < dates.length; j++)
				if(dates[i].compareTo(dates[j]) > 0){
					Date tmp = dates[i]; dates[i] = dates[j]; dates[j] = tmp;
				}
		for(int i = 0; i < dates.length; i++){
			String items = "";
			ArrayList<Integer> L = mDate2ListFields.get(dates[i]);
			for(int j = 0; j < L.size(); j++)
				items += L.get(j) + ",";
			System.out.println(dates[i].getYear() + "-" + (dates[i].getMonth()+1) + "-" + dates[i].getDate() +
					"\t" + dates[i] + "\t" + mDate2Quantity.get(dates[i]) + "\t" + items);
		}
		System.out.println("total : " + dates.length);
	}

	public void mapDates() {
		mDate2Slot = new HashMap<Date, Integer>();
		int start = 20;
		mDate2Slot.put(dates[0], start);
		date_sequence = new Date[2000];
		date_sequence[0] = Utility.next(dates[0],-start);
		for(int i = 1; i < date_sequence.length; i++){
			date_sequence[i] = Utility.next(date_sequence[i-1],1);
		}
		for(int i = 0; i < date_sequence.length; i++){
			System.out.println(i + " : " + Utility.dateMonthYear(date_sequence[i]));
		}
		//System.exit(-1);
		/*
		 * Calendar cal = Calendar.getInstance(); cal.setTime(dates[0]); for(int
		 * i = 0; i < 100; i++){ cal.add(Calendar.DATE, 1);
		 * System.out.println(cal.getTime().toString()); } if(true) return;
		 */

		for (int i = 1; i < dates.length; i++) {
			Date d = dates[i - 1];
			while (true) {
				d = Utility.next(d);
				start++;
				if (d.compareTo(dates[i]) == 0) {
					break;
				}
			}
			mDate2Slot.put(dates[i], start);
		}

		for (int i = 0; i < dates.length; i++) {
			System.out.println(dates[i].toString() + "\t" + Utility.dateMonthYear(dates[i]) + "\t"
					+ mDate2Slot.get(dates[i]));
		}
		//System.exit(-1);
		
		expected_slot = new int[fields.length];
		for(int i = 0; i < dates.length; i++){
			Date d = dates[i];
			ArrayList<Integer> L = mDate2ListFields.get(d);
			int sl = mDate2Slot.get(d);
			for(int j: L)
				expected_slot[j] = sl;
		}
	}
	public int rearrangeSlots(FieldCluster[] clusters, int earliest, int start, int end){
		int latest = date_sequence.length;//mDate2Slot.get(dates[end]);
		int eval1 = 1000000;
		int eval2 = 1000000;
		int sel_sl = -1;
		for(int sl = earliest; sl < latest; sl++){
			int e1 = -1;
			int e2 = 0;
			for(int i = 0; i < clusters.length; i++){
				// try to assign clusters i to slot sl + i
				for(int j = 0; j < clusters[i].size(); j++){
					int f = clusters[i].get(j);
					int d = Math.abs(expected_slot[f] - (sl+i));
					if(d > e1){
						e1 = d; e2 = 1;
					}else if(d == e1){
						e2++;
					}
				}
			}
			if(e1 < eval1 || (e1 == eval1 && e2 < eval2)){
				eval1 = e1; eval2 = e2; sel_sl = sl;
			}
		}
		if(sel_sl < 0){
			System.out.println(name() + "::rearrangeSlots, EXCEPTION earliest = " + earliest + ", latest = " + latest);
			System.exit(-1);
		}
		return sel_sl;
	}
	public FieldCluster[] compute(ArrayList<Integer> L, ArrayList<Integer> w){
		MultiKnapsackSolver solver = new MultiKnapsackSolver();
		int[] aw = new int[w.size()];
		for(int i = 0; i < w.size(); i++) aw[i] = w.get(i);
		FieldCluster[] C = solver.solve(aw,input.getMinP(),input.getMaxP());
		FieldCluster[] rC = new FieldCluster[C.length];
		for(int i = 0; i < C.length; i++){
			rC[i] = new FieldCluster();
			rC[i].setWeight(C[i].getWeight());
			for(int j = 0; j < C[i].size(); j++){
				int idx = C[i].get(j);
				rC[i].add(L.get(idx));
			}
		}
		return rC;
	}
	
	private int specifyStart(int min, int sz, int start, int end){
		int m = (start + end)/2;
		int s = m - sz/2 + 1;
		if(s < min) s = min;
		return s;
	}

	public HavestPlanningSolution solve(HavestPlanningInput input){
		this.input = input;
		
		analyze();
		mapDates();
		
		//MultiKnapsackSolver knapsack = new MultiKnapsackSolver();
		
		clusterIndices = new ArrayList<FieldClusterIndices>();
		int i = 0;
		while(i < dates.length){
			int S = 0;
			int j = i;
			ArrayList<Integer> L = new ArrayList<Integer>();
			while(j < dates.length){
				S += mDate2Quantity.get(dates[j]);
				ArrayList<Integer> Lj = mDate2ListFields.get(dates[j]); 
				for(int k = 0; k < Lj.size(); k++){
					L.add(Lj.get(k));
				}
			
				if(input.getMinP() <= S && S <= input.getMaxP() 
						|| S > input.getMaxP() 
						|| (j+1 < dates.length && mDate2Slot.get(dates[j+1]) - mDate2Slot.get(dates[i]) >= input.getClusterDuration())
						){
					break;
				}
				j++;
			}
			if(j >= dates.length) j--;// border case
			
			if(input.getMinP() <= S && S <= input.getMaxP() 
					|| S <= 7000 
					){
				FieldCluster[] clusters = new FieldCluster[1];
				clusters[0] = new FieldCluster();
				for(int e: L) clusters[0].add(e);
				clusters[0].setWeight(S);
				FieldClusterIndices CI = new FieldClusterIndices(clusters, i, j);
				System.out.println(name() + "::solve, admin a cluster (" + i + "," + j + "), dates.sz = " + dates.length);
				clusterIndices.add(CI);
				//break;
			}else if(S > 7000){
				ArrayList<Integer> w = new ArrayList<Integer>();
				for(int k = 0; k < L.size(); k++){
					int q = fields[L.get(k)].getQuantity();
					w.add(q);
				}
				FieldCluster[] C = compute(L, w);
				FieldClusterIndices CI = new FieldClusterIndices(C, i, j);
				clusterIndices.add(CI);
				//break;
			}
			
			i = j+1;
		}

		mCluster2Slot = new HashMap<FieldCluster, Integer>();
		mSlot2Cluster = new HashMap<Integer, FieldCluster>();
		int startIdx = 0;
		for(i = 0; i < clusterIndices.size(); i++){
			int sz = clusterIndices.get(i).getCluster().length;
			int I = clusterIndices.get(i).getStartIdx();
			int J = clusterIndices.get(i).getEndIdx();
			//int s = specifyStart(startIdx, sz, I, J);
			int s = rearrangeSlots(clusterIndices.get(i).getCluster(), startIdx, I, J);
			startIdx = s + sz;

			if(i == 0){
				startSlot = s;
			}else if(i == clusterIndices.size()-1){
				endSlot = startIdx - 1;
			}
			
			for(int j = 0; j < sz; j++){
				System.out.println(clusterIndices.get(i).getCluster()[j].getWeight());
				mCluster2Slot.put(clusterIndices.get(i).getCluster()[j], s+j);
				mSlot2Cluster.put(s+j, clusterIndices.get(i).getCluster()[j]);
			}

			int slot_begin = mDate2Slot.get(dates[clusterIndices.get(i).getStartIdx()]);
			int slot_end = mDate2Slot.get(dates[clusterIndices.get(i).getEndIdx()]);
			System.out.println(slot_begin + "\t" + slot_end + 
					": rearrange \t" + s + " -> " + (startIdx-1));
			System.out.println("---------------------------------------------");
		}
		
		ArrayList<HavestPlanningCluster> list_clusters = new ArrayList<HavestPlanningCluster>();
		double quality = 0;
		SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-DD");
		for(int sl = startSlot; sl <= endSlot; sl++) {
			FieldCluster C = mSlot2Cluster.get(sl);
			if(C != null){
				Date d = Utility.next(date_sequence[sl],DURATION);// after 11 month
				//String date_str = df.format(d);
				String date_str = DateTimeUtils.date2YYYYMMDD(d);
				
				HavestPlanningField[] HPF = new HavestPlanningField[C.size()];
				int qtt = 0;
				for(int j = 0; j < C.size(); j++){
					int fj = C.get(j);
					MField F = fields[fj];
					Date expected_havest_date = Utility.next(F.getmDate(),DURATION);
					
					//String expected_havest_date_str = df.format(expected_havest_date);
					String expected_havest_date_str = DateTimeUtils.date2YYYYMMDD(expected_havest_date);
					
					int days_late = Utility.distance(d, expected_havest_date);
					HPF[j] = new HavestPlanningField(fields[fj], expected_havest_date_str, days_late);
					quality += Utility.eval(input.getQualityFunction(), days_late);
					qtt += F.getQuantity();
				}
				HavestPlanningCluster hpc = new HavestPlanningCluster(date_str, HPF, qtt, C.size());
				list_clusters.add(hpc);
			}
		}
		HavestPlanningCluster[] arr_clusters = new HavestPlanningCluster[list_clusters.size()];
		for(int ii = 0; ii < list_clusters.size(); ii++)
			arr_clusters[ii] = list_clusters.get(ii);
		HavestPlanningSolution sol = new HavestPlanningSolution(arr_clusters, quality);
		return sol;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
