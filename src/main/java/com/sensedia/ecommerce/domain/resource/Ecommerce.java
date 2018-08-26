package com.sensedia.ecommerce.domain.resource;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Objects;
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
      this.vertx.eventBus().send("ecommerce.payment", body, callback -> {
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

    router.post("/api/purchase").handler(res -> {
      this.vertx.eventBus().send("ecommerce.purchase", res.getBodyAsJson(), callback -> {
        if (callback.succeeded()) {
          log.info("Success call for e-commerce purchase: {}", callback.result().body().toString());
        } else {
          log.error("Problem with e-commerce purchase");
        }
      });
      res.response().setStatusCode(HttpResponseStatus.ACCEPTED.code()).end();
    });

    router.get("/api/purchase").handler(res -> {
      val body = new JsonObject();
      this.vertx.eventBus().send("ecommerce.purchase.get", body, callback -> {
        if (callback.succeeded()) {
          log.info("Success call for e-commerce get purchase list");
          if (Objects.isNull(callback.result().body())) {
            res.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();
          }else{
            res.response().setStatusCode(HttpResponseStatus.OK.code())
              .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
              .end(callback.result().body().toString());
          }
        } else {
          log.error("Problem with e-commerce get purchase list");
          res.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
      });
    });

    router.get("/api/purchase/:id").handler(res -> {
      String cartId = res.request().getParam("id");
      val param = new JsonObject();
      param.put("cartId", cartId);

      this.vertx.eventBus().send("ecommerce.purchase.get.id", param, callback -> {
        if (callback.succeeded()) {
          log
            .info("Success call for e-commerce get purchase whith cartId: {}", cartId);
          if (Objects.isNull(callback.result().body())) {
            res.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end();
          } else {
            res.response().setStatusCode(HttpResponseStatus.OK.code())
              .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
              .end(callback.result().body().toString());
          }
        } else {
          log.error("Problem with e-commerce get purchase with id: {}", cartId);
          res.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
        }
      });
    });

    healthCheck.register("e-commerce", 500, future -> {
      future.complete(Status.OK());
      future.complete(Status.KO());
    });

    httpServer.requestHandler(router::accept).listen(8080);
  }
}
