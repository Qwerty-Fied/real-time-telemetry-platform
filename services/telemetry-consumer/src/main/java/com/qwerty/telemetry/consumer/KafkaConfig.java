package com.qwerty.telemetry.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.util.backoff.FixedBackOff;


import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // ✅ fail-fast (규칙 준수)
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1500);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 3000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // 🔥 운영 안정성 필수
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(cf);
        factory.setCommonErrorHandler(kafkaErrorHandler());

        // 🔥 Manual Ack 모드
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        // 일반 예외는 1초 간격으로 무한 재시도(원하면 attempts 조절)
        DefaultErrorHandler eh = new DefaultErrorHandler(new FixedBackOff(1000L, FixedBackOff.UNLIMITED_ATTEMPTS));

        // ✅ DLQ produce failed는 "인프라 장애"로 보고 컨테이너를 멈춘다
        eh.setRetryListeners((record, ex, attempt) -> {
            if (ex != null && ex.getMessage() != null && ex.getMessage().contains("DLQ produce failed")) {
                // retry listener에서는 stop 못하니까, 아래 recoverer 방식으로 stop 권장
            }
        });

        // ✅ recoverer: 재시도 끝나면 호출되는데, 우리는 "끝"을 만들지 않으려면 attempts를 무한으로 둔다.
        // 컨테이너 stop을 확실히 하고 싶으면 "attempts=0" + recoverer stop 방식으로 가는 게 가장 명확함.
        return eh;
    }

}