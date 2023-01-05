package com.sea.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sea.app.TargetData;
import com.sea.app.dao.MockDao;
import com.sea.utils.StringUtils;
import com.sea.utils.kafka.error.KafkaRetryConfig;
import com.sea.utils.kafka.error.dto.RedirectEventInfo;

@RestController
@RequestMapping("check")
public class CheckController {

	@Autowired
	MockDao MockDao;

	@Autowired
	StringUtils stringUtils;

	@Autowired
	KafkaRetryConfig kafkaRetryConfig;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

	@GetMapping()
	public String createData() throws IOException {

		List<TargetData> resultData = MockDao.selectAll();

		List<ConsumerRecord<String, String>> sourceTopic = getData(
				Arrays.asList("source-topic-1,source-topic-2,source-topic-3,source-topic-4,source-topic-5".split(",")));

		List<ConsumerRecord<String, String>> errorTopic = getData(Arrays.asList("error-topic".split(",")));

		List<ConsumerRecord<String, String>> retryTopic = getData(Arrays.asList(
				"retry-source-topic-1,retry-source-topic-2,retry-source-topic-3,retry-source-topic-4,retry-source-topic-5"
						.split(",")));

		List<ConsumerRecord<String, String>> redirectTopic = getData(Arrays.asList("redirect-topic".split(",")));

		List<DataInfo> dataInfoList = new ArrayList<>();

		sourceTopic.forEach(f -> {

			String relationId = new String(f.headers().lastHeader(kafkaRetryConfig.getRelationIdKey()).value(),
					StandardCharsets.UTF_8);
			String eventId = new String(f.headers().lastHeader(kafkaRetryConfig.getEventIdKey()).value(),
					StandardCharsets.UTF_8);

			DataInfo dataInfo = new DataInfo();

			dataInfo.message = f.value();
			dataInfo.relationId = relationId;
			dataInfo.index = eventId.replace(relationId + "-", "");
			if (dataInfo.message.indexOf("{") > 0) {
				dataInfo.errorInfo = dataInfo.message.substring(dataInfo.message.indexOf("{"));

			} else {
				dataInfo.errorInfo = "-";
			}

			// DB存在状況
			List<TargetData> tmpDB = resultData.stream().filter(filter -> filter.getEventId().equals(eventId)).toList();

			dataInfo.Db = String.valueOf(tmpDB.size());
			if (tmpDB.size() > 0) {
				dataInfo.comsumerTopicName = tmpDB.get(0).getTopicName();
			} else {
				dataInfo.comsumerTopicName = "-";
			}
			dataInfo.sourceTopicName = f.topic();

			// Error存在状況
			List<ConsumerRecord<String, String>> tmpError = errorTopic.stream().filter(filter -> {
				String errorEventId = new String(filter.headers().lastHeader(kafkaRetryConfig.getEventIdKey()).value(),
						StandardCharsets.UTF_8);
				return errorEventId.equals(eventId);
			}).toList();
			if (tmpError.size() == 0) {
				dataInfo.errorTopicOffset = "-";

			} else if (tmpError.size() == 1) {
				dataInfo.errorTopicOffset = String.valueOf(tmpError.get(0).offset());

			} else if (tmpError.size() > 1) {
				StringBuilder sbOffset = new StringBuilder();
				tmpError.forEach(f1 -> {
					sbOffset.append(f1.offset() + "|");
				});
				sbOffset.setLength(sbOffset.length() - 1);
				dataInfo.errorTopicOffset = sbOffset.toString();
			}

			// Retry存在状況
			List<ConsumerRecord<String, String>> tmpRetry = retryTopic.stream().filter(filter -> {
				String errorEventId = new String(filter.headers().lastHeader(kafkaRetryConfig.getEventIdKey()).value(),
						StandardCharsets.UTF_8);
				return errorEventId.equals(eventId);
			}).toList();
			if (tmpRetry.size() == 0) {
				dataInfo.retryTopic = "-";
				dataInfo.retryTopicOffset = "-";

			} else if (tmpRetry.size() == 1) {
				dataInfo.retryTopic = tmpRetry.get(0).topic();
				dataInfo.retryTopicOffset = String.valueOf(tmpRetry.get(0).offset());

			} else if (tmpRetry.size() > 1) {
				StringBuilder sbTopic = new StringBuilder();
				StringBuilder sbOffset = new StringBuilder();
				StringBuilder sbTime = new StringBuilder();
				tmpRetry.forEach(f1 -> {
					sbTopic.append(f1.topic() + "|");
					sbOffset.append(f1.offset() + "|");
					sbTime.append(sdf.format(new Date(f1.timestamp())) + "|");

				});
				sbTopic.setLength(sbTopic.length() - 1);
				sbOffset.setLength(sbOffset.length() - 1);
				sbTime.setLength(sbTime.length() - 1);
				dataInfo.retryTopic = sbTopic.toString();
				dataInfo.retryTopicOffset = sbOffset.toString();
				dataInfo.retryTime = sbTime.toString();
			}

			StringBuilder sb2 = new StringBuilder();
			List<ConsumerRecord<String, String>> tmpRedirect = redirectTopic.stream().filter(filter -> {
				try {
					RedirectEventInfo event = stringUtils.fromJson(filter.value(), RedirectEventInfo.class);

					if (event.getEventId().equals(eventId)) {
						sb2.append(filter.key() + "|");
					}
					return event.getEventId().equals(eventId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
			}).toList();

			if (tmpRedirect.size() > 0) {
				StringBuilder sb = new StringBuilder();
//				sb.append(false);
				tmpRedirect.forEach(aaa -> {

					RedirectEventInfo event;
					try {
						event = stringUtils.fromJson(aaa.value(), RedirectEventInfo.class);
						sb.append(event.getStatus() + "|");
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				sb.setLength(sb.length() - 1);
				sb2.setLength(sb2.length() - 1);
				dataInfo.redirectTopic = sb.toString();
				dataInfo.redirectTopicRecordKey = sb2.toString();
			} else {
				dataInfo.redirectTopic = "-";
				dataInfo.redirectTopicRecordKey = "-";
			}

			// redirectTopic
			dataInfoList.add(dataInfo);
		});

		List<String> lines = new ArrayList<>();
		lines.add(DataInfo.getTitle());
		dataInfoList.forEach(f -> {
			lines.add(f.toString());
		});

		if (Files.exists(Paths.get("d:/test.csv"), LinkOption.NOFOLLOW_LINKS)) {
			Files.delete(Paths.get("d:/test.csv"));
		}
		Files.write(Paths.get("d:/test.csv"), lines, StandardOpenOption.CREATE_NEW);

		return lines.toString();
	}

	private List<ConsumerRecord<String, String>> getData(List<String> topics) {

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
					System.out.println("No more messages.");
					break;
				}
			}
		}

		// consumer.subscribe(topics);

		return hashSet;

	}

	@GetMapping("/res")
	public String getResult() throws Exception {


		List<ConsumerRecord<String, String>> sourceTopic = getData(
				Arrays.asList("source-topic-1,source-topic-2,source-topic-3,source-topic-4,source-topic-5".split(",")));

		List<ConsumerRecord<String, String>> errorTopic = getData(Arrays.asList("error-topic".split(",")));

		List<ConsumerRecord<String, String>> retryTopic = getData(Arrays.asList(
				"retry-source-topic-1,retry-source-topic-2,retry-source-topic-3,retry-source-topic-4,retry-source-topic-5"
						.split(",")));

		List<ConsumerRecord<String, String>> redirectTopic = getData(Arrays.asList("redirect-topic".split(",")));

		final List<String> lines = new ArrayList<>();

		sourceTopic.forEach(record -> {

			String eventId = new String(record.headers().lastHeader(kafkaRetryConfig.getEventIdKey()).value(),
					StandardCharsets.UTF_8);
			String relationId = new String(record.headers().lastHeader(kafkaRetryConfig.getRelationIdKey()).value(),
					StandardCharsets.UTF_8);

			StringBuilder sb = new StringBuilder();
			sb.append(eventId);
			sb.append(",");
			sb.append(relationId);
			sb.append(",");
			sb.append(record.value());
			sb.append(",");
			sb.append(record.topic());
			sb.append(",");
			sb.append(record.partition());
			sb.append(",");
			sb.append(sdf.format(new Date(record.timestamp())));
			lines.add(sb.toString());
		});

		Files.write(Paths.get("E:/txt/sourceTopic.csv"), lines, StandardOpenOption.CREATE_NEW);

		lines.clear();

		retryTopic.forEach(record -> {
			String eventId = new String(record.headers().lastHeader(kafkaRetryConfig.getEventIdKey()).value(),
					StandardCharsets.UTF_8);
			String relationId = new String(record.headers().lastHeader(kafkaRetryConfig.getRelationIdKey()).value(),
					StandardCharsets.UTF_8);
			StringBuilder sb = new StringBuilder();
			sb.append(eventId);
			sb.append(",");
			sb.append(relationId);
			sb.append(",");
			sb.append(record.value());
			sb.append(",");
			sb.append(record.topic());
			sb.append(",");
			sb.append(record.partition());
			sb.append(",");
			sb.append(sdf.format(new Date(record.timestamp())));
			lines.add(sb.toString());
		});
		Files.write(Paths.get("E:/txt/retryTopic.csv"), lines, StandardOpenOption.CREATE_NEW);

		lines.clear();
		errorTopic.forEach(record -> {
			String eventId = new String(record.headers().lastHeader(kafkaRetryConfig.getEventIdKey()).value(),
					StandardCharsets.UTF_8);
			String relationId = new String(record.headers().lastHeader(kafkaRetryConfig.getRelationIdKey()).value(),
					StandardCharsets.UTF_8);
			String originTopic = new String(
					record.headers().lastHeader(kafkaRetryConfig.getOriginTopicNameKey()).value(),
					StandardCharsets.UTF_8);
			StringBuilder sb = new StringBuilder();
			sb.append(eventId);
			sb.append(",");
			sb.append(relationId);
			sb.append(",");
			sb.append(record.value());
			sb.append(",");
			sb.append(record.topic());
			sb.append(",");
			sb.append(record.partition());
			sb.append(",");
			sb.append(originTopic);
			sb.append(",");
			sb.append(sdf.format(new Date(record.timestamp())));
			lines.add(sb.toString());
		});
		Files.write(Paths.get("E:/txt/errorTopic.csv"), lines, StandardOpenOption.CREATE_NEW);

		lines.clear();
		redirectTopic.forEach(record -> {
			try {
				RedirectEventInfo event = stringUtils.fromJson(record.value(), RedirectEventInfo.class);

				StringBuilder sb = new StringBuilder();
				sb.append(event.getEventId());
				sb.append(",");
				sb.append(event.getRelationId());
				sb.append(",");
				sb.append(event.getStatus());
				sb.append(",");
				sb.append(record.topic());
				sb.append(",");
				sb.append(record.partition());
				sb.append(",");
				sb.append(sdf.format(new Date(record.timestamp())));
				lines.add(sb.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		Files.write(Paths.get("E:/txt/redirectTopic.csv"), lines, StandardOpenOption.CREATE_NEW);

		return "";

	}

	public class DataInfo {

		public String sourceTopicName = "";
		public String comsumerTopicName = "";
		public String errorTopicOffset = "";
		
		public String retryTopicOffset = "";
		public String retryTopic = "";
		public String retryTime = "";
		
		public String redirectTopic = "";
		public String redirectTopicRecordKey = "";

		public String Db = "";
		public String message = "";
		public String eventId = "";
		public String index = "";
		public String relationId = "";
		public String errorInfo = "";

		public static String getTitle() {
			StringBuilder sb = new StringBuilder();
			sb.append("relationId");
			sb.append(",");
			sb.append("index");
			sb.append(",");
			sb.append("errorInfo");
			sb.append(",");
			sb.append("Db");
			sb.append(",");
			sb.append("sourceTopicName");
			sb.append(",");
			sb.append("comsumerTopicName");
			sb.append(",");
			sb.append("errorTopicOffset");
			sb.append(",");
			sb.append("retryTopicOffset");
			sb.append(",");
			sb.append("retryTopic");
			sb.append(",");
			sb.append("retryTime");
			sb.append(",");
			sb.append("redirectTopic");
			sb.append(",");
			sb.append("redirectTopicRecordKey");
			sb.append(",");
			sb.append("message");
			return sb.toString();
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(relationId);
			sb.append(",");
			sb.append(index);
			sb.append(",");
			sb.append(errorInfo);
			sb.append(",");
			sb.append(Db);
			sb.append(",");
			sb.append(sourceTopicName);
			sb.append(",");
			sb.append(comsumerTopicName);
			sb.append(",");
			sb.append(errorTopicOffset);
			sb.append(",");
			sb.append(retryTopicOffset);
			sb.append(",");
			sb.append(retryTopic);
			sb.append(",");
			sb.append(retryTime);
			sb.append(",");
			sb.append(redirectTopic);
			sb.append(",");
			sb.append(redirectTopicRecordKey);
			sb.append(",");
			sb.append(message);
			return sb.toString();
		}

	}

}
