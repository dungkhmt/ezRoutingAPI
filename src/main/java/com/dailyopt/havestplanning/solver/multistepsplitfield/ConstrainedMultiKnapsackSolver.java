package com.dailyopt.havestplanning.solver.multistepsplitfield;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class ConstrainedMultiKnapsackSolver {
	protected int[] preload;// preload[i] is the pre-load in date i
	protected int[] qtt;// qtt[i] is the remain quantity of field i
	protected int[] minDate;
	protected int[] maxDate;
	protected int[] expectedHavestDate;

	protected int minLoad;
	protected int maxLoad;
	protected int m;
	protected int n;

	protected int maxDeltaDay = 5;
	protected int startDate;
	protected int endDate;

	// variables, invariants
	protected int[] x;// x[i] is the bin where item i is allocated (minDate[i]
						// <= x[i] <= maxDate[i])
	protected int[] load;// load[i] is the current load of bin i

	protected int[] violations_packing;// violations[i] is the violations of bin
										// i
	protected int total_violations_packing;
	protected int max_violations_packing;

	protected int[] violations_havest;// violations_havest[i] is the violations
										// of the havest date of field i
	protected int total_violations_havest;

	protected Random R;

	private SolverMultiStepSplitFields solver;

	public SolverMultiStepSplitFields getSolver() {
		return solver;
	}

	public ConstrainedMultiKnapsackSolver(SolverMultiStepSplitFields solver) {
		this.solver = solver;
	}

	public LeveledHavestPlanSolution solve(int[] preload, int[] qtt,
			int[] minDate, int[] maxDate, int minLoad, int maxLoad) {

		/*
		 * m = preload.length: number of bins (days), bins are numbered 0, 1,
		 * ..., m-1 n = qtt.length: number of items (fields), items are numbered
		 * 0, 1, ..., n-1 preload[i]: pre-load of bin i qtt[j]: quantity
		 * (weight) of item j minDate[j], maxDate[j]: item j can be loaded in
		 * bins minDate[j],...,maxDate[j] The total load of a bin is between
		 * [minLoad..maxLoad] or the load of a bin is 0 Bin i s.t. load[i] =
		 * maxLoad is forbidden to load other items objective: allocate each
		 * item i in a bin from {minDate[j],...,maxDate[j]} minimizing
		 * violations
		 */

		this.preload = preload;
		this.qtt = qtt;
		this.minDate = minDate;
		this.maxDate = maxDate;
		this.minLoad = minLoad;
		this.maxLoad = maxLoad;
		this.m = preload.length;
		this.n = qtt.length;

		expectedHavestDate = new int[minDate.length];
		for (int i = 0; i < expectedHavestDate.length; i++)
			expectedHavestDate[i] = (minDate[i] + maxDate[i]) / 2;

		startDate = 10000000;
		endDate = -10000000;
		for (int i = 0; i < n; i++) {
			if (startDate > minDate[i])
				startDate = minDate[i];
			if (endDate < maxDate[i])
				endDate = maxDate[i];
		}
		R = new Random();

		System.out
				.println(name() + "::solve start.... n = " + n + ", m = " + m);

		stateModel();
		search(5);

		getSolver().getLog().println("SOLUTION:");
	
		for (int i = 0; i < m; i++) {

			int sz = 0;
			String des = "";
			for (int j = 0; j < n; j++) {
				if (x[j] == i) {
					sz++;
					des += "(" + j + ",q[" + qtt[j] + "], d[" + minDate[j]
							+ "-" + maxDate[j] + "]) ";
				}
			}
			if (sz > 0) {
				System.out.print("date " + i + " : ");
				System.out.println("sz = " + sz + ", load = " + load[i]
						+ ", violations_packing = " + violations_packing[i]
						+ ", des = " + des);
				
				getSolver().getLog().print("date " + i + " : ");
				getSolver().getLog().println("sz = " + sz + ", load = " + load[i]
						+ ", violations_packing = " + violations_packing[i]
						+ ", des = ");// + des);
				
			}
		}

		int[] xd = new int[x.length];
		int[] quantity = new int[x.length];

		for(int d = 0; d < m; d++){
			ArrayList<Integer> I = getItemsOfBin(d);
			int[] q = new int[I.size()];
			for(int i = 0; i < I.size(); i++) q[i] = qtt[I.get(i)];
			double[] sq = selectQuantity(q, minLoad, maxLoad);
			for(int i = 0; i < I.size(); i++){
				int f = I.get(i);
				quantity[f] = (int)sq[i];
				xd[f] = d;
			}
		}
		
		
		return new LeveledHavestPlanSolution(xd, quantity);
	}
	public double[] selectQuantity(int[] q, int minQ, int maxQ){
		double[] sq = new double[q.length];
		int k = 0;
		int s = 0;
		int maxQ1 = maxQ;
		for(int i = 0; i < q.length; i++) s = s + q[i];
		
		if(s <= minQ){
			for(int i = 0; i < q.length; i++)
				sq[i] = q[i];
			return sq;
		}
		
		//for(int i = 0; i < q.length; i++) System.out.print(q[i] + " "); System.out.println();
		
		int[] idx = new int[q.length];
		for(int i = 0; i < q.length; i++) idx[i] = i;
		for(int i = 0; i < q.length-1;i++){
			for(int j = i+1; j < q.length; j++){
				if(q[i] > q[j]){
					int tmp = q[i]; q[i] = q[j]; q[j] = tmp;
					tmp = idx[i]; idx[i] = idx[j]; idx[j] = tmp;
				}
			}
		}
		//for(int i = 0; i < q.length; i++) System.out.print(q[i] + " "); System.out.println();
		//for(int i = 0; i < q.length; i++) System.out.print(idx[i] + " "); System.out.println();
		
		while(k < q.length-1){
			s = s - q[k];
			maxQ1 = maxQ1 - q[k];
			int r = s - maxQ1;
			//System.out.println("k = " + k + ", s = " + s + ", maxQ1 = " + maxQ1 + ", r = " + r + 
			//		", q[k] = " + q[k] + ", next = " + (q[k+1]*(1-r*1.0/s)));
			if(q[k] > q[k+1]*(1-r*1.0/s)){
				s += q[k];
				maxQ1 += q[k];
				//System.out.println("BREAK recover s = " + s + ", maxQ1 = " + maxQ1 + ", k = " + k);
				break;
			}else{
				sq[idx[k]] = q[k];
				//System.out.println("k = " + k + ", idx[" + k + "] = " + idx[k] + 
				//		" ACCEPT sq[" + idx[k] + "] = " + q[k] + ", s = " + s + ", maxQ1 = " + maxQ1);
			}
			k++;
		}
		double r = (s - maxQ1)*1.0/s;
		
		for(int i = k; i < q.length; i++){
			sq[idx[i]] = q[i]*(1-r);
			//System.out.println("i = " + i + ", idx[" + i + "] = " + idx[i] + ", q[idx[i]] = " + q[idx[i]] +
			//		", r = " + r + ", ACCEPT sq[" + idx[i] + "] = " + sq[idx[i]]);
		}
		double t = 0;
		for(int i = 0; i < sq.length; i++) t = t + sq[i];
	//	System.out.println("check t = " + t );
		
		return sq;
	}
	private HashMap<Integer, Integer> selectQuantity(int d){
		// return foreach field i, the quantity to be havested
		HashMap<Integer, Integer> havestQuantity = new HashMap<Integer, Integer>();
		ArrayList<Integer> I = getItemsOfBin(d);
		
		return havestQuantity;
	}
	public void stateModel() {
		x = new int[n];
		load = new int[m];
		violations_packing = new int[m];
		violations_havest = new int[n];
	}

	public void initPropagate() {
		for (int i = 0; i < m; i++) {
			load[i] = preload[i];
		}

		for (int j = 0; j < n; j++) {
			int d = x[j];
			load[d] += qtt[j];
		}

		max_violations_packing = 0;// Integer.MAX_VALUE
		total_violations_packing = 0;
		for (int i = 0; i < m; i++) {
			violations_packing[i] = violations_packing(load[i]);
			total_violations_packing += violations_packing[i];
			if (max_violations_packing < violations_packing[i])
				max_violations_packing = violations_packing[i];
		}

		total_violations_havest = 0;
		for (int i = 0; i < n; i++) {
			violations_havest[i] = qtt[i]
					* (Math.abs(x[i] - expectedHavestDate[i]));
			total_violations_havest += violations_havest[i];
		}
	}

	private void computeMaxViolationsPacking() {
		max_violations_packing = 0;// Integer.MAX_VALUE
		for (int i = 0; i < m; i++) {
			if (max_violations_packing < violations_packing[i])
				max_violations_packing = violations_packing[i];
		}

	}

	private int violations_packing(int aLoad) {
		if (aLoad == 0)
			return 0;
		else {
			if (aLoad < minLoad)
				return minLoad - aLoad;
			else if (aLoad > maxLoad)
				return aLoad - maxLoad;
			else
				return 0;
		}
	}

	public void assign(int i, int j) {
		// allocate item i into bin j and propagate
		int ob = x[i];
		if (ob == j)
			return;
		load[ob] -= qtt[i];
		load[j] += qtt[i];

		// update violations of packing
		total_violations_packing -= violations_packing[ob];
		total_violations_packing -= violations_packing[j];

		violations_packing[ob] = violations_packing(load[ob]);
		violations_packing[j] = violations_packing(load[j]);

		total_violations_packing += violations_packing[ob];
		total_violations_packing += violations_packing[j];

		// update violations of havest date
		total_violations_havest -= violations_havest[i];
		violations_havest[i] = qtt[i] * (Math.abs(expectedHavestDate[i] - j));
		total_violations_havest += violations_havest[i];

		computeMaxViolationsPacking();

		x[i] = j;
	}

	public int getAssignDeltaMaxViolationsPacking(int i, int j) {
		if (x[i] == j)
			return 0;
		int ob = x[i];// old bin of i
		int loadob = load[ob] - qtt[i];
		int loadj = load[j] + qtt[i];

		int vob = violations_packing(loadob);
		int vj = violations_packing(loadj);

		int nv = 0;
		for (int k = 0; k < m; k++) {
			if (k == ob) {
				if (nv < vob)
					nv = vob;
			} else if (k == j) {
				if (nv < vj)
					nv = vj;
			} else if (nv < violations_packing[k])
				nv = violations_packing[k];
		}
		return nv - max_violations_packing;
	}

	public int getAssignDeltaPacking(int i, int j) {
		// return the difference between old evaluation and new evaluation
		// (after assign item i to bin j)
		if (x[i] == j)
			return 0;
		int ob = x[i];// old bin of i
		int loadob = load[ob] - qtt[i];
		int loadj = load[j] + qtt[i];

		int vob = violations_packing(loadob);
		int vj = violations_packing(loadj);

		return (vob - violations_packing[ob]) + (vj - violations_packing[j]);
	}

	public int getAssignDeltaHavest(int i, int j) {
		if (x[i] == j)
			return 0;
		int h = qtt[i] * (Math.abs(expectedHavestDate[i] - j));
		return h - violations_havest[i];
	}

	public void initSolution() {
		for (int i = 0; i < n; i++) {
			// assign(i,expectedHavestDate[i]);
			HashSet<Integer> S = new HashSet<Integer>();
			for (int j = minDate[i]; j <= maxDate[i]; j++)
				if (preload[j] < maxLoad)
					S.add(j);

			int minD = Integer.MAX_VALUE;
			int sel_j = -1;
			for (int j : S) {
				if (minD > Math.abs(j - expectedHavestDate[i])) {
					minD = Math.abs(j - expectedHavestDate[i]);
					sel_j = j;
				}
			}
			x[i] = sel_j;// expectedHavestDate[i];
		}
		initPropagate();
	}

	public int getRemoveDeltaPackingViolations(int i) {
		// return the variation of violations of packing when removing item i
		// from its current bin
		int bi = x[i];
		int new_load = load[bi] - qtt[i];
		return violations_packing(new_load) - violations_packing[bi];
	}

	public int getReplaceDeltaPackingViolations(int i1, int i2) {
		// b2 = x[i2]: bin of i2
		// return the variation of violations of packing when adding item i1
		// into b2 and removing
		// i2 from its current bin b2
		int b2 = x[i2];
		int new_load = load[b2] + qtt[i1] - qtt[i2];
		return violations_packing(new_load) - violations_packing[b2];
	}

	public int getAddDeltaPackingViolations(int i, int b) {
		// return the variation of violations of packing when item i is added
		// into bin b
		int new_load = load[b] + qtt[i];
		return violations_packing(new_load) - violations_packing[b];
	}

	public ArrayList<Integer> getItemsOfBin(int b) {
		ArrayList<Integer> L = new ArrayList<Integer>();
		for (int i = 0; i < n; i++)
			if (x[i] == b)
				L.add(i);
		return L;
	}

	public boolean acceptDate(int i, int b) {
		// return true if item i can be placed in bin b
		return !forbiddenDate(b) && minDate[i] <= b && b <= maxDate[i];
	}

	public boolean forbiddenDate(int d) {
		return preload[d] >= maxLoad;
	}

	public void print() {
		for (int i = 0; i < m; i++) {
			System.out.print("Bin " + i + " : ");
			for (int j = 0; j < n; j++)
				if (x[j] == i)
					System.out.print(j + "(" + qtt[j] + ") ");
			System.out.println("load = " + load[i] + ", violations_packing = "
					+ violations_packing[i]);
		}

		for (int i = 0; i < n; i++) {
			System.out.println("x[" + i + "] = " + x[i] + ", expectedDate[" + i
					+ "] = " + expectedHavestDate[i] + ", violations_havest["
					+ i + "] = " + violations_havest[i]);
		}

		System.out.println("eval = " + eval());
	}

	public void performMoveSequence(ArrayList<Integer> moves, int d) {
		for (int k = moves.size() - 1; k >= 0; k--) {
			int i = moves.get(k);
			int j = d;
			if (k > 0) {
				j = x[moves.get(k - 1)];
			}
			int ob = x[i];
			assign(i, j);
			getSolver().getLog().println(
					name() + "::performMoveSequence, assign(" + i + "(code-"
							+ solver.getInput().getFields()[i].getCode()
							+ ", qtt-" + qtt[i] + ")  from " + ob + " -> " + j
							+ ") " + "eval = " + eval() + ", load[" + j
							+ "] = " + load[j] + ", violations_packing[" + j
							+ "] = " + violations_packing[j]);
		}
	}

	public void moveSequence(int maxIter) {
		PathMove PM = new PathMove(this);
		int count = 0;
		while (true) {
			PM.findOptimalMovePath(true);
			ArrayList<Integer> moves = PM.getMovedItems();
			int d = PM.getGlobalFinalDate();
			int best = PM.getGlobalBest();
			// if (best >= 0) break;
			System.out.print(name() + "::moveSequence, step " + count
					+ ", d = " + d + ", moves = ");
			getSolver().getLog().print(
					name() + "::moveSequence, step " + count + ", d = " + d
							+ ", moves = ");
			for (int k = moves.size() - 1; k >= 0; k--) {
				int i = moves.get(k);
				int bi = x[i];
				System.out.print(i + "[q-" + qtt[i] + ", d-" + bi + "], ");
				getSolver().getLog().print(
						i + "[q-" + qtt[i] + ", d-" + bi + "], ");
			}
			System.out.println();
			getSolver().getLog().println();
			/*
			 * for (int k = moves.size() - 1; k >= 0; k--) { int i =
			 * moves.get(k); int j = d; if (k > 0) { j = x[moves.get(k - 1)]; }
			 * int ob = x[i]; assign(i, j); //System.out.println(name() +
			 * "::moveSequence, assign(" + i + "(code-" +
			 * solver.getInput().getFields()[i].getCode() + ", qtt-" + qtt[i] +
			 * ")  from " + ob + " -> " + j + ") " // + "eval = " + eval() +
			 * ", load[" + j + "] = " + load[j] + // ", violations_packing[" + j
			 * + "] = " + violations_packing[j]); }
			 */

			performMoveSequence(moves, d);

			System.out.println(name() + "::moveSequence FINISH A LOOP best = "
					+ best + " -------------------------");
			getSolver().getLog().println(
					name() + "::moveSequence FINISH A LOOP best = " + best
							+ " -------------------------");
			count++;
			if (count >= maxIter)
				break;
		}
		getSolver().getLog().println(name() + "::moveSequence, POST");
		for (int i = 1; i <= 50; i++) {
			PM.findOptimalMovePath(20, -1, false);
			performMoveSequence(PM.getMovedItems(), PM.getGlobalFinalDate());
		}
	}

	public void search(int maxIter) {
		initSolution();
		print();

		moveSequence(200);

		// if(true)return;
		maxIter = 0;

		ArrayList<AssignMove> moves = new ArrayList<AssignMove>();
		for (int it = 0; it < maxIter; it++) {
			moves.clear();
			int min_delta_violations_packing = Integer.MAX_VALUE;
			int min_delta_violations_havest = Integer.MAX_VALUE;
			int min_delta_max_violations_packing = Integer.MAX_VALUE;

			for (int i = 0; i < n; i++) {
				for (int j = minDate[i]; j <= maxDate[i]; j++) {
					if (preload[j] == maxLoad)
						continue;// ignore full date (bin)

					int delta_violations_packing = getAssignDeltaPacking(i, j);
					int delta_violations_havest = getAssignDeltaHavest(i, j);
					int delta_max_violations_packing = getAssignDeltaMaxViolationsPacking(
							i, j);

					if (min_delta_max_violations_packing > delta_max_violations_packing) {
						min_delta_max_violations_packing = delta_max_violations_packing;
						min_delta_violations_packing = delta_violations_packing;
						min_delta_violations_havest = delta_violations_havest;

						moves.clear();
						moves.add(new AssignMove(i, j));
					} else if (min_delta_max_violations_packing == delta_max_violations_packing
							&& min_delta_violations_packing > delta_violations_packing) {
						min_delta_max_violations_packing = delta_max_violations_packing;
						min_delta_violations_havest = delta_violations_havest;

						moves.clear();
						moves.add(new AssignMove(i, j));
					} else if (min_delta_max_violations_packing == delta_max_violations_packing
							&& min_delta_violations_packing == delta_violations_packing
							&& min_delta_violations_havest > delta_violations_havest) {

						min_delta_violations_havest = delta_violations_havest;

						moves.clear();
						moves.add(new AssignMove(i, j));
					} else if (min_delta_max_violations_packing == delta_max_violations_packing
							&& min_delta_violations_packing == delta_violations_packing
							&& min_delta_violations_havest == delta_violations_havest) {
						moves.add(new AssignMove(i, j));
					}
				}
			}

			// perform the move
			if (moves.size() <= 0) {
				System.out.println(name() + "::search, NO MOVE --> BREAK");
				break;
			} else {
				AssignMove m = moves.get(R.nextInt(moves.size()));

				int o = x[m.i];
				int l1 = load[o];
				int l2 = load[m.v];

				assign(m.i, m.v);

				System.out.println(name() + "::search, Step " + it
						+ " -> move(" + m.i + " q(" + qtt[m.i] + "  from " + o
						+ " -> " + m.v + "), old_load[" + o + "] = " + l1
						+ ", old_load[" + m.v + "] = " + l2 + ", load[" + o
						+ "] = " + load[o] + ", load[" + m.v + "] = "
						+ load[m.v] + ", eval = " + eval());

				getSolver().getLog().println(
						name() + "::search, Step " + it + " -> move(" + m.i
								+ " q(" + qtt[m.i] + "  from " + o + " -> "
								+ m.v + "), old_load[" + o + "] = " + l1
								+ ", old_load[" + m.v + "] = " + l2 + ", load["
								+ o + "] = " + load[o] + ", load[" + m.v
								+ "] = " + load[m.v] + ", eval = " + eval());
			}
		}

		searchReduceTotalPackingViolations(10000);
		searchAggregateDates(1000);
		searchReduceTotalPackingViolations(10000);
		
		
	}

	private int sumQTT(ArrayList<Integer> I) {
		int t = 0;
		for (int i : I)
			t += qtt[i];
		return t;
	}

	private boolean canMove(ArrayList<Integer> I, int d) {
		for (int i : I)
			if (!acceptDate(i, d))
				return false;
		return true;
	}

	public void searchReduceTotalPackingViolations(int maxIter) {
		ArrayList<AssignMove> moves = new ArrayList<AssignMove>();
		for (int it = 0; it < maxIter; it++) {
			moves.clear();
			int min_delta_violations_packing = Integer.MAX_VALUE;

			for (int i = 0; i < n; i++) {
				for (int j = minDate[i]; j <= maxDate[i]; j++) {
					if (preload[j] == maxLoad)
						continue;// ignore full date (bin)

					int delta_violations_packing = getAssignDeltaPacking(i, j);

					if (min_delta_violations_packing > delta_violations_packing) {
						min_delta_violations_packing = delta_violations_packing;
						moves.clear();
						moves.add(new AssignMove(i, j));
					} else if (min_delta_violations_packing == delta_violations_packing) {
						moves.add(new AssignMove(i, j));
					}
				}
			}

			// perform the move
			if (moves.size() <= 0 || min_delta_violations_packing >= 0) {
				System.out.println(name() + "::search, NO MOVE --> BREAK");
				break;
			} else {
				AssignMove m = moves.get(R.nextInt(moves.size()));

				int o = x[m.i];
				int l1 = load[o];
				int l2 = load[m.v];

				assign(m.i, m.v);

				System.out.println(name()
						+ "::searchReducePackingViolations, Step " + it
						+ " -> move(" + m.i + " q(" + qtt[m.i] + "  from " + o
						+ " -> " + m.v + "), old_load[" + o + "] = " + l1
						+ ", old_load[" + m.v + "] = " + l2 + ", load[" + o
						+ "] = " + load[o] + ", load[" + m.v + "] = "
						+ load[m.v] + ", eval = " + eval() + ", delta = "
						+ min_delta_violations_packing);

				getSolver().getLog().println(
						name() + "::searchReducePackingViolations, Step " + it
								+ " -> move(" + m.i + " q(" + qtt[m.i]
								+ "  from " + o + " -> " + m.v + "), old_load["
								+ o + "] = " + l1 + ", old_load[" + m.v
								+ "] = " + l2 + ", load[" + o + "] = "
								+ load[o] + ", load[" + m.v + "] = "
								+ load[m.v] + ", eval = " + eval()
								+ ", delta = " + min_delta_violations_packing);
			}
		}

	}

	
	
	public void searchAggregateDates(int maxIter) {

		for (int it = 0; it < maxIter; it++) {
			int minDelta = Integer.MAX_VALUE;
			AggregateDatesMove sel_move = null;
			int sel_date = -1;
			for (int d = startDate; d <= endDate; d++) {
				AggregateDatesMove m = evaluateAggregateMove(d);
				//System.out.println(name() + "::searchAggregateDates, d = " + d + ", delta = " + m.getDelta() + ", sz = " + m.getDates().size());
				if (minDelta > m.getDelta()) {
					minDelta = m.getDelta();
					sel_move = m;
					sel_date = d;
					//System.out.println(name() + "::searchAggregateDates, UPDATE sel_date = " + d + 
					//		", minDelta = " + minDelta + ", sel_dates = " + sel_move.getDates().size());
				}
			}
			if(sel_move != null){
				if(minDelta >= 0){
					System.out.println(name() + "::searchAggregateDates, no improvement, BREAK");
					break;
				}else{
					//getSolver().getLog().println(name() + "::searchAggregateDates, sel_date = " + sel_date + 
					//		", load[" + sel_date + "] = " + load[sel_date] + ", dates = ");
					
					for(int d : sel_move.getDates()){
						ArrayList<Integer> I = getItemsOfBin(d);
						//getSolver().getLog().print(" date[" + d + ", load-" + load[d] + "]: ");
						for(int i: I){
							//getSolver().getLog().print("(" + i + ", qtt[" + qtt[i] + "]) ");
							assign(i,sel_date);
						}
						//getSolver().getLog().println();
					}
					
					getSolver().getLog().println(name() + "::searchAggregateDates, iter " + it + ", move aggregate, eval = " + eval() + 
							", load[" + sel_date + "] = " + load[sel_date] + ", delta = " + minDelta + ", dates.sz = " + sel_move.getDates().size());
				
					System.out.println(name() + "::searchAggregateDates, iter " + it + ", move aggregate, eval = " + eval() + 
							", load[" + sel_date + "] = " + load[sel_date] + ", delta = " + minDelta + ", dates.sz = " + sel_move.getDates().size());
				
				}
			}
			
			//break;
		}
	}

	public AggregateDatesMove evaluateAggregateMove(int d) {
		// from date d, try to aggregate fields of adjacent dates di (before and
		// after) of d to d
		// such that violation_packing[di] reduces ai > 0
		// total violations_packing reduces delta
		// return delta
		int loadd = load[d];
		int turn = 0;
		int dl = d - 1;
		int dr = d + 1;
		int reducedViolations = 0;
		maxDeltaDay = 0;
		for (int i = 0; i < getSolver().getInput().getFields().length; i++) {
			int del = getSolver().getInput().getFields()[i].getDeltaDays();
			maxDeltaDay = maxDeltaDay > del ? maxDeltaDay : del;
		}
		ArrayList<Integer> agg_dates = new ArrayList<Integer>();
		boolean hasLeft = true;
		boolean hasRight = true;
		while (hasLeft && hasRight) {
			if (turn % 2 == 0) {// find fields in preceding dates
				if (dl >= d - maxDeltaDay) {
					ArrayList<Integer> I = getItemsOfBin(dl);
					if (canMove(I, d)) {
						int l = sumQTT(I);
						if (l < minLoad && l > 0) {
							loadd += l;
							reducedViolations += (minLoad - l);
							agg_dates.add(dl);
							if (loadd >= minLoad)
								break;
						}
					}
					dl--;
				}else{
					hasLeft = false;
				}
			} else {// find fields in successing dates
				if (dr <= d + maxDeltaDay) {
					ArrayList<Integer> I = getItemsOfBin(dr);
					if (canMove(I, d)) {
						int l = sumQTT(I);
						if (l < minLoad && l > 0) {
							loadd += l;
							reducedViolations += (minLoad - l);
							agg_dates.add(dr);
							if (loadd >= minLoad)
								break;
						}
					}
					dr++;
				}else{
					hasRight = false;
				}
			}
			turn = 1 - turn;
		}

		int vd = violations_packing(loadd);

		int delta = (vd - reducedViolations - violations_packing[d]);
		return new AggregateDatesMove(agg_dates, delta);
	}

	public String eval() {
		return "(" + max_violations_packing + "," + total_violations_packing
				+ "," + total_violations_havest + ")";
	}

	public String name() {
		return "ConstrainedMultiKnapsackSolver";
	}

	public static void main(String[] args) {
		/*
		int[] qtt = new int[] { 3, 7, 9, 2, 6, 4, 4, 8, 5, 10, 3, 12, 6, 6, 7 };
		int minLoad = 10;
		int maxLoad = 18;
		int[] minDate = new int[] { 2, 5, 0, 2, 6, 0, 0, 3, 3, 2, 5, 6, 5, 4, 0 };
		int[] maxDate = new int[] { 4, 7, 1, 5, 8, 2, 3, 6, 6, 4, 7, 8, 8, 6, 3 };
		int[] preload = new int[] { 0, 0, 18, 0, 18, 0, 0, 18, 0 };

		ConstrainedMultiKnapsackSolver solver = new ConstrainedMultiKnapsackSolver(
				null);
		solver.solve(preload, qtt, minDate, maxDate, minLoad, maxLoad);
		*/
		int[] q = new int[]{30, 5, 45, 20, 10, 35};
		int maxQ = 130;
		ConstrainedMultiKnapsackSolver S = new ConstrainedMultiKnapsackSolver(null);
		double[] sq = S.selectQuantity(q, 100, maxQ);
		for(int i = 0; i < sq.length; i++)
			System.out.println(sq[i] + " ");
	}
}
