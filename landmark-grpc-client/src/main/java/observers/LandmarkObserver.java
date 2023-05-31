package observers;

import io.grpc.stub.StreamObserver;

interface LandmarkObserver<T> extends StreamObserver<T> {
    void waitForCompletion() throws InterruptedException;
}