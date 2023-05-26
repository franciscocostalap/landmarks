package pt.isel.cn.vision;

public class LandmarkPrediction {
    private final String name;
    private final float score;
    private final String latitude;
    private final String longitude;

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
        System.out.println(obj);

        System.out.println(this.getName().equals(((LandmarkPrediction) obj).getName()));
        System.out.println(this.getScore() == ((LandmarkPrediction) obj).getScore());
        System.out.println(this.getLatitude().equals(((LandmarkPrediction) obj).getLatitude()));
        System.out.println(this.getLongitude().equals(((LandmarkPrediction) obj).getLongitude()));
        return this.getName().equals(((LandmarkPrediction) obj).getName()) &&
                this.getScore() == ((LandmarkPrediction) obj).getScore() &&
                this.getLatitude().equals(((LandmarkPrediction) obj).getLatitude()) &&
                this.getLongitude().equals(((LandmarkPrediction) obj).getLongitude());
    }
}
