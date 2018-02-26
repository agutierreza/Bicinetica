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
    
    public static float[] calculatePower(Position pos1, Position pos2) {
    	
    	//Calculating increments
        int secDiff = pos2.getSeconds() - pos1.getSeconds();
		float hDiff = pos2.getAltitude() - pos1.getAltitude();// It should be minus the Position altitude x meters before (25m-50m is fine). A less demanding approximation is to get altitude from the Position instance x/p1.getAltitude() seconds before.

        float grade = hDiff / pos1.getDistance(pos2);
        double beta = Math.atan(grade);
        float betaSin = (float)Math.sin(beta);
        float betaCos = (float)Math.cos(beta);
    	
    	//interpolation of velocity for Position p1 and p2
    	float[] speed = Utilities.linealInterpolation(pos1.getSpeed(), pos2.getSpeed(), secDiff);

    	//interpolation of Latitude and Longitude is only needed at the end of the activity, taking consecutive Position instances given by GPS
    	//float[] lat = Utilities.linealInterpolation(pos1.getLatitude(), pos2.getLatitude(), secDiff);
    	//float[] lon = Utilities.linealInterpolation(pos1.getLongitude(), pos2.getLongitude(), secDiff);

    	//Calculate power from p1.getSeconds()... p2.getSeconds()-1;
    	float[] power = new float[secDiff];// secdiff+1 nodes but no power for the last one

    	for (int i = 0; i < secDiff - 1; i++) {
			float avgSpeed = Utilities.average(speed[i], speed[i + 1]);

			float pKin = (float)(Math.pow(speed[i+1], 2) - Math.pow(speed[i], 2)) * totalMass / 2;
			float pGravity = avgSpeed * gForce * totalMass * betaSin;
			float pDrag = 2 * (float)Math.pow(avgSpeed, 3) * drag * betaSin;
			float pRolling = avgSpeed * cRolling * gForce * betaCos;

    		power[i] = pKin + pGravity + pDrag + pRolling;
    	}

    	return power;
    }
}