package de.rheingold.utils;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.widget.Toast;

import org.chromium.content_public.browser.ContentBitmapCallback;

import github.nisrulz.screenshott.ScreenShott;

import static org.chromium.base.ContextUtils.getApplicationContext;

public class TabUtils
{
//    public boolean takeScreenshot(final String reason)
//    {
//        if (tab.getWebContents() == null)
//            return false;
//        final String url = tab.getWebContents().getUrl();
//        final String tabId = String.valueOf(tab.getId());
//        tab.getWebContents().getContentBitmapAsync(
//                Bitmap.Config.ARGB_8888, 1.f, new Rect(), new ContentBitmapCallback()
//                {
//                    @Override
//                    public void onFinishGetBitmap(Bitmap bitmap, int i)
//                    {
//                        if (bitmap != null)
//                            try
//                            {
//
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
