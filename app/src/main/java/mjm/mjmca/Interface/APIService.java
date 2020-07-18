package mjm.mjmca.Interface;

import mjm.mjmca.service.Notification.Response;
import mjm.mjmca.service.Notification.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAMkEowHM:APA91bHmqawNK7M7cswW1VPhp1AvNX3OdTgmWW5rZgOp9qOP1TH4PHIWYwmZOR0jt7EUeUpdkC0Zih_AJaWuG5djo7kivn1XnKcmYtA0iq0DBdABbY5B7RHlEa0wVds58PrEz3uCZud2"
            }
    )

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
