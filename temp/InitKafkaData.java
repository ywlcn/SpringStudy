package com.sea.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;

public class InitKafkaData {

	public static void main(String[] args) throws Exception {

		// 步驟1. 設定要連線到Kafka集群的相關設定
		Properties props = new Properties();
		props.put("bootstrap.servers", "192.168.56.100:30921");

		// 步驟2. 創建AdminClient的instance
		AdminClient adminClient = KafkaAdminClient.create(props); // 透過create()來產生adminClient的instance

		ListTopicsResult listTopicsResult = adminClient.listTopics();

		List<String> deleteTop = Arrays.asList("redirect-topic", "error-topic", "retry-source-topic-1",
				"retry-source-topic-2", "retry-source-topic-3", "retry-source-topic-4", "retry-source-topic-5",
				"source-topic-1", "source-topic-2", "source-topic-3", "source-topic-4", "source-topic-5");

		List<String> deleteList = new ArrayList<>();

		listTopicsResult.listings().get().forEach(f -> {
			System.out.println(String.format("Get Topic:[%s]", f));
			if (deleteTop.contains(f.name())) {
				deleteList.add(f.name());
			}
		});

		adminClient.deleteTopics(deleteTop);

		List<NewTopic> createList = new ArrayList<>();
		deleteTop.forEach(topicName -> {
			if (topicName.equals("redirect-topic")) {
				NewTopic n = new NewTopic(topicName, 1, (short) 2);
				Map<String, String> config = new HashMap<>();
				config.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT);
				config.put(TopicConfig.SEGMENT_BYTES_CONFIG, "5120");
				n = n.configs(config);

				createList.add(n);

			} else {

				createList.add(new NewTopic(topicName, 5, (short) 2));
			}
		});

		adminClient.createTopics(createList);

//		listTopicsResult = adminClient.listTopics();
//		listTopicsResult.listings().get().forEach(f -> {
//			System.out.println(String.format("create Topic:[%s]", f));
//		});

		adminClient.close();

	}

//	// 步驟3. 透過AdminClient的API來取得相關ConsumerGroup的訊息
//	// *** 取得Kafka叢集裡ConsumerGroup基本資訊 *** //
//	ListConsumerGroupsResult listConsumerGroupsResult = adminClient.listConsumerGroups();
//
//	// 步驟4. 指定想要查找的ConsumerGroup
//	String consumerGroupId = "xxxxx"; // <-- 替換你/妳的ConsumerGroup ID
//	System.out.println("ConsumerGroup: " + consumerGroupId);
//
//	ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = adminClient
//			.listConsumerGroupOffsets(consumerGroupId);
//	// 取得這個ConsumerGroup曾經訂閱過的Topics的最後offsets
//	Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = listConsumerGroupOffsetsResult
//			.partitionsToOffsetAndMetadata().get();
//
//	// 我們產生一個這個ConsumerGroup曾經訂閱過的TopicParition訊息
//	List<TopicPartition> topicPartitions = new ArrayList<>();
//	for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsetAndMetadataMap.entrySet()) {
//		TopicPartition topic_partition = entry.getKey(); // 某一個topic的某一個partition
//		OffsetAndMetadata offset = entry.getValue(); // offset
//
//		// 打印出來 (在API裡頭取到的offset都是那個partition最大的offset+1 (也就是下一個訊息會被assign的offset),
//		// 因此我們減1來表示現在己經消費過的最大offset
//		System.out.println(String.format(" Topic: %s Partiton: %d Offset: %d", topic_partition.topic(),
//				topic_partition.partition(), offset.offset()));
//		topicPartitions.add(topic_partition);
//	}
//
//	// 步驟5. 適當地釋放AdminClient的資源

}
