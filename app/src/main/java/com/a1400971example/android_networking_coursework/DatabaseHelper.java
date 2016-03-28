package com.a1400971example.android_networking_coursework;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpRetryException;
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

    // Remote database stuff
    private final String rootURL = "http://mayar.abertay.ac.uk/~1400971";
    private final String insertURL = rootURL + "insert_dex.php";
    private final String getListURL = rootURL + "getlist.php";

    // Dex table's CREATE string
    private static final String DEX_TABLE_CREATE = "CREATE TABLE " + DEX_TABLE_NAME + " (" +
            DEX_COLUMN_NAMES[0] + " TEXT, " +
            DEX_COLUMN_NAMES[1] + " INTEGER, " +
            DEX_COLUMN_NAMES[2] + " INTEGER, " + // Using INTEGER since BIT is unsupported
            DEX_COLUMN_NAMES[3] + " INTEGER);";

    // Bank table's CREATE string
    private static final String BANK_TABLE_CREATE = "CREATE TABLE " + BANK_TABLE_NAME + " (" +
            BANK_COLUMN_NAMES[0] + " TEXT, " +
            BANK_COLUMN_NAMES[1] + " TEXT, " +
            BANK_COLUMN_NAMES[2] + " INTEGER);";

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
        // TODO: Populate with appropriate resources

        //for (int i = 0; i < 5; ++i)
        //    addRomonDex("testRomon" + i, R.drawable.unknown_romon, db);

        // TODO: Figure out why addRomonDex seems to do _nothing_ here
        Romon romon = new Romon("testRomon01", R.drawable.unknown_romon, 0);
        addRomonDex(romon);

        /*
            for(int i = 0; i < 5; ++i)
                addRomonDex(new Romon("Romon" + i, R.drawable.unknown_romon, 0));
         */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void addRomonDex(String name, int resourceName, SQLiteDatabase db) {
        ContentValues row = new ContentValues();

        row.put(DEX_COLUMN_NAMES[0], name);
        row.put(DEX_COLUMN_NAMES[1], resourceName);
        row.put(DEX_COLUMN_NAMES[2], 0);
        row.put(DEX_COLUMN_NAMES[3], 0);

        db.insert(DEX_TABLE_NAME, null, row);
        //db.close();
        Log.i(TAG, "addRomonDex completed");
    }

    private void addRomonDex(Romon romon){
        AddDexTask task = new AddDexTask();
        task.execute(romon);
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

        // Checking that the result is valid
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

    public Romon getDexRomon(int id)
    {
        Romon val = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String idString = "'" + id + "'";

        Cursor result = db.query(DEX_TABLE_NAME, DEX_COLUMN_NAMES, "id=" + idString,
                null, null, null, null);

        if(result == null)
        {
            Log.i(TAG, "getDexRomon(int id): invalid result!");
            result.close();
        }
        else
        {
            val = new Romon(result.getString(0), result.getString(0), result.getInt(1));
        }
        return val;
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
                romons.add(new Romon(result.getString(0), result.getString(0), result.getInt(1)));
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
                romons.add(new Romon(result.getString(0), result.getString(1), result.getInt(2)));
                result.moveToNext();
            }
        }
        result.close();
        //db.close();
        return romons;
    }

    public void clearBank()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + BANK_TABLE_NAME);
    }

    // Async task for adding a new romon to the remote Dex, runs on a separate thread
    private class AddDexTask extends AsyncTask<Romon, Void, Void>{
        private HttpClient httpClient = new DefaultHttpClient();
        private HttpPost httpPost = new HttpPost(insertURL);

        @Override
        protected Void doInBackground(Romon... params) {
            Log.i(TAG, "dex add started...");
            // Ensuring that we check all of the params, not just the first
            for(Romon romon : params)
            {
                Log.i(TAG, "checking the romon passed through");
                // Associating column names and romon members as key-value pairs
                ArrayList<NameValuePair> romonDetails = new ArrayList<NameValuePair>(4);
                romonDetails.add(new BasicNameValuePair(DEX_COLUMN_NAMES[0], romon.getName()));
                romonDetails.add(new BasicNameValuePair(DEX_COLUMN_NAMES[1], Integer.toString(romon.getDrawableResource())));
                romonDetails.add(new BasicNameValuePair(DEX_COLUMN_NAMES[2], Boolean.toString(romon.getCaptureCount() > 0)));
                romonDetails.add(new BasicNameValuePair(DEX_COLUMN_NAMES[3], Integer.toString(romon.getCaptureCount())));
                Log.i(TAG, "romon details added");

                // Encoding the HTTP POST request
                try
                {
                    httpPost.setEntity(new UrlEncodedFormEntity(romonDetails));
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
                Log.i(TAG, "post created");

                // Making the request to the server
                try
                {
                    Log.i(TAG, "MAKING THE REQUEST!!!");
                    HttpResponse response = httpClient.execute(httpPost);
                    Log.i(TAG, "YAS!");

                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");

                    Log.i(TAG, "'Add' Response: " + responseString);
                }
                catch (IOException e)
                {
                    Log.i(TAG, "bollocks");
                    e.printStackTrace();
                }
                catch (Exception e) // Trying for generics
                {
                    Log.i(TAG, "generic bollocks");
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "Remote Dex DB add completed!");
            return null;
        }
    }

    // Async task for getting the dex list from a remote DB, runs on a separate thread
    private class GetDexTask extends AsyncTask<Void, Void, ArrayList<Romon>>
    {
        private HttpClient httpClient = new DefaultHttpClient();
        private HttpGet httpGet = new HttpGet(getListURL);

        @Override
        protected ArrayList<Romon> doInBackground(Void... params) {
            // Make a request for the data
            HttpResponse response = null;
            String responseString = "";
            try
            {
                response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                responseString = EntityUtils.toString(entity, "UTF-8");

                Log.i(TAG, "'Get' Response: " + responseString);
            }
            catch (ClientProtocolException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            Log.i(TAG, "Remote Dex get completed!");
            return null;
        }
    }
}
