package com.a1400971example.android_networking_coursework;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Matthew Owens on 06/03/16.
 */
public class TradeActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "TradeActivity";
    private ImageView romonImage;
    private ImageView otherRomonImage;
    private ImageView tradeImage;
    private Button confirmButton;

    private Romon selectedRomon = null;
    private int selectedRomonID = -1;

    private ArrayList<Romon> bankedRomon = new ArrayList<Romon>();
    private ArrayList<TextView> textViews = new ArrayList<TextView>();
    private ArrayList<ImageView> imageViews = new ArrayList<ImageView>();


    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);

        //TODO: Switch to view layout for selection
        //TODO: DB stuff
        //TODO: bluetooth / NFC

        // Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Using our list view initially to select a romon to trade
        setContentView(R.layout.list_layout);

        GridLayout gridLayout;
        gridLayout = (GridLayout)findViewById(R.id.gridLayout);
        gridLayout.setColumnCount(2);

        confirmButton = (Button)findViewById(R.id.toggleButton);
        confirmButton.setText("Confirm Selection");
        confirmButton.setOnClickListener(this);

        // Populating our bank
        DatabaseHelper db = new DatabaseHelper(this.getApplicationContext());
        bankedRomon = db.getBankRomon();

        for(int i = 0; i < bankedRomon.size(); ++i)
        {
            textViews.add(i, new TextView(this));
            textViews.get(i).setText(bankedRomon.get(i).getNickname());

            imageViews.add(i, new ImageView(this));

            // TODO: Change based on the romon
            imageViews.get(i).setImageResource(R.drawable.unknown_romon);

            textViews.get(i).setOnClickListener(this);
            imageViews.get(i).setOnClickListener(this);

            gridLayout.addView(textViews.get(i));
            gridLayout.addView(imageViews.get(i));
        }
    }
    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick");

        if(view == tradeImage)
        {
            Log.i(TAG, "Whee~~");
            // TODO: Change DB vals

            // Switching romon images
            Drawable ourDrawable = romonImage.getDrawable();
            romonImage.setImageDrawable(otherRomonImage.getDrawable());
            otherRomonImage.setImageDrawable(ourDrawable);
            tradeImage.setVisibility(View.INVISIBLE);
            tradeImage.setClickable(false);

        }
        if(view == confirmButton)
        {
            Log.i(TAG, "confirm button pressed");
            if(selectedRomon == null || selectedRomonID == -1)
            {
                Toast.makeText(this.getApplicationContext(), "Please select a romon to trade",
                        Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(this.getApplicationContext(), selectedRomon.getName() + " selected",
                        Toast.LENGTH_LONG).show();

                // Layout switching
                setContentView(R.layout.trade_layout);

                // ImageView init
                romonImage = (ImageView) findViewById(R.id.romonImage);
                otherRomonImage = (ImageView) findViewById(R.id.otherRomonImage);
                tradeImage = (ImageView) findViewById(R.id.tradeImage);

                // Setting the click listeners
                tradeImage.setOnClickListener(this);
            }
        }

        for(int i = 0 ; i < bankedRomon.size(); ++i)
        {
            if(view == textViews.get(i) || view == imageViews.get(i))
            {
                // Reverting text colour of previous selection
                if(selectedRomonID != -1) {
                    textViews.get(selectedRomonID).setTextColor(0xffffffff);
                }

                selectedRomon = bankedRomon.get(i);
                selectedRomonID = i;

                textViews.get(i).setTextColor(0xffb35050);
            }
        }
    }
}
