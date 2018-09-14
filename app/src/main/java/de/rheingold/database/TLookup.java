package de.rheingold.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import org.chromium.chrome.browser.ChromeApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.rheingold.models.Lookup;

import static de.rheingold.database.RHGDatabase.TLOOKUP;


public class TLookup
{

    public static final String PK_ID = "id";
    public static final String IHOSTNAME = "hostname";
    public static final String IKIND = "kind";
    public static final String ILIST = "list";
    public static final String ICREATED_AT = "created_at";
    public static final String IUPDATED_AT = "updated_at";
    public static final String FK_STUDYID = "study_id";


    public static List<Lookup> getContent()
    {
        SQLiteDatabase db = ChromeApplication.getDatabase().getReadableDatabase();
        try (Cursor c = db.query(TLOOKUP, null, null, null, null, null, null))
        {
            if (c.getCount() > 0)
            {
                List<Lookup> results = new ArrayList<>();
                Lookup entry;
                int idIndex = c.getColumnIndex(PK_ID);
                int hostnameIndex = c.getColumnIndex(IHOSTNAME);
                int kindIndex = c.getColumnIndex(IKIND);
                int listTypeIndex = c.getColumnIndex(ILIST);
                int createdIndex = c.getColumnIndex(ICREATED_AT);
                int updatedIndex = c.getColumnIndex(IUPDATED_AT);

                while (c.moveToNext())
                {
                    entry = new Lookup();
                    entry.id = c.getLong(idIndex);
                    entry.setHostname(c.getString(hostnameIndex));
                    entry.setKind(c.getString(kindIndex));
                    entry.setListType(c.getString(listTypeIndex));
                    entry.setCreatedAt(c.getString(createdIndex));
                    entry.setUpdatedAt(c.getString(updatedIndex));
                    results.add(entry);
                }
                return results;
            }
        }
        return null;
    }

    public static void setContent(JSONObject jsonObject)
    {
        SQLiteDatabase db = ChromeApplication.getDatabase().getWritableDatabase();
        db.beginTransaction();
        db.execSQL("DROP TABLE IF EXISTS " + RHGDatabase.TLOOKUP);
        try
        {
            JSONArray whitelist = jsonObject.getJSONArray("whitelist");
            JSONArray blacklist = jsonObject.getJSONArray("blacklist");
            Log.d(ChromeApplication.TAG_RHG_DATABASE, "Blacklist: " + blacklist);
            for (int i = 0; i < whitelist.length(); i++)
            {
                ContentValues values = new ContentValues();
//                values.put(TLookup.PK_ID, whitelist.getJSONObject(i).getInt(TLookup.PK_ID));
                values.put(TLookup.ILIST, "white");
                values.put(TLookup.IKIND, whitelist.getJSONObject(i).getString(TLookup.IKIND));
                values.put(TLookup.IHOSTNAME, whitelist.getJSONObject(i).getString(TLookup.IHOSTNAME));

                Log.d(ChromeApplication.TAG_RHG_DATABASE, "Adding row: " + values);
                db.insert(RHGDatabase.TLOOKUP, null, values);
            }

            for (int i = 0; i < blacklist.length(); i++)
            {
                ContentValues values = new ContentValues();
//                values.put(TLookup.PK_ID, blacklist.getJSONObject(i).getInt(TLookup.PK_ID));
                values.put(TLookup.ILIST, "black");
                values.put(TLookup.IKIND, blacklist.getJSONObject(i).getString(TLookup.IKIND));
                values.put(TLookup.IHOSTNAME, blacklist.getJSONObject(i).getString(TLookup.IHOSTNAME));

                Log.d(ChromeApplication.TAG_RHG_DATABASE, "Adding row: " + values);
                db.insert(RHGDatabase.TLOOKUP, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e)
        {
            e.printStackTrace();
            String message = "SetContent(...) - " + e.getMessage();
        } finally
        {
            db.endTransaction();
        }
    }

    public static void insertLookupEntry(SQLiteDatabase db, Lookup entry)
    {
        db.execSQL("INSERT INTO " + TLOOKUP
                + " VALUES " + "("
                + entry.id + ", "
                + "'" + entry.getHostname() + "', "
                + "'" + entry.getKind() + "', "
                + "'" + entry.getListType() + "', "
                + "'" + entry.getCreatedAt() + "', "
                + "'" + entry.getUpdatedAt() + "'"
                + ");"
        );
    }
}