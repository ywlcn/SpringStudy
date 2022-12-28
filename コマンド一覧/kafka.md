

# 1. コマンド一覧

```bash
#################################Topic####################################################
#https://jaceklaskowski.gitbooks.io/apache-kafka/content/kafka-admin-TopicCommand.html
//  --bootstrap-server
$ ./kafka-topics.sh --bootstrap-server=localhost:9092 --list

$ ./bin/kafka-topics.sh --bootstrap-server=localhost:9092 --describe --topic my-topic

$ ./kafka-topics.sh --bootstrap-server=localhost:9092 --delete --topic my-topic

$ ./kafka-topics.sh --bootstrap-server=localhost:9092 --create --topic=mytopic       


 --zookeeper   --->   --bootstrap-server  



#################################Consumer####################################################
# https://docs.cloudera.com/runtime/7.2.10/kafka-managing/topics/kafka-manage-cli-unsupported.html
$ ./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list

$ ./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group flume


$ ./kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic=mytopic
$ ./kafka-delete-records.sh --bootstrap-server localhost:9092  --offset-json-file ./offset-file.json



$ ./kafka-console-producer.sh --bootstrap-server=localhost:9092 --topic=mytopic


$ ./kafka-console-consumer.sh --bootstrap-server=localhost:9092 --topic=source-topic --from-beginning


```








