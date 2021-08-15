https://www.cnblogs.com/redcreen/tag/jvm/

https://segmentfault.com/a/1190000023859912?utm_source=sf-related  ★★



双亲委派机制   ---》  https://www.jianshu.com/p/1e4011617650

​     通过这个特性 可以吧自定义的class文件 放到 上层 加载器 实现 一些OSS的  覆盖！！！

沙箱安全机制   可以定义 Java.lang.string 但是  由于 XXXX  实际只会是 JDK原生的 String 发生作用

​      



疑問

１．　MinorでEden、servior の中に、使用されていないObjectの領域を回収することについて、どうやら判定？

２．servior からOldへの移動は　15回　Minor GCがある場合ですが、　　移動タイミングは　Minor GC？

　　　Full GCは・・・



Copy方法　Young

Mark-Sweap　　OLD





GC:   CMS  ---->   G1  



# Heap

デフォルトでは　物理メモリの1/64～1/4としている。

eden:from:to  8:1:1  -XX:+PrintGCDetails

MAT

# クラスローダー

- JVMClassLoader　　　Bootstrap Class Loader    jre/lib/rt.jar
- fs
- fsd
- fs

# GC種類





# メモ

Java虚拟机包括一套字节码指令集、一组寄存器、一个栈、一个垃圾回收堆和一个存储方法域





https://qiita.com/tshk_mtsys/items/5a027fe00b3bff009b17

＝＞　　GC　分類



守护线程　の定義　使用方法　　ユーザースレッドの関係・・・





https://li5jun.com/article/289.html





https://waylau.gitbooks.io/java-virtual-machine-specification/content/docs/01-Introduction/1-Introduction.html





https://www.oracle.com/java/technologies/javase/vmoptions-jsp.html





Sun Hotspot JVM为了提升对象内存分配的效率，对于所创建的线程都会分配一块独立的空间TLAB（Thread Local Allocation Buffer），其大小由JVM根据运行的情况计算而得，在TLAB上分配对象时不需要加锁，因此JVM在给线程的对象分配内存时会尽量的在TLAB上分配，在这种情况下JVM中分配对象内存的性能和C基本是一样高效的，但如果对象过大的话则仍然是直接使用堆空间分配

TLAB仅作用于新生代的Eden Space，因此在编写Java程序时，通常多个小的对象比大的对象分配起来更加高效。





**持久代:**　Meta・・・

   用于存放静态文件，如今Java类、方法等。持久代对垃圾回收没有显著影响，但是有些应用可能动态生成或者调用一些class，例如Hibernate等，在这种时候需要设置一个比较大的持久代空间来存放这些运行过程中新增的类。持久代大小通过-XX:MaxPermSize=<N>进行设置。 



MemoryUsage 对象包含四个值：

- init	表示 Java 虚拟机在启动期间从操作系统请求的用于内存管理的初始内存容量（以字节为单位）。Java 虚拟机可能在运行过程中从操作系统请求更多的内存，也可能将内存释放给系统。init 的值可以是不明确的。
- used	表示当前已经使用的内存量（以字节为单位）。
- committed	表示保证可以由 Java 虚拟机使用的内存量（以字节为单位）。已提交的内存量可以随时间而变化（增加或减少）。Java 虚拟机可能会将内存释放给系统，committed 可以小于 init。committed 将始终大于或等于 used。
- max	表示可以用于内存管理的最大内存量（以字节为单位）。可以不定义其值。如果定义了该值，最大内存量可能随时间而更改。已使用的内存量和已提交的内存量将始终小于或等于 max（如果定义了 max）。如果内存分配试图增加满足以下条件的已使用内存将会失败：used > committed，即使 used <= max 仍然为 true（例如，当系统的虚拟内存不足时）。

## メモリ使用解析

JVMメモリ：Heap　+　NoHeap　　　　それからNavtiveメモリ

測定方法は：

- Young

- Old

- metaspace

- java thread count * Xss

- other thread count * stacksize （非Java线程）

- Direct memory

- native memory

- codecache



## DirectMemory

上限設定：



