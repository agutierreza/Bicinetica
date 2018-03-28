package bicinetica.com.bicinetica.model;

import java.util.ArrayList;

public class CyclingIndoorPower {


    private static final float cRolling = 5.97f;
    private static final float CdA = 0.179f;
    private static final float kinMass =  3.5f;


    /**
     * Speed is in m/s. It calculates the power exerted during one second [t_0,t_0+1] in which the speed changes from s0 (instant t_0) to s1 (instant t_0+1).
     */
    public static int calculatePower(float s0, float s1) {
        float avgSpeed=Utilities.average(s0,s1);
        float Pkin= (float) (kinMass*(Math.pow(s1, 2)-Math.pow(s0, 2)));
        float Pr= cRolling *avgSpeed;
        float Pd=(float) (CdA*Math.pow(avgSpeed, 3));
        return (int) (Math.max(0, Pkin+Pr+Pd));
    }


    /**
     * Speed from speed sensor and power from strain gauges power meter. It gives a CdA snapshoot for one second. To
     * @param speedOld
     * @param speedNew is equal to speedOld + 1 second!!!!
     * @param realPower
     * @return
     */
    /* This class is for indoor but in a static trainer, but this method is intended to be used in a velodrome.
    public static float estimateIndoorCdA(float speedOld, float speedNew, int realPower) {
        float avgSpeed = Utilities.average(speedOld, speedNew);
        float pKin = (float)(Math.pow(speedNew, 2) - Math.pow(speedOld, 2)) * totalMass / 2;
        float pDrag = (float)Math.pow(avgSpeed, 3) * drag;
        float pRolling = avgSpeed * cRolling * gForce * totalMass;
        float power = pKin + pDrag + pRolling;
        float estCdA = 2*(realPower - pKin - pRolling)/(rho*(float) Math.pow(avgSpeed,3));
        return estCdA;
    }
    */


} 