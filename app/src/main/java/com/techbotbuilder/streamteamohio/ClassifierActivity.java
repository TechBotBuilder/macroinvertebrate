package com.techbotbuilder.streamteamohio;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.techbotbuilder.streamteamohio.Utils.Notifier;
import com.techbotbuilder.streamteamohio.classifier.LocalNeuralNetwork;
import com.techbotbuilder.streamteamohio.classifier.ProcessingIntentService;
import com.techbotbuilder.streamteamohio.classifier.Recognition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static com.techbotbuilder.streamteamohio.classifier.LocalNeuralNetwork.imgSize;

public class ClassifierActivity extends AppCompatActivity implements MyAdapter.ItemClickListener{

    ReceiveMessages processingMessageReceiver = null;
    boolean receiverIsRegistered = false;

    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private static final int TAKE_PHOTO = 1;
    private Uri currentPhotoUri=null;
    private static final int PICK_IMAGE_FILE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classifier);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            findViewById(R.id.capture_button).setClickable(false);
            Notifier.longNotify(this, R.string.no_camera);
        }

        processingMessageReceiver = new ReceiveMessages();

        List<Recognition> data=new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.resultList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(this, data);
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL
        );
        recyclerView.addItemDecoration(dividerItemDecoration);

        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_STREAM)){
            if (intent.getType() != null && intent.getType().contains("image/")) {
                handleIncomingImage(intent);
            }
        }

        LocalNeuralNetwork.updateContext(getApplicationContext());

    }

    @Override
    public void onResume(){
        super.onResume();
        if (!receiverIsRegistered){
            registerReceiver(processingMessageReceiver,new IntentFilter(ProcessingIntentService.BROADCAST_RESULT));
            receiverIsRegistered=true;
        }
    }

    public void captureButtonClick(View v){
        if (!getCameraPermission()) return;
        Intent getPhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (getPhotoIntent.resolveActivity(getPackageManager()) != null){
            File photoFile = getOutputMediaFile();
            if (photoFile == null) {
                Notifier.notify(this, "No file captured");
            } else {
                Uri photoUri = Uri.fromFile(photoFile);
                currentPhotoUri = photoUri;
                //Notifier.longNotify(this, String.valueOf(photoUri));
                getPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(getPhotoIntent, TAKE_PHOTO);
            }
        }
    }

    public void getFileClick(View v){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        if(intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(Intent.createChooser(intent, "Select images to classify"), PICK_IMAGE_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData){
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == RESULT_OK){
            if (requestCode == PICK_IMAGE_FILE && resultData != null){
                Log.i("MIClassifier","Intent result---------- " + resultData.getData());
                List<Uri> uris = new ArrayList<>();
                Uri uri0 = resultData.getData();
                if (uri0==null){
                    ClipData curis = resultData.getClipData();
                    if (curis!=null) {
                        for (int i = 0; i < curis.getItemCount(); i++) {
                            uris.add(curis.getItemAt(i).getUri());
                        }
                    }
                }else{
                    uris.add(uri0);
                }
                for (Uri uri: uris){
                    Log.d("MIClassifier","RESULT " +(uri));
                    classify(uri);
                }
            }else if (requestCode == TAKE_PHOTO){
                Uri uri = currentPhotoUri;
                //Notifier.longNotify(this, String.valueOf(uri));
                Log.d("MIClassifier","RESULT "+(uri));
                if (uri != null) classify(uri);
            }
        }
    }

    private boolean getWritePermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            Notifier.longNotify(this, "Need write permissions to save image to classify");
            return false;
        }else return true;
    }

    private boolean getCameraPermission(){
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Notifier.notify(this, R.string.no_camera);
            return false;
        }else{
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        1);
                Notifier.longNotify(this, "Need write permissions to save image to classify");
                return false;
            }else return true;
        }
    }

    @Override
    public void onItemClick(View v, int pos){
        String message = "You clicked " + mAdapter.getItem(pos) + " on row " + pos + " on view" + v;
        //Notifier.notify(this, message);
        Log.v("MIClassifier", message);
    }

    private File getOutputMediaFile() {
        if(getWritePermissions()) {
            File mediaStorageDirectory = new File(
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "MIClassifier");
            if (!mediaStorageDirectory.exists()) {
                if (!mediaStorageDirectory.mkdirs()) {
                    Log.d("MIClassifier", "Failed to create media output directory");
                    return null;
                }
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File mediaFile;
            mediaFile = new File(mediaStorageDirectory, "MI_" + timeStamp + ".jpg");
            return mediaFile;
        }
        else return null;
    }

    private void handleIncomingImage(Intent incomingIntent){
        Uri uri;
        Bundle b = incomingIntent.getExtras();
        try {
            uri = b.getParcelable(Intent.EXTRA_STREAM);
        }catch(NullPointerException e){
            return;
        }
        classify(uri);
    }


    private void classify(@NonNull Uri uri){
        //Notifier.notify(this, "Will classify " + uri.getPath());
        Recognition x = new Recognition(uri);
        mAdapter.add(x);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ClassifierActivity.this, ProcessingIntentService.class);
                Bundle b = new Bundle();
                b.putByteArray(ProcessingIntentService.BITMAP_BYTEARRAY_FLAG, bitmapToArray(uriToBitmap(uri)));
                b.putParcelable(ProcessingIntentService.URI_FLAG, uri);
                intent.putExtras(b);
                LocalNeuralNetwork.updateContext(ClassifierActivity.this);
                startService(intent);
            }
        });
    }

    //must be run on background thread (Glide)
    private Bitmap uriToBitmap(Uri uri){
        Bitmap x=null;
        try {
            x = Glide.with(this)
                    .asBitmap() //TODO can factor out this far?
                    .load(uri)
                    .centerCrop()
                    .override(imgSize, imgSize)
                    .submit()
                    .get();
        }catch(Exception e){
            Log.e("MIClassifier", "Failed to convert uri to bitmap", e);
        }
        return x;
    }

    private byte[] bitmapToArray(Bitmap bmp){
        //Convert to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(processingMessageReceiver);
        receiverIsRegistered=false;
    }

    @Override
    protected void onDestroy(){
        Intent intent = new Intent(this, ProcessingIntentService.class);
        stopService(intent);
        super.onDestroy();
    }

    public class ReceiveMessages extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null && action.equalsIgnoreCase(ProcessingIntentService.BROADCAST_RESULT)){
                Recognition recognition = (Recognition)intent.getSerializableExtra(
                        ProcessingIntentService.BROADCAST_RESULT);
                if (recognition != null) {
                    mAdapter.update(mAdapter.indexOf(recognition), recognition);
                }
            }
        }
    }

}
