package com.sensedia.ecommerce.infra.verticles;

import com.sensedia.ecommerce.infra.grpc.CashBackRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class CashbackUndoWorker extends AbstractVerticle {

  public void start() {
    this.vertx.eventBus().consumer("ecommerce.cashback.undo", handler -> {

      val config = new JsonObject()
        .put("host", config().getString("mongoHost"))
        .put("port", config().getInteger("mongoPort"))
        .put("db_name", "ecommerce");

      val obj = new JsonObject(handler.body().toString());

      val client = MongoClient.createShared(this.vertx, config);
      val query = new JsonObject()
        .put("paymentId", obj.getString("paymentId"));

      client.findOne("orders", query, null, res -> {
        if (res.succeeded()) {
          log.info("Success find order by paymentId: {}", obj.getString("paymentId"));

          val order = Json.decodeValue(res.result().toString(),
            com.sensedia.ecommerce.domain.data.Order.class);

          CashBackRequest.send(this.vertx, "undo", config().getString("cashBackHost"),
            config().getInteger("cashBackPort"), order);

          handler.reply("ok");
        } else {
          log.error("Error to find order by paymentId: {}", obj.getString("paymentId"), res.cause());
          handler.fail(1, "Call fail");
        }
      });
    });
  }
}
