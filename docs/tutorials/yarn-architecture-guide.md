# YARN资源管理架构指南

## 概述

YARN（Yet Another Resource Negotiator）是Hadoop 2.0引入的资源管理系统，它将资源管理和作业调度分离，使Hadoop能够支持更多类型的应用程序，不仅仅是MapReduce。

## YARN的诞生背景

### Hadoop 1.x的局限性

在Hadoop 1.x中，JobTracker承担了太多职责：
- **资源管理**: 管理集群中的计算资源
- **作业调度**: 决定作业的执行顺序
- **作业监控**: 监控作业的执行状态
- **任务调度**: 将任务分配给TaskTracker

这种设计导致了以下问题：
1. **单点故障**: JobTracker故障会导致整个集群不可用
2. **扩展性限制**: 单个JobTracker难以管理大规模集群
3. **资源利用率低**: 静态的slot分配导致资源浪费
4. **只支持MapReduce**: 无法运行其他类型的应用

### YARN的解决方案

YARN通过以下方式解决了这些问题：
- **职责分离**: 将资源管理和作业调度分离
- **多应用支持**: 支持MapReduce、Spark、Flink等多种应用
- **动态资源分配**: 根据需求动态分配资源
- **高可用性**: 支持ResourceManager的高可用配置

## YARN架构组件

### 1. ResourceManager (RM)

ResourceManager是YARN的核心组件，负责整个集群的资源管理。

#### 主要功能
- **资源分配**: 根据应用需求分配集群资源
- **应用管理**: 管理应用的生命周期
- **调度决策**: 决定资源分配给哪个应用
- **故障恢复**: 处理NodeManager和ApplicationMaster的故障

#### 核心子组件

##### Scheduler（调度器）
```
调度器类型：
├── FIFO Scheduler（先进先出）
│   ├── 简单的队列调度
│   └── 适用于小集群
├── Capacity Scheduler（容量调度器）
│   ├── 多队列支持
│   ├── 资源保证和限制
│   └── 弹性资源分配
└── Fair Scheduler（公平调度器）
    ├── 公平共享资源
    ├── 抢占机制
    └── 动态队列创建
```

##### ApplicationsManager（应用管理器）
- 接收应用提交请求
- 启动ApplicationMaster
- 监控ApplicationMaster状态
- 重启失败的ApplicationMaster

### 2. NodeManager (NM)

NodeManager运行在集群的每个节点上，负责该节点的资源管理。

#### 主要功能
- **容器管理**: 启动、监控和停止容器
- **资源监控**: 监控节点的CPU、内存使用情况
- **日志管理**: 收集和管理应用日志
- **健康检查**: 定期向ResourceManager报告节点状态

#### 核心组件
```
NodeManager组件：
├── ContainerManager
│   ├── 容器生命周期管理
│   └── 资源隔离和限制
├── NodeHealthChecker
│   ├── 节点健康状态检查
│   └── 磁盘、网络状态监控
├── NodeStatusUpdater
│   ├── 向RM报告节点状态
│   └── 接收RM的指令
└── ContainerExecutor
    ├── 容器进程启动
    └── 安全隔离
```

### 3. ApplicationMaster (AM)

ApplicationMaster是每个应用的"大脑"，负责协调应用的执行。

#### 主要功能
- **资源请求**: 向ResourceManager请求资源
- **任务调度**: 将任务分配给获得的容器
- **进度监控**: 监控任务执行进度
- **故障处理**: 处理任务失败和重试

#### 生命周期
```
ApplicationMaster生命周期：
1. 应用提交 → ResourceManager
2. ResourceManager启动ApplicationMaster
3. ApplicationMaster向ResourceManager注册
4. ApplicationMaster请求资源
5. ResourceManager分配容器
6. ApplicationMaster启动任务
7. 任务执行完成
8. ApplicationMaster注销并退出
```

### 4. Container（容器）

Container是YARN中资源分配的基本单位。

#### 容器特性
- **资源封装**: 封装CPU、内存等资源
- **进程隔离**: 提供进程级别的隔离
- **生命周期管理**: 由NodeManager管理
- **动态分配**: 根据需求动态创建和销毁

