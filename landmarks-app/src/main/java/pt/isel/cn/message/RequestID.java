package pt.isel.cn.message;

public class RequestID {
    public final String bucketName;
    public final String blobName;

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

    public String toString() {
        return bucketName + ";" + blobName;
    }
}
