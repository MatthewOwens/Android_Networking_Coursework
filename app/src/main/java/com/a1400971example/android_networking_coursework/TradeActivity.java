package com.a1400971example.android_networking_coursework;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by Matthew Owens on 06/03/16.
 */
public class TradeActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "TradeActivity";
    private ImageView romonImage;
    private ImageView otherRomonImage;
    private ImageView tradeImage;
    private Button confirmButton;

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

        setContentView(R.layout.list_layout);
        confirmButton = (Button)findViewById(R.id.toggleButton);
        confirmButton.setText("Select Trade Romon");
        confirmButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
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
            // TODO: Selections

            // Layout switching
            setContentView(R.layout.trade_layout);

            // ImageView init
            romonImage = (ImageView)findViewById(R.id.romonImage);
            otherRomonImage = (ImageView)findViewById(R.id.otherRomonImage);
            tradeImage = (ImageView)findViewById(R.id.tradeImage);

            // Setting the click listeners
            tradeImage.setOnClickListener(this);
        }
    }
}
