

# 1 Gradle実施の構成

 Gradleの実施は下記の３ステップの順番に実施している

- init

  setting.gradle内容の解析

- Configure

  build.gradleの解析と設定

- Execution

  実際タスクの実施

## 1.1 説明例

1.1.1 各ブロックの実施タイミング

- スクリプト

  ```groovy
  println "Outer"				・・・Configureの場合に実施される
  
  task hello {
      println "inner"			・・・Configureの場合に実施される
      doLast {
          println "doLast"		・・・Executionの場合に実施される
      }
      doFirst {
          println "doFirst"		・・・Executionの場合に実施される
      }g
  }
  ```

- 実施結果

  ```bash
  PS D:\VSCode\VsCodeWorkSpace\dep-test> gradle hello 
  
  > Configure project :app
  Outer
  inner
  
  > Task :app:hello
  doFirst
  doLast
  ```

  

# 2 依存関係定義

## 2.1 例

```groovy
plugins {
	id "java"
        // これは、依存関係を一元管理ために記載している
        // dependencyManagementとあわせて使用すると効果がある。
　　// これだけ記載して、あんまり意味ないと思う・・・
	id "io.spring.dependency-management" version "1.0.10.RELEASE"
}

repositories {
	// 効果は二つ：①、Jarパッケージのダウンロード用　　　②、推移的な依存関係情報を取得
	mavenCentral()
}

dependencyManagement {
	dependencies {
		// これを記載しないと、下の依存関係が解決できない
		// つまり、これは、下の依存関係でバージョンが記載されていない場合のために記載している
		dependency('org.springframework:spring-webmvc:5.2.9.RELEASE') {
			// このように指定すると、spring-webmvcの推移的な依存関係にspring-webが除外となる
			exclude 'org.springframework:spring-web'
		}
        
        // このような記述ほうほうもある
		dependencySet(group:'org.springframework', version: '4.1.4.RELEASE') {
            entry('spring-core') {
                exclude group: 'commons-logging', name: 'commons-logging'
            }
        }

        // すでに存在しているBOM定義をインポートする
        imports {
			mavenBom 'io.spring.platform:platform-bom:1.0.1.RELEASE'
     	}
        
		// このように指定すると、boot:spring-boot-starterは2.2.4.RELEASEで固定する
		// 下記のようになる。　　－－－　2.3.4.RELEASE -> 2.2.4.RELEASE　
		// もちろん、指定しない場合、上層の依存関係により決める
		//\--- org.springframework:spring-webmvc -> 5.2.9.RELEASE
		//     +--- org.springframework:spring-aop:5.2.9.RELEASE -> 5.2.5.RELEASE
		//     |    +--- org.springframework:spring-beans:5.2.5.RELEASE -> 5.2.9.RELEASE
		//     |    |    \--- org.springframework:spring-core:5.2.9.RELEASE
		//     |    |         \--- org.springframework:spring-jcl:5.2.9.RELEASE
		//     |    \--- org.springframework:spring-core:5.2.5.RELEASE -> 5.2.9.RELEASE (*)
		dependency 'org.springframework:spring-aop:5.2.5.RELEASE'
	}
}

dependencies {
    // main　sourceSetsに対する依存関係の定義
    implementation "org.springframework:spring-webmvc"
    
    // test　sourceSetsに対する依存関係の定義
	testCompile "org.springframework:spring-webmvc"
    
    // other　sourceSetsに対する依存関係の定義
    otherimplementation "org.springframework:spring-webmvc"
}
```







## 2.2 依存関係定義キー

- implementation

  Javaコードがきれいにコンパイルする必要がある、という依存関係を宣言しています。

- compile（api）

  Javaコードがきれいにコンパイルする必要がある、という依存関係を宣言しています。さらに、jarをビルドするとき、POIからJavaコードで使用するパッケージが`Import-Package`マニフェストエントリとして追加されます。

