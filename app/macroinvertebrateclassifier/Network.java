package com.techbotbuilder.macroinvertebrateclassifier;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by root on 1/25/17.
 */

public class Network {

    private Layer[] layers;
    private static final String categoriesFile = "categories";
    private String[] categories;

    public Network(Context context) throws IOException {
        loadLayers(context);
        loadCategories(context);
    }

    private void loadCategories(Context context) throws IOException {
        /*Category labels should be in order corresponding to alphanumeric order
         * that the category folders had during training
         */
        BufferedReader categoriesInfo = new BufferedReader(
                new InputStreamReader(context.getResources().openRawResource(R.raw.categories)));
        ArrayList<String> categories = new ArrayList<String>();
        String line;
        while ((line = categoriesInfo.readLine()) != null) categories.add(line);
        this.categories = new String[categories.size()];
        categories.toArray(this.categories);
        categoriesInfo.close();
    }

    public void loadLayers(Context context) throws IOException {
        /*Layer descriptors should be in order from input to output*/
        BufferedReader layersInfo = new BufferedReader(
                new InputStreamReader(context.getResources().openRawResource(R.raw.layers)));
        ArrayList<Layer> layers = new ArrayList<Layer>();
        String line;
        while ((line = layersInfo.readLine()) != null) layers.add(new Layer(line, context));
        this.layers = new Layer[layers.size()];
        layers.toArray(this.layers);
        layersInfo.close();
    }

    public String identify(Bitmap image) throws IOException {
        float[] currentData = parseImage(image);
        ////for (Layer layer: layers) currentData = layer.operate(currentData);
        ////int[] bestGuesses = sortedIndices(currentData);
        String result="";
        String formatter="%2.2f  ";//: %s\n";
        ////for(int i=0; i < 5; i++){
        for(int i=0; i < 50; i++){
            ////int guessindex = bestGuesses[i];
            ////result += String.format(formatter, currentData[guessindex], categories[guessindex]);
            result += String.format(formatter,currentData[i]);
        }
        return result;

    }

    private static float[] parseImage(Bitmap image) {
        return ImageParser.parseBitmap(image);
    }

    //Tested, works
    private static int[] sortedIndices(float[] data){
        int[] indices = new int[data.length];
        for (int i=0; i<data.length; i++) indices[i] = i;
        float[] values = new float[data.length];
        System.arraycopy(data, 0, values, 0, data.length);
        combinedSort(values, indices);
        return indices;
    }

    /*
    Thanks to wikipedia for refresher on quicksort
    https://en.wikipedia.org/wiki/Quicksort#Lomuto_partition_scheme
    */
    private static void combinedSort(float[] values, int[] indices){
        combinedSort(values, indices, 0, values.length-1);
    }
    private static void combinedSort(float[] values, int[] indices, int start, int stop){
        /* quicksort indices based on values */
        if (start < stop) {
            int pivotPoint = combinedPartition(values, indices, start, stop);
            combinedSort(values, indices, start, pivotPoint - 1);
            combinedSort(values, indices, pivotPoint + 1, stop);
        }
    }
    private static int combinedPartition(float[] values, int[] indices, int start, int stop){
        float pivotValue = values[stop];
        int swapPos = start;
        for (int i=start; i<stop; i++){
            if (values[i] >= pivotValue){ //Would be <=, but we want high to low
                combinedSwap(values, indices, i, swapPos);
                swapPos += 1;
            }
        }
        combinedSwap(values, indices, swapPos, stop);
        return swapPos;
    }
    private static void combinedSwap(float[] values, int[] indices, int pos1, int pos2){
        float tmpVal = values[pos1];
        int tmpInd = indices[pos1];
        values[pos1] = values[pos2];
        indices[pos1] = indices[pos2];
        values[pos2] = tmpVal;
        indices[pos2] = tmpInd;
    }

}
