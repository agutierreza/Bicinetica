package bicinetica.com.bicinetica.data;

public class Rodillo {
    private int id;
    private String brand, model;
    private int tension;
    private float paramLineal, paramCubic;
    //add id, brand, model etc
    public float getParamLineal() {
        return paramLineal;
    }

    public void setParamLineal(float paramLineal) {
        this.paramLineal = paramLineal;
    }

    public float getParamCubic() {
        return paramCubic;
    }

    public void setParamCubic(float paramCubic) {
        this.paramCubic = paramCubic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTension() {
        return tension;
    }

    public void setTension(int tension) {
        this.tension = tension;
    }

}