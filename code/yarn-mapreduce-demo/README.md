# YARN MapReduce 示例项目

这是一个全面的YARN资源管理和MapReduce编程示例项目，展示了如何在YARN集群上开发、部署和监控MapReduce应用程序。

## 项目概述

本项目包含以下核心功能：

### 1. MapReduce 示例
- **WordCount**: 经典的单词计数MapReduce程序
- 完整的Mapper、Reducer和Driver实现
- 支持自定义配置和性能优化

### 2. YARN 资源管理工具
- **YarnResourceMonitor**: YARN集群资源监控工具
- **YarnApplicationSubmitter**: 应用程序提交和管理工具
- **YarnConfigManager**: YARN配置管理和优化工具

### 3. 企业级特性
- 完整的日志记录和错误处理
- 性能监控和统计信息
- 配置管理和验证
- 高可用性支持

## 项目结构

```
yarn-mapreduce-demo/
├── pom.xml                                    # Maven项目配置
├── README.md                                  # 项目说明文档
└── src/main/java/
    ├── com/bigdata/mapreduce/wordcount/       # WordCount MapReduce示例
    │   ├── WordCountMapper.java               # Mapper实现
    │   ├── WordCountReducer.java              # Reducer实现
    │   └── WordCountDriver.java               # Driver主程序
    └── com/bigdata/yarn/                      # YARN工具类
        ├── YarnResourceMonitor.java           # 资源监控工具
        ├── YarnApplicationSubmitter.java      # 应用提交工具
        └── YarnConfigManager.java             # 配置管理工具
```

## 环境要求

- Java 8 或更高版本
- Apache Hadoop 3.x
- Apache Maven 3.6+
- YARN集群（可以是单节点伪分布式）

## 快速开始

### 1. 编译项目

```bash
# 进入项目目录
cd yarn-mapreduce-demo

# 编译并打包
mvn clean package
```

### 2. 准备测试数据

```bash
# 创建HDFS输入目录
hdfs dfs -mkdir -p /input/wordcount

# 上传测试文件
echo "hello world hello hadoop hello yarn" > test.txt
hdfs dfs -put test.txt /input/wordcount/
```

### 3. 运行WordCount示例

```bash
# 方式1：使用hadoop命令运行
hadoop jar target/yarn-mapreduce-demo-1.0.0.jar \
  com.bigdata.mapreduce.wordcount.WordCountDriver \
  /input/wordcount /output/wordcount

# 方式2：使用yarn命令运行
yarn jar target/yarn-mapreduce-demo-1.0.0.jar \
  com.bigdata.mapreduce.wordcount.WordCountDriver \
  /input/wordcount /output/wordcount
```

### 4. 查看结果

```bash
# 查看输出结果
hdfs dfs -cat /output/wordcount/part-r-00000
```

## 详细功能说明

### WordCount MapReduce程序

#### WordCountMapper
- 负责将输入文本分词并输出`<单词, 1>`键值对
- 支持单词清理和过滤
- 包含详细的日志记录

#### WordCountReducer
- 汇总相同单词的计数
- 支持高频词汇识别
- 提供统计信息输出

#### WordCountDriver
- MapReduce作业的主入口
- 支持YARN集群配置
- 包含作业监控和统计功能

### YARN工具类

#### YarnResourceMonitor
监控YARN集群资源使用情况：

```java
YarnResourceMonitor monitor = new YarnResourceMonitor();
monitor.init();

// 打印集群指标
monitor.printClusterMetrics();

// 打印节点报告
monitor.printNodeReports();

// 监控应用程序
monitor.printApplications(EnumSet.of(YarnApplicationState.RUNNING));

monitor.close();
```

#### YarnApplicationSubmitter
编程方式提交应用程序到YARN：

```java
YarnApplicationSubmitter submitter = new YarnApplicationSubmitter();
submitter.init();

// 提交MapReduce应用
ApplicationId appId = submitter.submitMapReduceApplication(
    "WordCount", 
    "/apps/wordcount.jar", 
    "com.bigdata.mapreduce.wordcount.WordCountDriver",
    new String[]{"/input", "/output"},
    "default"
);

// 监控应用执行
FinalApplicationStatus status = submitter.monitorApplication(appId, 600000);

submitter.close();
```

#### YarnConfigManager
管理和优化YARN配置：

