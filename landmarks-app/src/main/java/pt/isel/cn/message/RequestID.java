package pt.isel.cn.message;

public class RequestID {
    public String bucketName;
    public String blobName;

    public RequestID(String bucketName, String blobName) {
        this.bucketName = bucketName;
        this.blobName = blobName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getBlobName() {
        return blobName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

    public String toString() {
        return bucketName + ";" + blobName;
    }
}
