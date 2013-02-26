/*
 * @author: Benjamin Johnson
 * @since: Feb 2013
 * 
 * This class is the key component in the data model for Key Indicators.
 * It is used to represent the data for each of the indicators that the
 *    user creates in the app.
 * For this app, we are minimizing the amount of data recorded. Instead of
 *    recording time segments, we are just recording a running total for each
 *    week, month, and all time. This minimizes space usage for data that is not needed.
 * For this app, we are minimizing the necessary number of calculations by recording running totals.
 * 
 * The files that represent each indicator are plaintext and simple. They follow the folowing format:
 * <id>
 * <name>
 * <isTally(true,false)>
 * <isArchived(true,false)>
 * <isClockedIn(true,false)>
 * <clockInTime>(long)
 * <category>
 * <firstDayOfWeek(mm/dd/yyyy)> <actual> <goal>
 * .
 * .
 * .
 * ENDWEEKS
 * <month(mm/yyyy)> <actual> <goal>
 * .
 * .
 * .
 * ENDMONTHS
 * <allTimeTotal>(int)
 */
package com.blanksketchstudios.keyindicators.Model;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.blanksketchstudios.keyindicators.KIGlobals;
import com.blanksketchstudios.keyindicators.Model.*;

public class Indicator {
	private int indicatorID;
	private String name;
	private boolean isTally;
	private boolean isArchived;
	//Another option is the isInverse (think gaming, smoking) option.
	private boolean isClockedIn;
	private long clockInTime;
	private String category;
	//In the following hash maps, the integer array is a 2 element array, where the first is the actual, and the second is the goal
	private HashMap<String,Integer[]> weeks; //Here the String is mm/dd/yyyy (first of the week)
	private HashMap<String,Integer[]> months; //Here the String is mm/yyyy (the month)
	private int allTimeTotal;
	//At this point in time, I will only write setters and getters for the fields that I need them for..
	//I'll need all getters, and most setters eventually
	
	
	/**
	 * The complete constructor takes all arguments for all features.
	 */
	public Indicator(int id, String name, boolean tally, boolean archived, boolean clockedIn, 
						long clockInTime, String category, HashMap<String,Integer[]> weeks, 
						HashMap<String,Integer[]> months, int allTimeTotal )
	{
		this.indicatorID = id;
		this.name = name;
		this.isTally = tally;
		this.isArchived = archived;
		//Another option is the isInverse (think gaming, smoking) option.
		this.isClockedIn = clockedIn;
		this.clockInTime = clockInTime;
		this.category = category;
		this.weeks = weeks;
		this.months = months;
		this.allTimeTotal = allTimeTotal;
	}
	
	/**
	 * This constructor should only be called when creating a new indicator
	 * @param id : The new id for this indicator
	 * @param category : The new category for this indicator
	 */
	public Indicator(int id, String category){
		this.indicatorID = id;
		this.name = "New Indicator";
		this.isTally = false;
		this.isArchived = false;
		//Another option is the isInverse (think gaming, smoking) option.
		this.isClockedIn = false;
		this.clockInTime = 0;
		this.category = category;
		this.weeks = new HashMap<String,Integer[]>();
		this.months = new HashMap<String,Integer[]>();
		this.allTimeTotal = 0;
	}
	
	/* Data Storage Methods */
	/*
	 * This method should only be called by SaveIndicatorTask.java
	 * This method writes the Indicator data to a file named <id>.dat in the Android internal filesystem
	 */
	public boolean save(Context ctx){
		Log.d(KIGlobals.LogTag, "in save");
		try {
		FileOutputStream fos = ctx.openFileOutput(""+this.indicatorID+".dat", Context.MODE_PRIVATE);
		
		//Build the output string that will be written to the file
		StringBuilder outString = new StringBuilder();
		//First line is always the id
		outString.append(String.format("%d\n",this.indicatorID));
		//Second line is always the name
		outString.append(String.format("%s\n",this.name));
		//Third line is always the isTally boolean (there is probably a string format char for this... %b maybe,
		if (isTally){outString.append(String.format("true\n"));}
		else {outString.append(String.format("false\n"));}
		//Fourth is the isArchived boolean
		if (isArchived){outString.append(String.format("true\n"));}
		else {outString.append(String.format("false\n"));}
		//Fifth is the isClockedIn boolean
		if (isClockedIn){outString.append(String.format("true\n"));}
		else {outString.append(String.format("false\n"));}
		//Sixth is the clock in time (in milliseconds since reference)
		outString.append(String.format("%d\n",this.clockInTime));
		//Seventh line is the category
		outString.append(String.format("%s\n",this.category));
		//The next set of lines are the lines that represent the weekly data CURRENTLY UNORDERED
		for (Map.Entry<String, Integer[]> entry : weeks.entrySet()){
			outString.append(String.format("%s %d %d\n",entry.getKey(),entry.getValue()[0],entry.getValue()[1]));
		}
		//Now we put the end delimiter for this sections
		outString.append(String.format("ENDWEEKS\n"));
		//The next set of lines are the lines that represent the monthly data CURRENTLY UNORDERED
		for (Map.Entry<String, Integer[]> entry : months.entrySet()){
			outString.append(String.format("%s %d %d\n",entry.getKey(),entry.getValue()[0],entry.getValue()[1]));
		}
		//Now we delimit this data
		outString.append(String.format("ENDMONTHS\n"));
		//The final line contains the alltime total recorded for this indicator
		outString.append(String.format("%d\n", this.allTimeTotal));
		
		//Now we write and close the Indicator file
		fos.write(outString.toString().getBytes());
		fos.close();
		
		//Set the data master to refresh
		DataMaster dm = DataMaster.getDataMasterInstance(ctx);
		dm.setNeedsRefresh(true);
		}catch(Exception e){
			//fail gracefully
			Log.d(KIGlobals.LogTag,"FAILED");
			return false;
		}
		
		return true;
		
	}
	
	/* Setters and Getters */
	public int getIndicatorID(){
		return indicatorID;
	}
	public String getName(){
		return name;
	}
	public String getCategory(){
		return category;
	}
	public boolean isTally(){
		return isTally;
	}
	public boolean isArchived(){
		return isArchived;
	}
	public boolean isClockedIn(){
		return isClockedIn;
	}
	public long getClockInTime(){
		return clockInTime;
	}
	//Maybe i don't want to do this, but for now it works
	public HashMap<String,Integer[]> getWeeks(){
		return weeks;
	}
	public HashMap<String,Integer[]> getMonths(){
		return months;
	}
	public int getAllTimeTotal(){
		return allTimeTotal;
	}
	//setters
}
