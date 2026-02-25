package com.genesis.unipocket.global.config;

import com.genesis.unipocket.tempexpense.common.infrastructure.sse.ParsingProgressPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

	public static final String PARSE_NOTIFY_CHANNEL = "tempexpense:parse-notify";

	@Bean
	public ChannelTopic parseNotifyTopic() {
		return new ChannelTopic(PARSE_NOTIFY_CHANNEL);
	}

	@Bean
	public MessageListenerAdapter parseNotifyListenerAdapter(ParsingProgressPublisher publisher) {
		return new MessageListenerAdapter(publisher, "onPubSubNotification");
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
			RedisConnectionFactory connectionFactory,
			MessageListenerAdapter parseNotifyListenerAdapter,
			ChannelTopic parseNotifyTopic) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(parseNotifyListenerAdapter, parseNotifyTopic);
		return container;
	}
}
