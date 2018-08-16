package com.sensedia.ecommerce.domain.resource;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class Ecommerce extends AbstractVerticle {

  @Override
  public void start() {

    val router = Router.router(vertx);
    val healthCheck = HealthCheckHandler.create(vertx);
    val httpServer = this.vertx.createHttpServer();

    router.get("/api/healthcheck").handler(healthCheck);

    router.route().handler(BodyHandler.create());

    router.post("/api/payment").handler(res -> {
      val body = res.getBodyAsJson();
      this.vertx.eventBus().send("e-commerce.payment", body, callback -> {
        if (callback.succeeded()) {
          log.info("Success call for e-commerce payment: {}", callback.result().body().toString());
        } else {
          log.error("Problem with e-commerce payment");
        }
      });

      this.vertx.eventBus().send("ecommerce.cashback", body, callback -> {
        if (callback.succeeded()) {
          log.info("Success call for e-commerce cashback: {}", callback.result().body().toString());
        } else {
          log.error("Problem with e-commerce casback");
        }
      });

      res.response().setStatusCode(HttpResponseStatus.ACCEPTED.code()).end();
    });

    healthCheck.register("e-commerce", 500, future -> {
      future.complete(Status.OK());
      future.complete(Status.KO());
    });

    httpServer.requestHandler(router::accept).listen(8080);
  }
}
