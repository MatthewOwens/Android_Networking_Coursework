package com.a1400971example.android_networking_coursework;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Matthew Owens on 06/03/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "RomonDB";
    private static final String DEX_TABLE_NAME = "Dex";
    private static final String BANK_TABLE_NAME = "Bank";
    private static final String[] DEX_COLUMN_NAMES = {"name", "drawable_name", "captured", "encounter_count"};
    private static final String[] BANK_COLUMN_NAMES = {"name", "nickname", "drawable_name"};

    // Dex table's CREATE string
    private static final String DEX_TABLE_CREATE = "CREATE TABLE " + DEX_TABLE_NAME + " (" +
            DEX_COLUMN_NAMES[0] + " TEXT, " +
            DEX_COLUMN_NAMES[1] + " TEXT, " +
            DEX_COLUMN_NAMES[2] + " INTEGER, " + // Using INTEGER since BIT is unsupported
            DEX_COLUMN_NAMES[3] + " INTEGER);";

    // Bank table's CREATE string
    private static final String BANK_TABLE_CREATE = "CREATE TABLE " + BANK_TABLE_NAME + " (" +
            BANK_COLUMN_NAMES[0] + " TEXT, " +
            BANK_COLUMN_NAMES[1] + " TEXT, " +
            BANK_COLUMN_NAMES[2] + " TEXT);";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate");
        // Creates the database if it doesn't exist and adds the DEX and STORED tables
        db.execSQL(DEX_TABLE_CREATE);
        db.execSQL(BANK_TABLE_CREATE);

        // DEX table initial values, nonsense for testing
        for (int i = 0; i < 5; ++i)
            addRomonDex("testRomon" + i, "romon" + i, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //private void addRomonDex(String name, String imgPath)
    private void addRomonDex(String name, String imgPath, SQLiteDatabase db) {
        ContentValues row = new ContentValues();

        row.put(DEX_COLUMN_NAMES[0], name);
        row.put(DEX_COLUMN_NAMES[1], imgPath);
        row.put(DEX_COLUMN_NAMES[2], 0);
        row.put(DEX_COLUMN_NAMES[3], 0);

        db.insert(DEX_TABLE_NAME, null, row);
        //db.close();
        Log.i(TAG, "addRomonDex completed");
    }

    public void addRomonBank(int DexPosition, String nickname) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues row = new ContentValues();

        // Querying the DEX database to see if there's any matches at DexPosition
        Cursor result = db.query(DEX_TABLE_NAME, DEX_COLUMN_NAMES, "id=" + DexPosition, null, null, null, null);

        // Checking that the result was valid
        if (result == null)
            Log.i(TAG, "addRomonBank: invlid result!");
        else {
            row.put(BANK_COLUMN_NAMES[0], result.getString(0)); // Assigning the bank name to the dex name

            if (nickname == "")
                row.put(BANK_COLUMN_NAMES[1], result.getString(0)); // Assigning the bank nick to the dex name
            else
                row.put(BANK_COLUMN_NAMES[1], nickname);

            row.put(BANK_COLUMN_NAMES[2], result.getString(1)); // Assigning the bank path to the dex path

            db.insert(BANK_TABLE_NAME, null, row);
        }

        result.close();
        //db.close();
    }

    public void addRomonBank(Romon romon)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues row = new ContentValues();
        String nameSearch = "'" + romon.getName() + "'";

        // Querying the DEX database to match iamges
        Cursor result = db.query(DEX_TABLE_NAME, DEX_COLUMN_NAMES, "name=" + nameSearch, null, null, null, null);

        // Checking that the result is valud
        if(result == null)
            Log.i(TAG, "addRomonBank: invalid result!");
        else
        {
            result.moveToFirst();

            row.put(BANK_COLUMN_NAMES[0], result.getString(0));

            // If there's no nickname
            if(romon.getNickname() == "" || romon.getNickname() == romon.getName())
                row.put(BANK_COLUMN_NAMES[1], result.getString(0));
            else row.put(BANK_COLUMN_NAMES[1], romon.getNickname());

            // Image strings
            row.put(BANK_COLUMN_NAMES[2], result.getString(1));

            // TODO: Change dex capured and encoutner_count flags
            db.insert(BANK_TABLE_NAME, null, row);
        }

        result.close();
    }



    public void delRomonBank(int position)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(BANK_TABLE_NAME, "id=" + position, null);
        //db.close();
    }

    public int getDexCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.query(DEX_TABLE_NAME, DEX_COLUMN_NAMES, null, null, null, null, null);

        if(result == null)
        {
            Log.i(TAG, "getDexCount: invalid result!");
            result.close();
            //db.close();
            return -1;
        }
        else
        {
            result.close();
            //db.close();
            return result.getCount();
        }

    }

    public int getBankCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor result = db.query(BANK_TABLE_NAME, BANK_COLUMN_NAMES, null, null, null, null, null);

        if(result == null)
        {
            Log.i(TAG, "getBankCount: invalid result!");
            result.close();
            //db.close();
            return -1;
        }
        else
        {
            result.close();
            //db.close();
            return result.getCount();
        }
    }

    public ArrayList<Romon> getDexRomon()
    {
        ArrayList<Romon> romons = new ArrayList<Romon>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.query(DEX_TABLE_NAME, DEX_COLUMN_NAMES, null, null, null, null, null);

        if(result == null)
            Log.i(TAG, "getDexRomon: invalid result!");
        else
        {
            int romonCount = result.getCount();
            Log.i(TAG, "result count: " + romonCount);
            result.moveToFirst();

            for(int i = 0; i < romonCount; ++i)
            {
                romons.add(new Romon(result.getString(0), result.getString(0), result.getString(1)));
                result.moveToNext();
            }
        }
        result.close();
        //db.close();
        return romons;
    }

    public ArrayList<Romon> getBankRomon()
    {
        ArrayList<Romon> romons = new ArrayList<Romon>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.query(BANK_TABLE_NAME, BANK_COLUMN_NAMES, null, null, null, null, null);

        if(result == null)
            Log.i(TAG, "getBankRomon: invalid result!");
        else
        {
            int romonCount = result.getCount();
            Log.i(TAG, "result count: " + romonCount);
            result.moveToFirst();

            for(int i = 0; i < romonCount; ++i)
            {
                romons.add(new Romon(result.getString(0), result.getString(1), result.getString(2)));
                result.moveToNext();
            }
        }
        result.close();
        //db.close();
        return romons;
    }
}
