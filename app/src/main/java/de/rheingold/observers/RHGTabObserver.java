package de.rheingold.observers;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.ContextMenu;

import org.chromium.chrome.browser.ChromeApplication;
import org.chromium.chrome.browser.tab.Tab;
import org.chromium.chrome.browser.tab.TabObserver;
import org.chromium.content_public.browser.LoadUrlParams;
import org.chromium.content_public.browser.WebContents;

import de.rheingold.service.UploadJobService;
import de.rheingold.service.UploadJobServiceMessenger;

import static org.chromium.base.ContextUtils.getApplicationContext;

/*
 * Observes tab/url loading events
 *
 * */
public class RHGTabObserver extends RHGObserver implements TabObserver
{
    private static final String TAG = ChromeApplication.TAG_RHG_TABOBSERVER;
    public String loadedUrl = "";

    public RHGTabObserver(Tab tab)
    {
        super(tab);
    }

    @Override
    public void onPageLoadFinished(Tab tab)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);

//        new Handler().postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
        takeScreenshot(tab.getLatestReasonOfUpload());
//            }
//        }, 2000);

    }

    @Override
    public void onLoadStopped(final Tab tab, boolean toDifferentDocument)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName + ": " + toDifferentDocument);

//        new Handler().postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//        takeScreenshot(tab.getLatestReasonOfUpload());
//            }
//        }, 2000);
    }

    @Override
    public void onLoadUrl(Tab tab, LoadUrlParams params, int loadType)
    {
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "onLoadUrl: " + params.getUrl() + " - " + params.getShouldReplaceCurrentEntry());
//        tab.lastReason = reasonTyped;
//        takeScreenshot(reasonTyped);
    }

//    public class PageTransitionTypes {
//        public static final int PAGE_TRANSITION_LINK = 0;
//        public static final int PAGE_TRANSITION_TYPED = 1;
//        public static final int PAGE_TRANSITION_AUTO_BOOKMARK = 2;
//        public static final int PAGE_TRANSITION_AUTO_SUBFRAME = 3;
//        public static final int PAGE_TRANSITION_MANUAL_SUBFRAME = 4;
//        public static final int PAGE_TRANSITION_GENERATED = 5;
//        public static final int PAGE_TRANSITION_AUTO_TOPLEVEL = 6;
//        public static final int PAGE_TRANSITION_FORM_SUBMIT = 7;
//        public static final int PAGE_TRANSITION_RELOAD = 8;
//        public static final int PAGE_TRANSITION_KEYWORD = 9;
//        public static final int PAGE_TRANSITION_KEYWORD_GENERATED = 10;
//        public static final int PAGE_TRANSITION_LAST_CORE = PAGE_TRANSITION_KEYWORD_GENERATED;
//        public static final int PAGE_TRANSITION_CORE_MASK = 0xFF;
//        public static final int PAGE_TRANSITION_BLOCKED = 0x00800000;
//        public static final int PAGE_TRANSITION_FORWARD_BACK = 0x01000000;
//        public static final int PAGE_TRANSITION_FROM_ADDRESS_BAR = 0x02000000;
//        public static final int PAGE_TRANSITION_HOME_PAGE = 0x04000000;
//        public static final int PAGE_TRANSITION_FROM_API = 0x08000000;
//        public static final int PAGE_TRANSITION_CHAIN_START = 0x10000000;
//        public static final int PAGE_TRANSITION_CHAIN_END = 0x20000000;
//        public static final int PAGE_TRANSITION_CLIENT_REDIRECT = 0x40000000;
//        public static final int PAGE_TRANSITION_SERVER_REDIRECT = 0x80000000;
//        public static final int PAGE_TRANSITION_IS_REDIRECT_MASK = 0xC0000000;
//        public static final int PAGE_TRANSITION_QUALIFIER_MASK = 0xFFFFFF00;
//    }

    public enum PageTransitionTypes
    {
        LINK(0), //PAGE_TRANSITION_LINK,0
        TYPED(1), //PAGE_TRANSITION_TYPED,1, 16777224
        BOOKMARKED(2), //PAGE_TRANSITION_AUTO_BOOKMARK,2
        PAGE_TRANSITION_FORM_SUBMIT(7),//7
        RELOAD(8), //PAGE_TRANSITION_RELOAD,8
        FORWARD_BACK(0x01000000),//14
        PAGE_TRANSITION_FROM_ADDRESS_BAR(0x02000000),// (0x02000000),15
        PAGE_TRANSITION_HOME_PAGE(0x04000000);// (0x04000000),16

        public final int type;

        PageTransitionTypes(int i)
        {
            this.type = i;
        }
    }

    @Override
    public void onDidCommitProvisionalLoadForFrame(Tab tab, long frameId, boolean isMainFrame, String url, int transitionType)
    {
        String functionName = new Object()
        {
        }.getClass().getEnclosingMethod().getName();
        if (transitionType != 3) // PAGE_TRANSITION_AUTO_SUBFRAME
            Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName + ": " + transitionType);

        String transitionTypeString = "";
//        if(transitionType < 11)
//        if (transitionType < 2)
        switch (transitionType)
        {
            case 0:
                tab.setLatestReasonOfUpload("LINK");
                break;
            case 1:
                tab.setLatestReasonOfUpload("TYPED");
                break;
            case 2:
                tab.setLatestReasonOfUpload("BOOKMARKED");
                break;
            case 8:
                tab.setLatestReasonOfUpload("RELOAD");
                break;
            case 0x01000000:
                tab.setLatestReasonOfUpload("FORWARD_BACK");
                break;
//            case 0x01000008:
//                tab.setLatestReasonOfUpload("TYPED");
//                break;
//            case 2:
////                transitionTypeString = PageTransitionTypes.values()[transitionType].toString();
//                tab.setLatestReasonOfUpload(transitionTypeString);
//                Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName + " transition: " + transitionTypeString + " - " + isMainFrame);
////                break;
        }
//        else
//            transitionTypeString = String.valueOf(transitionType);


//        PageTransitionTypes. type(String.valueOf(transitionType));
//        PageTransitionTypes t = new PageTransitionTypes(transitionType);
//        tab.setLatestReasonOfUpload(transitionTypeString);
//        tab.lastReason = transitionTypeString;
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
        // happens too often: don't log
//        String functionName = new Object()
//        {
//        }.getClass().getEnclosingMethod().getName();
//        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
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
//        String functionName = new Object()
//        {
//        }.getClass().getEnclosingMethod().getName();
//        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, functionName);
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
//        tab.setLatestReasonOfUpload(reasonTyped);
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
