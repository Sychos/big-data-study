# Hadoop 3.3.4 在 Windows 10 上的安装配置指南

## 前置条件
- ✅ JDK 1.8 已安装
- ✅ Maven 3.5+ 已安装
- Windows 10 操作系统

## 第一步：下载 Hadoop 3.3.4

### 1.1 下载地址
访问 Apache Hadoop 官网：https://hadoop.apache.org/releases.html

或直接下载链接：
```
https://archive.apache.org/dist/hadoop/common/hadoop-3.3.4/hadoop-3.3.4.tar.gz
```

### 1.2 解压安装
1. 将下载的 `hadoop-3.3.4.tar.gz` 解压到 `C:\hadoop\` 目录
2. 确保路径为：`C:\hadoop\hadoop-3.3.4\`

## 第二步：下载 Windows 支持文件

### 2.1 下载 winutils.exe
Hadoop 在 Windows 上运行需要额外的工具文件：

1. 访问：https://github.com/steveloughran/winutils
2. 下载对应版本的 winutils.exe 和 hadoop.dll
3. 将文件放置到：`C:\hadoop\hadoop-3.3.4\bin\` 目录下

### 2.2 设置文件权限
以管理员身份运行 PowerShell，执行：
```powershell
# 进入 Hadoop bin 目录
cd C:\hadoop\hadoop-3.3.4\bin

# 设置 winutils.exe 权限
.\winutils.exe chmod 755 C:\hadoop\hadoop-3.3.4\bin\winutils.exe
```

## 第三步：配置环境变量

### 3.1 系统环境变量配置
1. 右键 "此电脑" → "属性" → "高级系统设置" → "环境变量"
2. 在 "系统变量" 中新建以下变量：

```
变量名：HADOOP_HOME
变量值：C:\hadoop\hadoop-3.3.4

变量名：HADOOP_CONF_DIR
变量值：C:\hadoop\hadoop-3.3.4\etc\hadoop

变量名：YARN_CONF_DIR
变量值：C:\hadoop\hadoop-3.3.4\etc\hadoop
```

### 3.2 更新 PATH 变量
在系统变量的 PATH 中添加：
```
%HADOOP_HOME%\bin
%HADOOP_HOME%\sbin
```

### 3.3 验证环境变量
重新打开 PowerShell，执行：
```powershell
echo $env:HADOOP_HOME
hadoop version
```

## 第四步：配置 Hadoop

### 4.1 配置 core-site.xml
编辑文件：`C:\hadoop\hadoop-3.3.4\etc\hadoop\core-site.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://10.132.144.24:9000</value>
    </property>
    <property>
        <name>hadoop.tmp.dir</name>
        <value>C:/hadoop/data/tmp</value>
    </property>
    <property>
        <name>hadoop.proxyuser.hadoop.hosts</name>
        <value>*</value>
    </property>
    <property>
        <name>hadoop.proxyuser.hadoop.groups</name>
        <value>*</value>
    </property>
</configuration>
```

### 4.2 配置 hdfs-site.xml
编辑文件：`C:\hadoop\hadoop-3.3.4\etc\hadoop\hdfs-site.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
    <property>
        <name>dfs.namenode.name.dir</name>
        <value>C:/hadoop/data/namenode</value>
    </property>
    <property>
        <name>dfs.datanode.data.dir</name>
        <value>C:/hadoop/data/datanode</value>
    </property>
    <property>
        <name>dfs.namenode.http-address</name>
        <value>localhost:9870</value>
    </property>
