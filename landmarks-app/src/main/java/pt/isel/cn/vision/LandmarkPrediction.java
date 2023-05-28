package pt.isel.cn.vision;

public class LandmarkPrediction {
    private String name;
    private float score;
    private String latitude;
    private String longitude;

    public LandmarkPrediction() {}

    public LandmarkPrediction(String name, float score, String latitude, String longitude) {
        this.name = name;
        this.score = score;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public float getScore() {
        return score;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "LandmarkPrediction{" +
                "name='" + name + '\'' +
                ", score=" + score +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        System.out.println("LandmarkPrediction.equals");
        if (obj == null) return false;
        if (!(obj instanceof LandmarkPrediction)) return false;
        return this.getName().equals(((LandmarkPrediction) obj).getName()) &&
                this.getScore() == ((LandmarkPrediction) obj).getScore() &&
                this.getLatitude().equals(((LandmarkPrediction) obj).getLatitude()) &&
                this.getLongitude().equals(((LandmarkPrediction) obj).getLongitude());
    }
}
