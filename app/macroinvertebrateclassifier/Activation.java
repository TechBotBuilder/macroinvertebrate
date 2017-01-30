package com.techbotbuilder.macroinvertebrateclassifier;

/**
 * Created by root on 1/25/17.
 */

class Activation {

    private Nonlinearity nonlinearity;

    Activation(String name){
        switch (name.toLowerCase()){
            case "relu":
                nonlinearity = new Relu();
                break;
            case "softmax":
                nonlinearity = new Softmax();
                break;
            case "sigmoid":
            default:
                nonlinearity = new Sigmoid();
                break;
        }
    }

    public float[] run(float[] data){
        return nonlinearity.activation(data);
    }

    private static class Sigmoid extends Nonlinearity{
        public float activation(float data){
            return 1 / (1 + (float) Math.exp(-data));
        }
    }

    private static class Relu extends Nonlinearity{
        public float activation(float data){
            return (data > 0) ? data : 0;
        }
    }

    private static class Softmax extends Nonlinearity {
        public float activation(float data) {
            return (float) Math.exp(data);
        }
        public float[] activation(float[] data){
            float[] result = super.activation(data);
            float partition = 0;
            for (float value: result) partition += value;
            if (partition <= 0) partition = 1f;
            for (int i=0; i<result.length; i++) result[i] /= partition;
            return result;
        }
    }
}

abstract class Nonlinearity{
    public abstract float activation(float data);
    public float[] activation(float[] data){
        float[] result = new float[data.length];
        for (int i=0; i<data.length; i++) result[i] = activation(data[i]);
        return result;
    }
}