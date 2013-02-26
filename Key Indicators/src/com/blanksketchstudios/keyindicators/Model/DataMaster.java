/*
 * @author: Benjamin Johnson
 * @since: Feb 2013
 * 
 * This class is the liaison between the controllers and the model.
 * This class is a singleton class that gets instantiated when the application loads.
 * It keeps an up to date representation of all of the indicators.
 */
package com.blanksketchstudios.keyindicators.Model;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.blanksketchstudios.keyindicators.KIGlobals;

public class DataMaster {
	private static DataMaster dataMasterInstance = null; //this is essential to our singleton pattern
	private boolean needsRefresh = true;
	private HashMap<Integer,Indicator> indicators; //{indicatorID:indicator}
	private ArrayList<String> categories;
	private Context context;
	private int numberOfIndicators = 0;
	
	protected DataMaster(Context ctx) throws IOException{
		//This constructor is only ever called from the getDataMasterInstance method
		
		//The context of the single instance should be set at this point, and changed whenever the getDataMasterInstance is called?
		this.context = ctx;
		this.needsRefresh = false;
		//When the instance is first created, it needs to load all of the indicators from file.
		
		//The list of indicators is kept in a file called idList.dat
		ArrayList<Integer> IDs = new ArrayList<Integer>();
		indicators = new HashMap<Integer,Indicator>(); //Use a sparse array because we are using Integer as the key. This could be a fun experiment
		categories = new ArrayList<String>();
		try {
		String filename = KIGlobals.idListFilename;
		InputStream stream = ctx.openFileInput(filename);
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		//Now read the file line to get a list of all of the indicatorIDs
		String[] ids = in.readLine().split(" ");
		for (String id : ids){
			IDs.add(Integer.parseInt(id));
		}
		in.close();
		//Now we've got all of the IDs of the indicators. Time to load them into indicators hashmap
		 String name;
		 boolean isTally;
		 boolean isArchived;
		 boolean isClockedIn;
		 long clockInTime;
		 String category;
		 HashMap<String,Integer[]> weeks = new HashMap<String,Integer[]>(); //Here the String is mm/dd/yyyy (first of the week)
		 HashMap<String,Integer[]> months = new HashMap<String,Integer[]>(); //Here the String is mm/yyyy (the month)
		 int allTimeTotal;
		 
		 //For each indicator, load it into memory from file.
		 for (Integer id : IDs){
			//open the file
			Log.d(KIGlobals.LogTag,String.format("Reading data for indicator: %d",id));
			filename = ""+id+".dat";
			stream = ctx.openFileInput(filename);
			in = new BufferedReader(new InputStreamReader(stream));
			//Now read the file into the data structure line by line (see Indicator.java for the format of the file)
			in.readLine(); //This line holds the ID of the indicator, but we already know that (id). We keep it in the file for added human readability
			name = in.readLine();
			isTally = Boolean.parseBoolean(in.readLine());
			isArchived = Boolean.parseBoolean(in.readLine());
			isClockedIn = Boolean.parseBoolean(in.readLine());
			clockInTime = Long.parseLong(in.readLine());
			//At this line we get the category of the indicator, this is a good time to load the categories array up
			category = in.readLine();
			if (!categories.contains(category)){
				categories.add(category);
			}
			String line = in.readLine();
			//Get all of the recorded week values for this indicator, as well as months.
			while (!line.equals("ENDWEEKS")){
				String ins[] = line.split(" ");
				Integer[] value = new Integer[2];
				value[0] = Integer.parseInt(ins[1]);
				value[1] = Integer.parseInt(ins[2]);
				weeks.put(ins[0], value);
				line = in.readLine();
			}
			line = in.readLine();
			while (!line.equals("ENDMONTHS")){
				Log.d(KIGlobals.LogTag,line);
				String ins[] = line.split(" ");
				Integer[] value = new Integer[2];
				value[0] = Integer.parseInt(ins[1]);
				value[1] = Integer.parseInt(ins[2]);
				months.put(ins[0], value);
				line = in.readLine();
			}
			allTimeTotal = Integer.parseInt(in.readLine());
			Log.d(KIGlobals.LogTag,"have all data for an indicator constructor");
			//Now that we have all of the data, create the indicator and add it to the indicators list
			Indicator newInd = new Indicator(id, name, isTally, isArchived, isClockedIn, clockInTime, category, weeks, months, allTimeTotal);
			indicators.put(id, newInd);
			numberOfIndicators++;
			in.close();
			Log.d(KIGlobals.LogTag,"indicator added, and stream closed");
		}
		} catch(FileNotFoundException e){
			//Fail gracefully?
			Log.e(KIGlobals.LogTag,"FNF in construct");
			return;
		}
		
	}
	/**
	 * When this method is called, either the already instantiated instance will return, 
	 *    or a new one will be created.
	 * When this is initially called, it could take some time to execute, so surround calls to this method with 
	 *    a progressHUD.
	 */
	public static DataMaster getDataMasterInstance(Context ctx)throws IOException{
		//If we haven't yet made the singleton instance, make it
		//Also, if the data has changed, reload it (time consideration?)
		if (dataMasterInstance==null || dataMasterInstance.needsRefresh()){
			dataMasterInstance = new DataMaster(ctx);
		}
		dataMasterInstance.context = ctx;
		return dataMasterInstance;
	}
	
	/**
	 * Creates a new default indicator, and saves its file to disk, also updating the idList.dat file.
	 * @param category : The category
	 * @return : a newly created Indicator, or null if there was an error
	 */
	public Indicator createNewIndicator(String category){
		
		//First we need to figure out what the next id should be.
		int nextID;
		try {
			InputStream stream = this.context.openFileInput(KIGlobals.idListFilename);
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			
			/* If we have reached this point in execution, then the idList.dat file exists and is populated by id numbers */
			//Read the file to determine what the next id is, which is always the last id+1
			String[] ids = in.readLine().split(" ");
			nextID = Integer.parseInt(ids[ids.length-1])+1;
			//we got what we came for, now lets close the file
			in.close();
			stream.close();
			
			//reopen the file for writing
			FileOutputStream fos = context.openFileOutput(KIGlobals.idListFilename, Context.MODE_PRIVATE);
			StringBuilder idString = new StringBuilder();
			for (String id : ids){
				idString.append(String.format("%s ",id));
			}
			idString.append(String.format("%d\n",nextID));
			fos.write(idString.toString().getBytes());
			fos.close();
			return new Indicator(nextID, category);
			
		}catch (FileNotFoundException e){
			//if the file was not found, then we should make it.
			Log.d(KIGlobals.LogTag,"File Not Found");
			try {
			OutputStream stream = this.context.openFileOutput(KIGlobals.idListFilename,Context.MODE_PRIVATE);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(stream));
			out.append(String.format("1\n"));
			out.flush();
			out.close();
			nextID = 1;
			}catch (Exception e2){
				Log.e(KIGlobals.LogTag,e2.getMessage());
				return null;
			}
			//Return the new indicator with an ID of 1, and the category from whence this method was called
			return new Indicator(nextID, category);
		}
		catch (IOException ioe){
			Log.e(KIGlobals.LogTag,ioe.getMessage());
			return null;
		}
	}
	/* setters */
	
	public void setNeedsRefresh(boolean needsRefresh){
		this.needsRefresh = needsRefresh;
	}
	/* getters */
	public HashMap<Integer,Indicator> getIndicators(){
		return indicators;
	}
	public ArrayList<String> getCategories(){
		return categories;
	}
	public int getNumberOfIndicators(){
		return numberOfIndicators;
	}
	public boolean needsRefresh(){
		return this.needsRefresh;
	}
}