- 解放条件
  - sun.nio.ch.Util.offerFirstTemporaryDirectBuffer ソース箇所
    - 17回以降　> TEMP_BUF_POOL_SIZE ()  static native int iovMax())
    - isBufferTooLarge    jdk.nio.maxCachedBufferSize=10485760で指定
  - 

https://dzone.com/articles/troubleshooting-problems-with-native-off-heap-memo

# JVM分析関連コマンド、ツール

## jinfo

```bash
## 対象PIDでのReservedCodeCacheSize　を表示
jinfo -flag ReservedCodeCacheSize [PID]
## 対象PIDでの[flag]　を設定変更
jinfo -flag [flag]=200M [PID]
## 対象PIDでシステムプロパティを表示
jinfo -sysprops [PID]
## 対象PIDでフラグを表示
jinfo -flags [PID]
```

## jps

起動されているJavaProcessを表示する

## jstat

メモリの使用状況を出力

```bash
jstat -gc [PID]
```

## jmap

対象PIDでObjectの使用状況やサイズなど

```bash
## ヒープ領域のダンプを取得
jmap -dump:<dump-options> [PID]
jmap -dump:format=b,file=./heapdump.hprof [PID]

##  print class loader statistics
jmap -clstats [PID]
## print information on objects awaiting finalization
jmap -finalizerinfo [PID]
## print histogram of java object heap if the "live" suboption is specified, only count live objects
jmap -histo[:live] [PID]


        
        
```



## jstack スレッドダンプ

```bash
jstack [PID]
```

## jconsole

JVM　GUIツール　GUIでメモリやThreadやJVMのMBeanの状況を確認できる

## jcmd　

```bash
## 対象PIDで使用可能な引数一覧を表示
jcmd [PID]
## スレッド一覧を表示
jcmd [PID] Thread.print
##　メモリ一覧を表示　「-XX:NativeMemoryTracking=summary/detail」が設定されていることが前提
jcmd [PID] VM.native_memory summary
##　GCダンプを取得
jcmd [PID] GC.heap_dump `pwd`/demo.hprof
##　JVMの詳細状況一覧を表示、起動引数やメモリ使用状況や
jcmd [PID] VM.info
##  ==jps
jcmd 
```

## jhsdb



-XX:NativeMemoryTracking=detail -Dreactor.netty.ioWorkerCount=1 -XX:MaxDirectMemorySize=200m



# JVMパラメータ

## メモリ関連パラメータ（よく使用）

