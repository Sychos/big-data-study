#!/bin/bash

# MapReduce 作业结果查看脚本
# 用法: ./view-mapreduce-results.sh <output_path> [options]
# 示例: ./view-mapreduce-results.sh /output/wordcount

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 显示帮助信息
show_help() {
    echo -e "${BLUE}MapReduce 作业结果查看脚本${NC}"
    echo ""
    echo "用法: $0 <output_path> [options]"
    echo ""
    echo "参数:"
    echo "  output_path    HDFS输出路径 (必需)"
    echo ""
    echo "选项:"
    echo "  -l, --list     仅列出输出目录结构"
    echo "  -d, --download 下载结果到本地"
    echo "  -s, --stats    显示文件统计信息"
    echo "  -h, --help     显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 /output/wordcount                    # 查看结果内容"
    echo "  $0 /output/wordcount -l                 # 仅列出文件"
    echo "  $0 /output/wordcount -d                 # 下载到本地"
    echo "  $0 /output/wordcount -s                 # 显示统计信息"
    echo ""
}

# 检查HDFS路径是否存在
check_path_exists() {
    local path=$1
    if ! hdfs dfs -test -e "$path"; then
        echo -e "${RED}错误: 路径 '$path' 不存在${NC}"
        echo -e "${YELLOW}提示: 请检查路径是否正确，或者作业是否已完成${NC}"
        exit 1
    fi
}

# 检查作业是否成功完成
check_job_success() {
    local path=$1
    if hdfs dfs -test -e "$path/_SUCCESS"; then
        echo -e "${GREEN}✓ 作业成功完成${NC}"
    else
        echo -e "${YELLOW}⚠ 警告: 未找到 _SUCCESS 文件，作业可能未完全成功${NC}"
    fi
}

# 列出目录结构
list_directory() {
    local path=$1
    echo -e "${BLUE}=== 输出目录结构 ===${NC}"
    hdfs dfs -ls -h "$path"
    echo ""
}

# 显示文件统计信息
show_statistics() {
    local path=$1
    echo -e "${BLUE}=== 文件统计信息 ===${NC}"
    
    # 统计文件数量
    local file_count=$(hdfs dfs -ls "$path" | grep "^-" | wc -l)
    echo "输出文件数量: $file_count"
    
    # 统计总大小
    local total_size=$(hdfs dfs -du -s -h "$path" | awk '{print $1}')
    echo "总大小: $total_size"
    
    # 统计记录数（仅对part文件）
    echo -e "\n${YELLOW}正在统计记录数...${NC}"
    local total_records=0
    for file in $(hdfs dfs -ls "$path/part-*" 2>/dev/null | awk '{print $8}'); do
        if [[ -n "$file" ]]; then
            local records=$(hdfs dfs -cat "$file" | wc -l)
            echo "$(basename $file): $records 条记录"
            total_records=$((total_records + records))
        fi
    done
    echo "总记录数: $total_records"
    echo ""
}

# 查看结果内容
view_results() {
    local path=$1
    echo -e "${BLUE}=== 作业结果内容 ===${NC}"
    
    # 检查是否有part文件
    local part_files=$(hdfs dfs -ls "$path/part-*" 2>/dev/null | wc -l)
    if [[ $part_files -eq 0 ]]; then
        echo -e "${RED}未找到结果文件 (part-*)${NC}"
        return 1
    fi
    
    echo "前20行结果:"
    echo "----------------------------------------"
    hdfs dfs -cat "$path/part-*" | head -20
    echo "----------------------------------------"
    
    # 如果结果很多，提示用户
    local total_lines=$(hdfs dfs -cat "$path/part-*" | wc -l)
    if [[ $total_lines -gt 20 ]]; then
        echo -e "${YELLOW}注意: 总共有 $total_lines 行结果，上面仅显示前20行${NC}"
        echo -e "${YELLOW}要查看完整结果，请使用: hdfs dfs -cat $path/part-*${NC}"
    fi
    echo ""
}

# 下载结果到本地
download_results() {
    local path=$1
    local local_dir="./mapreduce_results_$(date +%Y%m%d_%H%M%S)"
    
    echo -e "${BLUE}=== 下载结果到本地 ===${NC}"
    echo "下载路径: $local_dir"
    
    if hdfs dfs -get "$path" "$local_dir"; then
        echo -e "${GREEN}✓ 下载成功${NC}"
        echo "本地路径: $(pwd)/$local_dir"
        
        # 显示本地文件列表
        echo -e "\n本地文件列表:"
        ls -lh "$local_dir"
    else
        echo -e "${RED}✗ 下载失败${NC}"
        return 1
    fi
    echo ""
}

# 显示Web UI链接
show_web_links() {
    local path=$1
    echo -e "${BLUE}=== Web UI 链接 ===${NC}"
    echo "HDFS Web UI: http://10.132.144.24:9870/explorer.html#$path"
    echo "YARN Web UI: http://10.132.144.24:8088"
    echo ""
}

# 主函数
main() {
    # 检查参数
    if [[ $# -eq 0 ]] || [[ "$1" == "-h" ]] || [[ "$1" == "--help" ]]; then
        show_help
        exit 0
    fi
    
    local output_path=$1
    local option=${2:-""}
    
    # 检查hdfs命令是否可用
    if ! command -v hdfs &> /dev/null; then
        echo -e "${RED}错误: hdfs 命令未找到${NC}"
        echo -e "${YELLOW}请确保 Hadoop 已正确安装并配置环境变量${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}MapReduce 结果查看工具${NC}"
    echo "输出路径: $output_path"
    echo ""
    
    # 检查路径是否存在
    check_path_exists "$output_path"
    
    # 检查作业是否成功
    check_job_success "$output_path"
    
    # 根据选项执行不同操作
    case "$option" in
        "-l"|"--list")
            list_directory "$output_path"
            ;;
        "-d"|"--download")
            list_directory "$output_path"
            download_results "$output_path"
            ;;
        "-s"|"--stats")
            list_directory "$output_path"
            show_statistics "$output_path"
            ;;
        "")
            # 默认：显示所有信息
            list_directory "$output_path"
            show_statistics "$output_path"
            view_results "$output_path"
            show_web_links "$output_path"
            ;;
        *)
            echo -e "${RED}错误: 未知选项 '$option'${NC}"
            echo "使用 -h 或 --help 查看帮助信息"
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"