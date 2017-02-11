package com.techbotbuilder.macroinvertebrateclassifier;

import android.content.Context;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by root on 1/25/17.
 */

public class Layer {
    private String name;
    private Activation activation;
    private int inSize;
    private int outSize;
    private Context context;

    public Layer(String descriptor, Context context) throws IOException {
        this.context = context;
        parseLayerInfo(descriptor);
    }

    public void parseLayerInfo(String descriptor) throws IOException {
        /**
         * Loads layer information into layer.
         * Layer info should be stored as lines like
         * name,activation,input_size,output_size
         */
        descriptor = descriptor.toLowerCase().trim();
        String[] information = descriptor.split(",");
        name = information[0];
        activation = new Activation(information[1]);
        inSize = Integer.parseInt(information[2]);
        outSize = Integer.parseInt(information[3]);
    }

    public float[] operate(float[] data) throws IOException {
        /*Run this layer on the given data vector (a column vector)
        * We'll assume that data's size is appropriate - eg data.length==inSize.
        * Layer should be binary data of inSize*(outSize+1) 16-bit (short) values.
        * The +1 is for the bias data. The bias should be at the end of each matrix row.
        */
        float[] result = new float[outSize];
        InputStream binaryStream = context.getResources().getAssets().open("layers/"+name);
        DataInputStream shortStream = new DataInputStream(binaryStream);
        //read 16-bits at a time and convert results to a float
        for (int matrixRow=0; matrixRow<outSize; matrixRow++){
            for (int matrixColumn = 0; matrixColumn < data.length; matrixColumn++){
                float matrixElem = shortToFloat(shortStream.readShort());
                result[matrixRow] += matrixElem * data[matrixColumn];
            }
            float biasElem = shortToFloat(shortStream.readShort());
            result[matrixRow] += biasElem;
        }
        return activation.run(result);
    }

    /*Shorts are 16-bit integers, while floats are 32-bit floats.
    * I need to store this data. For a layer with 1024 inputs and
    * outputs, we would need 4mb for floats, while only 2mb for shorts.
    * Here we use simple linear scaling to exchange between them.
    */
    private static short floatToShort(float value){
        /*Convert float to our short representation */
        short result = (short)(value * 2048);
        return result;
    }
    private static float shortToFloat(short value){
        /*Convert our short representation of a float back to a float */
        float result = (float)value / 2048;
        return result;
    }
}
