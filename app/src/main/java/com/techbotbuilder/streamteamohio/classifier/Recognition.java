package com.techbotbuilder.streamteamohio.classifier;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

public class Recognition implements Serializable {
    private float[] confidences;
    private final int[] sortedConfidences;
    private final String title;
    private String uri;
    public float getConfidence(int i) {
        if (i < confidences.length) return confidences[i];
        else return 0;
    }
    public String getTitle(){return title; }
    public Uri getUri(){ return Uri.parse(uri); }
    public void setUri(Uri uri){this.uri = uri.toString();}
    public void setConfidences(float[] confidences){this.confidences = confidences;}

    public Recognition(Uri uri, String title, final float[] confidences){
        setUri(uri);
        this.title = title;
        this.confidences = confidences;
        final int length = confidences.length;
        int[] results = new int[length];
        SortedMap<Integer, Float> confs = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                float diff = confidences[o2]-confidences[o1];
                return (diff>=0) ? ((diff==0) ? o2-o1 : 1) : -1;
            }
        });
        for (int i=0; i<length; i++){
            confs.put(i, confidences[i]);
        }
        Integer[] res = confs.keySet().toArray(new Integer[]{});
        for (int i=0; i<length; i++) results[i] = res[i];
        sortedConfidences = results;
        Log.d("Recognition", this.toString());
    }

    public Recognition(Uri uri, float[] confidences){
        this(uri, uri.getLastPathSegment(), confidences);
    }
    public Recognition(Uri uri){
        this(uri, new float[]{0});
    }

    public int getIndexRanked(int ranking){
        if (ranking < sortedConfidences.length) return sortedConfidences[ranking];
        else return sortedConfidences.length-1;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        else return this.uri.equals(((Recognition)obj).getUri().toString());
    }

    @Override
    public String toString(){
        String result = getTitle() + " " + getUri() + " {";
        for (float f: confidences){
            result += String.format(Locale.getDefault(), " %4.2f", f);
        }
        return result + "}";
    }
}
