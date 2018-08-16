package com.sensedia.ecommerce.infra.verticles;

import io.vertx.core.AbstractVerticle;
import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentWorker extends AbstractVerticle {

  public void start() {
    this.vertx.eventBus().consumer("ecommerce.payment", handler -> {
      SecureRandom secureRandom = new SecureRandom();
      if (secureRandom.nextBoolean()) {
        handler.reply(handler.body());
      } else {
        handler.fail(1, "Call fail");
      }
    });
  }
}
