package pt.isel.cn.firestore;

import java.util.concurrent.ExecutionException;

public interface Repository<Document, Key> {
    Document getByID(Key key) throws ExecutionException, InterruptedException;
    void save(Document document, String name) throws ExecutionException, InterruptedException;
}
