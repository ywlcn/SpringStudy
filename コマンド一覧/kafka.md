

# 1. コマンド一覧

```bash
#################################Topic####################################################
#https://jaceklaskowski.gitbooks.io/apache-kafka/content/kafka-admin-TopicCommand.html
$ ./kafka-topics.bat --zookeeper localhost:2181 --list

$ ./bin/kafka-topics.bat --zookeeper localhost:2181 --describe --topic my-topic

$ ./kafka-topics.bat --zookeeper localhost:2181 --delete --topic my-topic

$ ./kafka-topics.sh --bootstrap-server=localhost:9092 --create --topic=mytopic       


--bootstrap-server   --zookeeper



#################################Consumer####################################################
# https://docs.cloudera.com/runtime/7.2.10/kafka-managing/topics/kafka-manage-cli-unsupported.html
$ ./kafka-consumer-groups.bat --bootstrap-server localhost:9092 --list

$ ./kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --group flume





$ ./kafka-console-producer.sh --bootstrap-server=localhost:9092 --topic=mytopic


$ ./kafka-console-consumer.sh --bootstrap-server=localhost:9092 --topic=mytopic


```








