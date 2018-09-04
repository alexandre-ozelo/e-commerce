package com.sensedia.ecommerce.infra.verticles;

import com.sensedia.ecommerce.domain.data.Purchase;
import com.sensedia.ecommerce.infra.kafka.Consumer;
import com.sensedia.ecommerce.infra.kafka.Producer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PaymentResponseWorker extends AbstractVerticle {

  public void start() {

    val consumer = Consumer
      .configureConsumer(this.vertx, "payment-response-group", config().getString("kafkaHost"),
        config().getInteger("kafkaPort").toString());

    consumer.handler(record -> {
      log.info("Processing key={}, value={}, partition={}, offset={}", record.key(),
        record.value(), record.partition(), record.offset());

      consumer.commit(ar -> {
        if (ar.succeeded()) {
          log.info("Last read message offset committed");
        }
      });

    });

    consumer.subscribe("payment-response-topic", ar -> {
      if (ar.succeeded()) {
        log.info("payment-response-topic subscribed");
      } else {
        log.error("Could not subscribe payment-response-topic: {}", ar.cause().getMessage());
      }
    });
  }
}
