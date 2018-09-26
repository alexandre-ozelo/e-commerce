package com.sensedia.ecommerce.infra.verticles;

import io.vertx.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PurchaseResponseWorker extends AbstractVerticle {

  public void start() {

    this.vertx.eventBus().consumer("ecommerce.response.success.purchase", handler -> {
      //TODO send purchase response
      log.info("Send success purchase response -> REST");
      handler.reply("ok");
    });

    this.vertx.eventBus().consumer("ecommerce.response.fail.purchase", handler -> {
      //TODO send purchase response
      log.info("Send fail purchase response -> REST");
      handler.reply("ok");
    });
  }
}
