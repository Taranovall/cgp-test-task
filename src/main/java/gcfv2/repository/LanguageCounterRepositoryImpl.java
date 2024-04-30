package gcfv2.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.TransactionOptions;
import gcfv2.connection.FirestoreConnection;
import gcfv2.util.Constant;

import java.util.HashMap;

import static gcfv2.util.Constant.COUNTER;

public class LanguageCounterRepositoryImpl implements LanguageCounterRepository {

    @Override
    public void incrementCounter(String language) {
        Firestore firestore = FirestoreConnection.getConnection();
        DocumentReference docRef = firestore.collection(Constant.COLLECTION_NAME).document(language);

        firestore.runTransaction(t -> {
            DocumentSnapshot document = t.get(docRef).get();
            if (document.exists()) {
                Long currentCounter = document.getLong(COUNTER);
                t.update(docRef, COUNTER, currentCounter + 1);
            } else {
                t.set(docRef, new HashMap<>() {{
                    put(COUNTER, 1);
                }});
            }
            return null;
        }, TransactionOptions.create());
    }
}
