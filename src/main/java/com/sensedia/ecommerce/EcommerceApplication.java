package com.sensedia.ecommerce;

import com.sensedia.ecommerce.domain.resource.Ecommerce;
import com.sensedia.ecommerce.infra.verticles.CashbackRegisterWorker;
import com.sensedia.ecommerce.infra.verticles.CashbackUndoWorker;
import com.sensedia.ecommerce.infra.verticles.PaymentResponseWorker;
import com.sensedia.ecommerce.infra.verticles.PaymentWorker;
import com.sensedia.ecommerce.infra.verticles.PurchaseResponseWorker;
import com.sensedia.ecommerce.infra.verticles.PurchaseWorker;
import io.netty.util.internal.StringUtil;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class EcommerceApplication {

  public static void main(String[] args) {
    val vertx = Vertx.vertx();

    val envStore = new ConfigStoreOptions().setType("env");
    val options = new ConfigRetrieverOptions().addStore(envStore);
    val retriever = ConfigRetriever.create(vertx, options);

    log.info("Starting application...");

    retriever.getConfig(conf -> {
      if (conf.succeeded()) {
        val config = conf.result();

        val metadata = new JsonObject()
          .put("mongoHost", StringUtil.isNullOrEmpty(config.getString("MONGO_HOST")) ? "localhost"
            : config.getString("MONGO_HOST"))
          .put("mongoPort", Objects.isNull(config.getInteger("MONGO_PORT")) ? 27017
            : config.getInteger("MONGO_PORT"))
          .put("kafkaHost", StringUtil.isNullOrEmpty(config.getString("KAFKA_HOST")) ? "localhost"
            : config.getString("KAFKA_HOST"))
          .put("kafkaPort", Objects.isNull(config.getInteger("KAFKA_PORT")) ? 9092
            : config.getInteger("KAFKA_PORT"))
          .put("cashBackHost",
            StringUtil.isNullOrEmpty(config.getString("CASH_BACK_HOST")) ? "localhost"
              : config.getString("CASH_BACK_HOST"))
          .put("cashBackPort", Objects.isNull(config.getInteger("CASH_BACK_PORT")) ? 8082
            : config.getInteger("CASH_BACK_PORT"))
          .put("cashBackPerc", Objects.isNull(config.getDouble("CASH_BACK_PERC")) ? 10.0
            : config.getDouble("CASH_BACK_PERC"));

        log.info("Enviroment data: {}", metadata.toString());

        val deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(metadata);

        vertx.deployVerticle(new Ecommerce(), deploymentOptions.setWorker(false));
        vertx.deployVerticle(new PaymentWorker(), deploymentOptions.setWorker(true));
        vertx.deployVerticle(new PaymentResponseWorker(), deploymentOptions.setWorker(true));
        vertx.deployVerticle(new CashbackRegisterWorker(), deploymentOptions.setWorker(true));
        vertx.deployVerticle(new CashbackUndoWorker(), deploymentOptions.setWorker(true));
        vertx.deployVerticle(new PurchaseWorker(), deploymentOptions.setWorker(true));
        vertx.deployVerticle(new PurchaseResponseWorker(), deploymentOptions.setWorker(true));
      }
    });
  }
}
