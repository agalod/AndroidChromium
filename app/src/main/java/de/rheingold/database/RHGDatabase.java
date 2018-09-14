package de.rheingold.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.chromium.chrome.browser.ChromeApplication;

public class RHGDatabase extends SQLiteOpenHelper
{

    private final String LOG_TAG = getClass().getSimpleName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "rheingold.db";
    private static RHGDatabase sInstance;
    public static final String TLOOKUP = "TLookup";


    private RHGDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized RHGDatabase getInstance(Context context)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_DATABASE, functionName);

        if (sInstance == null)
        {
            sInstance = new RHGDatabase(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_DATABASE, functionName);

//        SQLiteDatabase db = ChromeApplication.getRhgDatabase().getWritableDatabase();
        db.beginTransaction();
        try
        {
//            String tmp = "CREATE TABLE " + TLOOKUP + "(id INTEGER PRIMARY KEY AUTOINCREMENT);";
            String tmp = "CREATE TABLE " + TLOOKUP + "("
                + TLookup.PK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//                + FK_STUDYID + " INTEGER NULL, "
                + TLookup.IHOSTNAME + " TEXT NULL, "
                + TLookup.IKIND + " TEXT NULL, "
                + TLookup.ILIST + " TEXT NULL, "
                + TLookup.ICREATED_AT + " DATETIME NULL, "
                + TLookup.IUPDATED_AT + " DATETIME NULL"
                //A.S. TODO: add foreign key relationship
//                "FOREIGN KEY(" + FK_STUDYID + ") " + "REFERENCES " + T%%% + "(" + PK%%% + ")"
                + ");";
            Log.d(ChromeApplication.TAG_RHG_DATABASE, "Executing SQL: " + tmp);
            db.execSQL(tmp);
            db.setTransactionSuccessful();
        } catch (SQLException e)
        {
            e.printStackTrace();
            Log.d(ChromeApplication.TAG_RHG_DATABASE, "Error creating table: " + e.getMessage());
        }finally
        {
            db.endTransaction();
        }

        Log.d(ChromeApplication.TAG_RHG_DATABASE, "onCreate end");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onCreate(db);
        Log.d(LOG_TAG, "onUpgrade end");
    }
}
