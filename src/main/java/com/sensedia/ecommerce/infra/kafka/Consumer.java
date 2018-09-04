package com.sensedia.ecommerce.infra.kafka;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.serialization.JsonObjectDeserializer;
import java.util.Properties;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

public class Consumer {

  public static KafkaConsumer<String, JsonObject> configureConsumer(Vertx vertx, String group,
    String kafkaHost, String kafkaPort) {
    val conf = new Properties();
    conf.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, String.format("%s:%s", kafkaHost, kafkaPort));
    conf.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    conf.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonObjectDeserializer.class);
    conf.put(ConsumerConfig.GROUP_ID_CONFIG, group);
    conf.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    conf.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    return KafkaConsumer
      .create(vertx, conf, String.class, JsonObject.class);
  }
}
