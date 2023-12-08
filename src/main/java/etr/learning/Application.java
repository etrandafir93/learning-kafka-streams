package etr.learning;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafkaStreams
@SpringBootApplication
class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
	KafkaStreamsConfiguration kStreamsConfig(KafkaProperties kafkaProperties) {
		return new KafkaStreamsConfiguration(kafkaProperties.buildStreamsProperties());
	}

	public record PaymentInitiated(long paymentId, int amount, int accountId) {
	}

	public record PaymentConfirmedEvent(long paymentId, int legacyId) {
	}

	public record Payment(long paymentId, int legacyId, int amount, int accountId) {
	}

}