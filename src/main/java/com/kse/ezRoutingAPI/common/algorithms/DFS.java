package com.kse.ezRoutingAPI.common.algorithms;

import java.util.ArrayList;

public class DFS {
	private int[][] A;
	private int n;
	private int[] p;
		
	private void TRY(int i, ArrayList<Integer> cc){
		cc.add(i);
		for(int j = 0; j < n; j++)if(A[i][j] > 0){
			if(p[j] == -1){
				p[j] = i;
				TRY(j,cc);
			}
		}
	}
	
	public ArrayList<ArrayList<Integer>> computeConnectedComponents(int[][] A){
		this.A = A;
		this.n = A.length;
		p = new int[n];
		for(int i = 0; i < n; i++) p[i] = -1;
		ArrayList<ArrayList<Integer>> CC = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < n; i++)if(p[i] == -1){
			ArrayList<Integer> cc = new ArrayList<Integer>();
			p[i] = i;
			TRY(i,cc);
			CC.add(cc);
		}
		return CC;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
