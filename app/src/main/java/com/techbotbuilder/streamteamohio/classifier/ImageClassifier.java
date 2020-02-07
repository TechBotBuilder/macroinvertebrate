package com.techbotbuilder.streamteamohio.classifier;

import android.graphics.Bitmap;
import android.net.Uri;

public interface ImageClassifier {
    /**
     * Do classification on the image file at the given uri
     * @param uri a uri
     * @param bitmap a square, preferably small as possible for network
     * @return an ordered list of classification guesses, most likely first.
     */
    Recognition runOn(Bitmap bitmap, Uri uri);
    void close();
}
