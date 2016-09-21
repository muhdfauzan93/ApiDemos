package com.example.android.apis.app;

import com.example.android.apis.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Example of various Intent flags to modify the activity stack.
 * This activity demonstrates how to build a backstack of activities into an array of Intent[]
 * and use it to replace the current backstack by using makeRestartActivityTask() to create
 * Intent[0] with the flags set to re-launch ApiDemos task in its base state, Intent[1] to
 * launch ApiDemos at the "Views" "com.example.android.apis.Path", Intent[2] to launch ApiDemos
 * at the "Views/Lists" "com.example.android.apis.Path" and requesting to reset the back stack
 * to these three "activities". It then launches these intents using startActivities() if the
 * FLAG_ACTIVITY_CLEAR_TASK button is pressed, or putting them into a PendingIntent which it
 * "later" calls using PendingIntent.send()
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class IntentActivityFlags extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.intent_activity_flags);

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.flag_activity_clear_task);
        button.setOnClickListener(mFlagActivityClearTaskListener);
        button = (Button)findViewById(R.id.flag_activity_clear_task_pi);
        button.setOnClickListener(mFlagActivityClearTaskPIListener);
    }

    /**
     * This creates an array of Intent objects representing the back stack
     * for a user going into the Views/Lists API demos.
     */

    private Intent[] buildIntentsToViewsLists() {
        // We are going to rebuild our task with a new back stack.  This will
        // be done by launching an array of Intents, representing the new
        // back stack to be created, with the first entry holding the root
        // and requesting to reset the back stack.
        Intent[] intents = new Intent[3];

        // First: root activity of ApiDemos.
        // This is a convenient way to make the proper Intent to launch and
        // reset an application's task.
        intents[0] = Intent.makeRestartActivityTask(new ComponentName(this,
                com.example.android.apis.ApiDemos.class));

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(IntentActivityFlags.this, com.example.android.apis.ApiDemos.class);
        intent.putExtra("com.example.android.apis.Path", "Views");
        intents[1] = intent;

        intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(IntentActivityFlags.this, com.example.android.apis.ApiDemos.class);
        intent.putExtra("com.example.android.apis.Path", "Views/Lists");

        intents[2] = intent;
        return intents;
    }


    private OnClickListener mFlagActivityClearTaskListener = new OnClickListener() {
        public void onClick(View v) {
            startActivities(buildIntentsToViewsLists());
        }
    };

    private OnClickListener mFlagActivityClearTaskPIListener = new OnClickListener() {
        public void onClick(View v) {
            Context context = IntentActivityFlags.this;

            PendingIntent pi = PendingIntent.getActivities(context, 0,
                    buildIntentsToViewsLists(), PendingIntent.FLAG_UPDATE_CURRENT);

            try {
                pi.send();
            } catch (CanceledException e) {
                Log.w("IntentActivityFlags", "Failed sending PendingIntent", e);
            }
        }
    };
}
