package com.a1400971example.android_networking_coursework;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Vibrator vibrator;

    // The amount of time that can pass before the user is guaranteed to find a monster (seconds)
    // Currently 1 for debug purposes
    private static long maxFindTime = 1;

    private Button[] buttons = new Button[3];
    private ImageView foundImg;

    Romon foundRomon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");

        // Button initilisation
        buttons[0] = (Button) findViewById(R.id.battleButton);
        buttons[1] = (Button) findViewById(R.id.tradeButton);
        buttons[2] = (Button) findViewById(R.id.listButton);
        foundImg = (ImageView) findViewById(R.id.foundImage);

        for (int i = 0; i < 3; ++i)
            buttons[i].setOnClickListener(this);
        foundImg.setOnClickListener(this);

        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        Log.i(TAG, "vibrator: " + vibrator.hasVibrator());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

        // Getting the preferences file and editor
        SharedPreferences prefs = getPreferences(0);
        SharedPreferences.Editor prefEditor = prefs.edit();

        prefEditor.putLong("closeTime", System.currentTimeMillis() / 1000L);
        prefEditor.putBoolean("firstRun", false);
        prefEditor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        SharedPreferences prefs = getPreferences(0);
        long openTime = System.currentTimeMillis() / 1000L;
        long prevOpened = prefs.getLong("closeTime", -1L);

        Log.i(TAG, "openTime: " + openTime);
        Log.i(TAG, "prevOpened: " + prevOpened);

        // User hasn't opened the app before, give them a freebie
        if (prevOpened == -1) {
            Log.i(TAG, "Monster found -- initial");
        }
        else
        {
            double timeDiff = openTime - prevOpened;
            double findChance = timeDiff / maxFindTime;
            Random r = new Random();
            float roll = r.nextFloat();

            // Checking if we've found a monster
            if (findChance > roll)
            {
                Log.i(TAG, "Monster found -- rolled");
                foundRomon = new Romon("testRomon0", "testNick", "");
                foundImg.setImageResource(R.drawable.unknown_romon);    // TODO: Change based on the found romon
            }
            else
            {
                Log.i(TAG, "No monster found!");
            }
        }
    }

    public void onClick(View view) {
        // Giving the user a bit of feedback
        vibrator.vibrate(100);

        // Battle
        if (view == buttons[0]) {
            Log.i(TAG, "Battle button pressed!");
            //Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
            //startActivity(intent);
        }

        // Trade
        if (view == buttons[1]) {
            Log.i(TAG, "Trade button pressed!");
            Intent intent = new Intent(getApplicationContext(), TradeActivity.class);
            startActivity(intent);
        }

        // List
        if (view == buttons[2]) {
            Log.i(TAG, "List button pressed!");
            Intent intent = new Intent(getApplicationContext(), ListActivity.class);
            startActivity(intent);
        }
        if (view == foundImg) {

            // Test location stuff, should be in onResume
            LocationHelper locHelper = new LocationHelper(MainActivity.this);
            Toast.makeText(getApplicationContext(),
                    "Lat: " + locHelper.getLatitude() + "\nLong: " + locHelper.getLongtitude(),
                    Toast.LENGTH_LONG).show();

            DatabaseHelper db = new DatabaseHelper(this.getApplicationContext());
            db.addRomonBank(foundRomon);
        }
    }

}
