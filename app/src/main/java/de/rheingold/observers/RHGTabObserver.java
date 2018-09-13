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
import android.view.ContextMenu;
import android.widget.Toast;

import org.chromium.chrome.browser.ChromeApplication;
import org.chromium.chrome.browser.R;
import org.chromium.chrome.browser.tab.Tab;
import org.chromium.chrome.browser.tab.TabObserver;
import org.chromium.content_public.browser.ContentBitmapCallback;
import org.chromium.content_public.browser.LoadUrlParams;
import org.chromium.content_public.browser.WebContents;

import de.rheingold.service.UploadJobService;
import de.rheingold.service.UploadJobServiceMessenger;
import github.nisrulz.screenshott.ScreenShott;

import static org.chromium.base.ContextUtils.getApplicationContext;

/*
 * Observes tab/url loading events
 *
 * */
public class RHGTabObserver implements TabObserver
{
    private static final String TAG = ChromeApplication.TAG_RHG_TABOBSERVER;

    private Tab tab;
    public String loadedUrl = "";
    private ComponentName mServiceComponent;
    Handler mMessageHandler;
    String RHGreasonScroll;
    String RHGreasonLink;

    public boolean takeScreenshot(final String reason)
    {
        Log.d(ChromeApplication.TAG_RHG_SCREENSHOT, "Taking screenshot in " + getClass().getName());
        if (tab.getWebContents() == null)
        {
            return false;
        }
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


    public RHGTabObserver(Tab tab)
    {
        this.tab = tab;

        mMessageHandler = new UploadJobServiceMessenger(this.tab.mThemedApplicationContext);

        RHGreasonScroll = getApplicationContext().getString(org.chromium.chrome.browser.R.string.rhg_browseaction_scroll);
        RHGreasonLink = getApplicationContext().getString(org.chromium.chrome.browser.R.string.rhg_browseaction_link);

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
        extras.putString("reason", tab.mThemedApplicationContext.getResources().getString(R.string.rhg_browseaction_link));
        extras.putString("tabId", String.valueOf(tab.getId()));
        extras.putString("url", url);
        if (file != null)
            extras.putString("bitmapFile", file);

        builder.setExtras(extras);

        Log.d(TAG, "Scheduling job");
        JobScheduler tm = (JobScheduler) tab.mThemedApplicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(builder.build());
    }


//    private void sendMessage(String url)
//    {
//        Intent intent = new Intent(tab.mThemedApplicationContext, UploadService.class);
//        intent.putExtra("reason", tab.mThemedApplicationContext.getResources().getString(R.string.rhg_browseaction_link));
//        intent.putExtra("tabId", String.valueOf(tab.getId()));
//        intent.putExtra("url", url);
//        tab.mThemedApplicationContext.startService(intent);
//
////        Toast.makeText(getApplicationContext(), "Url-change",
////                Toast.LENGTH_SHORT).show();
//    }

    @Override
    public void onPageLoadFinished(Tab tab)
    {
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "onPageLoadFinished");
        takeScreenshot(RHGreasonLink);

    }

    @Override
    public void onLoadStopped(Tab tab, boolean toDifferentDocument)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName + ": " + toDifferentDocument);
        takeScreenshot(tab.lastGesture);
    }

    @Override
    public void onUpdateUrl(Tab tab, String url)
    {
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "onUpdateUrl: " + url);
        loadedUrl = url;
    }

    @Override
    public void onLoadStarted(Tab tab, boolean toDifferentDocument)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onContentChanged(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onLoadUrl(Tab tab, LoadUrlParams params, int loadType)
    {
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "onLoadUrl: " + params);
    }

    @Override
    public void onPageLoadStarted(Tab tab, String url)
    {
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "onPageLoadStarted: " + url);
        loadedUrl = url;
    }

    @Override
    public void onShown(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onHidden(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onClosingStateChanged(Tab tab, boolean closing)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName + ": " + closing);
    }

    @Override
    public void onDestroyed(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onPageLoadFailed(Tab tab, int errorCode)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName + ": " + errorCode);
    }

    @Override
    public void onFaviconUpdated(Tab tab, Bitmap icon)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onTitleUpdated(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onUrlUpdated(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onSSLStateUpdated(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onCrash(Tab tab, boolean sadTabShown)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onWebContentsSwapped(Tab tab, boolean didStartLoad, boolean didFinishLoad)
    {
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "onWebContentsSwapped");
    }

    @Override
    public void onContextMenuShown(Tab tab, ContextMenu menu)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onContextualActionBarVisibilityChanged(Tab tab, boolean visible)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onWebContentsInstantSupportDisabled()
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onLoadProgressChanged(Tab tab, int progress)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
//        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onToggleFullscreenMode(Tab tab, boolean enable)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onDidFailLoad(Tab tab, boolean isProvisionalLoad, boolean isMainFrame, int errorCode, String description, String failingUrl)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onDidStartProvisionalLoadForFrame(Tab tab, boolean isMainFrame, String validatedUrl)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
//        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onDidCommitProvisionalLoadForFrame(Tab tab, long frameId, boolean isMainFrame, String url, int transitionType)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
//        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onDidNavigateMainFrame(Tab tab, String url, String baseUrl, boolean isNavigationToDifferentPage, boolean isFragmentNavigation, int statusCode)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName + ": " + url + " - " + isNavigationToDifferentPage);
    }

    @Override
    public void didFirstVisuallyNonEmptyPaint(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onDidChangeThemeColor(Tab tab, int color)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onDidAttachInterstitialPage(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onDidDetachInterstitialPage(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onDidStartNavigationToPendingEntry(Tab tab, String url)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void onBackgroundColorChanged(Tab tab, int color)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }

    @Override
    public void webContentsCreated(Tab tab, WebContents sourceWebContents, long openerRenderProcessId, long openerRenderFrameId, String frameName, String targetUrl, WebContents newWebContents)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName + ": " + targetUrl);
    }

    @Override
    public void onReparentingFinished(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
    }
}