#### 容器规格
```java
// 容器资源规格示例
Resource containerResource = Resource.newInstance(
    1024,  // 内存 (MB)
    1      // CPU核心数
);

// 容器优先级
Priority priority = Priority.newInstance(1);

// 容器请求
ContainerRequest containerRequest = new ContainerRequest(
    containerResource,
    null,  // 节点偏好
    null,  // 机架偏好
    priority
);
```

## YARN工作流程

### 1. 应用提交流程

```
应用提交流程：
1. Client提交应用到ResourceManager
   ├── 应用信息（ApplicationSubmissionContext）
   ├── 应用类型（MapReduce、Spark等）
   └── 资源需求

2. ResourceManager验证应用
   ├── 检查资源配额
   ├── 验证用户权限
   └── 分配ApplicationId

3. ResourceManager启动ApplicationMaster
   ├── 选择合适的NodeManager
   ├── 分配容器给ApplicationMaster
   └── 启动ApplicationMaster进程

4. ApplicationMaster注册到ResourceManager
   ├── 报告自身信息
   ├── 获取集群信息
   └── 开始资源协商
```

### 2. 资源分配流程

```
资源分配流程：
1. ApplicationMaster请求资源
   ├── 指定资源需求（CPU、内存）
   ├── 指定位置偏好（数据本地性）
   └── 指定优先级

2. ResourceManager调度决策
   ├── 根据调度策略分配资源
   ├── 考虑集群资源状况
   └── 考虑队列配置和限制

3. ResourceManager分配容器
   ├── 选择合适的NodeManager
   ├── 创建ContainerToken
   └── 返回容器分配信息

4. ApplicationMaster启动容器
   ├── 联系目标NodeManager
   ├── 提供ContainerToken
   └── 启动应用任务
```

### 3. 任务执行流程

```
任务执行流程：
1. 容器启动
   ├── NodeManager验证ContainerToken
   ├── 分配本地资源
   └── 启动容器进程

2. 任务执行
   ├── 应用代码在容器中运行
   ├── ApplicationMaster监控进度
   └── NodeManager监控资源使用

3. 任务完成
   ├── 容器进程退出
   ├── NodeManager清理资源
   └── ApplicationMaster更新状态

4. 应用完成
   ├── 所有任务完成
   ├── ApplicationMaster注销
   └── ResourceManager清理应用信息
```

## YARN调度器详解

### 1. Capacity Scheduler（容量调度器）

#### 特点
- **多队列支持**: 支持多个队列，每个队列有独立的资源配置
- **资源保证**: 每个队列有最小资源保证
- **弹性共享**: 空闲资源可以被其他队列使用
- **层次化队列**: 支持队列的层次化组织

#### 配置示例
```xml
<!-- capacity-scheduler.xml -->
<configuration>
  <!-- 队列定义 -->
  <property>
    <name>yarn.scheduler.capacity.root.queues</name>
    <value>default,production,development</value>
  </property>
  
  <!-- 队列容量配置 -->
  <property>
    <name>yarn.scheduler.capacity.root.default.capacity</name>
    <value>40</value>
  </property>
  
  <property>
    <name>yarn.scheduler.capacity.root.production.capacity</name>
    <value>40</value>
  </property>
  
  <property>
    <name>yarn.scheduler.capacity.root.development.capacity</name>
    <value>20</value>
  </property>
  
  <!-- 最大容量配置 -->
  <property>
    <name>yarn.scheduler.capacity.root.default.maximum-capacity</name>
    <value>60</value>
  </property>
</configuration>
```

### 2. Fair Scheduler（公平调度器）

#### 特点
- **公平共享**: 所有应用公平共享集群资源
- **抢占机制**: 可以抢占其他应用的资源
- **动态队列**: 支持动态创建队列
- **多种公平策略**: 支持不同的公平性定义

#### 配置示例
```xml
<!-- fair-scheduler.xml -->
<allocations>
  <queue name="production">
    <minResources>10000 mb,0 vcores</minResources>
    <maxResources>90000 mb,0 vcores</maxResources>
    <maxRunningApps>50</maxRunningApps>
    <weight>40.0</weight>
    <schedulingPolicy>fair</schedulingPolicy>
  </queue>
  
  <queue name="development">
    <minResources>5000 mb,0 vcores</minResources>
    <maxResources>30000 mb,0 vcores</maxResources>
    <maxRunningApps>20</maxRunningApps>
    <weight>60.0</weight>
    <schedulingPolicy>fair</schedulingPolicy>
  </queue>
  
  <queuePlacementPolicy>
    <rule name="specified" />
    <rule name="user" />
    <rule name="default" queue="development" />
  </queuePlacementPolicy>
</allocations>
```

