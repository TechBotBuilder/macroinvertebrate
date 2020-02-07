package com.techbotbuilder.streamteamohio.classifier;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.techbotbuilder.streamteamohio.Utils.Notifier;
import com.techbotbuilder.streamteamohio.R;

public class ProcessingIntentService extends IntentService {

    public static final String URI_FLAG = "URI";
    public static final String BITMAP_BYTEARRAY_FLAG = "BYTEARRAY";
    public static final String BROADCAST_RESULT = "com.techbotbuilder.streamteamohio.result";

    protected static final int recommendedMaxQueue = 12;
    protected static int currentQueue = 0;

    private static ImageClassifier processor = null;

    /**
     * Creates a new worker thread (queue) called ProcessingIntentService
     * Call startService or startForeground from your class to invoke.
     */
    public ProcessingIntentService(){
        super("ProcessingIntentService");
        processor = LocalNeuralNetwork.getInstance();
        if (processor==null) throw new IllegalStateException("Initialize LocalNeuralNetwork instance " +
                "from context with resource access before starting processing service.");
    }

    //this runs in our worker thread.
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Uri uri;
        Bitmap bitmap=null;
        try {
            uri = intent.getExtras().getParcelable(URI_FLAG);
            byte[] data = intent.getExtras().getByteArray(BITMAP_BYTEARRAY_FLAG);
            bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        }catch(NullPointerException e){
            Log.e("IntentService", "Failed bitmap or uri collection from intent", e);
            currentQueue--;
            return;
        }
        Recognition results = processor.runOn(bitmap, uri);

        //TODO Write to image metadata as comment?

        //Return results to UI.
        //TODO add notifications?
        Intent resultBroadcast = new Intent(BROADCAST_RESULT);
        resultBroadcast.putExtra(BROADCAST_RESULT, results);
        sendBroadcast(resultBroadcast);
        currentQueue--;
    }

    public boolean isQueueFull(){
        return currentQueue >= recommendedMaxQueue;
    }
    private int getQueueSize(){return currentQueue;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (++currentQueue > recommendedMaxQueue){
            //Notifier.notify(this, R.string.warning_rec_max_queue);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        processor.close();
        processor = null;
        super.onDestroy();
    }

}
