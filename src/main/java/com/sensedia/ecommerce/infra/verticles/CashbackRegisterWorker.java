package com.sensedia.ecommerce.infra.verticles;

import com.sensedia.ecommerce.infra.grpc.CashBackRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class CashbackRegisterWorker extends AbstractVerticle {

  public void start() {
    this.vertx.eventBus().consumer("ecommerce.cashback.register", handler -> {
      val config = new JsonObject()
        .put("host", config().getString("mongoHost"))
        .put("port", config().getInteger("mongoPort"))
        .put("db_name", "ecommerce");
      val client = MongoClient.createShared(vertx, config);

      val order = Json.decodeValue(handler.body().toString(),
        com.sensedia.ecommerce.domain.data.Order.class);

      log.info("Received order: {} for register cashback.", order);

      client.save("orders", new JsonObject(Json.encode(order)), res -> {
        if (res.succeeded()) {

          log.info("Saved order with id " + order.getId());

          CashBackRequest.send(this.vertx, "register", config().getString("cashBackHost"),
            config().getInteger("cashBackPort"), order);

          handler.reply("Ok");
        } else {
          log.error("Error saving order : {}, with cause: {}", order,
            res.cause().getMessage());
          handler.fail(1, "Call fail");
        }
      });
    });
  }
}