- compileOnly（provided）

  上記のcompileと同じように、コードをコンパイルするために必要な依存関係を箇条書きにするために使用されます。

  違いは、compileOnly依存関係からJavaコードが使用するパッケージがImport-Packageマニフェストエントリとして表示されないことです。

  compileOnlyはtestCompileやtestRuntimeへは引き継がれない。

- testCompileOnly

  テストのコンパイル時のみ必要なライブラリを定義する場合、使用されます。

- runtimeOnly（apk）

  只在生成`apk`的时候参与打包，编译时不会参与，很少用。

- **implementation VS compile（api）**

  プロジェクトAに下記のような依存関係が定義されている。

  ```bash
  implementation 'com.android.support:appcompat-v7:26.1.0'
  compile 'com.android.support:design:26.1.0'
  ```

  プロジェクトBにプロジェクトAを下記のように定義している

  ```bash
  compile project(':プロジェクトA')
  ```

  結果として、`appcompat`はそのまま使用できないが、`design`は使用可能である。





testCompile（testImplementation）

`testCompile` 只在单元测试代码的编译以及最终打包测试apk时有效。



debugCompile（debugImplementation）

`debugCompile` 只在 **debug** 模式的编译和最终的 **debug apk** 打包时有效



releaseCompile（releaseImplementation）

`Release compile`仅仅针对 **Release** 模式的编译和最终的 **Release apk** 打包。



- API:モジュールにAPI依存関係が含まれている場合、モジュールがそれを転送したいことをGradleに知らせています 他のモジュールへの依存関係 ランタイムとコンパイル時間。この設定はコンパイルと同じように動作します (これは現在廃止されています)。 ライブラリモジュール。それは、API依存関係がその 外部API、Gradleはそれにアクセスできるすべてのモジュールを再コンパイルします コンパイル時の依存関係。だから、多数のAPIを持っている 依存関係はビルド時間を大幅に増やすことができます。あなたが望まない限り 依存関係のAPIを別のテストモジュール、アプリモジュールに公開する 代わりに実装依存関係を使用する必要があります。
- compileOnly:gradleはコンパイルクラスパスにのみ依存関係を追加します(ビルド出力には追加されません)。これは便利です Androidライブラリモジュールを作成するときに、 コンパイル時の依存関係ですが、 ランタイム。つまり、この設定を使用する場合、ライブラリ モジュールには、 依存関係が利用可能であり、その動作を正常に変更して 提供されていない場合でも機能します。これは、 一時的な依存関係を追加しないことによる最終APK 重要です。この設定は、指定されたとおりに動作します(現在は 非推奨) です。
- runtimeonly:gradleは、実行時に使用するために、ビルド出力にのみ依存関係を追加します。つまり、コンパイルには追加されません クラスパス。この設定はapkと同じように動作します(現在は 非推奨) です。



## 2.3 依存関係競争

### 2.3.1 特定の依存関係排除

```groovy
dependencies {
	complie('org.springframework:spring-webmvc:5.2.9.RELEASE') {
		exclude 'org.springframework:spring-web'
	}
}
```
### 2.3.2 推移的な依存関係全部除外

```groovy
dependencies {
	complie('org.springframework:spring-webmvc:5.2.9.RELEASE') {
		transitive=false
	}
}
```

### 2.3.3 特定の依存関係をバージョン指定

```groovy
configuration.all {
	resolutionStrategy{
		force 'org.springframework:spring-web'
	}
}
```

### 2.3.4 依存関係競争自動解決しない

```groovy
configuration.all {
	resolutionStrategy{
		fallOnVersionConflict()
	}
}
```



# 3 プラグイン

## 3.1 分類

### 3.1.1 作成方式より切り分け

- binary plugins 

- script plugins

  - 定義例  `other.gradle`

    ```groovy
    ext {
        veon='1.1.0'
        url='http://www.google.com'
    }
    ```

  - 使用例

    ```groovy
    apply from: 'other.gradle'
    ```

    

### 3.1.2 binary plugins の切り分け

- core plugin
- community plugin



- 型でプラグインを適用する

- ```
  apply plugin: JavaPlugin
  ```

- Applying a community plugin

