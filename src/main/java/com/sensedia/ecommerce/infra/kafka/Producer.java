package com.sensedia.ecommerce.infra.kafka;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.serialization.JsonObjectSerializer;
import java.util.Properties;
import lombok.val;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

public class Producer {
  public static KafkaProducer<String, JsonObject> configureProducer(Vertx vertx, String kafkaHost,
    String kafkaPort) {
    val conf = new Properties();
    conf.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.format("%s:%s", kafkaHost, kafkaPort));
    conf.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    conf.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonObjectSerializer.class);
    conf.put(ProducerConfig.ACKS_CONFIG, "1");
    return KafkaProducer.create(vertx, conf, String.class, JsonObject.class);
  }
}
