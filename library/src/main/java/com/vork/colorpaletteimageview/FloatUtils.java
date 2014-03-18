package com.vork.colorpaletteimageview;

public class FloatUtils {

    public static float weightedAverage(float... values) {
        assert values.length % 2 == 0;

        float sum = 0;
        float sumWeight = 0;

        for (int i = 0; i < values.length; i += 2) {
            float value = values[i];
            float weight = values[i + 1];

            sum += (value * weight);
            sumWeight += weight;
        }

        return sum / sumWeight;
    }

}