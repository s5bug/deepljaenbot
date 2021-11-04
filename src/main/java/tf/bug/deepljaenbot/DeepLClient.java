package tf.bug.deepljaenbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;

public class DeepLClient {

    private final HttpClient httpClient;
    private final String deeplToken;
    private final ObjectMapper objectMapper;

    public DeepLClient(HttpClient httpClient, String deeplToken) {
        this.httpClient = httpClient;
        this.deeplToken = deeplToken;
        this.objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    public Mono<DeepLResponse> request(String ja) {
        return this.httpClient
                .post()
                .uri("https://api-free.deepl.com/v2/translate")
                .sendForm((req, form) -> {
                    form.attr("auth_key", this.deeplToken);
                    form.attr("text", ja);
                    form.attr("source_lang", "JA");
                    form.attr("target_lang", "EN-US");
                })
                .responseContent()
                .aggregate()
                .asString(StandardCharsets.UTF_8)
                .flatMap(s -> Mono.fromCallable(() -> this.objectMapper.readValue(s, DeepLResponse.class)).subscribeOn(Schedulers.boundedElastic()));
    }

}
