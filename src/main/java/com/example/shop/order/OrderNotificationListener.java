package com.example.shop.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** A sample observer. Replace the log line with an email, a message to Kafka, and so on. */
@Component
class OrderNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationListener.class);

    @EventListener
    public void on(OrderPlacedEvent event) {
        log.info("Order {} for user '{}' is now {}", event.orderId(), event.owner(), event.status());
    }
}