## YARN资源管理

### 1. 资源类型

#### 基本资源类型
```java
// CPU和内存资源
Resource resource = Resource.newInstance(
    4096,  // 内存 (MB)
    2      // CPU核心数 (vCores)
);

// 扩展资源类型（Hadoop 3.x）
ResourceInformation gpuResource = ResourceInformation.newInstance(
    "yarn.io/gpu",
    "",
    1L,
    ResourceTypes.COUNTABLE,
    0L,
    Long.MAX_VALUE
);
```

#### 资源配置
```xml
<!-- yarn-site.xml -->
<configuration>
  <!-- NodeManager资源配置 -->
  <property>
    <name>yarn.nodemanager.resource.memory-mb</name>
    <value>8192</value>
  </property>
  
  <property>
    <name>yarn.nodemanager.resource.cpu-vcores</name>
    <value>4</value>
  </property>
  
  <!-- 容器最小资源 -->
  <property>
    <name>yarn.scheduler.minimum-allocation-mb</name>
    <value>512</value>
  </property>
  
  <property>
    <name>yarn.scheduler.minimum-allocation-vcores</name>
    <value>1</value>
  </property>
  
  <!-- 容器最大资源 -->
  <property>
    <name>yarn.scheduler.maximum-allocation-mb</name>
    <value>4096</value>
  </property>
  
  <property>
    <name>yarn.scheduler.maximum-allocation-vcores</name>
    <value>2</value>
  </property>
</configuration>
```

### 2. 资源隔离

#### CGroups资源隔离
```xml
<!-- 启用CGroups -->
<property>
  <name>yarn.nodemanager.container-executor.class</name>
  <value>org.apache.hadoop.yarn.server.nodemanager.LinuxContainerExecutor</value>
</property>

<property>
  <name>yarn.nodemanager.linux-container-executor.cgroups.hierarchy</name>
  <value>/hadoop-yarn</value>
</property>

<property>
  <name>yarn.nodemanager.linux-container-executor.cgroups.mount</name>
  <value>true</value>
</property>

<property>
  <name>yarn.nodemanager.linux-container-executor.cgroups.mount-path</name>
  <value>/sys/fs/cgroup</value>
</property>
```

## YARN高可用配置

### ResourceManager HA

```xml
<!-- yarn-site.xml -->
<configuration>
  <!-- 启用RM HA -->
  <property>
    <name>yarn.resourcemanager.ha.enabled</name>
    <value>true</value>
  </property>
  
  <!-- RM集群ID -->
  <property>
    <name>yarn.resourcemanager.cluster-id</name>
    <value>yarn-cluster</value>
  </property>
  
  <!-- RM ID列表 -->
  <property>
    <name>yarn.resourcemanager.ha.rm-ids</name>
    <value>rm1,rm2</value>
  </property>
  
  <!-- RM地址配置 -->
  <property>
    <name>yarn.resourcemanager.hostname.rm1</name>
    <value>node1.example.com</value>
  </property>
  
  <property>
    <name>yarn.resourcemanager.hostname.rm2</name>
    <value>node2.example.com</value>
  </property>
  
  <!-- ZooKeeper配置 -->
  <property>
    <name>yarn.resourcemanager.zk-address</name>
    <value>zk1:2181,zk2:2181,zk3:2181</value>
  </property>
</configuration>
```

## YARN监控和调优

### 1. 关键指标监控

#### 集群级别指标
- **资源利用率**: CPU、内存使用率
- **队列状态**: 各队列的资源分配和使用情况
- **应用统计**: 运行中、等待中、完成的应用数量
- **节点状态**: 活跃、丢失、不健康的节点数量

#### 应用级别指标
- **应用执行时间**: 从提交到完成的总时间
- **资源等待时间**: 等待资源分配的时间
- **容器分配效率**: 容器分配成功率和速度
- **任务失败率**: 任务失败和重试的比例

