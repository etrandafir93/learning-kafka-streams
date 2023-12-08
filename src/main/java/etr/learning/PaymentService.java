package etr.learning;

import etr.learning.Application.Payment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
class PaymentService {
	private final List<Payment> payments = new ArrayList<>();

	public void savePayment(Payment payment) {
		System.out.println("Saving payment: " + payment);
		payments.add(payment);
	}

	public List<Payment> getPayments() {
		return payments;
	}

	public void reset() {
		payments.clear();
	}
}