- ```
  plugins {
      id "com.jfrog.bintray" version "0.4.1"
  }
  ```

## 3.2 適用方法

### 3.2.1 Gradleの公式サイト　

 当該方式は　https://plugins.gradle.org/　をリポジトリにして、プラグインを取得する

```groovy
plugins {
  id "ch.so.agi.gretl" version "2.0.302"
}
```

### 3.2.2 リポジトリを指定

 当該方式は`buildscript`ブロックで指定されているリポジトリから、プラグインを取得する

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "ch.so.agi:gretl:2.0.302"
  }
}

apply plugin: "ch.so.agi.gretl"
```



## 3.3 core plugin一覧

ソースにある・・・



## 3.4 カスタマイズプラグイン

```groovy
apply plugin: HelloPlugin

class HelloPlugin implements Plugin<Project>{
    void apply(Project : project){
        println "Hello from HelloPlugin"
        project.task('hello'){
            doLast{
                println "Hello from HelloPlugin(doLast)"
            }
        }        
    }    
}
```



```groovy
apply plugin: GreetingPlugin

class GreetingPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("greeting", GreetingPluginExtension)
        project.task('hello') {
         	doLast{
            	println "${project.greeting.message} from ${project.greeting.greeter}"
            }
        }
    }
}

class GreetingPluginExtension {
    String message
    String greeter
}
// 設定方法１
greeting {
    message = 'Hi'
    greeter = 'Gradle'
}
// 設定方法２
greeting.message = 'Hi'
greeting.greeter = 'Gradle'
```









3.5 java プラグイン



```groovy
sourceSets {
    other {
    	output.resourcesDir=file('out/bin')
    	java.outputDir=file('out/bin')
        java {
            srcDir 'src/java/other'
            //srcDirs=['src/java/other']
        }
        resources {
            srcDir 'src/resources/other'
            //srcDirs=['src/resources/other']
        }
    }
}

task otherDoc(type:Jar){
    archiveBaseName = sourceSets.other.name
    archiveVersion = '0.0.1'
    destinationDirectory = file("${project.projectDir}/jar")
    from sourceSets.other.output
}

task otherDoc(type:Javadoc){
    destinationDir = file("${project.projectDir}/doc")
    source sourceSets.other.allJava
    title sourceSets.other.name
}
```







# 4 タスク

## ４.1 タスクの定義

### 4.1.1 コピータスク

```groovy
task copy(type: Copy) {
   from '$rootProject/resources'
   into '$rootProject/target'
   include('**/*.txt', '**/*.xml', '**/*.properties')
   exclude('**/*.git')
}
```





## 4.1 タスク種類ごとに設定

```groovy
tasks.withType(Javadoc){
	options.encoding = 'utf-8'
}

```



## 4.2 タスクの実施順番

- 同じプロジェクト内の順番

  - 方法１

    ```groovy
    task taskX {
    	doLast {
    		println 'taskX'
    	}
    }
    
    task taskY (dependsOn: "taskX"){
    	doLast {
    		println 'taskY'
    	}
    }
    ```

  - 方法２

    ```groovy
    taskX.dependsOn taskY
    taskY.dependsOn taskZ
    taskZ.shouldRunAfter taskX
    ```

- サブプロジェクト間の順序指定

  ```groovy
  project('project-a') {
      task taskX {
          dependsOn ':project-b:taskY'
          doLast {
              println 'taskX'
          }
      }
  }
  
  project('project-b') {
      task taskY {
          doLast {
              println 'taskY'
          }
      }
  }
  ```

## 4.3 タスクの実施指定

- プロパティより実施するかどうかの制御

  例：

  ```groovy
  println "Outer"	
  task hello {
      println "inner"
      doLast {
          println "doLast"
      }
      doFirst {
          println "doFirst"
      }
  }
  hello.onlyIf { !project.hasProperty('skipHello') }
  ```

  実施結果

  ```bash
  PS D:\VSCode\VsCodeWorkSpace\dep-test> gradle hello -PskipHello
  
  > Configure project :app
  Outer
  inner
  ```

  



5 Pulish