package etr.learning;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

import static etr.learning.PaymentConfirmedEnricherTest.KAFKA;

@TestConfiguration
class TestConfig {

	@Bean
	public KafkaAdmin admin() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
		return new KafkaAdmin(configs);
	}

	@Bean
	public NewTopic topic1() {
		return TopicBuilder.name(PaymentEnricher.PAYMENT_CONFIRMED_TOPIC)
				.partitions(2)
				.replicas(1)
				.compact()
				.build();
	}
	@Bean
	public NewTopic topic2() {
		return TopicBuilder.name(PaymentEnricher.PAYMENT_INITIATED_TOPIC)
				.partitions(2)
				.replicas(1)
				.compact()
				.build();
	}
}