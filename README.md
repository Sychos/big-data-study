# 大数据体系从入门到精通学习路线

## 项目简介
这是一个大数据体系从入门到精通的项目，旨在帮助初学者快速入门大数据，掌握大数据的核心技术，并能够在实际项目中应用，为以后的工作做好准备。

## 项目目标
1. 让初学者能够快速入门大数据
2. 掌握大数据的核心技术
3. 能够在实际项目中应用
4. 能够自己搭建主流大数据架构（Hadoop、Spark、Flink、Hive、MySQL、ClickHouse、Redis等）
5. 掌握大数据的核心技术栈

## 面向角色
- 初学者
- 有一定技术基础但没有系统学习过大数据技术的开发者
- 想通过项目实践掌握大数据技术的学习者

## 技术栈
- **编程语言**: Java 1.8
- **构建工具**: Maven 3.8+
- **版本控制**: Git
- **大数据框架**: Hadoop 3.3.4 ✅
- **存储计算**: HDFS + YARN ✅
- **数据库**: PostgreSQL 14+ (计划中)
- **流处理**: Spark 3.3+ + Flink 1.17+ (计划中)
- **数据分析**: ClickHouse 23.3+ (计划中)
- **缓存**: Redis 7.0+ (计划中)
- **数据仓库**: Hive 3.1+ (计划中)

> ✅ 表示已完成，其他为计划中

## 环境配置
### 硬件环境
- **台式机 (Win11)**: 主要用于代码编写和开发
- **笔记本 (Win10)**: 主要用于安装大数据环境和软件
- 两台电脑在同一内网环境中

### 软件环境
- **台式机 (Win11)**: JDK 1.8 + Maven
- **笔记本 (Win10)**: JDK 1.8 + Hadoop 3.3.4

## 当前项目进展

### 已完成功能
1. **环境搭建完成**
   - 台式机 (Win11): JDK 1.8 + Maven + IDE 环境配置完成
   - 笔记本 (Win10): JDK 1.8 + Hadoop 3.3.4 环境配置完成
   - Git 版本控制环境配置完成

2. **HDFS Java API 示例项目**
   - 完整的 Maven 项目结构
   - HDFS 连接和配置管理
   - 文件上传、下载、删除等基本操作
   - 目录创建、列表、删除等目录操作
   - 大文件处理示例
   - 日志分析项目示例
   - 完整的单元测试

3. **文档和教程**
   - Hadoop 在 Windows 10 上的详细安装教程
   - HDFS Java API 使用教程
   - 快速入门指南
   - 自动化安装脚本

### 技术特性
- **跨网络访问**: 支持台式机通过网络访问笔记本上的 Hadoop 集群
- **配置管理**: 灵活的配置文件管理，支持不同环境切换
- **错误处理**: 完善的异常处理和日志记录
- **代码复用**: 模块化设计，便于扩展和复用

## 快速开始

### 前置条件
1. 笔记本 (Win10) 上已安装并启动 Hadoop 3.3.4
2. 台式机 (Win11) 上已安装 JDK 1.8 和 Maven
3. 两台电脑在同一内网环境中

