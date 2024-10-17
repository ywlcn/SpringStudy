# 歴史

logbackは log4j 1.x の後継者である。 log4j 2.x とは競争関係である。構成上の関係は下記の図を表れている

![logbackVSlog4j](.\images\logbackVSlog4j.png)



- 基本構成：フィルター、ロガー、アペンダー、レイアウト
- logback.xmlの構造：root, logger, appender要素の役割、複数ファイル構成、logback-spring
- レベルの設定：TRACE, DEBUG, INFO, WARN, ERROR
- 出力先の設定：コンソール、ファイル、データベースなど
- ロガーの選定、複数回出力
- 出力フォーマットのカスタマイズ、パターンレイアウト
- 条件付きロギング：if, then, else要素

# 1 基本概念

## 1.1 ロガー

アプリ側と結んでいる、どのアペンダーへ出力の定義となる。アプリ側からLogger名を指定してLoggerを取得して出力するか、クラスのままで取得して、出力するか。

- 定義例

  ```xml
  	<logger name="com.test" additivity="false" level="INFO">
  		<appender-ref ref="APL_LOG" />
  	</logger>
  ```

- 使用例

  ```java
  package com.test.common;
  
  public class HelloWorld2 {
  
    public static void main(String[] args) {
        // "com.test"というLoggerが選択される
        Logger logger = LoggerFactory.getLogger(HelloWorld2.class);
        logger.debug("Hello world.");
        
        // "root"というLoggerが選択される
        Logger logger = LoggerFactory.getLogger("not-exist");
    }
  }
  ```

## 1.2 アペンダー

出力先として使用されるクラスのこと。既存のアペンダーには、コンソール、ファイル、Syslog、TCPソケット、JMSなどをいろいろな出力先のクラスが存在する。ユーザーは自分たちの状況に応じたアペンダーを簡単に作ることができる。

- 定義例

    ```xml
        <appender name="APL_LOG" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <encoder>
                <charset>${ENCODING_FILE}</charset>
                <pattern>${COMMON_LOG_PATTERN}</pattern>
            </encoder>
            <file>${LOG_DIR}/apl.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
                <fileNamePattern>${LOG_DIR}/apl.log.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
                <maxFileSize>10MB</maxFileSize>
                <maxHistory>10</maxHistory>
            </rollingPolicy>
        </appender>
    ```


## 1.3 レイアウト

出力時のレイアウトの定義となる。下記のように定義して、↑のアペンダーの`<pattern>`に利用する。利用可能の略語は下記のページを参照

https://logback.qos.ch/manual/layouts.html#conversionWord

- 定義例

    ```xml
    <property name="COMMON_LOG_PATTERN" value="%date{ISO8601}\t[%X{traceId}]\t[%thread]\t[%-5p]\t%c\t%m%n"/>
    ```

## 1.4 フィルダ



## 1.5 MDC



# 2 そのほか

## 2.1 設定ファイル

### 2.1.1 格納パス

### 2.1.2 分割

### 2.2 JMX