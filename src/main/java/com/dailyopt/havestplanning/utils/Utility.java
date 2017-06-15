package com.dailyopt.havestplanning.utils;

import java.util.Calendar;
import java.util.Date;

import com.dailyopt.havestplanning.model.QualityFunction;

public class Utility {
	
	public static double eval(QualityFunction f, int days_late){
		days_late = Math.abs(days_late);
		double d = f.getTop() - f.getBottom();
		int dx = f.getLength() - days_late;
		double dy = (dx*d*1.0)/f.getLength();
		
		return dy + f.getBottom();
	}
	public static Date next(Date d){
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DATE, 1);
		return cal.getTime();
	}

	public static Date next(Date d, int nbDays){
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DATE, nbDays);
		return cal.getTime();
	}
	public static int distance(Date d1, Date d2){
		int d = 0;
		if(d1.compareTo(d2) > 0){
			Date tmpd = d2;
			while(!tmpd.equals(d1)){
				tmpd = Utility.next(tmpd,1);
				d++;
			}
			d = -d;
		}else{
			Date tmpd = d1;
			while(!tmpd.equals(d2)){
				tmpd = Utility.next(tmpd,1);
				d++;
			}
		}
		return d;
	}
	public static String dateMonthYear(Date d){
		return d.getDate() + "-" + (d.getMonth() + 1) + "-" + d.getYear();
	}
}
