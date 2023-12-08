package etr.learning;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static etr.learning.PaymentEnricher.PAYMENT_CONFIRMED_TOPIC;
import static etr.learning.PaymentEnricher.PAYMENT_INITIATED_TOPIC;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest(classes = TestConfig.class)
class PaymentConfirmedEnricherTest {
	@Container
	static KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));

	private KafkaProducer<Long, String> producer;

	@Autowired
	private PaymentService paymentService;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.properties.bootstrap.servers", KAFKA::getBootstrapServers);
		registry.add("spring.kafka.streams.application-id", () -> "test_app_id");
	}


	@BeforeEach
	void beforeEach() {
		this.producer = new KafkaProducer<>(Map.of(
				"bootstrap.servers", KAFKA.getBootstrapServers(),
				"client.id", "test_producer",
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName(),
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()
		));
	}

	@Test
	void test() throws Exception {
		publish(PAYMENT_INITIATED_TOPIC, """
				{
					"paymentId": 1234,
					"amount": 100,
					"accountId": 1
				}
				""");
		publish(PAYMENT_CONFIRMED_TOPIC, """
				{
					"paymentId": 1234,
					"legacyId": 9999
				}
				""");

		sleep(5_000L);

		// then
		assertEquals(paymentService.getPayments().size(), 1);

	}

	private void publish(String topic, String body) throws Exception {
		producer.send(new ProducerRecord(topic, 123L, body)).get();
	}


}