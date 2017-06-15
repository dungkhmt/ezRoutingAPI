package com.dailyopt.havestplanning.solver;

import java.util.Date;

import com.dailyopt.havestplanning.model.Field;

public class MField extends Field {
	private Date mDate;

	public Date getmDate() {
		return mDate;
	}

	public void setmDate(Date mDate) {
		this.mDate = mDate;
		
	}
	
	public MField(String code, String districtCode, String ownerCode,
			double area, String date, int quantity, int deltaDays, Date mDate){
		super(code,districtCode,ownerCode,area,date,quantity,deltaDays);
		this.mDate = mDate;
	}
}
