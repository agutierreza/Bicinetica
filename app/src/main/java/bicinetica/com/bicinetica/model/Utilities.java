package bicinetica.com.bicinetica.model;

import java.util.ArrayList;
import java.util.Collection;

import bicinetica.com.bicinetica.data.Position;

public final class Utilities {

    public static ArrayList<Float> linealInterpolation(float a, float b, int n){
        ArrayList<Float> ar = new ArrayList<>();
        if (n==1) {
            ar.add(a);
            ar.add(b);
        }
        else {
            //for n nodes divide (n-1) and for until i<n
            float mesh = (b - a) / n;
            for (int i = 0; i <= n; i++) {
                ar.add(a + mesh * i);
            }
        }
        return ar;
    }

    public static float average(Collection<Float> items) {
        float res = 0;
        for (float item : items) res += item;
        return res / items.size();
    }

    public static float average(float... items) {
        float res = 0;
        for (float item : items) res += item;
        return res / items.length;
    }

    public static float powerAverage(Collection<Position> items) {
        float res = 0;
        for (Position item : items) res += item.getPower();
        return res / items.size();
    }
}
