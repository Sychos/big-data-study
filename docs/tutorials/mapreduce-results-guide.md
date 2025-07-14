# MapReduce 作业结果查看指南

本指南详细介绍如何查看和分析 MapReduce 作业的执行结果和统计信息。

## 1. 作业执行完成后的输出信息

### 1.1 控制台输出

当 WordCount 作业成功完成后，控制台会显示以下信息：

```
=== Job Statistics ===
Job ID: job_1234567890123_0001
Job Name: word count
Job State: SUCCEEDED
Map Input Records: 1000
Map Output Records: 5000
Reduce Input Records: 5000
Reduce Output Records: 500
HDFS Bytes Read: 10 MB
HDFS Bytes Written: 2 MB
=== End of Statistics ===

=== Output Results ===
Results are saved to: /output/wordcount
To view results, use one of the following methods:
1. HDFS Web UI: http://10.132.144.24:9870/explorer.html#/output/wordcount
2. Command line: hdfs dfs -cat /output/wordcount/*
3. Command line: hdfs dfs -ls /output/wordcount
```

### 1.2 统计信息解读

| 统计项 | 说明 |
|--------|------|
| **Job ID** | 作业的唯一标识符 |
| **Job Name** | 作业名称（word count） |
| **Job State** | 作业状态（SUCCEEDED/FAILED/KILLED） |
| **Map Input Records** | Map阶段处理的输入记录数 |
| **Map Output Records** | Map阶段输出的记录数 |
| **Reduce Input Records** | Reduce阶段接收的记录数 |
| **Reduce Output Records** | Reduce阶段输出的最终记录数 |
| **HDFS Bytes Read** | 从HDFS读取的字节数 |
| **HDFS Bytes Written** | 写入HDFS的字节数 |

## 2. 查看作业结果的方法

### 2.1 方法一：HDFS Web UI（推荐）

**访问地址：** `http://10.132.144.24:9870/explorer.html#/output/wordcount`

**操作步骤：**
1. 在浏览器中打开 HDFS Web UI
2. 导航到输出目录（如：`/output/wordcount`）
3. 点击文件名查看内容
4. 可以直接在浏览器中预览结果

**优点：**
- 图形化界面，操作简单
- 可以浏览目录结构
- 支持文件预览和下载

### 2.2 方法二：命令行查看文件内容

```bash
# 查看所有输出文件的内容
hdfs dfs -cat /output/wordcount/*

# 查看特定文件的内容
hdfs dfs -cat /output/wordcount/part-r-00000

# 将结果输出到本地文件
hdfs dfs -cat /output/wordcount/* > local_wordcount_results.txt
```

### 2.3 方法三：命令行查看目录结构

```bash
# 列出输出目录的所有文件
hdfs dfs -ls /output/wordcount

# 查看文件详细信息
hdfs dfs -ls -h /output/wordcount

# 递归查看目录结构
hdfs dfs -ls -R /output/wordcount
```

### 2.4 方法四：下载到本地查看

```bash
# 下载整个输出目录到本地
hdfs dfs -get /output/wordcount ./local_wordcount_output

# 下载特定文件到本地
hdfs dfs -get /output/wordcount/part-r-00000 ./wordcount_result.txt
```

## 3. 输出文件结构说明

### 3.1 典型的输出目录结构

```
/output/wordcount/
├── _SUCCESS                 # 作业成功完成标志文件
├── part-r-00000             # Reducer 0 的输出文件
└── part-r-00001             # Reducer 1 的输出文件
```

### 3.2 文件说明

- **_SUCCESS**: 空文件，表示作业成功完成
- **part-r-xxxxx**: Reducer的输出文件，包含实际的词频统计结果
- 文件数量等于Reducer任务数量（本例中设置为2个）

### 3.3 结果文件内容格式

```
apache  15
big     8
data    12
hadoop  25
mapreduce   18
yarn    10
```

每行格式：`单词<TAB>频次`

## 4. YARN Web UI 监控

### 4.1 访问 YARN Web UI

**地址：** `http://10.132.144.24:8088`

### 4.2 查看作业详情

1. **应用列表页面**
   - 显示所有已提交的应用
   - 可以看到应用状态、开始时间、持续时间等

2. **应用详情页面**
   - 点击应用ID进入详情页面
   - 查看作业配置、日志、任务详情等

3. **任务监控**
   - Map任务和Reduce任务的执行情况
   - 任务进度、资源使用情况
   - 失败任务的错误信息

### 4.3 关键监控指标

| 指标 | 说明 |
|------|------|
| **Application State** | 应用状态（RUNNING/FINISHED/FAILED） |
| **Progress** | 作业完成进度百分比 |
| **Elapsed Time** | 作业运行时间 |
| **Map Progress** | Map任务完成进度 |
| **Reduce Progress** | Reduce任务完成进度 |
| **Memory Usage** | 内存使用情况 |
| **CPU Usage** | CPU使用情况 |

## 5. 日志查看和问题排查

### 5.1 查看应用日志

```bash
# 查看应用日志
yarn logs -applicationId application_1234567890123_0001

# 查看特定容器日志
yarn logs -applicationId application_1234567890123_0001 -containerId container_1234567890123_0001_01_000001
```

### 5.2 常见问题排查

1. **作业失败**
   - 检查YARN Web UI中的错误信息
   - 查看应用日志中的异常堆栈
   - 验证输入路径是否存在
   - 检查输出路径是否已存在（需要删除）

2. **性能问题**
   - 检查Map/Reduce任务数量设置
   - 查看资源使用情况
   - 分析数据倾斜问题

3. **结果异常**
   - 验证输入数据格式
   - 检查Mapper和Reducer逻辑
   - 确认输出格式设置

## 6. 结果分析示例

### 6.1 WordCount 结果分析

假设处理的是一个包含技术文档的文本文件，典型的结果可能如下：

```
the     1250
and     890
of      756
to      623
a       445
in      398
is      367
for     298
that    276
with    234
```

### 6.2 性能分析

通过统计信息可以分析：
- **处理效率**: Map输出记录数 vs 输入记录数的比例
- **数据压缩**: 输入字节数 vs 输出字节数的比例
- **任务平衡**: 各个Reducer处理的数据量是否均衡

## 7. 最佳实践

### 7.1 结果验证

1. **检查 _SUCCESS 文件**：确保作业完全成功
2. **验证输出文件数量**：应该等于Reducer数量
3. **抽样检查结果**：验证词频统计的正确性
4. **对比预期结果**：与已知的小数据集结果对比

### 7.2 性能优化建议

1. **合理设置Reducer数量**：避免产生过多小文件
2. **监控资源使用**：确保集群资源得到充分利用
3. **数据本地化**：尽量让计算靠近数据存储
4. **使用Combiner**：减少网络传输数据量

### 7.3 生产环境注意事项

1. **结果备份**：重要结果应该备份到多个位置
2. **清理临时数据**：及时清理不需要的中间结果
3. **监控磁盘空间**：确保有足够空间存储结果
4. **权限管理**：设置合适的文件访问权限

---

通过以上方法，您可以全面地查看和分析 MapReduce 作业的执行结果，确保作业正确完成并获得预期的输出。