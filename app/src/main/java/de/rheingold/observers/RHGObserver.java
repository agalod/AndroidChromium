package de.rheingold.observers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import org.chromium.chrome.browser.ChromeApplication;
import org.chromium.chrome.browser.R;
import org.chromium.chrome.browser.tab.Tab;
import org.chromium.content_public.browser.ContentBitmapCallback;

import de.rheingold.database.TLookup;
import de.rheingold.service.UploadJobService;
import de.rheingold.service.UploadJobServiceMessenger;
import github.nisrulz.screenshott.ScreenShott;

import static org.chromium.base.ContextUtils.getApplicationContext;

abstract public class RHGObserver
{
    private static final String TAG = ChromeApplication.TAG_RHG_OBSERVER;

    protected Tab tab;
    protected ComponentName mServiceComponent;
    protected Handler mMessageHandler;
    protected String reasonScroll;
    protected String reasonLink;
    protected String reasonTyped;
    protected String reasonFocus;
    public String loadedUrl = "";

    public RHGObserver(Tab tab)
    {
        this.tab = tab;

        mMessageHandler = new UploadJobServiceMessenger(this.tab.mThemedApplicationContext);

        reasonScroll = tab.mThemedApplicationContext.getString(org.chromium.chrome.browser.R.string.rhg_browseaction_scroll);
        reasonFocus = tab.mThemedApplicationContext.getString(org.chromium.chrome.browser.R.string.rhg_browseaction_focus);
        reasonLink = getApplicationContext().getString(org.chromium.chrome.browser.R.string.rhg_browseaction_link);
        reasonTyped = getApplicationContext().getString(org.chromium.chrome.browser.R.string.rhg_browseaction_typed);

        mServiceComponent = new ComponentName(tab.mThemedApplicationContext, UploadJobService.class);
        if (mServiceComponent == null)
            Log.d(ChromeApplication.TAG_RHG_JOBSCHEDULER, "Error: service component could not be created.");
        else
            Log.d(ChromeApplication.TAG_RHG_JOBSCHEDULER, "OK: service component created.");
    }

    protected boolean takeScreenshot(final String reason)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "Taking screenshot because of " + reason + " in " + getClass().getName());

        if (tab.getWebContents() == null)
        {
            Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "Could not get web contents in " + getClass().getName());
            return false;
        }
        final String url = tab.getWebContents().getUrl();
        final String tabId = String.valueOf(tab.getId());
        if (!ChromeApplication.rhgScreenshots || !TLookup.isAllowed(url, ChromeApplication.rhgHasWhitelist))
        {
            startUploadJob(reason, url, null);
            return true;
        }

        tab.getWebContents().getContentBitmapAsync(
                Bitmap.Config.ARGB_8888, 1.f, new Rect(), new ContentBitmapCallback()
                {
                    @Override
                    public void onFinishGetBitmap(Bitmap bitmap, int i)
                    {
                        String functionName = new Object()
                        {
                        }.getClass().getEnclosingMethod().getName();
                        Log.d(ChromeApplication.TAG_RHG_SCREENSHOT, functionName + ": " + (i == 0 ? "OK" : "FAILED:" + i) + " - " + bitmap);

//                                public static final int SUCCESS = 0;
//                                public static final int FAILED = 1;
//                                public static final int SURFACE_UNAVAILABLE = 2;
//                                public static final int BITMAP_ALLOCATION_FAILURE = 3;

                        if (bitmap == null && i < 2)
                            takeScreenshot(reason);
                        else if (i > 1)
                        {
                            Log.d(ChromeApplication.TAG_RHG_SCREENSHOT, "Screenshot callback error code: " + i);
                        } else if (bitmap != null)
                        {
                            try
                            {
                                String file = ScreenShott.getInstance()
                                        .saveScreenshotToPicturesFolder(getApplicationContext(), bitmap, "my_screenshot").getAbsolutePath();
                                Log.d(ChromeApplication.TAG_RHG_SCREENSHOT, "Saved screenshot to " + file);

                                startUploadJob(reason, url, file);

                                // Display a toast
                                if (ChromeApplication.rhgDebugMode)
                                    Toast.makeText(getApplicationContext(), "Bitmap Saved at " + file,
                                            Toast.LENGTH_SHORT).show();
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        } else
                        {

                        }
                    }
                });
        return true;
    }


    public void startUploadJob(String reason, String url, String file)
    {
        JobInfo.Builder builder = new JobInfo.Builder(tab.jobIdCounter++, mServiceComponent);

        boolean onlyWifi = getApplicationContext().getResources().getBoolean(R.bool.onlywifi);
//        boolean requiresAnyConnectivity = true;//mAnyConnectivityRadioButton.isChecked();
        if (onlyWifi)
        {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        } else //if (requiresAnyConnectivity)
        {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        }

        PersistableBundle extras = new PersistableBundle();
        extras.putString("reason", reason);
        extras.putString("tabId", String.valueOf(tab.getId()));
        extras.putString("url", url);
        if (file != null)
        {
            Log.d(TAG, "Scheduling job with bitmpap. OnlyWifi: " + onlyWifi);
            extras.putString("bitmapFile", file);
        } else
            Log.d(TAG, "Scheduling job without bitmap");

        builder.setExtras(extras);

        JobScheduler tm = (JobScheduler) tab.mThemedApplicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(builder.build());
    }