| **パラメータ**                           | **意味**                                                   | **デフォルト**       | **説明**                                                     |
| ---------------------------------------- | ---------------------------------------------------------- | -------------------- | ------------------------------------------------------------ |
| -Xms                                     | 初始堆大小                                                 | 物理内存的1/64(<1GB) | 默认(MinHeapFreeRatio参数可以调整)空余堆内存小于40%时，JVM就会增大堆直到-Xmx的最大限制. |
| -Xmx                                     | 最大堆大小                                                 | 物理内存的1/4(<1GB)  | 默认(MaxHeapFreeRatio参数可以调整)空余堆内存大于70%时，JVM会减少堆直到 -Xms的最小限制 |
| -Xmn                                     | 年轻代大小(1.4or lator)                                    |                      | **注意**：此处的大小是（eden+ 2 survivor space).与jmap -heap中显示的New gen是不同的。 整个堆大小=年轻代大小 + 年老代大小 + 持久代大小. 增大年轻代后,将会减小年老代大小.此值对系统性能影响较大,Sun官方推荐配置为整个堆的3/8 |
| -XX:NewSize                              | 设置年轻代大小(for 1.3/1.4)                                |                      |                                                              |
| -XX:MaxNewSize                           | 年轻代最大值(for 1.3/1.4)                                  |                      |                                                              |
| -XX:PermSize<br>-XX:MetaspaceSize        | 设置持久代(perm gen)初始值                                 | 物理内存的1/64       |                                                              |
| -XX:MaxPermSize<br/>-XX:MaxMetaspaceSize | 设置持久代最大值                                           | 物理内存的1/4        |                                                              |
| -Xss                                     | 每个线程的堆栈大小                                         |                      | JDK5.0以后每个线程堆栈大小为1M,以前每个线程堆栈大小为256K.更具应用的线程所需内存大小进行 调整.在相同物理内存下,减小这个值能生成更多的线程.但是操作系统对一个进程内的线程数还是有限制的,不能无限生成,经验值在3000~5000左右 一般小的应用， 如果栈不是很深， 应该是128k够用的 大的应用建议使用256k。这个选项对性能影响比较大，需要严格的测试。（校长） 和threadstacksize选项解释很类似,官方文档似乎没有解释,在论坛中有这样一句话:"” -Xss is translated in a VM flag named ThreadStackSize” 一般设置这个值就可以了。 |
| -XX:ThreadStackSize                      | Thread Stack Size                                          |                      | (0 means use default stack size) [Sparc: 512; Solaris x86: 320 (was 256 prior in 5.0 and earlier); Sparc 64 bit: 1024; Linux amd64: 1024 (was 0 in 5.0 and earlier); all others 0.] |
| -XX:NewRatio                             | 年轻代(包括Eden和两个Survivor区)与年老代的比值(除去持久代) |                      | -XX:NewRatio=4表示年轻代与年老代所占比值为1:4,年轻代占整个堆栈的1/5 Xms=Xmx并且设置了Xmn的情况下，该参数不需要进行设置。 |
| -XX:SurvivorRatio                        | Eden区与Survivor区的大小比值                               |                      | 设置为8,则两个Survivor区与一个Eden区的比值为2:8,一个Survivor区占整个年轻代的1/10 |
|                                          |                                                            |                      |                                                              |
| -XX:ReservedCodeCacheSize                |                                                            | 240M(1.8~)           | NonNMethodCode＋ProfiledCode＋NonProfiledCode                |
| -XX:MaxDirectMemorySize                  |                                                            |                      |                                                              |
|                                          |                                                            |                      |                                                              |
|                                          |                                                            |                      |                                                              |
|                                          |                                                            |                      |                                                              |
|                                          |                                                            |                      |                                                              |
|                                          |                                                            |                      |                                                              |
|                                          |                                                            |                      |                                                              |
| -XX:NativeMemoryTracking                 | NativeMemory解析有効                                       | off                  | detail,summary   jcmd 23448 VM.native_memory summary  scale=MB |
|                                          |                                                            |                      |                                                              |







## メモリ関連パラメータ（その他）

