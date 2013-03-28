package com.bocai.model;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class GoogleAddress {

	private String country;
	private String state;
	private String city;
	private String street;
	private String streetNum;
	private String route;
	private String district;
	private String formattedAddress;
	
	public String getStreetNum() {
		return streetNum;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}
	
	public void setStreetNum(String streetNum) {
		this.streetNum = streetNum;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}


	public String getCountry() {
		return country;
	}
	
	public String getFormattedAddress() {
		return formattedAddress;
	}

	public String getState() {
		return state;
	}

	public String getCity() {
		return city;
	}

	public String getStreet() {
		if(street == null || street.equals("")){
			StringBuffer sb = new StringBuffer();
			sb.append(district);
			sb.append(route);
			sb.append(streetNum);
			street = sb.toString();
		}
		return street;
	}	
	
	public GoogleAddress(JSONObject jsonObject){
	
		Log.i("GoogleAddress", "Constructor===" + jsonObject.toString());
		
		try {
			formattedAddress = jsonObject.getString("formatted_address");
			JSONArray array = jsonObject.getJSONArray("address_components");
			for(int i=0;i < array.length();i++){
				JSONObject jsonAddr = array.getJSONObject(i);
				String longName = jsonAddr.optString("long_name");
				JSONArray types = jsonAddr.optJSONArray("types");
				if(contain(types,"street_number")){
					streetNum = longName;
				}else if(contain(types,"route")){
					route = longName;
				}else if(contain(types,"sublocality")){
					district = longName;
				}else if (contain(types,"locality")){
					city = longName;
				}else if (contain(types,"administrative_area_level_1")){
					state = longName;
				}else  if(contain(types,"country")){
					country = longName;
				}else{
					continue;
				}
				
			}
		   
		 
		} catch (Exception e) {
			Log.i("Address", e.getMessage(),e);
		}
		
		
	}
	
	private boolean contain(JSONArray array,String item){
		
		if(array == null || array.length() ==0){
			return false;
		}
	
		
		for(int i=0;i < array.length();i++){
			if(item.equals(array.optString(i))){
				return true;
			}
		}
		
		return false;
	}
	
	
	
}