//    public void startUploadJob(String reason, String url, String file)
//    {
//        JobInfo.Builder builder = new JobInfo.Builder(tab.jobIdCounter++, mServiceComponent);
//
//        boolean requiresUnmetered = true;//mWiFiConnectivityRadioButton.isChecked();
//        boolean requiresAnyConnectivity = true;//mAnyConnectivityRadioButton.isChecked();
//        if (requiresUnmetered)
//        {
//            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
//        } else if (requiresAnyConnectivity)
//        {
//            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
//        }
//
//        PersistableBundle extras = new PersistableBundle();
//        extras.putString("reason", reason);
//        extras.putString("tabId", String.valueOf(tab.getId()));
//        extras.putString("url", url);
//        if (file != null)
//            extras.putString("bitmapFile", file);
//
//        builder.setExtras(extras);
//
//        Log.d(TAG, "Scheduling job");
//        JobScheduler tm = (JobScheduler) tab.mThemedApplicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        tm.schedule(builder.build());
//    }
//
//    public boolean takeScreenshot(Tab tab, final String reason)
//    {
//        Log.d(ChromeApplication.TAG_RHG_SCREENSHOT, "Taking screenshot because of " + reason + " in " + getClass().getName());
//        if (tab.getWebContents() == null)
//        {
//            Log.d(ChromeApplication.TAG_RHG_SCREENSHOT, "Could not get web contents in " + getClass().getName());
//            return false;
//        }
//        final String url = tab.getWebContents().getUrl();
//        final String tabId = String.valueOf(tab.getId());
//        tab.getWebContents().getContentBitmapAsync(
//                Bitmap.Config.ARGB_8888, 1.f, new Rect(), new ContentBitmapCallback()
//                {
//                    @Override
//                    public void onFinishGetBitmap(Bitmap bitmap, int i)
//                    {
////                                public static final int SUCCESS = 0;
////                                public static final int FAILED = 1;
////                                public static final int SURFACE_UNAVAILABLE = 2;
////                                public static final int BITMAP_ALLOCATION_FAILURE = 3;
//                        String functionName = new Object()
//                        {
//                        }.getClass().getEnclosingMethod().getName();
//                        Log.d(ChromeApplication.TAG_RHG_SCREENSHOT, functionName + ": " + (i==0?"OK":"FAILED:"+i) + " - " + bitmap);
//                        if (bitmap != null)
//                            try
//                            {
//                                String file = ScreenShott.getInstance()
//                                        .saveScreenshotToPicturesFolder(getApplicationContext(), bitmap, "my_screenshot").getAbsolutePath();
//
//                                startUploadJob(reason, url, file);
//
//                                // Display a toast
//                                Toast.makeText(getApplicationContext(), "Bitmap Saved at " + file,
//                                        Toast.LENGTH_SHORT).show();
//                            } catch (Exception e)
//                            {
//                                e.printStackTrace();
//                            }
//                    }
//                });
//        return true;
//    }
}
