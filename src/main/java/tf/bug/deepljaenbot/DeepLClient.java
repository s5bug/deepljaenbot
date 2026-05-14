package tf.bug.deepljaenbot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.List;
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
        DeepLRequest pojo = new DeepLRequest();
        pojo.setText(List.of(ja));
        pojo.setSourceLang("JA");
        pojo.setTargetLang("EN-US");

        return Mono.fromCallable(() -> this.objectMapper.writeValueAsBytes(pojo))
                .flatMap(jsonBytes -> this.httpClient
                        .headers(h -> h.set("Authorization", "DeepL-Auth-Key " + this.deeplToken)
                                .set("Content-Type", "application/json"))
                        .post()
                        .uri("https://api-free.deepl.com/v2/translate")
                        .send((req, outbound) -> outbound.sendByteArray(Mono.just(jsonBytes)))
                        .responseSingle((response, byteBufMono) -> byteBufMono.asByteArray())
                )
                .flatMap(bytes -> Mono.fromCallable(() -> this.objectMapper.readValue(bytes, DeepLResponse.class)))
                .subscribeOn(Schedulers.boundedElastic());
    }

}
