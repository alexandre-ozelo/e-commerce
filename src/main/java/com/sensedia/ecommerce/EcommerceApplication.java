package com.sensedia.ecommerce;

import com.sensedia.ecommerce.domain.resource.Ecommerce;
import com.sensedia.ecommerce.infra.verticles.CashbackWorker;
import com.sensedia.ecommerce.infra.verticles.PaymentWorker;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class EcommerceApplication {

  public static void main(String[] args) {
    val vertx = Vertx.vertx();
    vertx.deployVerticle(new Ecommerce(), new DeploymentOptions().setWorker(false));
    vertx.deployVerticle(new PaymentWorker(), new DeploymentOptions().setWorker(true));
    vertx.deployVerticle(new CashbackWorker(), new DeploymentOptions().setWorker(true));
  }
}
