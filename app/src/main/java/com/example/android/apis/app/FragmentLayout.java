/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.app;

import com.example.android.apis.R;
import com.example.android.apis.Shakespeare;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Demonstration of using fragments to implement different activity layouts.
 * This sample provides a different layout (and activity flow) when run in
 * landscape. It crashes as it was in landscape mode because of a reference
 * to the non-existent containerViewId R.id.a_item in the call at line 156:
 * <p>
 * (FragmentTransaction) ft.replace(R.id.a_item, details)
 * <p>
 * This was obviously added by a runaway modification script, and the container
 * id should be R.id.details
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentLayout extends Activity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * onCreate, then we set our content view to our layout file R.layout.fragment_layout which is
     * either layout-land/fragment_layout.xml or layout/fragment_layout depending on orientation.
     *
     * @param savedInstanceState we do not use
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_layout);
    }

    /**
     * This is a secondary activity, to show what the user has selected
     * when the screen is not large enough to show it all in one activity.
     */
    public static class DetailsActivity extends Activity {

        /**
         * Called when the activity is starting. First we call through to our super's implementation
         * of onCreate. Then using a Resources instance for the application's package we get the
         * current configuration that is in effect for this resource object and check whether the
         * orientation of the screen given by the field Configuration.orientation is currently set
         * to Configuration.ORIENTATION_LANDSCAPE in which case this Activity is not needed since
         * a dual pane version is in use, so we finish this Activity and return to caller. Otherwise
         * we are in Configuration.ORIENTATION_PORTRAIT and are needed. If we are being recreated
         * <b>savedInstanceState</b> is not null and the system will have taken care of restoring our
         * Fragment so we are done. If it is null this is the first time and we need to add a new
         * instance of our Fragment. To do this we first create a new instance of the Fragment
         * <b>DetailsFragment details</b>, set its arguments to a map of the extras added to the
         * Intent which launched this Activity, and finally using the FragmentManager for interacting
         * with fragments associated with this activity we create a FragmentTransaction, use it to
         * add <b>details</b> to the activity state, and then commit that FragmentTransaction.
         *
         * @param savedInstanceState if null, first time initializations are needed
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE) {
                // If the screen is now in landscape mode, we can show the
                // dialog in-line with the list so we don't need this activity.
                finish();
                return;
            }

            if (savedInstanceState == null) {
                // During initial setup, plug in the details fragment.
                DetailsFragment details = new DetailsFragment();
                details.setArguments(getIntent().getExtras());
                getFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
            }
        }
    }

    /**
     * This is the "top-level" fragment, showing a list of items that the
     * user can pick.  Upon picking an item, it takes care of displaying the
     * data to the user as appropriate based on the current UI layout.
     */
    public static class TitlesFragment extends ListFragment {
        boolean mDualPane; // Flag to indicate whether we are in ORIENTATION_LANDSCAPE dual pane mode
        int mCurCheckPosition = 0; // Currently selected title to be displayed

        /**
         * Called when the fragment's activity has been created and this fragment's view hierarchy
         * instantiated. First we call through to our super's implementation of onActivityCreated.
         * Next we set the cursor for the list view of this ListFragment to an <b>ArrayAdapter</b>
         * consisting of the String[] array Shakespeare.TITLES. Then we determine whether we are
         * in ORIENTATION_LANDSCAPE (dual pane mode) by searching our Activity's content view for
         * a view with the id R.id.details, saving a reference to it in <b>View detailsFrame</b>.
         * If <b>detailsFrame</b> is not null and the View is VISIBLE we set our field <b>mDualPane</b>
         * to true. If <b>savedInstanceState</b> is not null we use it to retrieve the value of our
         * field mCurCheckPosition which our callback onSaveInstanceState saved under the key
         * "curChoice". If we have determined that we are in dual pane mode (ORIENTATION_LANDSCAPE)
         * we set the choice mode for our ListView to CHOICE_MODE_SINGLE so that the currently
         * selected item is highlighted, and then call our method <b>showDetails(mCurCheckPosition)</b>
         * to display the details of the selected item in the other pane.
         *
         * @param savedInstanceState If not null it contains mCurCheckPosition saved by our callback
         *                           onSaveInstanceState under the key "curChoice"
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Populate list with our static array of titles.
            setListAdapter(new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_activated_1, Shakespeare.TITLES));

            // Check to see if we have a frame in which to embed the details
            // fragment directly in the containing UI.
            View detailsFrame = getActivity().findViewById(R.id.details);
            mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

            if (savedInstanceState != null) {
                // Restore last state for checked position.
                mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
            }

            if (mDualPane) {
                // In dual-pane mode, the list view highlights the selected item.
                getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                // Make sure our UI is in the correct state.
                showDetails(mCurCheckPosition);
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("curChoice", mCurCheckPosition);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            showDetails(position);
        }

        /**
         * Helper function to show the details of a selected item, either by
         * displaying a fragment in-place in the current UI, or starting a
         * whole new activity in which it is displayed.
         */
        void showDetails(int index) {
            mCurCheckPosition = index;

            if (mDualPane) {
                // We can display everything in-place with fragments, so update
                // the list to highlight the selected item and show the data.
                getListView().setItemChecked(index, true);

                // Check what fragment is currently shown, replace if needed.
                DetailsFragment details = (DetailsFragment)
                        getFragmentManager().findFragmentById(R.id.details);
                if (details == null || details.getShownIndex() != index) {
                    // Make new fragment to show this selection.
                    details = DetailsFragment.newInstance(index);

                    // Execute a transaction, replacing any existing fragment
                    // with this one inside the frame.
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    if (index == 0) {
                        ft.replace(R.id.details, details);
                    } else {
                        ft.replace(R.id.details, details);
                    }
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                }

            } else {
                // Otherwise we need to launch a new activity to display
                // the dialog fragment with selected text.
                Intent intent = new Intent();
                intent.setClass(getActivity(), DetailsActivity.class);
                intent.putExtra("index", index);
                startActivity(intent);
            }
        }
    }

    /**
     * This is the secondary fragment, displaying the details of a particular
     * item.
     */
    public static class DetailsFragment extends Fragment {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        public static DetailsFragment newInstance(int index) {
            DetailsFragment f = new DetailsFragment();

            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putInt("index", index);
            f.setArguments(args);

            return f;
        }

        public int getShownIndex() {
            return getArguments().getInt("index", 0);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            if (container == null) {
                // We have different layouts, and in one of them this
                // fragment's containing frame doesn't exist.  The fragment
                // may still be created from its saved state, but there is
                // no reason to try to create its view hierarchy because it
                // won't be displayed.  Note this is not needed -- we could
                // just run the code below, where we would create and return
                // the view hierarchy; it would just never be used.
                return null;
            }

            ScrollView scroller = new ScrollView(getActivity());
            TextView text = new TextView(getActivity());
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    4, getActivity().getResources().getDisplayMetrics());
            text.setPadding(padding, padding, padding, padding);
            scroller.addView(text);
            text.setText(Shakespeare.DIALOGUE[getShownIndex()]);
            return scroller;
        }
    }

}
