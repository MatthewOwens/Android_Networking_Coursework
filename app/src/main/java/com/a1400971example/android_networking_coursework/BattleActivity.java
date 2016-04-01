package com.a1400971example.android_networking_coursework;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Matthew Owens on 07/03/16.
 */
public class BattleActivity extends Activity implements View.OnClickListener
{
    private Romon selectedRomon = null;
    private int selectedRomonID = -1;

    private ArrayList<Romon> bankedRomon = new ArrayList<Romon>();
    private ArrayList<TextView> list_textViews = new ArrayList<TextView>();
    private ArrayList<ImageView> list_imageViews = new ArrayList<ImageView>();
    private Button list_confirmButton;

    private ImageView[] battle_romonImages = new ImageView[2];
    private ImageView conclusion_victorImage;

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);

        // Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Ensuring widgets for other layouts are nulled
        for(int i = 0; i < 2; ++i)
            battle_romonImages[i] = null;
        conclusion_victorImage = null;


        // Getting the romon to battle with using our list
        setContentView(R.layout.list_layout);

        GridLayout gridLayout;
        gridLayout = (GridLayout)findViewById(R.id.gridLayout);
        gridLayout.setColumnCount(2);

        list_confirmButton = (Button)findViewById(R.id.toggleButton);
        list_confirmButton.setText("Confirm Selection");
        list_confirmButton.setOnClickListener(this);

        // Populating the bank
        DatabaseHelper db = new DatabaseHelper(this.getApplicationContext(),
                                getSharedPreferences("prefs", 0).getInt("dbVersion", 1));
        bankedRomon = db.getBankRomon();

        // Populating the list views
        for(int i = 0; i < bankedRomon.size(); ++i)
        {
            list_textViews.add(i, new TextView(this));
            list_textViews.get(i).setText(bankedRomon.get(i).getNickname());

            list_imageViews.add(i, new ImageView(this));
            list_imageViews.get(i).setImageResource(R.drawable.unknown_romon);  // TODO: fix

            list_textViews.get(i).setOnClickListener(this);
            list_imageViews.get(i).setOnClickListener(this);

            gridLayout.addView(list_textViews.get(i));
            gridLayout.addView(list_imageViews.get(i));
        }

    }


    @Override
    public void onClick(View view)
    {
        // If one of the list items was clicked
        for(int i = 0; i < bankedRomon.size(); ++i)
        {
            if(view == list_textViews.get(i) || view == list_imageViews.get(i))
            {
                // Reverting selected colour
                if(selectedRomonID != -1)
                    list_textViews.get(selectedRomonID).setTextColor(0xffffffff);

                list_textViews.get(i).setTextColor(0xffb35050);
                selectedRomon = bankedRomon.get(i);
                selectedRomonID = i;
            }
        }

        if(view == list_confirmButton)
        {
            // Ensuring that we've selected a romon
            if(selectedRomonID == -1 || selectedRomon == null)
            {
                Toast.makeText(this.getApplicationContext(), "Please select a romon to battle",
                        Toast.LENGTH_LONG).show();
            }
            else
            {
                // Changing to the battle layout
                setContentView(R.layout.battle_layout);
                battle_romonImages[0] = (ImageView)findViewById(R.id.battle_friendlyImage);
                battle_romonImages[1] = (ImageView)findViewById(R.id.battle_enemyImage);

                battle_romonImages[0].setImageResource(selectedRomon.getDrawableResource());
                battle_romonImages[1].setImageResource(R.drawable.unknown_romon);   // TODO: other romon

                for(int i = 0; i < 2; ++i)
                    battle_romonImages[i].setOnClickListener(this);
            }
        }

        if(view == battle_romonImages[0] || view == battle_romonImages[1])
        {
            // Changing to the conclusion layout
            setContentView(R.layout.battle_conclusion_layout);
            conclusion_victorImage = (ImageView)findViewById(R.id.conclusion_victorImage);

            // TODO: determine victor & send to other player
            conclusion_victorImage.setImageResource(R.drawable.unknown_romon);
        }
    }
}
