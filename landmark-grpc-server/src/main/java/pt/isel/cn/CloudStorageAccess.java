package pt.isel.cn;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

import static pt.isel.cn.Constants.LANDMARK_BUCKET;

public class CloudStorageAccess {
    private static final Logger logger = Logger.getLogger(CloudStorageAccess.class.getName());
    private static final String delimiter = ";";
    private final Storage storage;

    public CloudStorageAccess(Storage storage) {
        this.storage = storage;
    }

    public byte[] getBlobContent(String id) {
        String[] blobIdParts = id.split(delimiter);
        String bucketName = blobIdParts[0];
        String blobName = blobIdParts[1];

        BlobId blobId = BlobId.of(bucketName, blobName);
        Blob blob = storage.get(blobId);

        byte [] content = null;
        if (blob.getSize() < 1_000_000){
            logger.info("Blob is small, read all its content in one request");
            // Blob is small read all its content in one request
            content = blob.getContent();
            return content;
        } else {
            try (ReadChannel reader = blob.reader()) {
                logger.info("Blob is large, read its content in chunks");
                content = new byte[0];
                ByteBuffer bytes = ByteBuffer.allocate(64 * 1024);
                while (reader.read(bytes) > 0) {
                    logger.info("Read " + bytes.position() + " bytes");
                    bytes.flip();
                    bytes.get(content);
                    bytes.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }
}
