package com.sensedia.ecommerce.infra.grpc;

import com.sensedia.cashback.infra.proto.Order;
import com.sensedia.cashback.infra.proto.RegisterCashbackRequest;
import com.sensedia.cashback.infra.proto.RegisterCashbackTransactionGrpc;
import com.sensedia.cashback.infra.proto.Store;
import com.sensedia.cashback.infra.proto.User;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class CashBackRequest {

  public static void send(Vertx vertx, String type, String cashBackHost, Integer cashBackPort,
    com.sensedia.ecommerce.domain.data.Order order) {
    val channel = VertxChannelBuilder
      .forAddress(vertx, cashBackHost, cashBackPort)
      .usePlaintext(true)
      .build();

    val stub = RegisterCashbackTransactionGrpc
      .newVertxStub(channel);

    val cashbackRequest = RegisterCashbackRequest.newBuilder()
      .setOrder(Order.newBuilder().setId(order.getId()).setTotal(order.getPoints()).build())
      .setUser(
        User.newBuilder().setEmail(order.getUser().getEmail()).setId(order.getUser().getId())
          .build())
      .setStore(Store.newBuilder().setName(order.getStore()).build()).setType(type).build();

    stub.register(cashbackRequest, ar -> {
      if (ar.succeeded()) {
        log.info("CashBack response: {}", ar.result().getId());
      } else {
        log.error("Error in cashback service: {}", ar.cause().getMessage());
      }
    });
  }
}
