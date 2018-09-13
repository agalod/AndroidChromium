package de.rheingold.observers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import org.chromium.content_public.browser.GestureStateListener;

import java.io.File;

import de.rheingold.service.UploadJobService;
import de.rheingold.service.UploadJobServiceMessenger;
import de.rheingold.service.UploadService;
import github.nisrulz.screenshott.ScreenShott;

import static org.chromium.base.ContextUtils.getApplicationContext;

/*
* Gesture Observer
* */
public class RHGGestureObserver
{
    private static final String TAG = ChromeApplication.TAG_RHG_GESTURE;

    private Tab tab;
    public String loadedUrl = "";
    private ComponentName mServiceComponent;
    Handler mMessageHandler;
    String reasonScroll;

    public RHGGestureObserver(Tab tab)
    {
        this.tab = tab;

        mMessageHandler = new UploadJobServiceMessenger(this.tab.mThemedApplicationContext);

        reasonScroll = tab.mThemedApplicationContext.getString(org.chromium.chrome.browser.R.string.rhg_browseaction_scroll);

        mServiceComponent = new ComponentName(tab.mThemedApplicationContext, UploadJobService.class);
        if(mServiceComponent == null)
            Log.d(ChromeApplication.TAG_RHG_JOBSCHEDULER, "Error: service component could not be created.");
        else
            Log.d(ChromeApplication.TAG_RHG_JOBSCHEDULER, "OK: service component created.");
    }


    public void startUploadJob(String reason, String url, String file)
    {
        JobInfo.Builder builder = new JobInfo.Builder(tab.jobIdCounter++, mServiceComponent);

        boolean requiresUnmetered = true;//mWiFiConnectivityRadioButton.isChecked();
        boolean requiresAnyConnectivity = true;//mAnyConnectivityRadioButton.isChecked();
        if (requiresUnmetered)
        {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        } else if (requiresAnyConnectivity)
        {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        }

        PersistableBundle extras = new PersistableBundle();
        extras.putString("reason", reason);
        extras.putString("tabId", String.valueOf(tab.getId()));
        extras.putString("url", url);
        if (file != null)
            extras.putString("bitmapFile", file);

        builder.setExtras(extras);

        Log.d(TAG, "Scheduling job");
        JobScheduler tm = (JobScheduler) tab.mThemedApplicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(builder.build());
    }

    public GestureStateListener get(final Tab tab)
    {
        return new GestureStateListener()
        {
            public boolean takeScreenshot(final String reason)
            {
                if (tab.getWebContents() == null)
                    return false;
                final String url = tab.getWebContents().getUrl();
                final String tabId = String.valueOf(tab.getId());
                tab.getWebContents().getContentBitmapAsync(
                        Bitmap.Config.ARGB_8888, 1.f, new Rect(), new ContentBitmapCallback()
                        {
                            @Override
                            public void onFinishGetBitmap(Bitmap bitmap, int i)
                            {
                                if (bitmap != null)
                                    try
                                    {

                                        String file = ScreenShott.getInstance()
                                                .saveScreenshotToPicturesFolder(getApplicationContext(), bitmap, "my_screenshot").getAbsolutePath();

                                        startUploadJob(reason, url, file);

                                        // Display a toast
                                        Toast.makeText(getApplicationContext(), "Bitmap Saved at " + file,
                                                Toast.LENGTH_SHORT).show();
                                    } catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                            }
                        });
                return true;
            }

            @Override
            public void onFlingStartGesture(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "FlingEvent-Start: " + scrollOffsetY + ", " + scrollExtentY);
//                takeScreenshot(reasonScroll);
                tab.lastGesture = reasonScroll;
            }

            @Override
            public void onFlingEndGesture(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "FlingEvent-End: " + scrollOffsetY + ", " + scrollExtentY);
//                if (!takeScreenshot(reasonScroll))
//                    Toast.makeText(getApplicationContext(), "Could not take screenshot",
//                            Toast.LENGTH_SHORT).show();
                tab.lastGesture = reasonScroll;
            }

            @Override
            public void onScrollEnded(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "ScrollEvent-End: " + scrollOffsetY + ", " + scrollExtentY);
//                takeScreenshot(reasonScroll);
                tab.lastGesture = reasonScroll;
            }

            @Override
            public void onScrollStarted(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "ScrollEvent-Start: " + scrollOffsetY + ", " + scrollExtentY);
//                takeScreenshot(reasonScroll);
                tab.lastGesture = reasonScroll;
            }

            @Override
            public void onScrollUpdateGestureConsumed()
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "onScrollUpdateGestureConsumed");
//                takeScreenshot();
            }

            @Override
            public void onWindowFocusChanged(boolean hasWindowFocus)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "onWindowFocusChanged: " + hasWindowFocus);
                if (hasWindowFocus)
                    startUploadJob(tab.mThemedApplicationContext.getResources().getString(R.string.rhg_browseaction_focus), tab.getWebContents().getUrl(), null);
//                takeScreenshot();
            }

        };
    }
}
