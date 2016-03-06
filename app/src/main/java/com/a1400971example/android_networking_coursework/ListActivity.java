package com.a1400971example.android_networking_coursework;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Matthew Owens on 06/03/16.
 */
public class ListActivity extends Activity implements View.OnClickListener
{
    private final static String TAG = "ListActivity";

    Button toggleButton;
    boolean viewingDex = true;
    DatabaseHelper dbHelper;
    LinearLayout namesLayout;
    LinearLayout iconsLayout;

    ArrayList<Romon> dexRomon;
    ArrayList<Romon> bankRomon;

    ArrayList<TextView> dexNames = new ArrayList<TextView>();
    ArrayList<ImageView> dexIcons = new ArrayList<ImageView>();

    ArrayList<TextView> bankNames= new ArrayList<TextView>();
    ArrayList<ImageView> bankIcons = new ArrayList<ImageView>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate!");

        setContentView(R.layout.list_layout);
        dbHelper = new DatabaseHelper(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Setting up the toggleButton
        toggleButton = (Button)findViewById(R.id.toggleButton);
        toggleButton.setText("View Banked Romon");
        toggleButton.setOnClickListener(this);

        namesLayout = (LinearLayout)findViewById(R.id.iconsLayout);
        iconsLayout = (LinearLayout)findViewById(R.id.namesLayout);

        // Populating our romon lists
        dexRomon = dbHelper.getDexRomon();
        bankRomon = dbHelper.getBankRomon();

        // Setting up the textviews
        for(int i = 0; i < dexRomon.size(); ++i)
        {
            dexNames.add(i, new TextView(this));
            dexNames.get(i).setText(dexRomon.get(i).getName());
        }

        for(int i = 0; i < bankRomon.size(); ++i)
        {
            bankNames.add(i, new TextView(this));
            bankNames.get(i).setText(bankRomon.get(i).getName());
        }

        // Showing the dex values initially
        showDex();
    }

    public void onClick(View view)
    {
        Log.i(TAG, "onClick");
        if(view == toggleButton)
        {
            Log.i(TAG, "toggle!");
            if(viewingDex)
            {
                toggleButton.setText("View Romon Dex");
                showBank();
            }
            else
            {
                toggleButton.setText("View Banked Romon");
                showDex();
            }

            viewingDex = !viewingDex;
        }
    }

    private void showDex()
    {
        // Clearing the layouts
        namesLayout.removeAllViewsInLayout();
        iconsLayout.removeAllViewsInLayout();

        // Populating the namesLayout
        for(int i = 0; i < dexRomon.size(); ++i)
            namesLayout.addView(dexNames.get(i));
    }

    private void showBank()
    {
        // Clearing the layouts
        namesLayout.removeAllViewsInLayout();
        iconsLayout.removeAllViewsInLayout();

        // Populating the namesLayout
        for(int i = 0; i < bankRomon.size(); ++i)
            namesLayout.addView(bankNames.get(i));
    }
}
