package bicinetica.com.bicinetica;

import java.util.Arrays;

public class CyclingOutdoorPower {
    
    public static float[] linterpol(float a, float b, int n){
		float[] ar = new float[n+1];
		if (n==1) {
			ar[0]=a;
			ar[1]=b;
		}else {
			//for n nodes divide (n-1) and for until i<n
			float mesh=(b-a)/(n);
			for (int i=0;i<=n;i++) {
				ar[i]=a+mesh*i;
				//System.out.printf("val %d: %f\n",i,ar[i]);
			}
		}
    	return ar;
    }
    
    public static float distance(Position pos1, Position pos2) {
    	float geodesica, dlat, dlon, meanlat;
		geodesica=40030000;
		dlat=(pos2.getLatitude()-pos1.getLatitude())*(geodesica/360);
		meanlat=0.5f*(pos1.getLatitude()+pos2.getLatitude());
		dlon=(float) ((pos2.getLongitude()-pos1.getLongitude())*Math.sin(Math.toRadians(90-meanlat))*geodesica/360);
    	return (float) Math.sqrt(Math.pow(dlat, 2)+Math.pow(dlon, 2));
    }
    
    public static float[] power(Position pos1, Position pos2) {
    	//For now I'm setting the user's parameters here until we define the class User.
    	float totalMass, cRolling, gForce, CdA, rho, drag;
    	totalMass=80;
    	gForce=9.80665f;
    	cRolling=0.004f;
    	CdA=0.32f;
    	rho=1.226f;
    	drag=0.5f*CdA*rho;
    	
    	//Calculating increments
    	float hdiff, dist, grade, beta;
    	int secdiff;
    	hdiff=(float) (pos2.getAltitude()-pos1.getAltitude());// It should be minus the Position altitude x meters before (25m-50m is fine). A less demanding approximation is to get altitude from the Position instance x/p1.getAltitude() seconds before.
    	dist=distance(pos1,pos2);
    	secdiff=pos2.getSeconds()-pos1.getSeconds();
    	grade=hdiff/dist;
    	beta=(float) Math.atan(grade);
    	
    	//interpolation of velocity for Position p1 and p2
    	float [] speed=linterpol(pos1.getSpeed(),pos2.getSpeed(),secdiff);
    	//interpolation of Latitude and Longitude is only needed at the end of the activity, taking consecutive Position instances given by GPS
    	//float [] lat=linterpol(pos1.getLatitude(),pos2.getLatitude(),secdiff);
    	//float [] lon=linterpol(pos1.getLongitude(),pos2.getLongitude(),secdiff);    	
    	//Calculate power from p1.getSeconds()... p2.getSeconds()-1;
    	float [] power = new float[secdiff];// secdiff+1 nodes but no power for the last one
    	for (int i=0;i<secdiff-1;i++) {
    		float pKin, pGravity, pDrag, pRolling, avgSpeed;
    		avgSpeed=(speed[i]+speed[i+1])*0.5f;
    		pKin=(float) ((Math.pow(speed[i+1],2)-Math.pow(speed[i], 2))*0.5*totalMass);
    		pGravity= (float) (avgSpeed*gForce*totalMass*Math.sin(beta));
    		pDrag=(float) (2*Math.pow(avgSpeed,3)*drag*Math.sin(beta));
    		pRolling=(float) (avgSpeed*cRolling*gForce*Math.cos(beta));
    		power[i]=pKin+pGravity+pDrag+pRolling;
    	}
    	return power;
    }
}