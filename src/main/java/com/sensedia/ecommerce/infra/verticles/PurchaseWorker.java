package com.sensedia.ecommerce.infra.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.security.SecureRandom;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PurchaseWorker extends AbstractVerticle {

  private JsonObject config;

  public PurchaseWorker() {
    this.config = new JsonObject()
      .put("connection_string", "mongodb://localhost:27017")
      .put("db_name", "ecommerce");
  }

  public void start() {
    this.vertx.eventBus().consumer("ecommerce.purchase", handler -> {
      val client = MongoClient.createShared(vertx, config);
      val purchaseObj = new JsonObject(handler.body().toString());

      client.save("purchases", purchaseObj, res -> {
        if (res.succeeded()) {
          val id = res.result();
          log.info("Saved purchase with id " + id);
        } else {
          log.error("Error saving purchase: {}", handler.body().toString());
          res.cause().printStackTrace();
          handler.fail(1, "Call fail");
        }
      });

      this.vertx.eventBus().send("ecommerce.payment", purchaseObj, callback -> {
        if (callback.succeeded()) {
          log.info("Success call for e-commerce payment");
        } else {
          log.error("Problem with e-commerce payment");
        }
      });

      this.vertx.eventBus().send("ecommerce.cashback", purchaseObj, callback -> {
        if (callback.succeeded()) {
          log.info("Success call for e-commerce cashback");
        } else {
          log.error("Problem with e-commerce cashback");
        }
      });

      handler.reply("Ok");
    });

    this.vertx.eventBus().consumer("ecommerce.purchase.get", handler -> {
      val client = MongoClient.createShared(vertx, config);
      val query = new JsonObject();
      client.find("purchases", query, res -> {
        if (res.succeeded()) {
          log.info("Success find purchase list");
          handler.reply(res.result().toString());
        } else {
          log.error("Error get purchase list");
          res.cause().printStackTrace();
          handler.fail(1, "Call fail");
        }
      });
    });

    this.vertx.eventBus().consumer("ecommerce.purchase.get.id", handler -> {
      val client = MongoClient.createShared(vertx, config);
      val cartId = new JsonObject(handler.body().toString()).getValue("cartId");
      val query = new JsonObject()
        .put("cart.id", cartId);
      client.findOne("purchases", query, null, res -> {
        if (res.succeeded()) {
          log.info("Success find with id: {}", cartId);
          handler.reply(res.result());
        } else {
          log.error("Error get purchase with id {}", cartId);
          res.cause().printStackTrace();
          handler.fail(1, "Call fail");
        }
      });
    });
  }
}