### 2. 性能调优建议

#### 资源配置优化
```bash
# 1. 合理设置NodeManager资源
# 预留系统资源，避免过度分配
yarn.nodemanager.resource.memory-mb = 物理内存 * 0.8
yarn.nodemanager.resource.cpu-vcores = CPU核心数 - 1

# 2. 优化容器大小
# 避免资源碎片，提高利用率
yarn.scheduler.minimum-allocation-mb = 512
yarn.scheduler.increment-allocation-mb = 512

# 3. 调整心跳间隔
# 平衡响应性和网络开销
yarn.resourcemanager.nodemanagers.heartbeat-interval-ms = 1000
```

#### 调度器优化
```xml
<!-- Capacity Scheduler优化 -->
<property>
  <name>yarn.scheduler.capacity.resource-calculator</name>
  <value>org.apache.hadoop.yarn.util.resource.DominantResourceCalculator</value>
</property>

<!-- 启用抢占 -->
<property>
  <name>yarn.scheduler.capacity.preemption.enabled</name>
  <value>true</value>
</property>

<!-- 异步调度 -->
<property>
  <name>yarn.scheduler.capacity.schedule-asynchronously.enable</name>
  <value>true</value>
</property>
```

## 实际应用场景

### 1. 多租户环境

```
多租户资源隔离：
├── 生产环境队列（60%资源）
│   ├── 在线服务（30%）
│   └── 批处理任务（30%）
├── 开发环境队列（25%资源）
│   ├── 功能测试（15%）
│   └── 性能测试（10%）
└── 临时任务队列（15%资源）
    ├── 数据分析（10%）
    └── 紧急任务（5%）
```

### 2. 混合工作负载

```java
// 不同类型应用的资源需求
public class WorkloadManager {
    
    // 批处理任务：高内存，低CPU
    public Resource getBatchProcessingResource() {
        return Resource.newInstance(4096, 1);
    }
    
    // 实时计算：均衡资源
    public Resource getStreamProcessingResource() {
        return Resource.newInstance(2048, 2);
    }
    
    // 机器学习：高CPU，高内存
    public Resource getMachineLearningResource() {
        return Resource.newInstance(8192, 4);
    }
}
```

### 3. 弹性伸缩

```bash
# 动态添加节点
yarn rmadmin -addToClusterNodeLabels "gpu=true"
yarn rmadmin -replaceLabelsOnNode "node1:8032=gpu"

# 动态调整队列容量
yarn rmadmin -refreshQueues

# 节点维护模式
yarn rmadmin -transitionToStandby rm1
```

## 故障排查和运维

### 1. 常见问题诊断

#### 资源不足
```bash
# 检查集群资源状态
yarn top
yarn node -list -all

# 检查队列状态
yarn queue -status default

# 检查应用状态
yarn application -list -appStates RUNNING
```

#### 节点异常
```bash
# 检查不健康节点
yarn node -list -states UNHEALTHY

# 查看节点详细信息
yarn node -status node1:8032

# 检查NodeManager日志
tail -f $HADOOP_LOG_DIR/yarn-nodemanager-*.log
```

### 2. 日志分析

#### ResourceManager日志
```bash
# 查看RM日志
tail -f $HADOOP_LOG_DIR/yarn-resourcemanager-*.log

# 过滤特定应用
grep "application_1234567890123_0001" $HADOOP_LOG_DIR/yarn-resourcemanager-*.log
```

#### ApplicationMaster日志
```bash
# 获取AM日志
yarn logs -applicationId application_1234567890123_0001

# 获取特定容器日志
yarn logs -applicationId application_1234567890123_0001 -containerId container_1234567890123_0001_01_000001
```

## 总结

YARN作为Hadoop生态系统的资源管理核心，提供了：

1. **统一资源管理**: 支持多种计算框架
2. **高可扩展性**: 支持大规模集群部署
3. **多租户支持**: 提供资源隔离和配额管理
4. **高可用性**: 支持ResourceManager HA
5. **灵活调度**: 多种调度策略满足不同需求

掌握YARN的架构和原理，对于理解和使用Hadoop生态系统中的各种组件至关重要。在实际应用中，需要根据具体的业务需求和集群规模，合理配置和调优YARN参数，以获得最佳的性能和资源利用率。