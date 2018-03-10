package bicinetica.com.bicinetica.model;

import java.util.ArrayList;
import java.util.Collection;

import bicinetica.com.bicinetica.data.Position;
import bicinetica.com.bicinetica.data.PositionGPS;

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
    
    /**
     * Gives the best average @seg seconds for powerList
     * 
     * @param powerList List of activity power, second by second
     * @param seg The number of seconds to get the best average of
     */
    public static int cpseg(ArrayList<Integer> powerList, int seg) {
    	int sum = 0, bestSum = 0;
    	for (int i = 0; i < seg; i++) {
    		sum += powerList.get(i);
    	}
    	bestSum = sum;
    	for (int i = seg; i < powerList.size(); i++) {
    		sum += powerList.get(i) - powerList.get(i-seg);
    		if (sum > bestSum) {
    			bestSum = sum;
    		}
    	}
    	return Math.round((float)bestSum / seg);
    }
    
    public static ArrayList<Position> interpola(PositionGPS p1, PositionGPS p2) {
    	ArrayList<Position> positions = new ArrayList<>();
    	
    	//The number of Positions that we are going to add
    	int numPositions = (p2.getSeconds() - (int)p2.getSeconds() == 0) ?
                (int)p2.getSeconds() - (int)p1.getSeconds() + 1 : //This never happens anyway!
                (int)p2.getSeconds() - (int)p1.getSeconds() + 2;
    	//numPositions = (int)p2.getSeconds() - (int)p1.getSeconds() + 2;
    	
    	//line parameters for Lat,Lon,Alt, speed
    	float modLat, modLon, modAlt, modSpd, bLat, bLon, bAlt, bSpd;
    	float duration = p2.getSeconds()-p1.getSeconds();
    	
    	modLat = (p2.getLatitude() - p1.getLatitude()) / duration;
    	modLon = (p2.getLongitude() - p1.getLongitude()) / duration;
    	modAlt = (p2.getAltitude() - p1.getAltitude()) / duration;
    	modSpd = (p2.getSpeed() - p1.getSpeed()) / duration;
    	
    	bLat = p1.getLatitude() - modLat * p1.getSeconds();
    	bLon = p1.getLongitude() - modLon * p1.getSeconds();
    	bAlt = p1.getAltitude() - modAlt * p1.getSeconds();
    	bSpd = p1.getSpeed() - modSpd * p1.getSpeed();
    	
    	//First point using parameters
    	int seconds= (int) p1.getSeconds();
    	float lat, lon, alt, spd;
    	lat = modLat * seconds + bLat;
    	lon = modLon * seconds + bLon;
    	alt = modAlt * seconds + bAlt;
    	spd = modSpd * seconds + bSpd;
    	Position p = new Position(lat, lon, alt, spd, seconds);
    	positions.add(p);
    	
    	//Adding the rest from the slope module :)
    	for (int i = 1; i < numPositions; i++) {
    		lat += modLat;
    		lon += modLon;
    		alt += modAlt;
    		spd += modSpd;
    		seconds++;
    		positions.add(new Position(lat, lon, alt, spd, seconds));
    	}
    	
    	return positions;
    }    

}
