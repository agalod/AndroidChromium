package de.rheingold.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.chromium.chrome.browser.ChromeApplication;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.rheingold.models.Lookup;

import static de.rheingold.database.RHGDatabase.TLOOKUP;


/*
 * Controller for whitelist and blacklist
 * */
public class TLookup
{
    public static final String PK_ID = "id";
    public static final String IHOSTNAME = "hostname";
    public static final String IKIND = "kind";
    public static final String ILIST = "list";
    public static final String ICREATED_AT = "created_at";
    public static final String IUPDATED_AT = "updated_at";
    public static final String FK_STUDYID = "study_id";

    static public ArrayList<String> whitelist = new ArrayList<String>();
    static public ArrayList<String> blacklist = new ArrayList<String>();

//    public TLookup()
//    {
//        blacklist.add("signin");
//        blacklist.add("sign_in");
//    }

    public static boolean isAllowed(String url, boolean isWhitelist)
    {
        SQLiteDatabase db = ChromeApplication.getRhgDatabase().getReadableDatabase();
        if (isInBlacklist(url))
        {
            return false;
        }

        if (ChromeApplication.rhgHasWhitelist) // whitelist mode
        {
            if (isInWhitelist(url))
            {
                Log.d(ChromeApplication.TAG_RHG_LOOKUP, "Allowing screenhot for " + url + " (whitelist).");
                return true;
            }
            Log.d(ChromeApplication.TAG_RHG_LOOKUP, "Refusing screenshot for " + url + " (not in whitelist)");
            return false;
        }
        Log.d(ChromeApplication.TAG_RHG_LOOKUP, "Allowing screenhot for " + url);
        return true;
    }

    public static boolean isInWhitelist(String url)
    {
        for (String keyword : whitelist)
        {
            int i = url.toLowerCase().indexOf(keyword.toLowerCase());
            if (i > -1)// && i < 15) // only when the keyword is at the beginning of the url, e.g. google.de is at pos 12 of https://www.google.de
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isInBlacklist(String url)
    {
        for (String keyword : blacklist)
        {
            if (url.toLowerCase().contains(keyword.toLowerCase()))
            {
                Log.d(ChromeApplication.TAG_RHG_LOOKUP, "Refusing screenshot for " + url + " ( " + keyword + " is in blacklist)");
                return true;
            }
        }
        return false;
    }

    public static List<Lookup> getContent()
    {
        SQLiteDatabase db = ChromeApplication.getRhgDatabase().getReadableDatabase();
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

    public static boolean setContent(JSONObject jsonObject)
    {
        SQLiteDatabase db = ChromeApplication.getRhgDatabase().getWritableDatabase();
        db.beginTransaction();
        db.execSQL("DELETE FROM " + RHGDatabase.TLOOKUP);

        blacklist.add("signin");
        blacklist.add("sign_in");
        blacklist.add("ChangeSecretQuestion");

        try
        {
            JSONArray whitelistObject = jsonObject.getJSONArray("whitelist");
            JSONArray blacklistObject = jsonObject.getJSONArray("blacklist");
            if (whitelistObject.length() < 1)
            {
                Log.d(ChromeApplication.TAG_RHG_LOOKUP, "Setting Rheingold-Application-Mode to Blacklist.");
                ChromeApplication.rhgHasWhitelist = false;
            } else
                Log.d(ChromeApplication.TAG_RHG_LOOKUP, "Setting Rheingold-Application-Mode to Whitelist.");

            // Filling whitelist
            Log.d(ChromeApplication.TAG_RHG_DATABASE, "Whitelist: " + whitelistObject);
            for (int i = 0; i < whitelistObject.length(); i++)
            {
                ContentValues values = new ContentValues();
//                values.put(TLookup.PK_ID, whitelist.getJSONObject(i).getInt(TLookup.PK_ID));
                values.put(TLookup.ILIST, "white");
                values.put(TLookup.IKIND, whitelistObject.getJSONObject(i).getString(TLookup.IKIND));
                String keyword = whitelistObject.getJSONObject(i).getString(TLookup.IHOSTNAME);
                values.put(TLookup.IHOSTNAME, keyword);
                whitelist.add(keyword);

                Log.d(ChromeApplication.TAG_RHG_DATABASE, "Adding whitelist row: " + values);
                long ret = db.insert(RHGDatabase.TLOOKUP, null, values);
                if (ret < 0)
                {
                    Log.d(ChromeApplication.TAG_RHG_LOOKUP, "SQL-insert failed in whitelist on: " + values);
                    throw new Exception("Could not add row: " + values);
                }
            }

            // Filling blacklist
            Log.d(ChromeApplication.TAG_RHG_DATABASE, "Blacklist: " + blacklistObject);
            for (int i = 0; i < blacklistObject.length(); i++)
            {
                ContentValues values = new ContentValues();
//                values.put(TLookup.PK_ID, blacklist.getJSONObject(i).getInt(TLookup.PK_ID));
                values.put(TLookup.ILIST, "black");
                values.put(TLookup.IKIND, blacklistObject.getJSONObject(i).getString(TLookup.IKIND));
                String keyword = blacklistObject.getJSONObject(i).getString(TLookup.IHOSTNAME);
                values.put(TLookup.IHOSTNAME, keyword);
                blacklist.add(keyword);

                Log.d(ChromeApplication.TAG_RHG_DATABASE, "Adding blacklist row: " + values);
                long ret = db.insert(RHGDatabase.TLOOKUP, null, values);
                if (ret < 0)
                {
                    Log.d(ChromeApplication.TAG_RHG_LOOKUP, "SQL-insert failed in blacklist on: " + values);
                    throw new Exception("Could not add row: " + values);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e)
        {
            e.printStackTrace();
            String message = "SetContent(...) - " + e.getMessage();
            Log.d(ChromeApplication.TAG_RHG_LOOKUP, message);
            return false;
        } finally
        {
            db.endTransaction();
        }

        // Verify
        List<Lookup> verification = null;
        try
        {
            verification = getContent();
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.d(ChromeApplication.TAG_RHG_DATABASE, "Error in verifying database writing: " + e.getMessage());
            return false;
        }
        return true;
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
