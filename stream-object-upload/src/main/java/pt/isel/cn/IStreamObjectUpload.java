package pt.isel.cn;

import com.google.cloud.storage.BlobId;

import java.io.IOException;

interface IStreamObjectUpload {
    BlobId storeObject(byte[] bytes, int bytesSize) throws IOException;
    void closeWriteChannel() throws IOException;
}
