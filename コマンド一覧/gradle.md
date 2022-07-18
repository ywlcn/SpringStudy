

# 1 Gradle実施の構成

 Gradleの実施は下記の３ステップの順番に実施している

- init

  setting.gradle内容の解析

  プロジェクト構成の生成

  Configureステップの実施に必要なBuildScr定義の解析？　JVMの準備　クラスパスなど・・・・❓

- Configure

  build.gradleの解析と設定

- Execution

  実際タスクの実施

## 1.1 説明例

1.1.1 各ブロックの実施タイミング

- スクリプト

  ```groovy
  println "Outer"				・・・Configureのステップに実施される
  
  task hello {
      println "inner"			・・・Configureのステップに実施される
      doLast {
          println "doLast"		・・・Executionのステップに実施される
      }
      doFirst {
          println "doFirst"		・・・Executionのステップに実施される
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
// この部分の定義は本build.gradleを実施する際に、必要な設定クラスパスなど
buildscript {
	// 変数定義
	ext.versions = [
		'java' : '1.8',
		'springboot' : '2.3.4.RELEASE',
	]
	// このリポジトリの定義は「buildscript」のdependenciesのために定義する
	repositories {
		mavenCentral()    
		maven {
			url "https://plugins.gradle.org/m2/"
		}
	}
	// ここには本build.gradleを実施する際、必要になるJarファイルをクラスパスに入れる
    // 例えばpluginやimportなど
	dependencies {
		classpath("org.springframework.boot:spring-boot-gradle-plugin:${versions.springboot}")
		classpath "com.moowork.gradle:gradle-node-plugin:1.3.1"
	}
}

// ラグイン定義方法１
// こにはもしGradleに付属以外のpluginを使用したい場合、buildscriptに必ず使用されるプラグインの
// ファイルをクラスパスに追加する必要です。
apply plugin: 'avaPlugin'
apply plugin: 'eclipse'

//プラグイン定義方法２
//この記述方法であれば、デフォルトGradleに付属しているプラグインはIDだけでよいです。
//もし付属していなければ「https://plugins.gradle.org/」にアクセスして、ダウンロードします。
//その場合バージョンを記載しなければならないです。
plugins {
	id "java"
    // これは、依存関係を一元管理ために記載している
    // dependencyManagementとあわせて使用すると効果がある。
	id "io.spring.dependency-management" version "1.0.10.RELEASE"
}

repositories {
	// 効果は二つ：①、Jarパッケージのダウンロード用　　　②、推移的な依存関係情報を取得
	mavenCentral()
}

// 推移的な依存関係の定義
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
    
    implementation fileTree(dir: "${System.properties['user.home']}/lib/" , includes: ['*.jar'])
    implementation files('libs/foo.jar', 'libs/bar.jar')
    
    // test　sourceSetsに対する依存関係の定義
	testCompile "org.springframework:spring-webmvc"
    
    // other　sourceSetsに対する依存関係の定義
    otherimplementation "org.springframework:spring-webmvc"
}


jar {
    manifestContentCharset 'utf-8'
    metadataCharset 'utf-8'
    manifest {
        attributes "Main-Class": "cn.buddie.GradleTest"
    }
    from {
        configurations.compile.collect { it.isDirectory() ? it : zipTree(it) }
    }
}

eclipse {
    classpath {
       downloadSources=true
    }
}

task sourcesJar(type: Jar, dependsOn: classes) {
    classifier = 'sources'
    from sourceSets.main.allSource
}

task javadocJar(type: Jar, dependsOn: javadoc) {
    classifier = 'javadoc'
    from javadoc.destinationDir
}

tasks.withType(Javadoc) {
    options.addStringOption('Xdoclint:none', '-quiet')
    options.addStringOption('encoding', 'UTF-8')
    options.addStringOption('charSet', 'UTF-8')
}

artifacts {
    archives sourcesJar
    archives javadocJar
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

- runtimeOnly

  ❓❓❓❓

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
configuration.all.resolutionStrategy{
	force 'org.springframework:spring-web'
}
```

### 2.3.4 依存関係競争自動解決しない

```groovy
configurations.all {
    resolutionStrategy.failOnVersionConflict()
}
```

## 2.4 カスタマイズConfig❓





## 2.5 依存関係ローカルコピー

```groovy
task copyDependenciesToLocalDir(type: Copy){
	from configurations.XXX.asFileTree
    to "${System.properties['user.home']}/lib/temp"
}
XXX:compile rumetime ...
```



## 2.6 SnapShotと動態モジュールのキャッシュ設定

```groovy
// https://docs.gradle.org/current/userguide/dynamic_versions.html#sec:controlling_dependency_caching_programmatically
configurations.all {
    // 設定DynamicVersionsのキャッシュ期間
    resolutionStrategy.cacheDynamicVersionsFor 10, 'minutes'
    // 設定モジュールのキャッシュ期間
    resolutionStrategy.cacheChangingModulesFor 4, 'hours'
}
```





# 3 プラグイン

## 3.1 分類

### 3.1.1 作成方式より切り分け

- binary plugins 

