package com.sensedia.ecommerce.infra.verticles;

import com.sensedia.ecommerce.domain.data.Payment;
import com.sensedia.ecommerce.infra.kafka.Producer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PaymentWorker extends AbstractVerticle {

  public void start() {

    val producer = Producer.configureProducer(this.vertx, config().getString("kafkaHost"),
      config().getInteger("kafkaPort").toString());

    this.vertx.eventBus().consumer("ecommerce.payment", handler -> {

      val payment = Json.decodeValue(handler.body().toString(), Payment.class);

      log.info("Received message message: {}", payment);

      val record =
        KafkaProducerRecord
          .create("payment-topic", payment.getId(), new JsonObject(handler.body().toString()));

      producer.write(record, done -> {
        if (done.succeeded()) {
          log.info("Success send message");
          handler.reply("ok");
        } else {
          log.error("Error send message", done.cause());
          handler.fail(1, "Call fail");
        }
      });
    });
  }
}
