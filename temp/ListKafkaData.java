package com.sea.test;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

public class ListKafkaData {

	public static void main(String[] args) throws Exception {

		//,source-topic-2,source-topic-3,source-topic-4,source-topic-5
		List<ConsumerRecord<String, String>> sourceTopic = getData(
				Arrays.asList("source-topic-1".split(",")));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

		sourceTopic.forEach(record -> {

			System.out.println(
					String.format("message:[%s],time:[%s],", record.value(), sdf.format(new Date(record.timestamp()))));
		});

	}

	private static List<ConsumerRecord<String, String>> getData(List<String> topics) {

		List<ConsumerRecord<String, String>> hashSet = new ArrayList<>();

		final Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.56.100:30921");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer" + UUID.randomUUID().toString());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		try (// Create the consumer using props.
				Consumer<String, String> consumer = new KafkaConsumer<>(props)) {
			List<TopicPartition> topicPartitions = new ArrayList<>();
			topics.forEach(t -> {

				consumer.partitionsFor(t).forEach(p -> {
					TopicPartition tp = new TopicPartition(t, p.partition());
					topicPartitions.add(tp);
				});

			});
			consumer.assign(topicPartitions);
			consumer.seekToBeginning(topicPartitions);

//		consumer.beginningOffsets(topicPartitions);

			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
				for (ConsumerRecord<String, String> record : records) {
					long offset = record.offset();
					String value = record.value();
					hashSet.add(record);
//				System.out.printf("Received %s: %s%n", offset, value);
				}
				// Early exit. Remove entirely to keep the consumer alive indefinitely.
				if (records.count() == 0) {
					System.out.println("No more messages. from " + topics.toString());
					break;
				}
			}
		}

		// consumer.subscribe(topics);

		return hashSet;

	}

}