</configuration>
```

### 4.3 配置 mapred-site.xml
编辑文件：`C:\hadoop\hadoop-3.3.4\etc\hadoop\mapred-site.xml`

```xml
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
    <property>
        <name>mapreduce.application.classpath</name>
        <value>%HADOOP_HOME%/share/hadoop/mapreduce/*,%HADOOP_HOME%/share/hadoop/mapreduce/lib/*</value>
    </property>
</configuration>
```

### 4.4 配置 yarn-site.xml
编辑文件：`C:\hadoop\hadoop-3.3.4\etc\hadoop\yarn-site.xml`

```xml
<?xml version="1.0"?>
<configuration>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
    <property>
        <name>yarn.nodemanager.env-whitelist</name>
        <value>JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CONF_DIR,CLASSPATH_PREPEND_DISTCACHE,HADOOP_YARN_HOME,HADOOP_MAPRED_HOME</value>
    </property>
    <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>localhost</value>
    </property>
    <property>
        <name>yarn.resourcemanager.webapp.address</name>
        <value>localhost:8088</value>
    </property>
</configuration>
```

### 4.5 配置 hadoop-env.cmd
编辑文件：`C:\hadoop\hadoop-3.3.4\etc\hadoop\hadoop-env.cmd`

在文件末尾添加：
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_XXX
set HADOOP_PREFIX=C:\hadoop\hadoop-3.3.4
set HADOOP_CONF_DIR=%HADOOP_PREFIX%\etc\hadoop
set YARN_CONF_DIR=%HADOOP_PREFIX%\etc\hadoop
set PATH=%PATH%;%HADOOP_PREFIX%\bin
```

注意：请将 `jdk1.8.0_XXX` 替换为您实际的 JDK 安装路径。

## 第五步：创建数据目录

在 PowerShell 中执行：
```powershell
# 创建必要的目录
New-Item -ItemType Directory -Force -Path "C:\hadoop\data\tmp"
New-Item -ItemType Directory -Force -Path "C:\hadoop\data\namenode"
New-Item -ItemType Directory -Force -Path "C:\hadoop\data\datanode"
New-Item -ItemType Directory -Force -Path "C:\hadoop\logs"
```

## 第六步：格式化 NameNode

在 PowerShell 中执行：
```powershell
# 进入 Hadoop bin 目录
cd C:\hadoop\hadoop-3.3.4\bin

# 格式化 NameNode（只需执行一次）
.\hdfs namenode -format
```

看到 "Storage directory has been successfully formatted" 表示格式化成功。

## 第七步：启动 Hadoop 服务

### 7.1 启动 HDFS
```powershell
# 启动 NameNode
cd C:\hadoop\hadoop-3.3.4\sbin
.\start-dfs.cmd
```

### 7.2 启动 YARN
```powershell
# 启动 YARN
.\start-yarn.cmd
```

### 7.3 验证服务状态
```powershell
# 查看 Java 进程
jps
```

应该看到以下进程：
- NameNode
- DataNode
- ResourceManager
- NodeManager

## 第八步：验证安装

### 8.1 Web 界面验证
打开浏览器访问：
- HDFS Web UI: http://localhost:9870
- YARN Web UI: http://localhost:8088

### 8.2 命令行验证
```powershell
# 查看 HDFS 状态
hdfs dfsadmin -report

# 创建测试目录
hdfs dfs -mkdir /test

# 查看目录
hdfs dfs -ls /

# 上传测试文件
echo "Hello Hadoop" > test.txt
hdfs dfs -put test.txt /test/

# 查看文件内容
hdfs dfs -cat /test/test.txt
```

## 第九步：停止服务

当需要停止 Hadoop 服务时：
```powershell
cd C:\hadoop\hadoop-3.3.4\sbin

# 停止 YARN
.\stop-yarn.cmd

# 停止 HDFS
.\stop-dfs.cmd
```

## 常见问题解决

### 问题1：权限错误
```powershell
# 设置目录权限
winutils.exe chmod 755 C:\hadoop\data
winutils.exe chmod 755 C:\hadoop\data\namenode
winutils.exe chmod 755 C:\hadoop\data\datanode
```

### 问题2：端口冲突
如果默认端口被占用，可以修改配置文件中的端口号。

### 问题3：Java 路径问题
确保 JAVA_HOME 环境变量正确设置，且指向 JDK 1.8 安装目录。

## 下一步

完成 Hadoop 安装后，您可以：
1. 学习 HDFS 命令操作
2. 编写 MapReduce 程序
3. 安装 Hive 数据仓库
4. 集成 Spark 大数据处理框架

---

**恭喜！您已成功在 Windows 10 上安装配置了 Hadoop 3.3.4！** 🎉