- script plugins

  - gradeファイルの方法

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
  
  - Javaソースでのほうほう
  
    - `[projectPath]\buildSrc\src\main\java` 　pluginを実現されたクラスファイルを格納すれば　例えば
  
      ```Java
      
      import org.gradle.api.Plugin;
      import org.gradle.api.Project;
      
      public class BinaryRepositoryVersionPlugin implements Plugin<Project> {
          public void apply(Project project) {
              BinaryRepositoryExtension extension =
                  project.getExtensions().create("binaryRepo", BinaryRepositoryExtension.class);
      
              project.getTasks().register("latestArtifactVersion", LatestArtifactVersion.class, task -> {
                  task.getCoordinates().set(extension.getCoordinates());
                  task.getServerUrl().set(extension.getServerUrl());
              });
          }
      }
      ```
  
    - 使用例
  
      ```groovy
      apply　plugins : BinaryRepositoryVersionPlugin
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



XXX.jar/META-INF/gradle-plugins/[pluginName].properties

```
implementation-class=com.andriod.xxx.xxx
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









## 3.5 java プラグイン

- 提供するタスク
- 提供する拡張

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



## 3.6 eclipse プラグイン

- 提供するタスク

- 提供する拡張





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
    first.finalizedBy second
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

- プロパティより実施するかどうかの制御、Excute部分のみ、コンフィグは実施される

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

  



# 5 Pulish















# 6 class

Gradleタスクを実施する際に、下記のパスに、Build.gradleをクラスファイルに生成される・・

D:\Tools-Java\gradle-5.6.4\jar-caches\caches\5.6.4\scripts-remapped

例：







# 7 ユーザーフォルダ







# 8 Daemon



# 9.ラッパー



# 10.マルチプロジェクト

http://gradle.monochromeroad.com/docs/userguide/multi_project_builds.html

## 10.1 関連コマンド

```groovy
// 対象プロジェクトの依存関係プロジェクトをコンパイルせず、本プロジェクトのみコンパイルする
Gradle :[project]:build -a


```

## 10.2 構成

```groovy
test
 ├─src
 ├─build.gradle
 ├─settings.gradle
 ├─shared
 │  ├─src
 │  └─build.gradle  // なくでもよい、親プロジェクトのbuild.gradleに定義してもよい
 │
 ├─api
 │  ├─src
 │  └─build.gradle  // なくでもよい、親プロジェクトのbuild.gradleに定義してもよい
 │
 └─services
    ├─src
    └─build.gradle  // なくでもよい、親プロジェクトのbuild.gradleに定義してもよい
 
```

## 10.3 build.gradle記載例

- settings.gradle

```groovy
rootProject.name = 'test'

include 'shared'
include 'api'
include 'services'
include 'AAA:BBB:CCC' //階層的なフォルダ構成

// 各サブプロジェクトのBuild.gradleファイル名の指定
// todo-sharedというサブプロジェクトであれば、shared.gradleとする
rootProject.children.each{
    it.buildFileName = it.name + '.gradle' - 'todo-'
}

```

- build.gradle

```groovy
// 全てのプロジェクトに適用
allprojects {
    task hello << {task -> println "I'm $task.project.name" }
}
// Root以外のプロジェクトに適用
subprojects {
    hello << {println "- I depend on water"}
}
// 特な対象プロジェクトに操作
configure(subprojects.findAll {it.name != 'tropicalFish'}) {
    hello << {println '- I love to spend time in the arctic waters.'}
}
```



# Debug

- 一回デバッグ終了後、Gradle --stopが必要かも



# groovy



# XX Gradle使用ポイント

- タスク名前はキャメルケースの先頭文字で短縮できまる。

  例えば、`gradle dependencies = gradle d`   ただし、短縮より、重複する場合、エラーになる

- 特定タスクをスキップ

  `gradle build -x test`   javaソースをビルドして、単体テスト実施せず

- Javaプロジェクトでいくつかよく使用するタスク

  - `gradle dependencies (gradle d)`  依存関係一覧を表示
  - `gradle properties` プロパティ一覧を表示
  - `gradle tasks` タスク一覧を表示

- マルチプロジェクトの場合、特定プロジェクトのタスクを実施したい場合

  `gradle [project-name]:build`   OR  `gradle build  -p [project-name]`

- build.gradle以外のスクリプトファイルを実施する場合

  `gradle build -b test.gradle`   

- 変数を追加して実施する

  `gradle build -Pmyprop=myvalue`   Gradleスクリプトで使用する普通な変数

  `gradle build -Dmyprop=myvalue`   JVMへ連携するのシステム変数

- デバッグ

  ```bash
  gradle551 help -Dorg.gradle.debug=true
  ```

  5005ポートを利用している

- ローカルモード

  ```
  gradle build --offline
  ```

- バージョンConflict時エラーになるため

  ```groovy
  configurations.all {
      resolutionStrategy.failOnVersionConflict()
  }
  ```

- 依存関係Conflict時詳しい情報を表示

  ```groovy
  gradle :dependencyInsight --configuration houxSpring --dependency org.slf4j:slf4j-api
  ```

  





ps aux | grep Gradle | awk '{print $2}' | xargs kill -9

