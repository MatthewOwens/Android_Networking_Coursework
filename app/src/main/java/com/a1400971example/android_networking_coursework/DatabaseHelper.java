package com.a1400971example.android_networking_coursework;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.provider.Settings;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpRetryException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Matthew Owens on 06/03/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "RomonDB";
    private static final String DEX_TABLE_NAME = "Dex";
    private static final String BANK_TABLE_NAME = "Bank";
    private static final String[] DEX_COLUMN_NAMES = {"_id", "name", "drawable_name", "captured", "encounter_count"};
    private static final String[] BANK_COLUMN_NAMES = {"_id", "name", "nickname", "drawable_name"};

    // Remote database stuff
    private final String rootURL = "http://mayar.abertay.ac.uk/~1400971/";
    private final String insertURL = rootURL + "AndroidNetworking/Romon/php/insert_dex.php";
    private final String getListURL = rootURL + "AndroidNetworking/Romon/php/getlist.php";
    private final String deleteURL = rootURL + "AndroidNetworking/Romon/php/remove_dex.php";

    // Dex table's CREATE string
    private static final String DEX_TABLE_CREATE = "CREATE TABLE " + DEX_TABLE_NAME + " (" +
            DEX_COLUMN_NAMES[0] + " INTEGER PRIMARY KEY, " +
            DEX_COLUMN_NAMES[1] + " TEXT, " +
            DEX_COLUMN_NAMES[2] + " INTEGER, " +
            DEX_COLUMN_NAMES[3] + " INTEGER, " + // Using INTEGER since BIT is unsupported
            DEX_COLUMN_NAMES[4] + " INTEGER);";

    // Bank table's CREATE string
    private static final String BANK_TABLE_CREATE = "CREATE TABLE " + BANK_TABLE_NAME + " (" +
            BANK_COLUMN_NAMES[0] + " INTEGER PRIMARY KEY, " +
            BANK_COLUMN_NAMES[1] + " TEXT, " +
            BANK_COLUMN_NAMES[2] + " TEXT, " +
            BANK_COLUMN_NAMES[3] + " INTEGER);";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "onCreate");
        // Creates the database if it doesn't exist and adds the DEX and STORED tables
        db.execSQL(DEX_TABLE_CREATE);
        db.execSQL(BANK_TABLE_CREATE);

        updateDex(db);
    }

    private void updateDex(SQLiteDatabase db)
    {

        // Getting the dex
        ArrayList<Romon> dexList = null;
        GetDexTask dexTask = new GetDexTask();

        try
        {
            dexList = new ArrayList<Romon>();
            dexList = dexTask.execute().get();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }

        if(dexList != null)
        {
            Log.i(TAG, "dexList populated!");

            // Populating the local dex database
            for (Romon romon : dexList)
            {
                addRomonDex(romon, db);
            }
        }
        else
            Log.i(TAG, "dexList population failed!");

        /*
            for(int i = 0; i < 5; ++i)
                addRomonDex(new Romon("Romon" + i, R.drawable.unknown_romon, 0));
         */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Dropping the current dex table and replacing it with the remote
        db.execSQL("DROP TABLE " + DEX_TABLE_NAME);
        updateDex(db);
    }

    private void addRomonDex(String name, int resourceName, SQLiteDatabase db) {
        ContentValues row = new ContentValues();

        row.put(DEX_COLUMN_NAMES[1], name);
        row.put(DEX_COLUMN_NAMES[2], resourceName);
        row.put(DEX_COLUMN_NAMES[3], 0);
        row.put(DEX_COLUMN_NAMES[4], 0);

        db.insert(DEX_TABLE_NAME, null, row);
        //db.close();
        Log.i(TAG, "addRomonDex completed");
    }

    // Adding romon to the local dex DB
    private void addRomonDex(Romon romon, SQLiteDatabase db)
    {
        ContentValues row = new ContentValues();
        row.put(DEX_COLUMN_NAMES[1], romon.getName());
        row.put(DEX_COLUMN_NAMES[2], romon.getDrawableResource());
        row.put(DEX_COLUMN_NAMES[3], 0);
        row.put(DEX_COLUMN_NAMES[4], 0);

        db.insert(DEX_TABLE_NAME, null, row);
        Log.i(TAG, "added " + romon.getName() + " to the dex!");
    }

    // Adding romon to the remote dex DB, makes testing things a hell of a lot faster
    public void addRomonDexRemote(Romon... romons)
    {
        AddDexTask task = new AddDexTask();
        task.execute(romons);
    }

    public void removeRomonDexRemote(Integer... ids)
    {
        DeleteDexTask task = new DeleteDexTask();
        task.execute(ids);
    }

    public void addRomonBank(int DexPosition, String nickname) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues row = new ContentValues();

        // Querying the DEX database to see if there's any matches at DexPosition
        Cursor result = db.query(DEX_TABLE_NAME, DEX_COLUMN_NAMES, "_id=" + DexPosition, null, null, null, null);

        // Checking that the result was valid
        if (result == null)
            Log.i(TAG, "addRomonBank: invlid result!");
        else {
            row.put(BANK_COLUMN_NAMES[1], result.getString(1)); // Assigning the bank name to the dex name

            if (nickname == "")
                row.put(BANK_COLUMN_NAMES[2], result.getString(1)); // Assigning the bank nick to the dex name
            else
                row.put(BANK_COLUMN_NAMES[2], nickname);

            row.put(BANK_COLUMN_NAMES[3], result.getString(2)); // Assigning the bank path to the dex path

            db.insert(BANK_TABLE_NAME, null, row);
        }

        //result.close();
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

            row.put(BANK_COLUMN_NAMES[1], result.getString(1));

            // If there's no nickname
            if(romon.getNickname() == "" || romon.getNickname() == romon.getName())
                row.put(BANK_COLUMN_NAMES[2], result.getString(1));
            else row.put(BANK_COLUMN_NAMES[2], romon.getNickname());

            // Image strings
            row.put(BANK_COLUMN_NAMES[3], result.getString(2));

            // TODO: Change dex capured and encoutner_count flags
            db.insert(BANK_TABLE_NAME, null, row);
        }

        //result.close();
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
            //result.close();
            //db.close();
            return -1;
        }
        else
        {
            //result.close();
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
            //result.close();
            //db.close();
            return -1;
        }
        else
        {
            //result.close();
            //db.close();
            return result.getCount();
        }
    }

    public Romon getDexRomon(int id)
    {
        // Incrementing the id, since the database _id column starts from 1
        id++;

        Romon val = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String idString = "'" + id + "'";

        Cursor result = db.query(DEX_TABLE_NAME, DEX_COLUMN_NAMES, "_id=" + idString,
                null, null, null, null);

        if(result == null)
        {
            Log.i(TAG, "getDexRomon(int id): invalid result!");
            //result.close();
        }
        else
        {
            result.moveToFirst();
            val = new Romon(result.getString(1), result.getString(1), result.getInt(2));
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
                romons.add(new Romon(result.getString(1), result.getString(1), result.getInt(2)));
                result.moveToNext();
            }
        }
        //result.close();
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
                romons.add(new Romon(result.getString(1), result.getString(2), result.getInt(3)));
                result.moveToNext();
            }
        }
        //result.close();
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
                // Associating column names and romon members as key-value pairs
                ArrayList<NameValuePair> romonDetails = new ArrayList<NameValuePair>(4);
                romonDetails.add(new BasicNameValuePair(DEX_COLUMN_NAMES[1], romon.getName()));
                romonDetails.add(new BasicNameValuePair(DEX_COLUMN_NAMES[2], Integer.toString(romon.getDrawableResource())));
                romonDetails.add(new BasicNameValuePair(DEX_COLUMN_NAMES[3], Boolean.toString(romon.getCaptureCount() > 0)));
                romonDetails.add(new BasicNameValuePair(DEX_COLUMN_NAMES[4], Integer.toString(romon.getCaptureCount())));

                // Encoding the HTTP POST request
                try
                {
                    httpPost.setEntity(new UrlEncodedFormEntity(romonDetails));
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }

                // Making the request to the server
                try
                {
                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");

                    Log.i(TAG, "'Add' Response: " + responseString);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (Exception e) // Trying for generics
                {
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

            // Processing the JSON results
            JSONArray romonArray = null;
            if(responseString != null && responseString != "")
            {
                try
                {
                    romonArray = new JSONArray(responseString);
                }
                catch(JSONException e)
                {
                    e.printStackTrace();
                }
            }

            // Parsing our results
            ArrayList<Romon> ret = new ArrayList<Romon>();

            if(romonArray != null)
            {
                for(int i = 0; i < romonArray.length(); ++i)
                {
                    try
                    {
                        JSONObject entry = romonArray.getJSONObject(i);
                        ret.add(new Romon((String)entry.get(DEX_COLUMN_NAMES[1].toString()),
                                entry.getInt(DEX_COLUMN_NAMES[2]),
                                entry.getInt(DEX_COLUMN_NAMES[4])));
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            return ret;
        }

        @Override
        protected void onPostExecute(ArrayList<Romon> result)
        {
            Log.i(TAG, "DEX POPULATED!");
        }
    }

    private class DeleteDexTask extends AsyncTask<Integer, Void, Void>
    {
        private HttpClient httpClient = new DefaultHttpClient();
        private HttpPost httpPost = new HttpPost(deleteURL);

        @Override
        protected Void doInBackground(Integer... ids)
        {
            // Ensuring that we remove all specified ids
            for(Integer id : ids)
            {
                ArrayList<NameValuePair> idDetails = new ArrayList<NameValuePair>(1);
                idDetails.add(new BasicNameValuePair("_id", id.toString()));

                // Encoding the POST data
                try
                {
                    httpPost.setEntity(new UrlEncodedFormEntity(idDetails));
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }

                // Making the request
                try
                {
                    HttpResponse response = httpClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");

                    Log.i(TAG, "Remove response: " + responseString);
                }
                catch (ClientProtocolException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }
}
