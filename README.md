uid-generator-spring-boot-starter
==========================
基于 [百度UidGenerator](https://github.com/baidu/uid-generator), 做了以下改动：
- 改造为spring-boot-starter的形式，不用部署为分布式，直接建表、在项目中引入，即可使用
- 针对时钟回拨，提供了修正选项（默认启用，可通过配置关闭），小于阈值直接休眠，大于阈值更改机器号
- 对机器id用尽提供了复用策略：取余
- 解除id位数限制，由“必须64位”改为“不大于64位”，可根据需要获取更短id

参数均可通过Spring进行自定义，默认参数为：
- delta seconds (30 bits)  
当前时间，相对于时间基点"2019-02-20"的增量值，单位：秒，最多可支持约34年，超出抛异常
- worker id (16 bits)  
机器id，最多可支持约6.5w次机器启动。内置实现为在启动时由数据库分配，默认分配策略为用后即弃，当前复用策略为取余。
- sequence (7 bits)  
每秒下的并发序列，7 bits可支持每秒128个并发，超出128则等待下一秒

默认参数下，初始id长度为12，最终随时间增加，最长到16位

Quick Start
------------

这里介绍如何在SpringBoot项目中使用uid-generator-spring-boot-starter, 具体流程如下:<br/>

### 步骤1: 创建表WORKER_NODE
在项目数据库里，运行sql脚本以导入表WORKER_NODE, 脚本如下:
```sql
DROP DATABASE IF EXISTS `xxxx`;
CREATE DATABASE `xxxx` ;
use `xxxx`;
DROP TABLE IF EXISTS WORKER_NODE;
CREATE TABLE WORKER_NODE
(
ID BIGINT NOT NULL AUTO_INCREMENT COMMENT 'auto increment id',
HOST_NAME VARCHAR(64) NOT NULL COMMENT 'host name',
PORT VARCHAR(64) NOT NULL COMMENT 'port',
TYPE INT NOT NULL COMMENT 'node type: CONTAINER(1), ACTUAL(2), FAKE(3)',
LAUNCH_DATE DATE NOT NULL COMMENT 'launch date',
MODIFIED TIMESTAMP NOT NULL COMMENT 'modified time',
CREATED TIMESTAMP NOT NULL COMMENT 'created time',
PRIMARY KEY(ID)
)
 COMMENT='DB WorkerID Assigner for UID Generator',ENGINE = INNODB;
```
配置好数据库连接

### 步骤2: Maven引用
当前项目打包，或从Maven仓库中引入uid-generator-spring-boot-starter包
```xml
<dependency>
    <groupId>com.github.wujun234</groupId>
    <artifactId>uid-generator-spring-boot-starter</artifactId>
    <version>1.0.2.RELEASE</version>
</dependency>
```
### 步骤3: 开始使用

UidGenerator接口提供了 UID 生成和解析的方法，提供了两种实现: 
- [DefaultUidGenerator](src/main/java/com/github/wujun234/uid/impl/DefaultUidGenerator.java)  
实时生成
- [CachedUidGenerator](src/main/java/com/github/wujun234/uid/impl/CachedUidGenerator.java)  
生成一次id之后，按序列号+1生成一批id，缓存，供之后请求

如对UID生成性能有要求, 请使用CachedUidGenerator

```java
//@Resource
//private UidGenerator defaultUidGenerator;

@Resource
private UidGenerator cachedUidGenerator;

@Test
public void testSerialGenerate() {
    // Generate UID
    long uid = cachedUidGenerator.getUID();

    // Parse UID into [Timestamp, WorkerId, Sequence]
    // {"UID":"450795408770","timestamp":"2019-02-20 14:55:39","workerId":"27","sequence":"2"}
    System.out.println(cachedUidGenerator.parseUID(uid));

}
```
### 步骤4: 可选设置
#### 自定义配置
以下为可选配置, 如未指定将采用默认值
```yml
uid:
  timeBits: 30             # 时间位, 默认:30
  workerBits: 16           # 机器位, 默认:16
  seqBits: 7               # 序列号, 默认:7
  epochStr: "2019-02-20"   # 初始时间, 默认:"2019-02-20"
  enableBackward: true    # 是否容忍时钟回拨, 默认:true
  maxBackwardSeconds: 1    # 时钟回拨最长容忍时间（秒）, 默认:1
  CachedUidGenerator:     # CachedUidGenerator相关参数
    boostPower: 3          # RingBuffer size扩容参数, 可提高UID生成的吞吐量, 默认:3
    paddingFactor: 50      # 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50
    #scheduleInterval: 60    # 默认:不配置此项, 即不使用Schedule线程. 如需使用, 请指定Schedule线程时间间隔, 单位:秒
```
#### 可选实现
选用CachedUidGenerator时，可以选择实现“拒绝策略”的拓展
- 拒绝策略: 当环已满, 无法继续填充时  
默认无需指定, 将丢弃Put操作, 仅日志记录. 如有特殊需求, 请实现RejectedPutBufferHandler接口(支持Lambda表达式)
- 拒绝策略: 当环已空, 无法继续获取时  
默认无需指定, 将记录日志, 并抛出UidGenerateException异常. 如有特殊需求, 请实现RejectedTakeBufferHandler接口(支持Lambda表达式)
