package com.kse.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {
	public static long dateTime2Int(String dt){
		// convert datetime to int (seconds), datetime example is 2016-10-04 10:30:15
		/*
		String[] s = dt.split(" ");
		String[] d = s[0].split("-");
		int year = Integer.valueOf(d[0]);
		int month = Integer.valueOf(d[1]);
		int day = Integer.valueOf(d[2]);
		String[] t = s[1].split(":");
		int hour = Integer.valueOf(t[0]);
		int minute = Integer.valueOf(t[1]);
		int second = Integer.valueOf(t[2]);
		*/
		try{
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = dateFormat.parse(dt);
			return date.getTime()/1000;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return 0;
	}
	public static String unixTimeStamp2DateTime(long dt){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//dt is measured in seconds and must be converted into milliseconds
		return dateFormat.format(dt*1000);
	}
	public static void main(String[] args){
		long t1 = (long)DateTimeUtils.dateTime2Int("2016-10-04 10:30:15");
		long t2 = (long)DateTimeUtils.dateTime2Int("2016-10-04 10:31:10");
		long t = t1 - t2;
		String dt1 = DateTimeUtils.unixTimeStamp2DateTime(t1);
		
		System.out.println("t1 = " + t1 + ", t2 = " + t2 + ", t = " + t + ", dt1 = " + dt1);
	}
}
