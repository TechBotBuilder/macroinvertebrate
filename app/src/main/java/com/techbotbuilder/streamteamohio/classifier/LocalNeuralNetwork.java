/*
 Reference https://github.com/tensorflow/tensorflow/tree/master/tensorflow/lite/experimental/support/java

 I made this a singleton to instantiate the model with a context
 */

package com.techbotbuilder.streamteamohio.classifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.techbotbuilder.streamteamohio.R;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;


public class LocalNeuralNetwork implements ImageClassifier {

    private static String modelSaveName;
    private static final int nClasses = 21;
    public static final int imgSize = 299;

    private ImageProcessor imageProcessor;
    private TensorImage tImage;

    private Interpreter tfLite;//the actual model
    private TensorBuffer probabilityBuffer;//container for outputs

    private static LocalNeuralNetwork instance=null;
    public static void updateContext(Context context){
        if (instance == null) {
            instance = new LocalNeuralNetwork(context);
        }
    }
    public static LocalNeuralNetwork getInstance(){
        return instance;
    }


    private LocalNeuralNetwork(Context c){
        //model
        updateModel(c);

        //inputs
        imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(imgSize, imgSize, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0, 255))
                .build();
        tImage = new TensorImage(DataType.FLOAT32);

        //outputs
        probabilityBuffer = TensorBuffer.createFixedSize(new int[]{1,nClasses}, DataType.FLOAT32);
    }

    private void updateModel(Context c){
        //model preferences
        SharedPreferences sharedPref = c.getSharedPreferences(
                c.getString(R.string.preference_file_key), Context.MODE_PRIVATE); //TODO ? might need Context.MODE_MULTI_PROCESS
        modelSaveName = sharedPref.getString("MODEL_NAME", c.getString(R.string.default_model));

        //build model
        try {
            ByteBuffer tfLiteModel
                    = FileUtil.loadMappedFile(c, modelSaveName);
            tfLite = new Interpreter(tfLiteModel);
        }catch (IOException e1){
            Log.e("MIClassifier", "Error reading model " + modelSaveName, e1);
            modelSaveName = c.getString(R.string.default_model);
            try {
                ByteBuffer tfLiteModel
                        = FileUtil.loadMappedFile(c, modelSaveName);
                tfLite = new Interpreter(tfLiteModel);
            }catch(IOException e2){
                Log.e("MIClassifier", "Error reading default model " + modelSaveName, e2);

            }
        }
    }

    @Override
    public Recognition runOn(Bitmap x, Uri uri) {
        Recognition result;
        try {
            tImage.load(x);
            tImage = imageProcessor.process(tImage);
            tfLite.run(tImage.getBuffer(), probabilityBuffer.getBuffer());
            result = new Recognition(uri, probabilityBuffer.getFloatArray());

        }catch (Exception e){
            Log.e("MIClassifier", "Failed to load image " + uri, e);
            result = new Recognition(uri, new float[]{-1,-1,-1});
        }
        x.recycle(); //TODO research if this is a good idea
        return result;
    }

    @Override
    public void close() {
        //tfLite.close();
    }

}
