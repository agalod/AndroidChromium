package de.rheingold.observers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.ContextMenu;
import android.widget.Toast;

import org.chromium.chrome.browser.ChromeApplication;
import org.chromium.chrome.browser.R;
import org.chromium.chrome.browser.tab.Tab;
import org.chromium.chrome.browser.tab.TabObserver;
import org.chromium.content_public.browser.LoadUrlParams;
import org.chromium.content_public.browser.WebContents;

import de.rheingold.service.UploadService;

import static org.chromium.base.ContextUtils.getApplicationContext;

/*
*
*
* */
public class RHGTabObserver implements TabObserver
{
    private Tab tab;
    public String loadedUrl = "";

    public RHGTabObserver(Tab tab)
    {
        this.tab = tab;
    }

    private void sendMessage(String url)
    {
        Intent intent = new Intent(tab.mThemedApplicationContext, UploadService.class);
        intent.putExtra("reason", tab.mThemedApplicationContext.getResources().getString(R.string.rhg_browseaction_link));
        intent.putExtra("tabId", String.valueOf(tab.getId()));
        intent.putExtra("url", url);
        tab.mThemedApplicationContext.startService(intent);

//        Toast.makeText(getApplicationContext(), "Url-change",
//                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShown(Tab tab)
    {

    }

    @Override
    public void onHidden(Tab tab)
    {

    }

    @Override
    public void onClosingStateChanged(Tab tab, boolean closing)
    {

    }

    @Override
    public void onDestroyed(Tab tab)
    {

    }

    @Override
    public void onContentChanged(Tab tab)
    {
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "onContentChanged");
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
    public void onPageLoadFinished(Tab tab)
    {
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "onPageLoadFinished");
        sendMessage(loadedUrl);

    }

    @Override
    public void onPageLoadFailed(Tab tab, int errorCode)
    {

    }

    @Override
    public void onFaviconUpdated(Tab tab, Bitmap icon)
    {

    }

    @Override
    public void onTitleUpdated(Tab tab)
    {

    }

    @Override
    public void onUrlUpdated(Tab tab)
    {

    }

    @Override
    public void onSSLStateUpdated(Tab tab)
    {

    }

    @Override
    public void onCrash(Tab tab, boolean sadTabShown)
    {

    }

    @Override
    public void onWebContentsSwapped(Tab tab, boolean didStartLoad, boolean didFinishLoad)
    {
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "onWebContentsSwapped");
    }

    @Override
    public void onContextMenuShown(Tab tab, ContextMenu menu)
    {

    }

    @Override
    public void onContextualActionBarVisibilityChanged(Tab tab, boolean visible)
    {

    }

    @Override
    public void onWebContentsInstantSupportDisabled()
    {

    }

    @Override
    public void onLoadStarted(Tab tab, boolean toDifferentDocument)
    {
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "onLoadStarted");
    }

    @Override
    public void onLoadStopped(Tab tab, boolean toDifferentDocument)
    {

    }

    @Override
    public void onLoadProgressChanged(Tab tab, int progress)
    {

    }

    @Override
    public void onUpdateUrl(Tab tab, String url)
    {
        Log.d(ChromeApplication.TAG_RHG_TABOBSERVER, "onUpdateUrl: " + url);
        loadedUrl = url;
    }

    @Override
    public void onToggleFullscreenMode(Tab tab, boolean enable)
    {

    }

    @Override
    public void onDidFailLoad(Tab tab, boolean isProvisionalLoad, boolean isMainFrame, int errorCode, String description, String failingUrl)
    {

    }

    @Override
    public void onDidStartProvisionalLoadForFrame(Tab tab, boolean isMainFrame, String validatedUrl)
    {

    }

    @Override
    public void onDidCommitProvisionalLoadForFrame(Tab tab, long frameId, boolean isMainFrame, String url, int transitionType)
    {

    }

    @Override
    public void onDidNavigateMainFrame(Tab tab, String url, String baseUrl, boolean isNavigationToDifferentPage, boolean isFragmentNavigation, int statusCode)
    {

    }

    @Override
    public void didFirstVisuallyNonEmptyPaint(Tab tab)
    {

    }

    @Override
    public void onDidChangeThemeColor(Tab tab, int color)
    {

    }

    @Override
    public void onDidAttachInterstitialPage(Tab tab)
    {

    }

    @Override
    public void onDidDetachInterstitialPage(Tab tab)
    {

    }

    @Override
    public void onDidStartNavigationToPendingEntry(Tab tab, String url)
    {

    }

    @Override
    public void onBackgroundColorChanged(Tab tab, int color)
    {

    }

    @Override
    public void webContentsCreated(Tab tab, WebContents sourceWebContents, long openerRenderProcessId, long openerRenderFrameId, String frameName, String targetUrl, WebContents newWebContents)
    {

    }

    @Override
    public void onReparentingFinished(Tab tab)
    {

    }
}
