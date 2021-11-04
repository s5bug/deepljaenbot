package tf.bug.deepljaenbot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DeepLJaEnBot {

    public static void main(String[] args) {
        String propertiesPath = args[0];
        Properties properties = new Properties();
        try(InputStream is = Files.newInputStream(Path.of(propertiesPath))) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String token = properties.getProperty("discord_token");

        String deeplToken = properties.getProperty("deepl_token");

        DeepLJaEnBotClient.create(token, deeplToken).flatMap(DeepLJaEnBotClient::registerEvents).block();
    }

}
