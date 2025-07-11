package com.bigdata.mapreduce.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * WordCount Reducer类
 * 负责汇总相同单词的计数
 * 
 * @author BigData Team
 * @version 1.0.0
 */
public class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    
    private static final Logger logger = LoggerFactory.getLogger(WordCountReducer.class);
    
    // 可重用的输出对象
    private IntWritable result = new IntWritable();
    
    /**
     * Reduce方法：汇总相同键的所有值
     * 
     * @param key 输入键（单词）
     * @param values 输入值列表（该单词的所有计数）
     * @param context 上下文对象，用于输出结果
     * @throws IOException IO异常
     * @throws InterruptedException 中断异常
     */
    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        
        int sum = 0;
        
        // 遍历所有值并求和
        for (IntWritable value : values) {
            sum += value.get();
        }
        
        // 设置结果值
        result.set(sum);
        
        // 输出<单词, 总计数>键值对
        context.write(key, result);
        
        // 记录处理结果（仅在调试模式下）
        if (logger.isDebugEnabled()) {
            logger.debug("Reduced word: {} -> count: {}", key.toString(), sum);
        }
        
        // 记录高频词汇
        if (sum >= 100) {
            logger.info("High frequency word found: {} -> count: {}", key.toString(), sum);
        }
    }
    
    /**
     * setup方法：在Reduce任务开始前调用
     * 可以用于初始化资源
     */
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        logger.info("WordCountReducer started for task: {}", context.getTaskAttemptID());
    }
    
    /**
     * cleanup方法：在Reduce任务结束后调用
     * 可以用于清理资源和输出统计信息
     */
    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);
        
        // 获取计数器信息
        long processedWords = context.getCounter("WordCount", "ProcessedWords").getValue();
        logger.info("WordCountReducer completed for task: {}, processed {} unique words", 
                   context.getTaskAttemptID(), processedWords);
    }
}