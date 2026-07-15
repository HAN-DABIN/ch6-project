package com.example.ch6project.common.config;

import com.example.ch6project.domain.payment.event.PaymentCompletedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    /**
     * Kafka Producer 설정
     *
     * Producer는 애플리케이션에서 Kafka로 메시지를 보내는 역할을 한다.
     * 여기서는 key는 String, value는 PaymentCompletedEvent 객체로 보낸다.
     */
    @Bean
    public ProducerFactory<String, PaymentCompletedEvent> paymentCompletedProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Kafka 브로커 주소
        // docker-compose에서 kafka-1, kafka-2, kafka-3을 각각 9092, 9093, 9094로 열어두었기 때문에 모두 등록한다.
        config.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092,localhost:9093,localhost:9094"
        );

        // Kafka 메시지 value를 JSON으로 변환해주는 Serializer
        // PaymentCompletedEvent 객체를 Kafka에 보낼 때 JSON 형태로 직렬화한다.
        JacksonJsonSerializer<PaymentCompletedEvent> valueSerializer =
                new JacksonJsonSerializer<>();

        // 타입 정보를 Kafka Header에 추가하지 않는다.
        // Consumer에서도 PaymentCompletedEvent.class를 명시해서 역직렬화할 것이기 때문에 헤더 타입 정보가 없어도 된다.
        valueSerializer.setAddTypeInfo(false);

        // ProducerFactory 생성
        // key는 StringSerializer로 직렬화하고,
        // value는 JacksonJsonSerializer로 JSON 직렬화한다.
        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                valueSerializer
        );
    }

    /**
     * KafkaTemplate 설정
     *
     * 실제 코드에서 Kafka로 메시지를 보낼 때 사용하는 객체다.
     * PaymentCompletedProducer에서 이 KafkaTemplate을 주입받아 send()를 호출한다.
     */
    @Bean
    public KafkaTemplate<String, PaymentCompletedEvent> paymentCompletedKafkaTemplate() {
        return new KafkaTemplate<>(paymentCompletedProducerFactory());
    }

    /**
     * Kafka Consumer 설정
     *
     * Consumer는 Kafka 토픽에서 메시지를 읽는 역할을 한다.
     * 여기서는 key는 String, value는 PaymentCompletedEvent 객체로 읽는다.
     */
    @Bean
    public ConsumerFactory<String, PaymentCompletedEvent> paymentCompletedConsumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Kafka 브로커 주소
        config.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092,localhost:9093,localhost:9094"
        );

        // Consumer Group ID
        // 같은 groupId를 가진 Consumer들은 메시지를 나누어 처리한다.
        // 여기서는 인기 메뉴 랭킹 반영용 Consumer 그룹이라는 의미로 menu-ranking-group을 사용한다.
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "menu-ranking-group");

        // Consumer가 처음 실행되어 읽은 offset이 없을 때 어디서부터 읽을지 설정
        // earliest는 토픽에 남아 있는 가장 오래된 메시지부터 읽는다.
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Kafka 메시지 value를 PaymentCompletedEvent 객체로 변환해주는 Deserializer
        JacksonJsonDeserializer<PaymentCompletedEvent> valueDeserializer =
                new JacksonJsonDeserializer<>(PaymentCompletedEvent.class);

        // 역직렬화를 허용할 패키지 지정
        // 보안상 아무 클래스나 역직렬화하지 않도록 신뢰할 패키지를 제한한다.
        valueDeserializer.addTrustedPackages("com.example.ch6project");

        // Producer에서 타입 헤더를 넣지 않도록 했으므로,
        // Consumer도 Kafka Header의 타입 정보에 의존하지 않도록 설정한다.
        valueDeserializer.setUseTypeHeaders(false);

        // ConsumerFactory 생성
        // key는 StringDeserializer로 읽고,
        // value는 JacksonJsonDeserializer로 PaymentCompletedEvent 객체로 읽는다.
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    /**
     * @KafkaListener에서 사용할 Listener Container Factory
     *
     * @KafkaListener는 이 Factory를 통해 Consumer를 생성하고,
     * 지정한 topic의 메시지를 계속 감시한다.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent>
    paymentCompletedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // 위에서 만든 ConsumerFactory를 Listener에 연결한다.
        factory.setConsumerFactory(paymentCompletedConsumerFactory());

        return factory;
    }
}