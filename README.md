UidGenerator
==========================
[In Chinese 中文版](README.zh_cn.md)

UidGenerator is a Java implemented, [Snowflake](https://github.com/twitter/snowflake) based unique ID generator. It
works as a component, and allows users to override workId bits and initialization strategy. As a result, it is much more
suitable for virtualization environment, such as [docker](https://www.docker.com/). Besides these, it overcomes
concurrency limitation of Snowflake algorithm by consuming future time; parallels UID produce and consume by caching
UID with RingBuffer; eliminates CacheLine pseudo sharing, which comes from RingBuffer, via padding. And finally, it
can offer over <font color=red>6 million</font> QPS per single instance.

Requires：[Java8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)+,
[MySQL](https://dev.mysql.com/downloads/mysql/)(Default implement as WorkerID assigner; If there are other implements, MySQL is not required)

Snowflake
-------------
![Snowflake](doc/snowflake.png)  
** Snowflake algorithm：** An unique id consists of worker node, timestamp and sequence within that timestamp. Usually,
it is a 64 bits number(long), and the default bits of that three fields are as follows:

* sign(1bit)  
  The highest bit is always 0.

* delta seconds (28 bits)  
  The next 28 bits, represents delta seconds since a customer epoch(2016-05-20). The maximum time will be 8.7 years.

* worker id (22 bits)  
  The next 22 bits, represents the worker node id, maximum value will be 4.2 million. UidGenerator uses a build-in
  database based ```worker id assigner``` when startup by default, and it will dispose previous work node id after
  reboot. Other strategy such like 'reuse' is coming soon.

* sequence (13 bits)   
  the last 13 bits, represents sequence within the one second, maximum is 8192 per second by default.
  
**The parameters above can be configured in spring bean**


CachedUidGenerator
-------------------
RingBuffer is an array，each item of that array is called 'slot', every slot keeps a uid or a flag(Double RingBuffer).
The size of RingBuffer is 2^<sup>n</sup>, where n is positive integer and equal or greater than bits of
```sequence```. Assign bigger value to ```boostPower``` if you want to enlarge RingBuffer to improve throughput.

###### Tail & Cursor pointer
* Tail Pointer

  Represents the latest produced UID. If it catches up with cursor, the ring buffer will be full, at that moment, no put
  operation should be allowed, you can specify a policy to handle it by assigning
  property ```rejectedPutBufferHandler```.
  
* Cursor Pointer

  Represents the latest already consumed UID. If cursor catches up with tail, the ring buffer will be empty, and
  any take operation will be rejected. you can also specify a policy to handle it  by assigning
  property ```rejectedTakeBufferHandler```.

![RingBuffer](doc/ringbuffer.png)  

CachedUidGenerator used double RingBuffer，one RingBuffer for UID, another for status(if valid for take or put)

Array can improve performance of reading, due to the CUP cache mechanism. At the same time, it brought the side
effect of 「False Sharing」, in order to solve it, cache line padding is applied.

![FalseSharing](doc/cacheline_padding.png) 

#### RingBuffer filling
* Initialization padding
  During RingBuffer initializing，the entire RingBuffer will be filled.
  
* In-time filling
  Whenever the percent of available UIDs is less than threshold ```paddingFactor```, the fill task is triggered. You can
  reassign that  threshold in Spring bean configuration.
  
* Periodic filling
  Filling periodically in a scheduled thread. The```scheduleInterval``` can be reassigned in Spring bean configuration.


Quick Start
------------
Here we have a demo with 4 steps to introduce how to integrate UidGenerator into Spring based projects.<br/>

### Step 1: Install Java8, Maven, MySQL
If you have already installed maven, jdk8+ and Mysql or other DB which supported by Mybatis, just skip to next.<br/>
Download [Java8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html),
[MySQL](https://dev.mysql.com/downloads/mysql/) and [Maven](https://maven.apache.org/download.cgi),
and install jdk, mysql. For maven, extracting and setting MAVEN_HOME is enough.

#### Set JAVA_HOME & MAVEN_HOME
Here is a sample script to set JAVA_HOME and MAVEN_HOME
```shell
export MAVEN_HOME=/xxx/xxx/software/maven/apache-maven-3.3.9
export PATH=$MAVEN_HOME/bin:$PATH
JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_91.jdk/Contents/Home";
export JAVA_HOME;
```

### Step 2: Create table WORKER_NODE
Replace ```xxxxx``` with real database name, and run following script to create table,
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
TYPE INT NOT NULL COMMENT 'node type: ACTUAL or CONTAINER',
LAUNCH_DATE DATE NOT NULL COMMENT 'launch date',
MODIFIED TIMESTAMP NOT NULL COMMENT 'modified time',
CREATED TIMESTAMP NOT NULL COMMENT 'created time',
PRIMARY KEY(ID)
)
 COMMENT='DB WorkerID Assigner for UID Generator',ENGINE = INNODB;
```

Reset property of 'jdbc.url', 'jdbc.username' and 'jdbc.password' in [mysql.properties](src/test/resources/uid/mysql.properties).

### Step 3: Spring configuration
#### DefaultUidGenerator
There are two implements of UidGenerator: [DefaultUidGenerator](src/main/java/com/baidu/fsg/uid/impl/DefaultUidGenerator.java), [CachedUidGenerator](src/main/java/com/baidu/fsg/uid/impl/CachedUidGenerator.java).<br/>
For performance sensitive application, CachedUidGenerator is recommended.

```xml
<!-- DefaultUidGenerator -->
<bean id="defaultUidGenerator" class="com.baidu.fsg.uid.impl.DefaultUidGenerator" lazy-init="false">
    <property name="workerIdAssigner" ref="disposableWorkerIdAssigner"/>

    <!-- Specified bits & epoch as your demand. No specified the default value will be used -->
    <property name="timeBits" value="29"/>
    <property name="workerBits" value="21"/>
    <property name="seqBits" value="13"/>
    <property name="epochStr" value="2016-09-20"/>
</bean>
 
<!-- Disposable WorkerIdAssigner based on Database -->
<bean id="disposableWorkerIdAssigner" class="com.baidu.fsg.uid.worker.DisposableWorkerIdAssigner" />

```

#### CachedUidGenerator
Copy beans of CachedUidGenerator to 'test/resources/uid/cached-uid-spring.xml'.
```xml
<!-- CachedUidGenerator -->
<bean id="cachedUidGenerator" class="com.baidu.fsg.uid.impl.CachedUidGenerator">
    <property name="workerIdAssigner" ref="disposableWorkerIdAssigner" />
 
    <!-- The config below is option -->
    <!-- Specified bits & epoch as your demand. No specified the default value will be used -->
    <property name="timeBits" value="29"/>
    <property name="workerBits" value="21"/>
    <property name="seqBits" value="13"/>
    <property name="epochStr" value="2016-09-20"/>
    <!-- RingBuffer size, to improve the throughput. -->
    <!-- Default as 3. Sample: original bufferSize=8192, after boosting the new bufferSize= 8192 << 3 = 65536 -->
    <property name="boostPower" value="3"></property>
 
    <!-- In-time padding, available UIDs percentage(0, 100) of the RingBuffer, default as 50 -->
    <!-- Sample: bufferSize=1024, paddingFactor=50 -> threshold=1024 * 50 / 100 = 512. -->
    <!-- When the rest available UIDs < 512, RingBiffer will be padded in-time -->
    <property name="paddingFactor" value="50"></property>
 
    <!-- Periodic padding -->
    <!-- Default is disabled. Enable as below, scheduleInterval unit as Seconds. -->
    <property name="scheduleInterval" value="60"></property>
 
    <!-- Policy for rejecting put on RingBuffer -->
    <property name="rejectedPutBufferHandler" ref="XxxxYourPutRejectPolicy"></property>
 
    <!-- Policy for rejecting take from RingBuffer -->
    <property name="rejectedTakeBufferHandler" ref="XxxxYourTakeRejectPolicy"></property>
 
</bean>
 
<!-- Disposable WorkerIdAssigner based on Database -->
<bean id="disposableWorkerIdAssigner" class="com.baidu.fsg.uid.worker.DisposableWorkerIdAssigner" />
 
<!-- Mybatis config... -->
```

#### Mybatis config
[mybatis-spring.xml](src/test/resources/uid/mybatis-spring.xml) shows as below:
```xml
<!-- Spring annotation scan -->
<context:component-scan base-package="com.baidu.fsg.uid" />

<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource" />
    <property name="mapperLocations" value="classpath:/META-INF/mybatis/mapper/M_WORKER*.xml" />
</bean>

<!-- transaction -->
<tx:annotation-driven transaction-manager="transactionManager" order="1" />

<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
	<property name="dataSource" ref="dataSource" />
</bean>

<!-- Mybatis Mapper scan -->
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
	<property name="annotationClass" value="org.springframework.stereotype.Repository" />
	<property name="basePackage" value="com.baidu.fsg.uid.worker.dao" />
	<property name="sqlSessionFactoryBeanName" value="sqlSessionFactory" />
</bean>

<!-- datasource config -->
<bean id="dataSource" parent="abstractDataSource">
	<property name="driverClassName" value="${mysql.driver}" />
	<property name="maxActive" value="${jdbc.maxActive}" />
	<property name="url" value="${jdbc.url}" />
	<property name="username" value="${jdbc.username}" />
	<property name="password" value="${jdbc.password}" />
</bean>

<bean id="abstractDataSource" class="com.alibaba.druid.pool.DruidDataSource" destroy-method="close">
	<property name="filters" value="${datasource.filters}" />
	<property name="defaultAutoCommit" value="${datasource.defaultAutoCommit}" />
	<property name="initialSize" value="${datasource.initialSize}" />
	<property name="minIdle" value="${datasource.minIdle}" />
	<property name="maxWait" value="${datasource.maxWait}" />
	<property name="testWhileIdle" value="${datasource.testWhileIdle}" />
	<property name="testOnBorrow" value="${datasource.testOnBorrow}" />
	<property name="testOnReturn" value="${datasource.testOnReturn}" />
	<property name="validationQuery" value="${datasource.validationQuery}" />
	<property name="timeBetweenEvictionRunsMillis" value="${datasource.timeBetweenEvictionRunsMillis}" />
	<property name="minEvictableIdleTimeMillis" value="${datasource.minEvictableIdleTimeMillis}" />
	<property name="logAbandoned" value="${datasource.logAbandoned}" />
	<property name="removeAbandoned" value="${datasource.removeAbandoned}" />
	<property name="removeAbandonedTimeout" value="${datasource.removeAbandonedTimeout}" />
</bean>

<bean id="batchSqlSession" class="org.mybatis.spring.SqlSessionTemplate">
	<constructor-arg index="0" ref="sqlSessionFactory" />
	<constructor-arg index="1" value="BATCH" />
</bean>
```

### Step 4: Run UnitTest
Run [CachedUidGeneratorTest](src/test/java/com/baidu/fsg/uid/CachedUidGeneratorTest.java), shows how to generate / parse UniqueID:
```java
@Resource
private UidGenerator uidGenerator;

@Test
public void testSerialGenerate() {
    // Generate UID
    long uid = uidGenerator.getUID();

    // Parse UID into [Timestamp, WorkerId, Sequence]
    // {"UID":"180363646902239241","parsed":{    "timestamp":"2017-01-19 12:15:46",    "workerId":"4",    "sequence":"9"        }}
    System.out.println(uidGenerator.parseUID(uid));

}
```

### Tips
For low concurrency and long term application, less ```seqBits``` but more ```timeBits``` is recommended. For
example, if DisposableWorkerIdAssigner is adopted and the average reboot frequency is 12 per node per day, with the
configuration ```{"workerBits":23,"timeBits":31,"seqBits":9}```, one project can run for 68 years with 28 nodes
and entirely concurrency 14400 UID/s.

For frequent reboot and long term application, less ```seqBits``` but more ```timeBits``` and ```workerBits``` is
recommended. For example, if DisposableWorkerIdAssigner is adopted and the average reboot frequency is 24 * 12 per node
per day, with the configuration ```{"workerBits":27,"timeBits":30,"seqBits":6}```, one project can run for 34 years
with 37 nodes and entirely concurrency 2400 UID/s.

#### Experiment for Throughput
To figure out CachedUidGenerator's UID throughput, some experiments are carried out.<br/>
Firstly, workerBits is arbitrarily fixed to 20, and change timeBits from 25(about 1 year) to 32(about 136 years),<br/>

|timeBits|25|26|27|28|29|30|31|32|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|throughput|6,831,465|7,007,279|6,679,625|6,499,205|6,534,971|7,617,440|6,186,930|6,364,997|

![throughput1](doc/throughput1.png)

Then, timeBits is arbitrarily fixed to 31, and workerBits is changed from 20(about 1 million total reboots) to 29(about
 500 million total reboots),<br/>

|workerBits|20|21|22|23|24|25|26|27|28|29|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|throughput|6,186,930|6,642,727|6,581,661|6,462,726|6,774,609|6,414,906|6,806,266|6,223,617|6,438,055|6,435,549|

![throughput2](doc/throughput2.png)

It is obvious that whatever the configuration is, CachedUidGenerator always has the ability to provide **6 million**
stable throughput, what sacrificed is just life expectancy, this is very cool.

Finally, both timeBits and workerBits are fixed to 31 and 23 separately, and change the number of CachedUidGenerator
consumer. Since our CPU only has 4 cores, \[1, 8\] is chosen.<br/>

|consumers|1|2|3|4|5|6|7|8|
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
|throughput|6,462,726|6,542,259|6,077,717|6,377,958|7,002,410|6,599,113|7,360,934|6,490,969|

![throughput3](doc/throughput3.png)
