package bicinetica.com.bicinetica.model;

public final class Utilities {

    public static float[] linealInterpolation(float a, float b, int n){
        float[] ar = new float[n+1];
        if (n==1) {
            ar[0]=a;
            ar[1]=b;
        }
        else {
            //for n nodes divide (n-1) and for until i<n
            float mesh = (b - a) / (n);
            for (int i=0; i<=n; i++) {
                ar[i] = a + mesh * i;
            }
        }
        return ar;
    }

    public static float average(float... items) {
        float res = 0;
        for (float item : items) res += item;
        return res / items.length;
    }
}
