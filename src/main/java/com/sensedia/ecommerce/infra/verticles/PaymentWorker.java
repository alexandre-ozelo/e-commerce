package com.sensedia.ecommerce.infra.verticles;

import com.sensedia.ecommerce.domain.data.Purchase;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PaymentWorker extends AbstractVerticle {

  public void start() {
    this.vertx.eventBus().consumer("ecommerce.payment", handler -> {

      val purchase = Json.decodeValue(handler.body().toString(), Purchase.class);

      log.info("Received message message: {}", purchase);

      handler.reply(handler.body());
      //handler.fail(1, "Call fail");
    });
  }
}
