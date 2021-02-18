package de.micromata.paypal.data;

import de.micromata.paypal.PayPalConfig;

import java.util.List;

/**
 * Data classes implementing this interface can be updated using a PATCH call.
 * @see de.micromata.paypal.PayPalConnector#updatePayment(PayPalConfig, String, List)
 */
public interface Updatable {
}
