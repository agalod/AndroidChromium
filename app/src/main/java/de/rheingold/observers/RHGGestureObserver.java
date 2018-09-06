package de.rheingold.observers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import org.chromium.chrome.browser.ChromeApplication;
import org.chromium.chrome.browser.R;
import org.chromium.chrome.browser.tab.Tab;
import org.chromium.content_public.browser.ContentBitmapCallback;
import org.chromium.content_public.browser.GestureStateListener;

import java.io.File;

import de.rheingold.service.UploadService;
import github.nisrulz.screenshott.ScreenShott;

import static org.chromium.base.ContextUtils.getApplicationContext;

public class RHGGestureObserver
{
    public static GestureStateListener get(final Tab tab)
    {
        return new GestureStateListener()
        {
            public boolean takeScreenshot()
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

                                        File file = ScreenShott.getInstance()
                                                .saveScreenshotToPicturesFolder(getApplicationContext(), bitmap, "my_screenshot");

                                        sendMessage(tab.mThemedApplicationContext.getString(org.chromium.chrome.browser.R.string.rhg_browseaction_scroll), url, file);

                                        // Display a toast
                                        Toast.makeText(getApplicationContext(), "Bitmap Saved at " + file.getAbsolutePath(),
                                                Toast.LENGTH_SHORT).show();
                                    } catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                            }
                        });
                return true;
            }

            private void sendMessage(String reason, String url, File file)
            {
                Intent intent = new Intent(tab.mThemedApplicationContext, UploadService.class);
                intent.putExtra("reason", reason);
                intent.putExtra("tabId", String.valueOf(tab.getId()));
                intent.putExtra("url", url);
                if (file != null)
                    intent.putExtra("bitmapFile", file);
                tab.mThemedApplicationContext.startService(intent);

//                Toast.makeText(getApplicationContext(), "Url-change",
//                        Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onFlingStartGesture(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "FlingEvent-Start: " + scrollOffsetY + ", " + scrollExtentY);
            }

            @Override
            public void onFlingEndGesture(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "FlingEvent-End: " + scrollOffsetY + ", " + scrollExtentY);
                if (!takeScreenshot())
                    Toast.makeText(getApplicationContext(), "Could not take screenshot",
                            Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onScrollEnded(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "ScrollEvent-End: " + scrollOffsetY + ", " + scrollExtentY);
                takeScreenshot();
            }

            @Override
            public void onScrollStarted(int scrollOffsetY, int scrollExtentY)
            {
                Log.d(ChromeApplication.TAG_RHG_GESTURE, "ScrollEvent-Start: " + scrollOffsetY + ", " + scrollExtentY);
                takeScreenshot();
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
                    sendMessage(tab.mThemedApplicationContext.getResources().getString(R.string.rhg_browseaction_focus), tab.getWebContents().getUrl(), null);
//                takeScreenshot();
            }

        };
    }
}
