package com.sensedia.ecommerce.infra.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CashbackWorker extends AbstractVerticle {

  public void start() {
    this.vertx.eventBus().consumer("ecommerce.cashback", handler -> {
      SecureRandom secureRandom = new SecureRandom();
      if (secureRandom.nextBoolean()) {
        handler.reply(handler.body());
      } else {
        handler.fail(1, "Call fail");
      }
    });
  }
}
