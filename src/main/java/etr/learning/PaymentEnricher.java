package etr.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import etr.learning.Application.PaymentInitiated;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.apache.kafka.common.serialization.Serdes.Long;
import static org.springframework.kafka.support.serializer.JsonDeserializer.USE_TYPE_INFO_HEADERS;


@Component
class PaymentEnricher {

	public static final String PAYMENT_INITIATED_TOPIC = "payment.initiated";
	public static final String PAYMENT_CONFIRMED_TOPIC = "payment.confirmed";

	private final PaymentService paymentService;

	PaymentEnricher(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@Autowired
	void buildPipeline(StreamsBuilder streamsBuilder) {
		KStream<Long, IncompletePayment> initiatedPayments = streamsBuilder
				.stream(PAYMENT_INITIATED_TOPIC, Consumed.with(Long(), fromJson(PaymentInitiated.class)))
				.peek((__, payment) -> System.out.println("Received Payment Initiated: " + payment))
				.map((__, payment) -> new KeyValue<>(payment.paymentId(), new IncompletePayment(payment)));

		KStream<Long, IncompletePayment> confirmedPayments = streamsBuilder
				.stream(PAYMENT_CONFIRMED_TOPIC, Consumed.with(Long(), fromJson(Application.PaymentConfirmedEvent.class)))
				.peek((__, payment) -> System.out.println("Received Payment Confirmed: " + payment))
				.map((__, payment) -> new KeyValue<>(payment.paymentId(), new IncompletePayment(payment)));

		confirmedPayments.merge(initiatedPayments)
				.peek((key, payment) -> System.out.println("Merged streams: key=" + key + ", value=" + payment))
				.groupByKey(Grouped.with(Long(), fromJson(IncompletePayment.class)))
				.reduce(IncompletePayment::reduce, Materialized.with(Long(), fromJson(IncompletePayment.class)))
				.toStream()
				.mapValues((payment) -> {
					System.out.println("[Before Filter] Initiated + Confirmed Payment: " + payment);
					return payment;
				})
				.filter((__, payment) -> payment.hasBoth())
				.peek((__, payment) -> System.out.println("[After Filter] Initiated + Confirmed Payment: " + payment))
				.foreach((__, payment) -> paymentService.savePayment(payment.complete()));
	}

	private static final ObjectMapper objectMapper = JsonMapper.builder().build();

	static <V> Serde<V> fromJson(Class<V> type) {
		JsonDeserializer<V> deserializer = new JsonDeserializer<>(type);
		deserializer.configure(Map.of(USE_TYPE_INFO_HEADERS, false), true);
		return org.apache.kafka.common.serialization.Serdes.serdeFrom(new JsonSerializer<>(objectMapper), deserializer);
	}
}