```java
YarnConfigManager configManager = new YarnConfigManager();

// 配置ResourceManager
configManager.configureResourceManager("localhost", 8032, 8088);

// 配置NodeManager资源
configManager.configureNodeManagerResources(8192, 4, 100);

// 配置调度器
configManager.configureScheduler(
    "org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler");

// 应用性能优化
configManager.applyPerformanceOptimizations();

// 验证配置
boolean isValid = configManager.validateConfiguration();
```

## 高级用法

### 1. 自定义YARN配置

创建`yarn-site.xml`配置文件：

```xml
<configuration>
    <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>your-rm-host</value>
    </property>
    <property>
        <name>yarn.nodemanager.resource.memory-mb</name>
        <value>8192</value>
    </property>
    <property>
        <name>yarn.nodemanager.resource.cpu-vcores</name>
        <value>4</value>
    </property>
</configuration>
```

### 2. 多队列配置

配置容量调度器支持多队列：

```java
// 在YarnConfigManager中配置多队列
configManager.configureScheduler(
    "org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler");

// 提交到特定队列
ApplicationId appId = submitter.submitMapReduceApplication(
    "WordCount", jarPath, mainClass, args, "production");
```

### 3. 性能调优

#### MapReduce性能优化
```java
// 在WordCountDriver中设置优化参数
conf.setInt("mapreduce.map.memory.mb", 2048);
conf.setInt("mapreduce.reduce.memory.mb", 4096);
conf.set("mapreduce.map.java.opts", "-Xmx1536m");
conf.set("mapreduce.reduce.java.opts", "-Xmx3072m");
```

#### YARN资源优化
```java
// 应用性能优化配置
configManager.applyPerformanceOptimizations();

// 自定义优化
Map<String, String> customOptimizations = new HashMap<>();
customOptimizations.put("yarn.scheduler.capacity.node-locality-delay", "40");
customOptimizations.put("yarn.nodemanager.vmem-check-enabled", "false");
```

## 监控和调试

### 1. 应用程序监控

```bash
# 查看YARN应用列表
yarn application -list

# 查看应用详情
yarn application -status application_xxx

# 查看应用日志
yarn logs -applicationId application_xxx
```

### 2. 集群监控

访问YARN Web UI：
- ResourceManager: http://your-rm-host:8088
- NodeManager: http://your-nm-host:8042
- MapReduce History Server: http://your-history-host:19888

### 3. 性能分析

使用YarnResourceMonitor获取详细的资源使用情况：

```java
// 获取集群资源利用率
ResourceUtilization utilization = monitor.getClusterResourceUtilization();
System.out.println("Memory utilization: " + utilization.getMemoryUtilization() + "%");
System.out.println("CPU utilization: " + utilization.getCoreUtilization() + "%");
```

## 故障排查

### 常见问题

1. **内存不足错误**
   ```
   Container killed by YARN for exceeding memory limits
   ```
   解决方案：增加容器内存配置

2. **连接ResourceManager失败**
   ```
   Failed to connect to ResourceManager
   ```
   解决方案：检查网络连接和RM地址配置

3. **权限问题**
   ```
   Permission denied
   ```
   解决方案：检查HDFS权限和用户配置

### 日志分析

查看详细日志：
```bash
# ApplicationMaster日志
yarn logs -applicationId application_xxx -containerId container_xxx

# NodeManager日志
tail -f $HADOOP_LOG_DIR/yarn-yarn-nodemanager-*.log

# ResourceManager日志
tail -f $HADOOP_LOG_DIR/yarn-yarn-resourcemanager-*.log
```

## 扩展开发

### 1. 自定义MapReduce程序

参考WordCount示例，创建自己的MapReduce程序：

```java
public class CustomMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    // 实现自定义逻辑
}

public class CustomReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    // 实现自定义逻辑
}

public class CustomDriver {
    // 配置和提交作业
}
```

### 2. 集成其他大数据组件

- **Spark on YARN**: 运行Spark应用程序
- **Flink on YARN**: 运行Flink流处理作业
- **HBase**: 与HBase集成进行数据处理

## 贡献指南

1. Fork本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开Pull Request

## 许可证

本项目采用Apache License 2.0许可证。详情请参阅[LICENSE](LICENSE)文件。

## 联系方式

- 项目维护者：BigData Team
- 邮箱：bigdata@example.com
- 项目地址：https://github.com/bigdata-team/yarn-mapreduce-demo

## 更新日志

### v1.0.0 (2024-01-15)
- 初始版本发布
- 实现WordCount MapReduce示例
- 添加YARN资源监控工具
- 添加应用程序提交工具
- 添加配置管理工具
- 完善文档和示例