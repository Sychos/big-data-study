# HDFS Java API 示例项目

这是一个完整的HDFS Java API操作示例项目，演示了如何使用Java程序与Hadoop HDFS进行交互。

## 项目结构

```
hdfs-java-demo/
├── pom.xml                                    # Maven配置文件
├── README.md                                  # 项目说明文档
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bigdata/
│   │   │           └── hdfs/
│   │   │               ├── util/
│   │   │               │   └── HDFSUtil.java          # HDFS工具类
│   │   │               ├── example/
│   │   │               │   ├── HDFSBasicExample.java  # 基础操作示例
│   │   │               │   └── HDFSLargeFileExample.java # 大文件处理示例
│   │   │               └── project/
│   │   │                   └── LogAnalyzer.java       # 日志分析项目案例
│   │   └── resources/
│   │       └── log4j.properties              # 日志配置文件
│   └── test/
│       └── java/
│           └── com/
│               └── bigdata/
│                   └── hdfs/
│                       └── util/
│                           └── HDFSUtilTest.java      # 单元测试
```

## 功能特性

### 1. HDFSUtil工具类
- ✅ 创建目录
- ✅ 上传文件到HDFS
- ✅ 从HDFS下载文件
- ✅ 删除HDFS文件或目录
- ✅ 列出目录内容
- ✅ 读取HDFS文件内容
- ✅ 写入内容到HDFS文件
- ✅ 获取文件状态信息
- ✅ 连接管理和资源释放

### 2. 示例程序
- **基础操作示例**: 演示HDFS的基本文件操作
- **大文件处理示例**: 演示如何高效处理大文件
- **日志分析项目**: 完整的Web日志分析案例

### 3. 单元测试
- 完整的单元测试覆盖
- 自动化测试环境搭建和清理
- 各种边界情况测试

## 环境要求

- **JDK**: 1.8+
- **Maven**: 3.5+
- **Hadoop**: 3.3.4（已安装并启动）
- **操作系统**: Windows 10/Linux/macOS

## 快速开始

### 1. 克隆项目
```bash
cd e:\devCode\big-data-study\code
cd hdfs-java-demo
```

### 2. 确保Hadoop环境运行
```bash
# 启动Hadoop服务
start-dfs.cmd
start-yarn.cmd

# 验证HDFS服务
hdfs dfsadmin -report
```

### 3. 编译项目
```bash
mvn clean compile
```

### 4. 运行示例

#### 基础操作示例
```bash
mvn exec:java -Dexec.mainClass="com.bigdata.hdfs.example.HDFSBasicExample"
```

#### 大文件处理示例
```bash
mvn exec:java -Dexec.mainClass="com.bigdata.hdfs.example.HDFSLargeFileExample"
```

#### 日志分析项目
```bash
mvn exec:java -Dexec.mainClass="com.bigdata.hdfs.project.LogAnalyzer"
```

### 5. 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=HDFSUtilTest
```

## 配置说明

### HDFS连接配置
在代码中修改HDFS连接地址：
```java
// 默认配置
HDFSUtil hdfsUtil = new HDFSUtil("hdfs://10.132.144.24:9000");

// 如果你的HDFS运行在不同端口，请修改相应地址
// HDFSUtil hdfsUtil = new HDFSUtil("hdfs://your-hadoop-host:9000");
```

### 用户权限配置
项目默认使用"hadoop"用户身份访问HDFS，如需修改：
```java
// 在HDFSUtil构造函数中修改
System.setProperty("HADOOP_USER_NAME", "your-username");
```

### 日志级别配置
修改 `src/main/resources/log4j.properties` 文件：
```properties
# 修改日志级别
log4j.rootLogger=DEBUG, stdout  # 改为DEBUG可看到更详细日志
```

## 使用示例

### 基本文件操作
```java
public class QuickStart {
    public static void main(String[] args) throws Exception {
        // 1. 初始化HDFS连接
        HDFSUtil hdfsUtil = new HDFSUtil("hdfs://10.132.144.24:9000");
        
        // 2. 创建目录
        hdfsUtil.createDirectory("/user/myapp");
        
        // 3. 写入文件
        hdfsUtil.writeFile("/user/myapp/hello.txt", "Hello HDFS!");
        
        // 4. 读取文件
        String content = hdfsUtil.readFile("/user/myapp/hello.txt");
        System.out.println("文件内容: " + content);
        
        // 5. 列出目录
        hdfsUtil.listFiles("/user/myapp");
        
        // 6. 关闭连接
        hdfsUtil.close();
    }
}
```

### 文件上传下载
```java
// 上传本地文件到HDFS
hdfsUtil.uploadFile("local_file.txt", "/user/myapp/remote_file.txt");

