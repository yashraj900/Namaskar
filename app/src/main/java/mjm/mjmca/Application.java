package mjm.mjmca;

import com.google.firebase.database.FirebaseDatabase;

public class Application extends android.app.Application {
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
