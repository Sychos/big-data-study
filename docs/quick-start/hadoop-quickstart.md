# Hadoop 3.3.4 快速启动指南

## 📋 前置检查清单
- ✅ JDK 1.8 已安装
- ✅ Maven 3.5+ 已安装
- ✅ Windows 10 系统
- ✅ 管理员权限

## 🚀 快速安装步骤

### 步骤 1：下载 Hadoop
```powershell
# 下载地址
https://archive.apache.org/dist/hadoop/common/hadoop-3.3.4/hadoop-3.3.4.tar.gz

# 解压到
C:\hadoop\hadoop-3.3.4\
```

### 步骤 2：运行自动配置脚本
```powershell
# 以管理员身份运行 PowerShell
# 进入项目目录
cd e:\devCode\big-data-study

# 执行配置脚本
.\scripts\setup-hadoop-win10.ps1
```

### 步骤 3：重启 PowerShell
关闭当前 PowerShell 窗口，重新以管理员身份打开。

### 步骤 4：格式化 NameNode
```powershell
# 只需执行一次
hdfs namenode -format
```

### 步骤 5：启动服务
```powershell
# 启动 HDFS
start-dfs.cmd

# 启动 YARN
start-yarn.cmd
```

## 🔍 验证安装

### 1. 检查进程
```powershell
jps
```
应该看到：
- NameNode
- DataNode
- ResourceManager
- NodeManager

### 2. Web 界面验证
- **HDFS Web UI**: http://localhost:9870
- **YARN Web UI**: http://localhost:8088

### 3. 命令行测试
```powershell
# 查看 HDFS 状态
hdfs dfsadmin -report

# 创建测试目录
hdfs dfs -mkdir /test

# 上传测试文件
echo "Hello Hadoop from Windows 10!" > test.txt
hdfs dfs -put test.txt /test/

# 查看文件
hdfs dfs -ls /test
hdfs dfs -cat /test/test.txt
```

## 🛠️ 常用命令

### 启动/停止服务
```powershell
# 启动所有服务
start-dfs.cmd
start-yarn.cmd

# 停止所有服务
stop-yarn.cmd
stop-dfs.cmd
```

### HDFS 操作
```powershell
# 查看目录
hdfs dfs -ls /

# 创建目录
hdfs dfs -mkdir /mydir

# 上传文件
hdfs dfs -put localfile.txt /mydir/

# 下载文件
hdfs dfs -get /mydir/localfile.txt .

# 删除文件
hdfs dfs -rm /mydir/localfile.txt

# 删除目录
hdfs dfs -rm -r /mydir
```

## ⚠️ 常见问题

### 问题 1：权限错误
```powershell
# 设置目录权限
winutils.exe chmod 755 C:\hadoop\data
```

### 问题 2：端口被占用
检查并修改配置文件中的端口：
- NameNode: 9000 → 9001
- Web UI: 9870 → 9871
- YARN: 8088 → 8089

### 问题 3：Java 路径问题
```powershell
# 检查 Java 版本
java -version

# 检查环境变量
echo $env:JAVA_HOME
```

## 📊 性能监控

### 系统资源监控
```powershell
# 查看 Java 进程
jps -l

# 查看内存使用
jps -v
```

### Web 界面监控
- **集群概览**: http://localhost:9870/dfshealth.html
- **数据节点**: http://localhost:9870/datanodes.html
- **YARN 应用**: http://localhost:8088/cluster/apps

## 🎯 下一步学习

完成 Hadoop 安装后，建议按以下顺序学习：

1. **HDFS 深入学习**
   - 文件系统架构
   - 副本机制
   - 安全模式

2. **MapReduce 编程**
   - 词频统计示例
   - 自定义 Mapper/Reducer
   - 作业提交和监控

3. **YARN 资源管理**
   - 资源调度
   - 队列管理
   - 应用程序生命周期

## 📚 学习资源

- [Hadoop 官方文档](https://hadoop.apache.org/docs/r3.3.4/)
- [HDFS 架构指南](https://hadoop.apache.org/docs/r3.3.4/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html)
- [MapReduce 教程](https://hadoop.apache.org/docs/r3.3.4/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html)

---

**🎉 恭喜！您已成功配置 Hadoop 环境，可以开始大数据学习之旅了！**

如有问题，请参考详细安装文档：`docs/installation/hadoop-installation-win10.md`