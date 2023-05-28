package pt.isel.cn;

import com.google.cloud.storage.BlobId;

interface IStreamObjectUpload {
    BlobId storeObject(byte[] bytes, int bytesSize);
    void closeWriteChannel();
}
