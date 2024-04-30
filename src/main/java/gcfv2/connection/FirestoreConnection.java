package gcfv2.connection;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import gcfv2.util.Constant;

public class FirestoreConnection {

    public static Firestore getConnection() {
        FirestoreOptions firestoreOptions = FirestoreOptions.getDefaultInstance().toBuilder()
                .setProjectId(Constant.PROJECT_ID)
                .build();

        return firestoreOptions.getService();
    }
}
