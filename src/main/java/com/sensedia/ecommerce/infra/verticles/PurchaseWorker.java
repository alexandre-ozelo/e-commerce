package com.sensedia.ecommerce.infra.verticles;

import com.sensedia.ecommerce.domain.data.Order;
import com.sensedia.ecommerce.domain.data.Payment;
import com.sensedia.ecommerce.domain.data.Purchase;
import com.sensedia.ecommerce.domain.data.Purchase.Cart.Items;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PurchaseWorker extends AbstractVerticle {

  private JsonObject config;
  private Double cashBackPerc;

  public void start() {

    this.config = new JsonObject()
      .put("host", config().getString("mongoHost"))
      .put("port", config().getInteger("mongoPort"))
      .put("db_name", "ecommerce");

    this.cashBackPerc = config().getDouble("cashBackPerc");

    this.vertx.eventBus().consumer("ecommerce.purchase", handler -> {
      val client = MongoClient.createShared(vertx, config);
      val purchaseObj = new JsonObject(handler.body().toString());
      val purchase = Json.decodeValue(handler.body().toString(), Purchase.class);
      val payment = Payment.builder().id(UUID.randomUUID().toString())
        .user(purchase.getCart().getUser())
        .creditCard(purchase.getCreditCard()).ammount(this.processPayment(purchase)).build();
      val order = Order.builder().user(purchase.getCart().getUser())
        .id(UUID.randomUUID().toString()).paymentId(payment.getId())
        .points(this.processCashback(payment)).store("Beer house").build();

      client.save("purchases", purchaseObj, res -> {
        if (res.succeeded()) {
          log.info("Saved purchase with id " + payment.getId());

          this.vertx.eventBus()
            .send("ecommerce.payment", Json.encode(payment), callback -> {
              if (callback.succeeded()) {
                log.info("Success call for e-commerce payment");
              } else {
                log.error("Problem with e-commerce payment");
              }
            });

          this.vertx.eventBus()
            .send("ecommerce.cashback.register", Json.encode(order), callback -> {
              if (callback.succeeded()) {
                log.info("Success call for e-commerce cashback");
              } else {
                log.error("Problem with e-commerce cashback");
              }

            });
          handler.reply("Ok");
        } else {
          log.error("Error saving purchase: {}, with cause: {}", handler.body().toString(),
            res.cause().getMessage());
          handler.fail(1, "Call fail");
        }
      });
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

  private BigDecimal processPayment(Purchase purchase) {
    return purchase.getCart().getItems().stream().map(Items::getPrice)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Double processCashback(Payment payment) {
    return payment.getAmmount().multiply(BigDecimal.valueOf(this.cashBackPerc / 100)).doubleValue();
  }

}