| **パラメータ**              | **意味**                                                   | **デフォルト**       | **説明**                                                     |
| --------------------------- | ---------------------------------------------------------- | -------------------- | ------------------------------------------------------------ |
| -Xms                        | 初始堆大小                                                 | 物理内存的1/64(<1GB) | 默认(MinHeapFreeRatio参数可以调整)空余堆内存小于40%时，JVM就会增大堆直到-Xmx的最大限制. |
| -Xmx                        | 最大堆大小                                                 | 物理内存的1/4(<1GB)  | 默认(MaxHeapFreeRatio参数可以调整)空余堆内存大于70%时，JVM会减少堆直到 -Xms的最小限制 |
| -Xmn                        | 年轻代大小(1.4or lator)                                    |                      | **注意**：此处的大小是（eden+ 2 survivor space).与jmap -heap中显示的New gen是不同的。 整个堆大小=年轻代大小 + 年老代大小 + 持久代大小. 增大年轻代后,将会减小年老代大小.此值对系统性能影响较大,Sun官方推荐配置为整个堆的3/8 |
| -XX:NewSize                 | 设置年轻代大小(for 1.3/1.4)                                |                      |                                                              |
| -XX:MaxNewSize              | 年轻代最大值(for 1.3/1.4)                                  |                      |                                                              |
| -XX:PermSize                | 设置持久代(perm gen)初始值                                 | 物理内存的1/64       |                                                              |
| -XX:MaxPermSize             | 设置持久代最大值                                           | 物理内存的1/4        |                                                              |
| -Xss                        | 每个线程的堆栈大小                                         |                      | JDK5.0以后每个线程堆栈大小为1M,以前每个线程堆栈大小为256K.更具应用的线程所需内存大小进行 调整.在相同物理内存下,减小这个值能生成更多的线程.但是操作系统对一个进程内的线程数还是有限制的,不能无限生成,经验值在3000~5000左右 一般小的应用， 如果栈不是很深， 应该是128k够用的 大的应用建议使用256k。这个选项对性能影响比较大，需要严格的测试。（校长） 和threadstacksize选项解释很类似,官方文档似乎没有解释,在论坛中有这样一句话:"” -Xss is translated in a VM flag named ThreadStackSize” 一般设置这个值就可以了。 |
| -XX:ThreadStackSize         | Thread Stack Size                                          |                      | (0 means use default stack size) [Sparc: 512; Solaris x86: 320 (was 256 prior in 5.0 and earlier); Sparc 64 bit: 1024; Linux amd64: 1024 (was 0 in 5.0 and earlier); all others 0.] |
| -XX:NewRatio                | 年轻代(包括Eden和两个Survivor区)与年老代的比值(除去持久代) |                      | -XX:NewRatio=4表示年轻代与年老代所占比值为1:4,年轻代占整个堆栈的1/5 Xms=Xmx并且设置了Xmn的情况下，该参数不需要进行设置。 |
| -XX:SurvivorRatio           | Eden区与Survivor区的大小比值                               |                      | 设置为8,则两个Survivor区与一个Eden区的比值为2:8,一个Survivor区占整个年轻代的1/10 |
| -XX:LargePageSizeInBytes    | 内存页的大小不可设置过大， 会影响Perm的大小                |                      | =128m                                                        |
| -XX:+UseFastAccessorMethods | 原始类型的快速优化                                         |                      |                                                              |
| -XX:+DisableExplicitGC      | 关闭System.gc()                                            |                      | 这个参数需要严格的测试                                       |
| -XX:MaxTenuringThreshold    | 垃圾最大年龄                                               |                      | 如果设置为0的话,则年轻代对象不经过Survivor区,直接进入年老代. 对于年老代比较多的应用,可以提高效率.如果将此值设置为一个较大值,则年轻代对象会在Survivor区进行多次复制,这样可以增加对象再年轻代的存活 时间,增加在年轻代即被回收的概率 该参数只有在串行GC时才有效. |
| -XX:+AggressiveOpts         | 加快编译                                                   |                      |                                                              |
| -XX:+UseBiasedLocking       | 锁机制的性能改善                                           |                      |                                                              |
| -Xnoclassgc                 | 禁用垃圾回收                                               |                      |                                                              |
| -XX:SoftRefLRUPolicyMSPerMB | 每兆堆空闲空间中SoftReference的存活时间                    | 1s                   | softly reachable objects will remain alive for some amount of time after the last time they were referenced. The default value is one second of lifetime per free megabyte in the heap |
| -XX:PretenureSizeThreshold  | 对象超过多大是直接在旧生代分配                             | 0                    | 单位字节 新生代采用Parallel Scavenge GC时无效 另一种直接在旧生代分配的情况是大的数组对象,且数组中无外部引用对象. |
| -XX:TLABWasteTargetPercent  | TLAB占eden区的百分比                                       | 1%                   |                                                              |
|                             |                                                            |                      |                                                              |



## GC関連参数パラメータ

| **パラメータ**              |                                                   | デフォルト | **説明**                                                     |
| --------------------------- | ------------------------------------------------- | ---------- | ------------------------------------------------------------ |
| -XX:+UseParallelGC          | Full GC采用parallel MSC (此项待验证)              |            | 选择垃圾收集器为并行收集器.此配置仅对年轻代有效.即上述配置下,年轻代使用并发收集,而年老代仍旧使用串行收集.(此项待验证) |
| -XX:+UseParNewGC            | 设置年轻代为并行收集                              |            | 可与CMS收集同时使用 JDK5.0以上,JVM会根据系统配置自行设置,所以无需再设置此值 |
| -XX:ParallelGCThreads       | 并行收集器的线程数                                |            | 此值最好配置与处理器数目相等 同样适用于CMS                   |
| -XX:+UseParallelOldGC       | 年老代垃圾收集方式为并行收集(Parallel Compacting) |            | 这个是JAVA 6出现的参数选项                                   |
| -XX:MaxGCPauseMillis        | 每次年轻代垃圾回收的最长时间(最大暂停时间)        |            | 如果无法满足此时间,JVM会自动调整年轻代大小,以满足此值.       |
| -XX:+UseAdaptiveSizePolicy  | 自动选择年轻代区大小和相应的Survivor区比例        |            | 设置此选项后,并行收集器会自动选择年轻代区大小和相应的Survivor区比例,以达到目标系统规定的最低相应时间或者收集频率等,此值建议使用并行收集器时,一直打开. |
| -XX:GCTimeRatio             | 设置垃圾回收时间占程序运行时间的百分比            |            | 公式为1/(1+n)                                                |
| -XX:+*CollectGen0First*     | FullGC时是否先YGC                                 | false      |                                                              |
| -XX:+*ScavengeBeforeFullGC* | Full GC前调用YGC                                  | true       | Do young generation GC prior to a full GC. (Introduced in 1.4.1.) |
|                             |                                                   |            |                                                              |
|                             |                                                   |            |                                                              |



