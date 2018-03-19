package bicinetica.com.bicinetica.model;

import java.util.ArrayList;

public class CyclingIndoorPower {

    private float param1;
    private float param2;

    //For now I'm setting the user's parameters here until we define the class User.
    private static final float totalMass = 80;
    private static final float gForce = 9.80665f;
    private static final float cRolling = 0.004f;
    private static final float CdA = 0.32f;
    private static final float rho = 1.226f;
    private static final float drag = CdA * rho / 2;
    //This constant until estimateIndoorParam is finished
    private static final float kinMass =  1.5f;


    CyclingIndoorPower(float param1, float param2){
        this.param1 =param1;
        this.param2=param2;
    }

    public int calculatePower(float s0, float s1) {
        float avgSpeed=Utilities.average(s0,s1);
        float Pkin= (float) (kinMass*(Math.pow(s1, 2)-Math.pow(s0, 2)));
        float Pr= param1 *avgSpeed;
        float Pd=(float) (param2*Math.pow(avgSpeed, 3));
        return (int) (Math.max(0, Pkin+Pr+Pd));
    }
    /**
     * Speed from speed sensor and power from strain gauges power meter. It gives a CdA snapshoot for one second. To
     * @param speedOld
     * @param speedNew is equal to speedOld + 1 second!!!!
     * @param realPower
     * @return
     */
    public static float estimateIndoorCdA(float speedOld, float speedNew, int realPower) {
        float avgSpeed = Utilities.average(speedOld, speedNew);
        float pKin = (float)(Math.pow(speedNew, 2) - Math.pow(speedOld, 2)) * totalMass / 2;
        float pDrag = (float)Math.pow(avgSpeed, 3) * drag;
        float pRolling = avgSpeed * cRolling * gForce * totalMass;
        float power = pKin + pDrag + pRolling;
        float estCdA = 2*(realPower - pKin - pRolling)/(rho*(float) Math.pow(avgSpeed,3));
        return estCdA;
    }



} 