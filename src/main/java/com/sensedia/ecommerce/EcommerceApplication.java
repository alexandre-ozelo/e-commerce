package com.sensedia.ecommerce;

import com.sensedia.ecommerce.domain.resource.Ecommerce;
import com.sensedia.ecommerce.infra.verticles.CashbackWorker;
import com.sensedia.ecommerce.infra.verticles.PaymentWorker;
import com.sensedia.ecommerce.infra.verticles.PurchaseWorker;
import io.netty.util.internal.StringUtil;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class EcommerceApplication {

  public static void main(String[] args) {
    val vertx = Vertx.vertx();

    val envStore = new ConfigStoreOptions()
      .setType("env");
    val options = new ConfigRetrieverOptions()
      .addStore(envStore);
    val retriever = ConfigRetriever.create(vertx, options);

    log.info("Starting application...");

    retriever.getConfig(conf -> {
      if (conf.succeeded()) {
        val config = conf.result();

        val metadata = new JsonObject().put("host",
          StringUtil.isNullOrEmpty(config.getString("MONGO_HOST")) ? "localhost"
            : config.getString("MONGO_HOST"))
          .put("port", Objects.isNull(config.getInteger("MONGO_PORT")) ? 27017
            : config.getInteger("MONGO_PORT"));

        log.info("Database connection data: {}", metadata.toString());

        val deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(metadata);

        vertx.deployVerticle(new Ecommerce(), deploymentOptions.setWorker(false));
        vertx.deployVerticle(new PaymentWorker(), deploymentOptions.setWorker(true));
        vertx.deployVerticle(new CashbackWorker(), deploymentOptions.setWorker(true));
        vertx.deployVerticle(new PurchaseWorker(), deploymentOptions.setWorker(true));
      }
    });
  }
}
