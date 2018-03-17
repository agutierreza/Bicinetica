package bicinetica.com.bicinetica.model;

import java.util.ArrayList;

import bicinetica.com.bicinetica.data.Position;

public class CyclingOutdoorPower {

	//For now I'm setting the user's parameters here until we define the class User.
	private static final float totalMass = 80;
	private static final float gForce = 9.80665f;
	private static final float cRolling = 0.004f;
	private static final float CdA = 0.32f;
	private static final float rho = 1.226f;
	private static final float drag = CdA * rho / 2;
    
    public static float calculatePower(Position pos1, Position pos2) {
    	//Calculating increments
        // It should be minus the Position altitude x meters before (25m-50m is fine). A less demanding approximation is to get altitude from the Position instance x/p1.getAltitude() seconds before.
		float hDiff = pos2.getAltitude() - pos1.getAltitude();

        float grade = hDiff / pos1.getDistance(pos2);
        return calculatePower(pos1, pos2, grade);
    }

	public static float calculatePower(Position pos1, Position pos2, float grade) {
		double beta = Math.atan(grade);
		//Calculate power from p1.getSeconds()... p2.getSeconds()-1;
		float avgSpeed = Utilities.average(pos2.getSpeed(), pos1.getSpeed());
		float pKin = (float)(Math.pow(pos2.getSpeed(), 2) - Math.pow(pos1.getSpeed(), 2)) * totalMass / 2;
		float pGravity = avgSpeed * gForce * totalMass * (float)Math.sin(beta);
		float pDrag = (float)Math.pow(avgSpeed, 3) * drag;
		float pRolling = avgSpeed * cRolling * gForce * totalMass * (float)Math.cos(beta);
		float power = pKin + pGravity + pDrag + pRolling;

		power = Math.max(0, power);
		return Float.isNaN(power) ? 0 : power;
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

    public static float estimateIndoorCdA(ArrayList<Float> speed, ArrayList<Integer> power){
        float estCdA=0;
        //TODO: Second method for estimating CdA
        return estCdA;
    }
}
