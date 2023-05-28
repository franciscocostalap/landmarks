package pt.isel.cn;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.WriteChannel;

import java.io.IOException;
import java.util.logging.Logger;

public class StreamObjectUpload implements IStreamObjectUpload {
    private static final Logger logger = Logger.getLogger(StreamObjectUpload.class.getName());
    private WriteChannel writeChannel;
    private final Storage storage;
    private BlobInfo.Builder blobInfoBuilder;
    private BlobInfo blobInfo;

    public StreamObjectUpload(
            Storage storage,
            BlobInfo.Builder blobInfoBuilder
    ) {
        this.storage = storage;
        this.blobInfoBuilder = blobInfoBuilder;
    }

    /**
     * Stores the object in GCS.
     * @param bytes the object to be stored
     * @param bytesSize the size of the object
     * @return the BlobId of the object stored
     */
    @Override
    public BlobId storeObject(byte[] bytes, int bytesSize) {
        if(writeChannel == null){
            String contentType = ImageContentTypeChecker.getContentType(bytes);
            BlobInfo blobInfo = blobInfoBuilder.setContentType(contentType).build();
            this.writeChannel = this.storage.writer(blobInfo);
            this.blobInfo = blobInfo;
        }

        try {
            writeChannel.write(java.nio.ByteBuffer.wrap(bytes, 0, bytesSize));
            return blobInfo.getBlobId();
        } catch (IOException e) {
            try {
                writeChannel.close();
            } catch (IOException e2) {
                throw new RuntimeException(e2);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the write channel.
     */
    @Override
    public void closeWriteChannel() {
        try {
            writeChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
