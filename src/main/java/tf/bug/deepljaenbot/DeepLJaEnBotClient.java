package tf.bug.deepljaenbot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateMono;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageReferenceData;
import discord4j.rest.util.AllowedMentions;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeepLJaEnBotClient {

    private final GatewayDiscordClient gdc;
    private final DeepLClient deeplClient;

    public static Mono<DeepLJaEnBotClient> create(String token, String deeplToken) {
        return DiscordClient.create(token).login().map(gdc -> new DeepLJaEnBotClient(gdc, deeplToken));
    }

    public DeepLJaEnBotClient(GatewayDiscordClient gdc, String deeplToken) {
        this.gdc = gdc;
        this.deeplClient = new DeepLClient(HttpClient.create(), deeplToken);
    }

    public Mono<Void> registerEvents() {
        Publisher<?> mce =
                this.gdc.on(MessageCreateEvent.class)
                        .flatMap(this::handleMessageCreation);
        Publisher<?> rae =
                this.gdc.on(ReactionAddEvent.class)
                        .flatMap(this::handleReactionAdd);

        return Mono.when(mce, rae);
    }

    private static final Pattern JA_PATTERN =
            Pattern.compile("\\p{sc=Han}|\\p{sc=Hiragana}|\\p{sc=Katakana}", Pattern.UNICODE_CHARACTER_CLASS);

    private static final String WHITE_QUESTION_MARK_ORNAMENT = "\u2754";

    public Publisher<?> handleMessageCreation(MessageCreateEvent mce) {
        String content = mce.getMessage().getContent();

        Matcher contentMatch = JA_PATTERN.matcher(content);

        if(contentMatch.find()) {
            return mce.getMessage().addReaction(ReactionEmoji.unicode(WHITE_QUESTION_MARK_ORNAMENT)).retry(4);
        } else {
            return Mono.empty();
        }
    }

    public Publisher<?> handleReactionAdd(ReactionAddEvent rae) {
        if(rae.getUserId().equals(this.gdc.getSelfId())) {
            return Mono.empty();
        } else {
            Optional<ReactionEmoji.Unicode> unicodeReactOpt = rae.getEmoji().asUnicodeEmoji();
            if (unicodeReactOpt.isPresent()) {
                ReactionEmoji.Unicode unicodeReact = unicodeReactOpt.get();
                if (WHITE_QUESTION_MARK_ORNAMENT.equals(unicodeReact.getRaw())) {
                    return rae.getMessage().retry(4).flatMap(message -> {
                        String content = message.getContent();
                        return this.deeplClient.request(content).flatMap(deepLResponse -> {
                            String english = deepLResponse.getTranslations().get(0).getText();

                            return message.getChannel().flatMap(messageChannel -> {
                                MessageCreateSpec mcs = MessageCreateSpec.builder()
                                        .content(english)
                                        .messageReference(message.getId())
                                        .allowedMentions(AllowedMentions.builder().repliedUser(false).build())
                                        .build();

                                return messageChannel.createMessage(mcs).onErrorResume(e -> {
                                    MessageCreateSpec ecs = MessageCreateSpec.builder()
                                            .content(e.getMessage())
                                            .build();

                                    return messageChannel.createMessage(ecs);
                                });
                            });
                        });
                    });
                } else {
                    return Mono.empty();
                }
            } else {
                return Mono.empty();
            }
        }
    }

}