### 运行步骤
1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd big-data-study
   ```

2. **配置 HDFS 连接**
   - 编辑 `code/hdfs-java-demo/src/main/resources/hdfs-config.properties`
   - 修改 `hdfs.namenode.address` 为笔记本的 IP 地址

3. **编译项目**
   ```bash
   cd code/hdfs-java-demo
   mvn clean compile
   ```

4. **下载依赖**
   ```bash
   mvn dependency:copy-dependencies
   ```

5. **运行快速入门示例**
   ```bash
   java -cp "target/classes;target/dependency/*" com.bigdata.hdfs.QuickStart
   ```

### 示例功能
- **QuickStart.java**: HDFS 基本操作演示
- **HDFSBasicExample.java**: 基础文件操作示例
- **HDFSLargeFileExample.java**: 大文件处理示例
- **LogAnalyzer.java**: 日志分析项目示例

## 学习路线规划

### 第一阶段：基础环境搭建 (1-2周)
#### 1.1 台式机环境配置
- [x] 安装JDK 1.8
- [x] 配置JAVA_HOME环境变量
- [x] 安装Maven
- [x] 配置Maven环境变量和本地仓库
- [x] 安装IDE (推荐IntelliJ IDEA)
- [x] 配置Git环境

#### 1.2 笔记本环境配置
- [x] 安装JDK 1.8
- [x] 配置JAVA_HOME环境变量
- [x] 安装Hadoop 3.3.4
- [x] 配置Hadoop环境变量
- [x] 配置Hadoop伪分布式模式
- [x] 启动HDFS和YARN服务

#### 1.3 数据库环境
- [ ] 安装PostgreSQL
- [ ] 创建项目数据库
- [ ] 配置数据库连接

### 第二阶段：Hadoop生态系统学习 (2-3周)
#### 2.1 HDFS分布式文件系统
- [x] HDFS架构原理学习
- [x] HDFS命令行操作
- [x] Java API操作HDFS
- [x] 实现文件上传下载功能

#### 2.2 YARN资源管理
- [ ] YARN架构原理学习
- [ ] 资源调度机制
- [ ] 编写MapReduce程序
- [ ] 提交作业到YARN集群

#### 2.3 MapReduce编程
- [ ] MapReduce编程模型
- [ ] 词频统计案例
- [ ] 数据清洗案例
- [ ] 自定义InputFormat和OutputFormat

### 第三阶段：Hive数据仓库 (1-2周)
#### 3.1 Hive安装配置
- [ ] 安装Hive
- [ ] 配置Hive Metastore
- [ ] 集成PostgreSQL作为元数据库

#### 3.2 Hive SQL学习
- [ ] 创建数据库和表
- [ ] 数据导入导出
- [ ] 分区表和分桶表
- [ ] Hive函数使用
- [ ] 自定义UDF函数

### 第四阶段：Spark大数据处理 (2-3周)
#### 4.1 Spark Core
- [ ] 安装配置Spark
- [ ] Spark架构原理
- [ ] RDD编程
- [ ] Spark SQL使用
- [ ] DataFrame和Dataset API

#### 4.2 Spark Streaming
- [ ] 流处理概念
- [ ] DStream编程
- [ ] 实时数据处理案例
- [ ] 与Kafka集成

### 第五阶段：Flink流处理 (2周)
#### 5.1 Flink基础
- [ ] 安装配置Flink
- [ ] Flink架构原理
- [ ] DataStream API
- [ ] 窗口操作

#### 5.2 Flink高级特性
- [ ] 状态管理
- [ ] 检查点机制
- [ ] 事件时间处理
- [ ] 与Kafka集成

### 第六阶段：ClickHouse数据分析 (1-2周)
#### 6.1 ClickHouse安装配置
- [ ] 安装ClickHouse
- [ ] 配置集群
- [ ] 创建数据库和表

#### 6.2 ClickHouse使用
- [ ] 数据导入
- [ ] 查询优化
- [ ] 物化视图
- [ ] 与其他组件集成

### 第七阶段：Redis缓存 (1周)
#### 7.1 Redis基础
- [ ] 安装配置Redis
- [ ] 数据类型和操作
- [ ] 持久化机制

#### 7.2 Redis集成
- [ ] Java客户端使用
- [ ] 缓存策略设计
- [ ] 与大数据组件集成

### 第八阶段：项目实战 (3-4周)
#### 8.1 数据采集模块
- [ ] 设计数据采集架构
- [ ] 实现日志采集功能
- [ ] 实现数据库数据采集
- [ ] 实现API数据采集

#### 8.2 数据处理模块
- [ ] 数据清洗和转换
- [ ] 实时数据处理
- [ ] 批量数据处理
- [ ] 数据质量监控

#### 8.3 数据存储模块
- [ ] 设计数据存储架构
- [ ] HDFS数据存储
- [ ] Hive数据仓库
- [ ] ClickHouse数据分析

#### 8.4 数据分析模块
- [ ] 实时数据分析
- [ ] 历史数据分析
- [ ] 数据可视化
- [ ] 报表生成

### 第九阶段：系统优化和监控 (1-2周)
#### 9.1 性能优化
- [ ] Hadoop集群优化
- [ ] Spark作业优化
- [ ] Flink作业优化
- [ ] 数据库查询优化

#### 9.2 监控告警
- [ ] 集群监控
- [ ] 作业监控
- [ ] 数据质量监控
- [ ] 告警机制设计

## 项目结构
```
big-data-study/
├── .gitignore              # Git忽略文件
├── README.md               # 项目说明
├── code/                   # 代码目录
│   └── hdfs-java-demo/     # HDFS Java API 示例项目
│       ├── README.md       # 项目说明
│       ├── pom.xml         # Maven配置文件
│       └── src/            # 源代码
│           ├── main/
│           │   ├── java/com/bigdata/hdfs/
│           │   │   ├── QuickStart.java          # 快速入门示例
│           │   │   ├── config/HDFSConfig.java   # HDFS配置类
│           │   │   ├── example/                 # 示例代码
│           │   │   │   ├── HDFSBasicExample.java
│           │   │   │   └── HDFSLargeFileExample.java
│           │   │   ├── project/LogAnalyzer.java # 日志分析项目
│           │   │   └── util/HDFSUtil.java       # HDFS工具类
│           │   └── resources/
│           │       ├── hdfs-config.properties   # HDFS配置
│           │       └── log4j.properties         # 日志配置
│           └── test/java/                       # 测试代码
├── docs/                   # 文档目录
│   ├── installation/       # 安装教程
│   │   └── hadoop-installation-win10.md        # Hadoop安装教程
│   ├── quick-start/        # 快速入门
│   │   └── hadoop-quickstart.md                # Hadoop快速入门
│   └── tutorials/          # 学习教程
│       └── hdfs-java-api-tutorial.md           # HDFS Java API教程
├── scripts/                # 脚本文件
│   └── setup-hadoop-win10.ps1                  # Hadoop自动安装脚本
├── 生成要求.md              # 项目生成要求
└── 项目目标.md              # 项目目标说明
```

## 学习资源
### 官方文档
- [Hadoop官方文档](https://hadoop.apache.org/docs/)
- [Spark官方文档](https://spark.apache.org/docs/latest/)
- [Flink官方文档](https://flink.apache.org/docs/stable/)
- [Hive官方文档](https://hive.apache.org/)
- [ClickHouse官方文档](https://clickhouse.com/docs/)

### 推荐书籍
- 《Hadoop权威指南》
- 《Spark快速大数据分析》
- 《Flink基础教程》
- 《大数据技术原理与应用》

## 注意事项
1. 每个阶段完成后需要进行实践验证
2. 遇到问题及时记录和解决
3. 定期备份代码和配置文件
4. 保持学习笔记和总结
5. 积极参与社区讨论和交流

## 贡献指南
欢迎提交Issue和Pull Request来完善这个项目！

## 许可证
MIT License

---

**开始你的大数据学习之旅吧！** 🚀