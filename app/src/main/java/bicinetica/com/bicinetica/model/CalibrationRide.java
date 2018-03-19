package bicinetica.com.bicinetica.model;

import java.util.ArrayList;

import bicinetica.com.bicinetica.data.CalibrationPoint;
import bicinetica.com.bicinetica.data.Rodillo;

public class CalibrationRide {


    private ArrayList<CalibrationPoint> data;



    public float getLastSpeed(CalibrationPoint point) {
        int i = data.indexOf(point);
        if (i == -1) {
            return 0;
        }
        return data.get(i - 1).getSpeed();
    }

    public void estimateIndoorParam(Rodillo unRodillo){
        float Crr = 0, CdA = 0;

        //TODO: Second method for estimating CdA
        //Change units:speed data from km/h to m/s
        for (int i=0; i<data.size();i++) {
            float speed=data.get(i).getSpeed()/3.6f;
            data.get(i).setSpeed(speed);;
        }
        //create matrix from data: matrixData= column1: speed column2: power
        //create matrix X= column1: speed column2: speed^3
        //create matrix product=X'*X
        //create vector v=product^(-1)*X'
        //create vector [paramLin parmCubic]=v*matrixData
    }
    public  void Calibration(Rodillo aRodillo) {
        float param1 = 0, param2 = 0;
        //Remove CalibrationPoints in which power is zero and speed>0









        aRodillo.setParamLineal(param1);
        aRodillo.setParamCubic(param2);
    }


}