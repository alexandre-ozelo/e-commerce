package com.sensedia.ecommerce.infra.verticles;

import com.sensedia.ecommerce.domain.data.Purchase;
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

      val purchase = Json.decodeValue(handler.body().toString(), Purchase.class);

      log.info("Received message message: {}", purchase);

      val record =
        KafkaProducerRecord
          .create("payment-topic", purchase.get_id(), new JsonObject(handler.body().toString()));

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
