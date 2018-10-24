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
import org.chromium.content_public.browser.GestureStateListener;

import de.rheingold.service.UploadJobService;
import de.rheingold.service.UploadJobServiceMessenger;
import github.nisrulz.screenshott.ScreenShott;

import static org.chromium.base.ContextUtils.getApplicationContext;

/*
* Gesture Observer
* */
public class RHGGestureObserver extends RHGObserver
{
    private static final String TAG = ChromeApplication.TAG_RHG_GESTURE;

    public RHGGestureObserver(Tab tab)
    {
        super(tab);
    }

    public void startUploadJob(String reason, String url, String file)
    {
        JobInfo.Builder builder = new JobInfo.Builder(tab.jobIdCounter++, mServiceComponent);

        boolean onlyWifi = getApplicationContext().getResources().getBoolean(R.bool.onlywifi);//mWiFiConnectivityRadioButton.isChecked();
        boolean requiresAnyConnectivity = true;//mAnyConnectivityRadioButton.isChecked();
        if (onlyWifi)
        {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        } else if (requiresAnyConnectivity)
        {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        }
//        builder.setRequiresDeviceIdle(true);

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
            @Override
            public void onFlingStartGesture(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "FlingEvent-Start: " + scrollOffsetY + ", " + scrollExtentY);
//                takeScreenshot(reasonScroll);
                tab.setLatestReasonOfUpload(reasonScroll);
            }

            @Override
            public void onFlingEndGesture(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "FlingEvent-End: " + scrollOffsetY + ", " + scrollExtentY);
                takeScreenshot(reasonScroll);
                tab.setLatestReasonOfUpload(reasonScroll);
            }

            @Override
            public void onScrollUpdateGestureConsumed()
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "onScrollUpdateGestureConsumed");
//                takeScreenshot(reasonScroll);
            }

            @Override
            public void onScrollEnded(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "ScrollEvent-End: " + scrollOffsetY + ", " + scrollExtentY);
                takeScreenshot(reasonScroll);
                tab.setLatestReasonOfUpload(reasonScroll);
            }

            @Override
            public void onScrollStarted(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "ScrollEvent-Start: " + scrollOffsetY + ", " + scrollExtentY);
                tab.setLatestReasonOfUpload(reasonScroll);
            }

            @Override
            public void onWindowFocusChanged(boolean hasWindowFocus)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "onWindowFocusChanged: " + hasWindowFocus);
                if (hasWindowFocus)
                    takeScreenshot(reasonFocus);
                else
                    takeScreenshot("idle:idle");
            }

        };
    }
}
