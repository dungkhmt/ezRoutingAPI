package com.kse.utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LOGGER {
	
	public static final Logger LOGGER = Logger.getLogger("Logger");

	private String path;
	
	private Handler fileHandler;
	private Formatter simFormatter;
	
	
	public LOGGER(String path){
		this.path = path;
	}
	
	public void open(){
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Date date = new Date();
		
		try {
			fileHandler = new FileHandler(path+dateFormat.format(date)+".txt");
			simFormatter = new SimpleFormatter();
			
			LOGGER.addHandler(fileHandler);
			fileHandler.setFormatter(simFormatter);
			
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close(){
		fileHandler.close();
	}
}
