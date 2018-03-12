package bicinetica.com.bicinetica.model;

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
		float hDiff = pos2.getAltitude() - pos1.getAltitude();// It should be minus the Position altitude x meters before (25m-50m is fine). A less demanding approximation is to get altitude from the Position instance x/p1.getAltitude() seconds before.

        float grade = hDiff / pos1.getDistance(pos2);
        double beta = Math.atan(grade);
    	
    	//interpolation of velocity for Position p1 and p2

    	//interpolation of Latitude and Longitude is only needed at the end of the activity, taking consecutive Position instances given by GPS
    	//float[] lat = Utilities.linealInterpolation(pos1.getLatitude(), pos2.getLatitude(), secDiff);
    	//float[] lon = Utilities.linealInterpolation(pos1.getLongitude(), pos2.getLongitude(), secDiff);

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
}
