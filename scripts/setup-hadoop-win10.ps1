# Hadoop 3.3.4 Windows 10 自动配置脚本
# 使用方法：以管理员身份运行 PowerShell，然后执行此脚本

# 设置错误处理
$ErrorActionPreference = "Stop"

Write-Host "=== Hadoop 3.3.4 Windows 10 配置脚本 ===" -ForegroundColor Green
Write-Host "请确保您已经安装了 JDK 1.8 和 Maven 3.5+" -ForegroundColor Yellow

# 检查管理员权限
if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "错误：请以管理员身份运行此脚本！" -ForegroundColor Red
    exit 1
}

# 配置变量
$HADOOP_VERSION = "3.3.4"
$HADOOP_HOME = "C:\hadoop\hadoop-$HADOOP_VERSION"
$HADOOP_DATA_DIR = "C:\hadoop\data"
$WINUTILS_URL = "https://github.com/steveloughran/winutils/raw/master/hadoop-3.0.0/bin/winutils.exe"
$HADOOP_DLL_URL = "https://github.com/steveloughran/winutils/raw/master/hadoop-3.0.0/bin/hadoop.dll"

# 函数：检查 Java 安装
function Test-JavaInstallation {
    try {
        $javaVersion = java -version 2>&1 | Select-String "version"
        if ($javaVersion -match "1\.8") {
            Write-Host "✓ Java 1.8 已安装" -ForegroundColor Green
            return $true
        } else {
            Write-Host "✗ 需要 Java 1.8，当前版本：$javaVersion" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "✗ Java 未安装或未配置环境变量" -ForegroundColor Red
        return $false
    }
}

# 函数：创建目录
function New-HadoopDirectories {
    Write-Host "创建 Hadoop 目录结构..." -ForegroundColor Cyan
    
    $directories = @(
        "$HADOOP_DATA_DIR\tmp",
        "$HADOOP_DATA_DIR\namenode",
        "$HADOOP_DATA_DIR\datanode",
        "C:\hadoop\logs"
    )
    
    foreach ($dir in $directories) {
        if (!(Test-Path $dir)) {
            New-Item -ItemType Directory -Force -Path $dir | Out-Null
            Write-Host "✓ 创建目录：$dir" -ForegroundColor Green
        } else {
            Write-Host "✓ 目录已存在：$dir" -ForegroundColor Yellow
        }
    }
}

# 函数：下载文件
function Download-File {
    param(
        [string]$Url,
        [string]$OutputPath
    )
    
    try {
        Write-Host "下载：$Url" -ForegroundColor Cyan
        Invoke-WebRequest -Uri $Url -OutFile $OutputPath -UseBasicParsing
        Write-Host "✓ 下载完成：$OutputPath" -ForegroundColor Green
    } catch {
        Write-Host "✗ 下载失败：$Url" -ForegroundColor Red
        Write-Host "错误：$($_.Exception.Message)" -ForegroundColor Red
    }
}

# 函数：设置环境变量
function Set-HadoopEnvironmentVariables {
    Write-Host "设置环境变量..." -ForegroundColor Cyan
    
    # 设置系统环境变量
    [Environment]::SetEnvironmentVariable("HADOOP_HOME", $HADOOP_HOME, "Machine")
    [Environment]::SetEnvironmentVariable("HADOOP_CONF_DIR", "$HADOOP_HOME\etc\hadoop", "Machine")
    [Environment]::SetEnvironmentVariable("YARN_CONF_DIR", "$HADOOP_HOME\etc\hadoop", "Machine")
    
    # 更新 PATH
    $currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
    $hadoopPaths = @(
        "$HADOOP_HOME\bin",
        "$HADOOP_HOME\sbin"
    )
    
    foreach ($path in $hadoopPaths) {
        if ($currentPath -notlike "*$path*") {
            $currentPath += ";$path"
        }
    }
    
    [Environment]::SetEnvironmentVariable("PATH", $currentPath, "Machine")
    
    Write-Host "✓ 环境变量设置完成" -ForegroundColor Green
    Write-Host "注意：需要重新打开 PowerShell 窗口以使环境变量生效" -ForegroundColor Yellow
}

# 函数：创建配置文件
function New-HadoopConfigFiles {
    Write-Host "创建 Hadoop 配置文件..." -ForegroundColor Cyan
    
    $configDir = "$HADOOP_HOME\etc\hadoop"
    
    # core-site.xml
    $coreSiteContent = @"
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
"@
    
    # hdfs-site.xml
    $hdfsSiteContent = @"
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
"@
    
    # mapred-site.xml
    $mapredSiteContent = @"
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
"@
    
    # yarn-site.xml
    $yarnSiteContent = @"
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
"@
    
    # 写入配置文件
    $coreSiteContent | Out-File -FilePath "$configDir\core-site.xml" -Encoding UTF8
    $hdfsSiteContent | Out-File -FilePath "$configDir\hdfs-site.xml" -Encoding UTF8
    $mapredSiteContent | Out-File -FilePath "$configDir\mapred-site.xml" -Encoding UTF8
    $yarnSiteContent | Out-File -FilePath "$configDir\yarn-site.xml" -Encoding UTF8
    
    Write-Host "✓ 配置文件创建完成" -ForegroundColor Green
}

# 主执行流程
try {
    # 检查 Java 安装
    if (!(Test-JavaInstallation)) {
        Write-Host "请先安装 JDK 1.8 并配置环境变量" -ForegroundColor Red
        exit 1
    }
    
    # 检查 Hadoop 是否已安装
    if (!(Test-Path $HADOOP_HOME)) {
        Write-Host "错误：未找到 Hadoop 安装目录：$HADOOP_HOME" -ForegroundColor Red
        Write-Host "请先下载并解压 Hadoop 3.3.4 到 C:\hadoop\" -ForegroundColor Yellow
        Write-Host "下载地址：https://archive.apache.org/dist/hadoop/common/hadoop-3.3.4/hadoop-3.3.4.tar.gz" -ForegroundColor Yellow
        exit 1
    }
    
    Write-Host "✓ 找到 Hadoop 安装目录：$HADOOP_HOME" -ForegroundColor Green
    
    # 创建目录结构
    New-HadoopDirectories
    
    # 下载 Windows 支持文件
    $winutilsPath = "$HADOOP_HOME\bin\winutils.exe"
    $hadoopDllPath = "$HADOOP_HOME\bin\hadoop.dll"
    
    if (!(Test-Path $winutilsPath)) {
        Download-File -Url $WINUTILS_URL -OutputPath $winutilsPath
    } else {
        Write-Host "✓ winutils.exe 已存在" -ForegroundColor Yellow
    }
    
    if (!(Test-Path $hadoopDllPath)) {
        Download-File -Url $HADOOP_DLL_URL -OutputPath $hadoopDllPath
    } else {
        Write-Host "✓ hadoop.dll 已存在" -ForegroundColor Yellow
    }
    
    # 设置环境变量
    Set-HadoopEnvironmentVariables
    
    # 创建配置文件
    New-HadoopConfigFiles
    
    # 设置文件权限
    if (Test-Path $winutilsPath) {
        Write-Host "设置文件权限..." -ForegroundColor Cyan
        & "$winutilsPath" chmod 755 "$HADOOP_HOME\bin\winutils.exe"
        & "$winutilsPath" chmod 755 "$HADOOP_DATA_DIR"
        & "$winutilsPath" chmod 755 "$HADOOP_DATA_DIR\namenode"
        & "$winutilsPath" chmod 755 "$HADOOP_DATA_DIR\datanode"
        Write-Host "✓ 文件权限设置完成" -ForegroundColor Green
    }
    
    Write-Host "" 
    Write-Host "=== 配置完成！===" -ForegroundColor Green
    Write-Host "下一步操作：" -ForegroundColor Yellow
    Write-Host "1. 重新打开 PowerShell 窗口（以使环境变量生效）" -ForegroundColor White
    Write-Host "2. 执行：hdfs namenode -format（格式化 NameNode，只需执行一次）" -ForegroundColor White
    Write-Host "3. 执行：start-dfs.cmd（启动 HDFS）" -ForegroundColor White
    Write-Host "4. 执行：start-yarn.cmd（启动 YARN）" -ForegroundColor White
    Write-Host "5. 访问 Web 界面：" -ForegroundColor White
    Write-Host "   - HDFS: http://localhost:9870" -ForegroundColor Cyan
    Write-Host "   - YARN: http://localhost:8088" -ForegroundColor Cyan
    
} catch {
    Write-Host "配置过程中发生错误：$($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")