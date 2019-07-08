package open.it.com.petit.Mqtt;

/**
 * Created by user on 2017-11-03.
 */

public interface Mqtt {
    void publish(String pubTopic, byte[] bytes);

    void connect(String subTopic, String pubTopic, String clientId);

    void disConnect();
}
