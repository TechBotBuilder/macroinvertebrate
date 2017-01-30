package com.techbotbuilder.macroinvertebrateclassifier;

import android.content.res.Resources;

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

    public Layer(String descriptor) throws IOException {
        parseLayerInfo(descriptor);
    }

    public void parseLayerInfo(String descriptor) throws IOException {
        /**
         * Loads layer information into layer.
         * Layer info should be stored as lines like
         * name,activation
         */
        descriptor = descriptor.toLowerCase().trim();
        String[] information = descriptor.split(",");
        name = information[0];
        activation = new Activation(information[1]);
        inSize = Integer.getInteger(information[2]);
        outSize = Integer.getInteger(information[3]);
    }

    public float[] operate(float[] data) throws IOException {
        /*Run this layer on the given data vector (a column vector)
        * We'll assume that data's size is appropriate - eg data.length==inSize
        * layer should be binary data of inSize*(outSize+1) 16-bit float values
        * the +1 is for the binary layer
        */
        float[] result = new float[outSize];
        InputStream binaryStream = Resources.getSystem().getAssets().open("layers/"+name);
        DataInputStream floatStream = new DataInputStream(binaryStream);
        //read 16-bits at a time and convert results to a float
        for (int matrixRow=0; matrixRow<outSize; matrixRow++){
            float biasElem = 0.0f;
            result[matrixRow] += biasElem;
            for (int matrixColumn = 0; matrixColumn < data.length; matrixColumn++){
                float matrixElem = 0.0f;
                result[matrixRow] += matrixElem * data[matrixColumn];
            }
        }
        return activation.run(result);
    }

    /*Shorts are 16-bit integers, while floats are 32-bit floats.
    * I need to store this data. For a layer with 1024 inputs and
    * outputs, we would need 4mb for floats, while only 2mb for shorts.
    * See https://en.wikipedia.org/wiki/IEEE_floating_point#Basic_and_interchange_formats
    *  for information about floats (single, 32 bits) and halves (our 16-bit things)
    */
    private static short floatToShort(float value){
        /*TODO*/
        /*Convert float to our short representation
         * Float:
         *  - 1 sign bit
         *  -
         */
        short result=0;

        return result;
    }
    private static float shortToFloat(short value){
        /*TODO*/
        /*Convert our short representation of a float back to a float
         */
        float result=0;

        return result;
    }
}
