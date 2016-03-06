package com.a1400971example.android_networking_coursework;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by Matthew Owens on 06/03/16.
 */
public class ListActivity extends Activity implements View.OnClickListener
{
    private final static String TAG = "ListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate!");

        setContentView(R.layout.list_layout);
    }

    @Override
    public void onClick(View v) {

    }
}
