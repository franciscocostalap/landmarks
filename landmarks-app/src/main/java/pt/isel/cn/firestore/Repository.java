package pt.isel.cn.firestore;

import java.util.concurrent.ExecutionException;

public interface Repository<Document, Key> {
   void save(Document document, String name) throws ExecutionException, InterruptedException;
}
