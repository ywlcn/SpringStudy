# 例

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
    implementation "org.springframework:spring-webmvc"
}
```







# 依存関係定義

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





#### testCompile（testImplementation）

`testCompile` 只在单元测试代码的编译以及最终打包测试apk时有效。



#### debugCompile（debugImplementation）

`debugCompile` 只在 **debug** 模式的编译和最终的 **debug apk** 打包时有效



#### releaseCompile（releaseImplementation）

`Release compile`仅仅针对 **Release** 模式的编译和最终的 **Release apk** 打包。



- API:モジュールにAPI依存関係が含まれている場合、モジュールがそれを転送したいことをGradleに知らせています 他のモジュールへの依存関係 ランタイムとコンパイル時間。この設定はコンパイルと同じように動作します (これは現在廃止されています)。 ライブラリモジュール。それは、API依存関係がその 外部API、Gradleはそれにアクセスできるすべてのモジュールを再コンパイルします コンパイル時の依存関係。だから、多数のAPIを持っている 依存関係はビルド時間を大幅に増やすことができます。あなたが望まない限り 依存関係のAPIを別のテストモジュール、アプリモジュールに公開する 代わりに実装依存関係を使用する必要があります。
- compileOnly:gradleはコンパイルクラスパスにのみ依存関係を追加します(ビルド出力には追加されません)。これは便利です Androidライブラリモジュールを作成するときに、 コンパイル時の依存関係ですが、 ランタイム。つまり、この設定を使用する場合、ライブラリ モジュールには、 依存関係が利用可能であり、その動作を正常に変更して 提供されていない場合でも機能します。これは、 一時的な依存関係を追加しないことによる最終APK 重要です。この設定は、指定されたとおりに動作します(現在は 非推奨) です。
- runtimeonly:gradleは、実行時に使用するために、ビルド出力にのみ依存関係を追加します。つまり、コンパイルには追加されません クラスパス。この設定はapkと同じように動作します(現在は 非推奨) です。