// 从HDFS下载文件到本地
hdfsUtil.downloadFile("/user/myapp/remote_file.txt", "downloaded_file.txt");
```

### 大文件处理
```java
// 流式读取大文件
FSDataInputStream in = fs.open(new Path("/user/data/large_file.txt"));
BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

String line;
while ((line = reader.readLine()) != null) {
    // 处理每一行数据
    processLine(line);
}

reader.close();
in.close();
```

## 常见问题解决

### 1. 连接超时
```java
// 在Configuration中设置超时参数
Configuration conf = new Configuration();
conf.set("ipc.client.connect.timeout", "10000");
conf.set("ipc.client.connect.max.retries", "3");
```

### 2. 权限问题
```bash
# 设置HDFS目录权限
hdfs dfs -chmod 777 /user
hdfs dfs -chown hadoop:hadoop /user
```

### 3. 内存不足
```bash
# 设置JVM参数
export MAVEN_OPTS="-Xmx2g -Xms1g"
```

### 4. 编码问题
确保所有文件操作都使用UTF-8编码：
```java
// 读取时指定编码
String content = new String(bytes, "UTF-8");

// 写入时指定编码
outputStream.write(content.getBytes("UTF-8"));
```

## 性能优化建议

### 1. 批量操作
```java
// 批量上传多个文件
for (String localFile : localFiles) {
    hdfsUtil.uploadFile(localFile, "/user/data/" + new File(localFile).getName());
}
```

### 2. 缓冲区优化
```java
// 设置合适的缓冲区大小
Configuration conf = new Configuration();
conf.set("io.file.buffer.size", "131072"); // 128KB
```

### 3. 连接复用
```java
// 复用HDFSUtil实例，避免频繁创建连接
HDFSUtil hdfsUtil = new HDFSUtil("hdfs://10.132.144.24:9000");
try {
    // 执行多个操作
    hdfsUtil.createDirectory("/user/data1");
    hdfsUtil.createDirectory("/user/data2");
    // ...
} finally {
    hdfsUtil.close();
}
```

## 扩展开发

### 1. 添加新功能
在 `HDFSUtil` 类中添加新的方法：
```java
/**
 * 复制HDFS文件
 */
public boolean copyFile(String srcPath, String destPath) {
    // 实现文件复制逻辑
}
```

### 2. 自定义配置
创建配置文件 `hdfs-config.properties`：
```properties
hdfs.uri=hdfs://10.132.144.24:9000
hdfs.user=hadoop
hdfs.buffer.size=131072
```

### 3. 添加监控
```java
// 添加操作耗时监控
long startTime = System.currentTimeMillis();
// 执行HDFS操作
long endTime = System.currentTimeMillis();
logger.info("操作耗时: {} 毫秒", endTime - startTime);
```

## 学习路径

1. **基础学习**: 先运行 `HDFSBasicExample`，理解基本操作
2. **进阶学习**: 运行 `HDFSLargeFileExample`，学习大文件处理
3. **项目实践**: 运行 `LogAnalyzer`，了解实际应用场景
4. **深入研究**: 阅读源码，理解HDFS API原理
5. **扩展开发**: 基于项目框架开发自己的应用

## 相关资源

- [Hadoop官方文档](https://hadoop.apache.org/docs/r3.3.4/)
- [HDFS架构指南](https://hadoop.apache.org/docs/r3.3.4/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html)
- [Hadoop Java API文档](https://hadoop.apache.org/docs/r3.3.4/api/)

## 贡献指南

欢迎提交Issue和Pull Request来改进这个项目！

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

---

**Happy Coding! 🚀**