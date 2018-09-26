package com.sensedia.ecommerce.infra.verticles;

import com.sensedia.ecommerce.infra.grpc.CashBackRequest;
import com.sensedia.ecommerce.infra.kafka.Consumer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
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

      if (record.value().getBoolean("paymentApproval")) {
        this.sendSuccessPayment(record.key());
      } else {
        this.sendFailPayment(record.key());
        this.sendUndoCashBack(record.key());
      }

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

  private void sendUndoCashBack(String paymentId) {
    this.vertx.eventBus()
      .send("ecommerce.cashback.undo", new JsonObject().put("paymentId", paymentId),
        callback -> {
          if (callback.succeeded()) {
            log.info("Success call for e-commerce cashback undo");
          } else {
            log.error("Problem with e-commerce cashback undo");
          }
        });
  }

  private void sendSuccessPayment(String paymentId) {
    this.vertx.eventBus()
      .send("ecommerce.response.success.purchase", new JsonObject().put("paymentId", paymentId),
        callback -> {
          if (callback.succeeded()) {
            log.info("Success call for e-commerce payment success");
          } else {
            log.error("Problem with e-commerce payment success");
          }
        });
  }

  private void sendFailPayment(String paymentId) {
    this.vertx.eventBus()
      .send("ecommerce.response.fail.purchase", new JsonObject().put("paymentId", paymentId),
        callback -> {
          if (callback.succeeded()) {
            log.info("Success call for e-commerce payment fail");
          } else {
            log.error("Problem with e-commerce payment fail");
          }
        });
  }
}
