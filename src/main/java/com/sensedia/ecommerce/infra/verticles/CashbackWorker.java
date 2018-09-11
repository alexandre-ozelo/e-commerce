package com.sensedia.ecommerce.infra.verticles;

import com.sensedia.cashback.infra.proto.Order;
import com.sensedia.cashback.infra.proto.RegisterCashbackRequest;
import com.sensedia.cashback.infra.proto.RegisterCashbackTransactionGrpc;
import com.sensedia.cashback.infra.proto.RegisterCashbackTransactionGrpc.RegisterCashbackTransactionVertxStub;
import com.sensedia.cashback.infra.proto.Store;
import com.sensedia.cashback.infra.proto.User;
import com.sensedia.ecommerce.domain.data.Purchase;
import io.grpc.ManagedChannel;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.grpc.VertxChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class CashbackWorker extends AbstractVerticle {

  public void start() {
    this.vertx.eventBus().consumer("ecommerce.cashback", handler -> {
      val purchase = Json.decodeValue(handler.body().toString(), Purchase.class);

      log.info("Received cashback message: {}", purchase);

      ManagedChannel channel = VertxChannelBuilder
        .forAddress(vertx, config().getString("cashBackHost"), config().getInteger("cashBackPort"))
        .usePlaintext(true)
        .build();

      final RegisterCashbackTransactionVertxStub stub = RegisterCashbackTransactionGrpc
        .newVertxStub(channel);

      final RegisterCashbackRequest cashbackRequest = RegisterCashbackRequest.newBuilder()
        .setOrder(Order.newBuilder().setId("AB").setTotal(10D).build()).setUser(
          User.newBuilder().setEmail("joe@joe.com").setId("joe").build())
        .setStore(Store.newBuilder().setName("ABC").build()).setType("register").build();

      stub.register(cashbackRequest,ar ->{
        if (ar.succeeded()){
          log.info("Got the server response: {}",  ar.result().getId());
        } else {
          log.error("Coult not reach server: {}", ar.cause().getMessage());
        }
      });

      handler.reply(handler.body());
      //handler.fail(1, "Call fail");
    });
  }
}
