package etr.learning;

import etr.learning.Application.Payment;
import etr.learning.Application.PaymentConfirmedEvent;
import etr.learning.Application.PaymentInitiated;

record IncompletePayment(PaymentInitiated initiated, PaymentConfirmedEvent confirmed) {

	IncompletePayment(PaymentInitiated initiated) {
		this(initiated, null);
	}

	IncompletePayment(PaymentConfirmedEvent confirmed) {
		this(null, confirmed);
	}

	IncompletePayment reduce(IncompletePayment other) {
		System.err.println("Reducing " + this + " with " + other);
		if (this.hasBoth())
			return this;
		if (other.hasBoth())
			return other;

		if (this.hasOnlyInitiated() && other.hasOnlyConfirmed())
			return new IncompletePayment(this.initiated, other.confirmed);
		if (this.hasOnlyConfirmed() && other.hasOnlyInitiated())
			return new IncompletePayment(other.initiated, this.confirmed);

		if (this.hasNone())
			return other;
		if (other.hasNone())
			return this;

		return this;
	}

	boolean hasOnlyInitiated() {
		return initiated != null && confirmed == null;
	}

	boolean hasOnlyConfirmed() {
		return initiated == null && confirmed != null;
	}

	boolean hasBoth() {
		return initiated != null && confirmed != null;
	}

	boolean hasNone() {
		return initiated == null && confirmed == null;
	}

	public Payment complete() {
		return new Payment(
				confirmed.paymentId(),
				confirmed.legacyId(),
				initiated.amount(),
				initiated.accountId()
		);
	}
}