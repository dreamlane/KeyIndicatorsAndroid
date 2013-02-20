package com.blanksketchstudios.keyindicators;

import java.io.IOException;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.blanksketchstudios.keyindicators.Model.DataMaster;
import com.blanksketchstudios.keyindicators.Model.Indicator;
import com.blanksketchstudios.keyindicators.Model.SaveIndicatorTask;

public class CategoryListView extends FragmentActivity implements ActionBar.OnNavigationListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private DataMaster dataMaster;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(KIGlobals.LogTag,"On Create");
        setContentView(R.layout.activity_category_list_view);

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        getActionBarThemedContextCompat(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[] {
                                getString(R.string.title_section1),
                                getString(R.string.title_section2),
                                getString(R.string.title_section3),
                        }),
                this);
        //Get a handle on the DataMaster
        try {
        dataMaster = DataMaster.getDataMasterInstance(this);
        Log.d(KIGlobals.LogTag,"Got this far");
        } catch(IOException e){
        	//fail gracefully
        	Log.e(KIGlobals.LogTag,"Caught IOException in CategoryListView onCreate");
        }
        //Let's make sure we've actually got an indicator created, if not show a tutorial image
        if (dataMaster.getNumberOfIndicators() == 0){
        	//Display the tutorial image, and skip everything else by returning from this method
        	/*TODO: Display tutorial image here */
        	Log.d(KIGlobals.LogTag,"No indicators");
        	return;
        }
        Log.d(KIGlobals.LogTag,"Got this far");
        //Populate the Category list view with each category
        ListView categoryListView = (ListView)findViewById(R.id.categoryListView);
        ArrayAdapter<String> categoryListViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, dataMaster.getCategories());
        categoryListView.setAdapter(categoryListViewAdapter);
        Log.d(KIGlobals.LogTag,"Got this far");
    }
    /**
     * The button callback
     */
    public void testSave(View view){
    	Log.d(KIGlobals.LogTag, "in TestSave");
    	Indicator newIndicator = dataMaster.createNewIndicator("None");
    	newIndicator.save(this);
    }
    /**
     * Backward-compatible version of {@link ActionBar#getThemedContext()} that
     * simply returns the {@link android.app.Activity} if
     * <code>getThemedContext</code> is unavailable.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Context getActionBarThemedContextCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return getActionBar().getThemedContext();
        } else {
            return this;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.category_list_view, menu);
        return true;
    }
    
    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        Fragment fragment = new DummySectionFragment();
        Bundle args = new Bundle();
        args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        return true;
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_category_list_view_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

}
