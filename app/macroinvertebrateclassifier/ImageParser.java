package com.techbotbuilder.macroinvertebrateclassifier;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by root on 1/27/17.
 */

public class ImageParser {

    public static int IMAGE_DIMENSION = 32; //32 px by 32 px
    public static float SIGMA1 = 0.8f;
    public static float SIGMA2 = 1.3f; //these seem to work for 32x32 images

    public static float[] parseBitmap(Bitmap image){
        /*Make an image usable as input to a neural network
         * Return IMAGE_DIMENSION**2-length array of values in [8,1]
         * that can be fed to the neural network.
         */
        Bitmap resultingImage = asGrayscale(differenceOfGaussians(
                resizeImage(image, IMAGE_DIMENSION, IMAGE_DIMENSION), SIGMA1, SIGMA2));
        int[] pixels = new int[IMAGE_DIMENSION*IMAGE_DIMENSION];
        resultingImage.getPixels(pixels, 0, IMAGE_DIMENSION, 0, 0, IMAGE_DIMENSION, IMAGE_DIMENSION);
        //now want results each in [0,1] range
        float[] results = new float[pixels.length];
        for (int i=0; i<pixels.length; i++) results[i] = (float) pixels[i] / 255;
        return results;
    }

    //Tested, works
    protected static Bitmap resizeImage(Bitmap original, int width, int height){
        /*Resize image to specified size
         * return image scaled so that if needed some image is lost
         * instead of keeping whitespace in the margins
         */
        float widthRescale = (float) width / original.getWidth();
        float heightRescale = (float) height / original.getHeight();
        float rescale = Math.max(widthRescale, heightRescale); //draw cases to convince self this is what need
        int newWidth = (int) (original.getWidth() * rescale);
        int newHeight = (int) (original.getHeight() * rescale);
        Bitmap rescaled = Bitmap.createScaledBitmap(original, newWidth, newHeight, false);
        //this image is possibly bigger on one side than it's supposed to be
        //so let's just look at the middle part of the image
        return Bitmap.createBitmap(rescaled, (newWidth-width)/2, (newHeight-height)/2, width, height);
    }

    //Tested, works
    protected static Bitmap grayscaleImage(Bitmap original){
        /*Convert RGB to grayscale image */
        int width = original.getWidth();
        int height = original.getHeight();
        Bitmap grayscale = Bitmap.createBitmap(width, height, original.getConfig());
        for (int row=0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int pix = original.getPixel(col, row);
                int newpix = (Color.red(pix)*299 + Color.green(pix)*587 + Color.blue(pix)*114)/1000;
                grayscale.setPixel(col, row, Color.rgb(newpix, newpix, newpix));
            }
        }
        return grayscale;
    }

    //Partially tested, probably works ;)
    protected static Bitmap asGrayscale(Bitmap original){
        /*Convert RGB to grayscale
         * Use same algorithm as Python Image Library (PIL), used by Keras.
         * See http://pillow.readthedocs.io/en/3.1.x/reference/Image.html#PIL.Image.Image.convert
         */
        int width = original.getWidth();
        int height = original.getHeight();
        Bitmap grayscale = Bitmap.createBitmap(width, height, original.getConfig());
        for (int row=0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int pix = original.getPixel(col, row);
                int newpix = (Color.red(pix)*299 + Color.green(pix)*587 + Color.blue(pix)*114)/1000;
                grayscale.setPixel(col, row, newpix);
            }
        }
        return grayscale;
    }

    /*
    Difference of Gaussian with much help from Wikipedia and ImageMagick documentation
    https://en.wikipedia.org/wiki/Difference_of_Gaussians
    http://www.imagemagick.org/Usage/convolve/#dog
    */
    //Tested, works
    protected static Bitmap differenceOfGaussians(Bitmap original, float sigma1, float sigma2){
        /*Perform a difference-of-gaussian filter on image original with specified kernal sizes
         * Should have simga1 < sigma2, with sigma2/sigma1 around 1.6
         */
        int width = original.getWidth();
        int height = original.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, original.getConfig());
        //result is mutable
        for (int row=0; row < height; row++){
            for (int col=0; col < width; col++){
                int gauss1 = gaussian(original, col, row, sigma1);
                int gauss2 = gaussian(original, col, row, sigma2);
                int diffOfGaussians = Color.rgb(
                        trimRGB(Color.red(gauss1) - Color.red(gauss2)),
                        trimRGB(Color.green(gauss1) - Color.green(gauss2)),
                        trimRGB(Color.blue(gauss1) - Color.blue(gauss2)));
                result.setPixel(col, row, diffOfGaussians);
            }
        }
        int[] pixels = new int[width*height];
        original.getPixels(pixels,0,width,0,0,width,height);
        return result;
    }
    private static int trimRGB(int val){
        return trim(val, 0, 255);
    }
    private static int trim(int val, int lower, int upper){
        return val < lower ? lower : (val > upper ? upper : val);
    }
    private static int gaussian(Bitmap image, int x, int y, float sigma){
        float[] values = {0,0,0};
        /* Let's limit to a radius-10 (square) kernal so we do not have
         * an imageDimension ^ 4 complexity for processing each image*/
        int radius = 10;
        int xLowerBound = Math.max(0, x-radius);
        int xUpperBound = Math.min(image.getWidth(), x+radius);
        int yLowerBound = Math.max(0, y-radius);
        int yUpperBound = Math.min(image.getHeight(), y+radius);
        for (int row=yLowerBound; row<yUpperBound; row++){
            for (int col=xLowerBound; col < xUpperBound; col++){
                float gaussianFactor = gaussianWithoutConstants(x, y, col, row, sigma);
                int color = image.getPixel(col, row);
                values[0] += Color.red(color) * gaussianFactor;
                values[1] += Color.green(color) * gaussianFactor;
                values[2] += Color.blue(color) * gaussianFactor;
            }
        }
        for (int i=0; i<values.length; i++){
            values[i] = (float) (values[i] / (2*Math.PI*sigma*sigma));
        }
        return Color.rgb((int)values[0], (int)values[1], (int)values[2]);
    }
    private static float gaussianWithoutConstants(int x, int y, int x0, int y0, float sigma){
        /*Perform distance calculation with Gaussian dropoff
         */
        int dx = x-x0;
        int dy = y-y0;
        return (float) Math.exp( -( dx*dx + dy*dy ) / (2 * sigma*sigma) );
    }
}
