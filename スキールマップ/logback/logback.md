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

- 任意に複雑な条件を実現して、対象ログを出力するかどうかをフィルターリング	
  - 通常フィルター(Filter)
  - ターボフィルター(TurboFilter)

- 区別
  - `TurboFilter`はロギングコンテキストに紐付けられている。したがって、アペンダーが使用されたときにだけ呼ばれるのではなく、ロギング要求が発生するたびに呼ばれることになる。つまり、ターボフィルターの有効範囲はアペンダーに割り当てられたフィルターよりも広い。
  - ターボフィルターが呼ばれるのは`LoggingEvent`オブジェクトが作成される前。 `TurboFilter`オブジェクトは、ロギング要求をフィルタリングするのにロギングイベントを必要としない。つまり、ターボフィルターはロギングイベントの高速なフィルタリングを意図したもの。

## 1.5 MDC(Mapped Diagnostic Context)

診断コンテキスト

- ソースコードで登録

  ```java
  package chapters.mdc;
  
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.slf4j.MDC;
  
  import ch.qos.logback.classic.PatternLayout;
  import ch.qos.logback.core.ConsoleAppender;
  
  public class SimpleMDC {
    static public void main(String[] args) throws Exception {
  
      // MDCにfirstを登録
      MDC.put("first", "Dorothy");
  
      ・・・
      
      Logger logger = LoggerFactory.getLogger(SimpleMDC.class);
      // We now put the last name
      MDC.put("last", "Parker");
  
      // The most beautiful two words in the English language according
      // to Dorothy Parker:
      logger.info("Check enclosed.");
      logger.debug("The most beautiful two words in English.");
  
      MDC.put("first", "Richard");
      MDC.put("last", "Nixon");
      logger.info("I am not a crook.");
      logger.info("Attributed to the former US president. 17 Nov 1973.");
    }
  
      ・・・
  
  }
  ```

- レイアウトで利用

  ```xml
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender"> 
    <layout>
      <Pattern>%X{first} %X{last} - %m%n</Pattern>
    </layout> 
  </appender>
  ```

# 2 そのほか

## 2.1 設定ファイル

### 2.1.1 格納パス

logbackの設定は下記の順番に探している。

1. logback は[クラスパス](https://logback.qos.ch/faq.html#configFileLocation)上で*logback.groovy*というファイルを探します。
2. 見つからなかったら、今度は[クラスパス](https://logback.qos.ch/faq.html#configFileLocation)上で*logback-test.xml*というファイルを探します。
3. 見つからなかったら、今度は[クラスパス](https://logback.qos.ch/faq.html#configFileLocation)上で*logback.xml*というファイルを探します。
4. 何も見つからなかったら、自動的に[`BasicConfigurator`](https://logback.qos.ch/xref/ch/qos/logback/classic/BasicConfigurator.html)を使って設定します。ロギング出力は直接コンソールに出力されるようになります。

### 2.1.2 分割

設定ファイルは複数ファイルに分割することが可能

- Subファイル`src/main/java/chapters/configuration/includedConfig.xml`

  ```xml
  <included>
    <appender name="includedConsole" class="ch.qos.logback.core.ConsoleAppender">
      <encoder>
        <pattern>"%d - %m%n"</pattern>
      </encoder>
    </appender>
  </included>
  ```

- 引用

  ```xml
  <configuration>
    <include file="src/main/java/chapters/configuration/includedConfig.xml"/>
  
    <root level="DEBUG">
      <appender-ref ref="includedConsole" />
    </root>
  
  </configuration>
  ```

### 2.1.3 分岐の利用

- ```xml
     <!-- if-then form -->
     <if condition="some conditional expression">
      <then>
        ...
      </then>
    </if>
  
    <!-- if-then-else form -->
    <if condition="some conditional expression">
      <then>
        ...
      </then>
      <else>
        ...
      </else>
    </if>
  ```

  



### 