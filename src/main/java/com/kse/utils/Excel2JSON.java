package com.kse.utils;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kse.ezRoutingAPI.deliverygoods.model.DeliveryGoodInput;
import com.kse.ezRoutingAPI.deliverygoods.model.DeliveryRequest;
import com.kse.ezRoutingAPI.deliverygoods.model.Shipper;
import com.kse.ezRoutingAPI.deliverygoods.model.Store;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiInput;
import com.kse.ezRoutingAPI.dichung.model.SharedTaxiRequest;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.PickupDeliveryInput;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.PickupDeliveryRequest;
import com.kse.ezRoutingAPI.pickupdeliverycontainers.model.Truck;


public class Excel2JSON {
	
	public static String getStringValue(Cell c){
		String val = "";
		switch(c.getCellType()){
		case Cell.CELL_TYPE_BOOLEAN:
			val = c.getBooleanCellValue() + "";
			break;
		case Cell.CELL_TYPE_STRING:
			val = c.getStringCellValue();
			break;
		case Cell.CELL_TYPE_NUMERIC:
			val = c.getNumericCellValue() + "";
			break;
	}

		return val;
	}
	
	public static String excel2JSONdichung(String fn){
		try{
			
			HSSFWorkbook wb = null;
			File f = new File(fn);
			wb = new HSSFWorkbook(new FileInputStream(f));
			HSSFSheet sheet = wb.getSheetAt(1);
			Iterator<Row> rows = sheet.iterator();
			ArrayList<SharedTaxiRequest> requests = new ArrayList<SharedTaxiRequest>();
			rows.next();
			int DT = 900;// 15 minutes
			while(rows.hasNext()){
				Row r = rows.next();
				
				Cell c = r.getCell(0);
				
				String ticketCode = getStringValue(c);
				c = r.getCell(1);
				String dt = getStringValue(c);
				c = r.getCell(3);
				String addr = getStringValue(c);
				String[] s = dt.split(" ");
				String[] s1 = s[0].split(":");
				String hour = s1[0];
				String minute = s1[1];
				s1 = s[1].split("/");
				String dd = s1[0];
				String month = s1[1];
				String year = s1[2];
				
				String stdDT = year + "-" + month + "-" + dd + " " + hour + ":" + minute + ":" + "00";
				long udt = DateTimeUtils.dateTime2Int(stdDT);
				String earlyPickup = DateTimeUtils.unixTimeStamp2DateTime(udt - DT);
				String latePickup = DateTimeUtils.unixTimeStamp2DateTime(udt + DT);
				String deliveryAddr = "Noi Bai International Airport, Phu Cuong, Hanoi, Vietnam";
				
				String nbPassengers = getStringValue(r.getCell(5));
				
				System.out.println(ticketCode + "\t" + dt + "\t" + stdDT + "\t" + earlyPickup + "\t" + 
				latePickup + "\t" + addr + "\t" + nbPassengers);
				
				SharedTaxiRequest req = new SharedTaxiRequest(ticketCode,addr,earlyPickup,latePickup,
						deliveryAddr,"-",(int)Math.floor(Double.valueOf(nbPassengers)));
				
				requests.add(req);
				
				/*
				Iterator<Cell> cells = r.iterator();
				while(cells.hasNext()){
					Cell c = cells.next();
					String val = "-";
					switch(c.getCellType()){
						case Cell.CELL_TYPE_BOOLEAN:
							val = c.getBooleanCellValue() + "";
							break;
						case Cell.CELL_TYPE_STRING:
							val = c.getStringCellValue();
							break;
						case Cell.CELL_TYPE_NUMERIC:
							val = c.getNumericCellValue() + "";
							break;
					}
					System.out.print(val + "\t");
				}
				System.out.println();
				*/
			}
			
			/*
			String json = "{\n";
			json += "\"requests\":";
			for(int i = 0; i < requests.size(); i++){
				SharedTaxiRequest req = requests.get(i);
			}
			json += "}";
			*/
			
			SharedTaxiRequest[] R = new SharedTaxiRequest[requests.size()];
			for(int i = 0; i < requests.size(); i++) 
				R[i] = requests.get(i);
			int[] cap = new int[]{4,6};
			SharedTaxiInput input = new SharedTaxiInput(R, cap, 900, 10000, 3600, 5);
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(input);
			return json;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

	public static String excel2JSONPickupDeliveryContainer(String fn){
		try{
			
			XSSFWorkbook wb = null;
			File f = new File(fn);
			wb = new XSSFWorkbook(new FileInputStream(f));
			
			
			// read requests
			XSSFSheet sheet = wb.getSheetAt(0);
			Iterator<Row> rows = sheet.iterator();
			ArrayList<DeliveryRequest> requests = new ArrayList<DeliveryRequest>();
			rows.next();
			
			while(rows.hasNext()){
				Row r = rows.next();
				
				Cell c = r.getCell(1);
				
				String requestCode = getStringValue(c);
				//System.out.println(requestCode);
				
				c = r.getCell(2);
				String deliveryAddr = getStringValue(c);
				
				c = r.getCell(3);
				String deliveryLatLng = getStringValue(c);
				
				c = r.getCell(4);
				String earlyDeliveryDateTime = getStringValue(c);
				
				c = r.getCell(5);
				String lateDeliveryDateTime = getStringValue(c);
				
				c = r.getCell(6);
				String sWeight = getStringValue(c);
				
				c = r.getCell(7);
				String sVolumn = getStringValue(c);
				
				double weight = Double.valueOf(sWeight);
				double volumn = Double.valueOf(sVolumn);
				
				DeliveryRequest req = new DeliveryRequest(requestCode, deliveryAddr, deliveryLatLng,
						earlyDeliveryDateTime, lateDeliveryDateTime, weight, volumn);
				
				requests.add(req);
				
			}
			

			// read store
			sheet = wb.getSheetAt(1);
			rows = sheet.iterator();
			ArrayList<Store> stores = new ArrayList<Store>();
			rows.next();
			while(rows.hasNext()){
				Row r = rows.next();
				
				Cell c = r.getCell(1);
				String storeCode = getStringValue(c);
				
				
				c = r.getCell(2);
				String name = getStringValue(c);
				
				c = r.getCell(3);
				String address = getStringValue(c);
				
				c = r.getCell(4);
				String currentLatLng = getStringValue(c);
				
				Store store = new Store(storeCode,name,address,currentLatLng);
				
				stores.add(store);
				
			}
			
			
			//read shippers
			sheet = wb.getSheetAt(2);
			rows = sheet.iterator();
			ArrayList<Shipper> shippers = new ArrayList<Shipper>();
			rows.next();
			while(rows.hasNext()){
				Row r = rows.next();
				
				Cell c = r.getCell(1);
				String shipperCode = getStringValue(c);
				
				
				c = r.getCell(2);
				String name = getStringValue(c);
				
		
				c = r.getCell(3);
				String currentLatLng = getStringValue(c);
				
				c = r.getCell(4);
				String sWeight = getStringValue(c);
				double weight = Double.valueOf(sWeight);
				
				c = r.getCell(5);
				String sVolumn = getStringValue(c);
				double volumn = Double.valueOf(sVolumn);
				
				Shipper shipper = new Shipper(shipperCode, name, currentLatLng, weight, volumn);
				
				shippers.add(shipper);
				
			}
			
			
			
			DeliveryRequest[] R = new DeliveryRequest[requests.size()];
			for(int i = 0; i < requests.size(); i++)
				R[i] = requests.get(i);
			
			Store[] St = new Store[stores.size()];
			for(int i = 0; i < stores.size(); i++)
				St[i] = stores.get(i);
			
			Shipper[] sh = new Shipper[shippers.size()];
			for(int i=0; i < shippers.size(); i++)
				sh[i] = shippers.get(i);
			
			DeliveryGoodInput input = new DeliveryGoodInput(R, St[0], sh);
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(input);
			return json;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

	public static String excel2JSONDeliveryGoods(String fn){
		try{
			
			XSSFWorkbook wb = null;
			File f = new File(fn);
			wb = new XSSFWorkbook(new FileInputStream(f));
			
			
			// read requests
			XSSFSheet sheet = wb.getSheetAt(0);
			Iterator<Row> rows = sheet.iterator();
			ArrayList<PickupDeliveryRequest> requests = new ArrayList<PickupDeliveryRequest>();
			rows.next();
			
			while(rows.hasNext()){
				Row r = rows.next();
				
				Cell c = r.getCell(1);
				
				String requestCode = getStringValue(c);
				//System.out.println(requestCode);
				
				c = r.getCell(2);
				String pickupAddr = getStringValue(c);
				
				c = r.getCell(3);
				String pickupLatLng = getStringValue(c);
				
				c = r.getCell(4);
				String pickupDateTime = getStringValue(c);
				
				c = r.getCell(6);
				String deliveryAddr = getStringValue(c);
				
				c = r.getCell(7);
				String deliveryLatLng = getStringValue(c);
				
				c = r.getCell(8);
				String deliveryDateTime = getStringValue(c);
				
				c = r.getCell(10);
				String sc = getStringValue(c);
				//System.out.println(sc);
				int quantity = (int)(Math.floor(Double.valueOf(sc)));
				
				PickupDeliveryRequest req = new PickupDeliveryRequest(requestCode, pickupAddr, pickupLatLng, pickupDateTime, deliveryAddr, deliveryLatLng, deliveryDateTime, quantity);
				
				requests.add(req);
				
			}
			

			// read trucks
			sheet = wb.getSheetAt(1);
			rows = sheet.iterator();
			ArrayList<Truck> trucks = new ArrayList<Truck>();
			rows.next();
			while(rows.hasNext()){
				Row r = rows.next();
				
				Cell c = r.getCell(1);
				String truckCode = getStringValue(c);
				
				
				c = r.getCell(2);
				int capacity = (int)Math.floor(Double.valueOf(getStringValue(c)));
				
				c = r.getCell(3);
				String driver = getStringValue(c);
				
				c = r.getCell(4);
				String currentLatLng = getStringValue(c);
				
				c = r.getCell(5);
				String depotLatLng = getStringValue(c);
				
				Truck truck = new Truck(truckCode,capacity,driver,currentLatLng,depotLatLng);
				
				trucks.add(truck);
				
			}
			
			
			PickupDeliveryRequest[] R = new PickupDeliveryRequest[requests.size()];
			for(int i = 0; i < requests.size(); i++)
				R[i] = requests.get(i);
			
			Truck[] T = new Truck[trucks.size()];
			for(int i = 0; i < trucks.size(); i++) 
				T[i] = trucks.get(i);
			
			
			PickupDeliveryInput input = new PickupDeliveryInput(R, T);
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(input);
			return json;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args){
		//String json = excel2JSONdichung("C:/DungPQ/projects/ezRoutingAPI/Export_08092016_2999.xls");
		
		//String json = excel2JSONPickupDeliveryContainer("C:/DungPQ/projects/ezRoutingAPI/pickup-delivery-container.xlsx");
		
		String json = excel2JSONPickupDeliveryContainer("C:/DungPQ/projects/ezRoutingAPI/delivery-input-template.xlsx");
		
		System.out.println(json);
		
		
	}
}