## JVMダンプ収集パラメータ

| **パラメータ**                  | **説明**                                                     |
| ------------------------------- | ------------------------------------------------------------ |
| -XX:+HeapDumpOnOutOfMemoryError | 当OutOfMemoryError发生时自动生成 Heap Dump 文件。            |
| -XX:+HeapDumpBeforeFullGC       | 当 JVM 执行 FullGC 前执行 dump。                             |
| -XX:+HeapDumpAfterFullGC        | 当 JVM 执行 FullGC 后执行 dump。                             |
| -XX:+HeapDumpOnCtrlBreak        | 交互式获取dump。在控制台按下快捷键Ctrl + Break时，JVM就会转存一下堆快照。 |
| -XX:HeapDumpPath=d:\test.hprof  | 指定 dump 文件存储路径。                                     |
|                                 |                                                              |



## CMS(Contents Management System)パラメータ

| **パラメータ**                         | **説明**                                  | **デフォルト** |                                                              |
| -------------------------------------- | ----------------------------------------- | -------------- | ------------------------------------------------------------ |
| -XX:+UseConcMarkSweepGC                | 使用CMS内存收集                           |                | 测试中配置这个以后,-XX:NewRatio=4的配置失效了,原因不明.所以,此时年轻代大小最好用-Xmn设置.??? |
| -XX:+AggressiveHeap                    |                                           |                | 试图是使用大量的物理内存 长时间大内存使用的优化，能检查计算资源（内存， 处理器数量） 至少需要256MB内存 大量的CPU／内存， （在1.4.1在4CPU的机器上已经显示有提升） |
| -XX:CMSFullGCsBeforeCompaction         | 多少次后进行内存压缩                      |                | 由于并发收集器不对内存空间进行压缩,整理,所以运行一段时间以后会产生"碎片",使得运行效率降低.此值设置运行多少次GC以后对内存空间进行压缩,整理. |
| -XX:+CMSParallelRemarkEnabled          | 降低标记停顿                              |                |                                                              |
| -XX+UseCMSCompactAtFullCollection      | 在FULL GC的时候， 对年老代的压缩          |                | CMS是不会移动内存的， 因此， 这个非常容易产生碎片， 导致内存不够用， 因此， 内存的压缩这个时候就会被启用。 增加这个参数是个好习惯。 可能会影响性能,但是可以消除碎片 |
| -XX:+UseCMSInitiatingOccupancyOnly     | 使用手动定义初始化定义开始CMS收集         |                | 禁止hostspot自行触发CMS GC                                   |
| -XX:CMSInitiatingOccupancyFraction=70  | 使用cms作为垃圾回收 使用70％后开始CMS收集 | 92             | 为了保证不出现promotion failed(见下面介绍)错误,该值的设置需要满足以下公式**[CMSInitiatingOccupancyFraction计算公式](http://www.cnblogs.com/redcreen/archive/2011/05/04/2037057.html#CMSInitiatingOccupancyFraction_value)** |
| -XX:CMSInitiatingPermOccupancyFraction | 设置Perm Gen使用到达多少比率时触发        | 92             |                                                              |
| -XX:+CMSIncrementalMode                | 设置为增量模式                            |                | 用于单CPU情况                                                |
| -XX:+CMSClassUnloadingEnabled          |                                           |                |                                                              |

## JVM実行時データ収集パラメータ



| パラメータ                            | 説明                                                     | 出力例                                                       |
| ------------------------------------- | -------------------------------------------------------- | ------------------------------------------------------------ |
| -XX:+PrintGC                          |                                                          | 输出形式:[GC 118250K->113543K(130112K), 0.0094143 secs] [Full GC 121376K->10414K(130112K), 0.0650971 secs] |
| -XX:+PrintGCDetails                   |                                                          | 输出形式:[GC [DefNew: 8614K->781K(9088K), 0.0123035 secs] 118250K->113543K(130112K), 0.0124633 secs] [GC [DefNew: 8614K->8614K(9088K), 0.0000665 secs][Tenured: 112761K->10414K(121024K), 0.0433488 secs] 121376K->10414K(130112K), 0.0436268 secs] |
| -XX:+PrintGCTimeStamps                |                                                          |                                                              |
| -XX:+PrintGC:PrintGCTimeStamps        |                                                          | 可与-XX:+PrintGC -XX:+PrintGCDetails混合使用 输出形式:11.851: [GC 98328K->93620K(130112K), 0.0082960 secs] |
| -XX:+PrintGCApplicationStoppedTime    | 打印垃圾回收期间程序暂停的时间.可与上面混合使用          | 输出形式:Total time for which application threads were stopped: 0.0468229 seconds |
| -XX:+PrintGCApplicationConcurrentTime | 打印每次垃圾回收前,程序未中断的执行时间.可与上面混合使用 | 输出形式:Application time: 0.5291524 seconds                 |
| -XX:+PrintHeapAtGC                    | 打印GC前后的详细堆栈信息                                 |                                                              |
| -Xloggc:filename                      | 把相关日志信息记录到文件以便分析. 与上面几个配合使用     |                                                              |
| -XX:+PrintClassHistogram              | garbage collects before printing the histogram.          |                                                              |
| -XX:+PrintTLAB                        | 查看TLAB空间的使用情况                                   |                                                              |
| -XX:+PrintTenuringDistribution        | 查看每次minor GC后新的存活周期的阈值                     | Desired survivor size 1048576 bytes, new threshold 7 (max 15) new threshold 7即标识新的存活周期的阈值为7。 |
| -XX:+UseGCLogFileRotation             |                                                          |                                                              |
| -XX:GCLogFileSize=N                   |                                                          | Nに1K, 1M, 1G なりサイズをしていするとログファイルのローテーションされる閾値が設定できる |

 





## ほかの色々

- JVMデバッグ

  ```
  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=1044 Main
  ```

  





export JAVA_TOOL_OPTIONS ???







# jstat 結果の見方

| タイトル | 意味                                                         |
| -------- | ------------------------------------------------------------ |
| S0C      | Survivor 領域 0 の現在の容量 (KB)                            |
| S1C      | Survivor 領域 1 の現在の容量 (KB)                            |
| S0U      | Survivor 領域 0 の使用率 (KB)                                |
| S1U      | Survivor 領域 1 の使用率 (KB)                                |
| EC       | Eden 領域の現在の容量 (KB)                                   |
| EU       | Eden 領域の使用率 (KB)                                       |
| OC       | Old 領域の現在の容量 (KB)                                    |
| OU       | Old 領域の使用率 (KB)                                        |
| PC -> MC | Permanent 領域の現在の容量 (KB)<br>Metaspace 領域の現在の容量 (KB) |
| PU -> MU | Permanent 領域の使用率 (KB)<br/>Metaspace 領域の使用率 (KB)  |
| YGC      | 若い世代の GC イベント数                                     |
| YGCT     | 若い世代のガベージコレクション時間                           |
| FGC      | フル GC イベント数                                           |
| FGCT     | フルガベージコレクション時間                                 |
| GCT      | ガベージコレクション総時間                                   |
| CGC      | 平行ガベージコレクション時間イベント数                       |
| CGCT     | 平行ガベージコレクション時間                                 |
|          |                                                              |
|          |                                                              |
|          |                                                              |
|          |                                                              |
|          |                                                              |
|          |                                                              |
|          |                                                              |

