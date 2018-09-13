package de.rheingold.service;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import org.chromium.chrome.browser.ChromeApplication;

import java.lang.ref.WeakReference;


/**
 * A {@link Handler} allows you to send messages associated with a thread. A {@link Messenger}
 * uses this handler to communicate from {@link UploadJobService}. It's also used to make
 * the start and stop views blink for a short period of time.
 */
public class UploadJobServiceMessenger extends Handler
{

    // Prevent possible leaks with a weak reference.
    private WeakReference<Context> mActivity;

    public UploadJobServiceMessenger(Context activity)
    {
        super(/* default looper */);
        this.mActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg)
    {
        Log.d(ChromeApplication.TAG_RHG_UPLOADSERVICEMESSENGER, "Message received from service: " + msg);

//        View showStartView = mainActivity.findViewById(R.id.onstart_textview);
//        View showStopView = mainActivity.findViewById(R.id.onstop_textview);
        Message m;
        switch (msg.what)
        {
            /*
             * Receives callback from the service when a job has landed
             * on the app. Turns on indicator and sends a message to turn it off after
             * a second.
             */
            case ChromeApplication.UPLOADMSG_SUCCESS:
                // Start received, turn on the indicator and show text.
//                showStartView.setBackgroundColor(getColor(R.color.start_received));
                Toast.makeText(mActivity.get(), "Message uploaded", Toast.LENGTH_LONG).show();

                // Send message to turn it off after a second.
//                m = Message.obtain(this, ChromeApplication.MSG_UNCOLOR_START);
//                sendMessageDelayed(m, 1000L);
                break;
//            /*
//             * Receives callback from the service when a job that previously landed on the
//             * app must stop executing. Turns on indicator and sends a message to turn it
//             * off after two seconds.
//             */
//            case ChromeApplication.MSG_COLOR_STOP:
//                // Stop received, turn on the indicator and show text.
//                showStopView.setBackgroundColor(getColor(R.color.stop_received));
//                updateParamsTextView(msg.obj, "stopped");
//
//                // Send message to turn it off after a second.
//                m = obtainMessage(ChromeApplication.MSG_UNCOLOR_STOP);
//                sendMessageDelayed(m, 2000L);
//                break;
//            case ChromeApplication.MSG_UNCOLOR_START:
//                showStartView.setBackgroundColor(getColor(R.color.none_received));
//                updateParamsTextView(null, "");
//                break;
//            case ChromeApplication.MSG_UNCOLOR_STOP:
//                showStopView.setBackgroundColor(getColor(R.color.none_received));
//                updateParamsTextView(null, "");
//                break;
        }
    }
}