
/*
 * @author: Benjamin Johnson
 * @since: Feb 2013
 * 
 * This class used to display a loading HUD while performing operations that might take awhile
 * 
 * We might want to update it to return a Boolean
 */
package com.blanksketchstudios.keyindicators.Model;
import com.blanksketchstudios.keyindicators.KIGlobals;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class SaveIndicatorTask extends AsyncTask<Indicator, Void, Boolean> {

	private Activity activity;
    private ProgressDialog progressDialog;

    public SaveIndicatorTask(Activity activity) {
        super();
        this.activity = activity;
    }
	@Override
	protected Boolean doInBackground(Indicator... indicators) {
		// This is where we call .save on the indicator
		Log.d(KIGlobals.LogTag, "in doInBackground");
		return indicators[0].save(this.activity);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 * This method will be used to display the progressDialog
	 */
	 @Override
     protected void onPreExecute() {
     super.onPreExecute();
         progressDialog = ProgressDialog.show(activity, "Saving", "Saving", true);//This may need to be tweeked
     }
	 
	   @Override
       protected void onPostExecute(Boolean result) {
           progressDialog.dismiss();
       }

}
