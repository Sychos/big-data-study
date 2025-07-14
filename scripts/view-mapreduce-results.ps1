# MapReduce 作业结果查看脚本 (PowerShell版本)
# 用法: .\view-mapreduce-results.ps1 <output_path> [options]
# 示例: .\view-mapreduce-results.ps1 /output/wordcount

param(
    [Parameter(Mandatory=$false, Position=0)]
    [string]$OutputPath,
    
    [Parameter(Mandatory=$false)]
    [switch]$List,
    
    [Parameter(Mandatory=$false)]
    [switch]$Download,
    
    [Parameter(Mandatory=$false)]
    [switch]$Stats,
    
    [Parameter(Mandatory=$false)]
    [switch]$Help
)

# 颜色定义
$Colors = @{
    Red = 'Red'
    Green = 'Green'
    Yellow = 'Yellow'
    Blue = 'Blue'
    Cyan = 'Cyan'
    White = 'White'
}

# 显示帮助信息
function Show-Help {
    Write-Host "MapReduce 作业结果查看脚本" -ForegroundColor $Colors.Blue
    Write-Host ""
    Write-Host "用法: .\view-mapreduce-results.ps1 <output_path> [options]"
    Write-Host ""
    Write-Host "参数:"
    Write-Host "  output_path    HDFS输出路径 (必需)"
    Write-Host ""
    Write-Host "选项:"
    Write-Host "  -List          仅列出输出目录结构"
    Write-Host "  -Download      下载结果到本地"
    Write-Host "  -Stats         显示文件统计信息"
    Write-Host "  -Help          显示此帮助信息"
    Write-Host ""
    Write-Host "示例:"
    Write-Host "  .\view-mapreduce-results.ps1 /output/wordcount                    # 查看结果内容"
    Write-Host "  .\view-mapreduce-results.ps1 /output/wordcount -List              # 仅列出文件"
    Write-Host "  .\view-mapreduce-results.ps1 /output/wordcount -Download          # 下载到本地"
    Write-Host "  .\view-mapreduce-results.ps1 /output/wordcount -Stats             # 显示统计信息"
    Write-Host ""
}

# 检查HDFS路径是否存在
function Test-HDFSPath {
    param([string]$Path)
    
    try {
        $result = & hdfs dfs -test -e $Path 2>$null
        return $LASTEXITCODE -eq 0
    }
    catch {
        return $false
    }
}

# 检查作业是否成功完成
function Test-JobSuccess {
    param([string]$Path)
    
    $successFile = "$Path/_SUCCESS"
    if (Test-HDFSPath $successFile) {
        Write-Host "✓ 作业成功完成" -ForegroundColor $Colors.Green
        return $true
    }
    else {
        Write-Host "⚠ 警告: 未找到 _SUCCESS 文件，作业可能未完全成功" -ForegroundColor $Colors.Yellow
        return $false
    }
}

# 列出目录结构
function Show-DirectoryListing {
    param([string]$Path)
    
    Write-Host "=== 输出目录结构 ===" -ForegroundColor $Colors.Blue
    try {
        & hdfs dfs -ls -h $Path
        Write-Host ""
    }
    catch {
        Write-Host "错误: 无法列出目录 $Path" -ForegroundColor $Colors.Red
    }
}

# 显示文件统计信息
function Show-Statistics {
    param([string]$Path)
    
    Write-Host "=== 文件统计信息 ===" -ForegroundColor $Colors.Blue
    
    try {
        # 统计文件数量
        $fileList = & hdfs dfs -ls $Path 2>$null | Where-Object { $_ -match "^-" }
        $fileCount = ($fileList | Measure-Object).Count
        Write-Host "输出文件数量: $fileCount"
        
        # 统计总大小
        $sizeInfo = & hdfs dfs -du -s -h $Path 2>$null
        if ($sizeInfo) {
            $totalSize = ($sizeInfo -split '\s+')[0]
            Write-Host "总大小: $totalSize"
        }
        
        # 统计记录数（仅对part文件）
        Write-Host ""
        Write-Host "正在统计记录数..." -ForegroundColor $Colors.Yellow
        
        $partFiles = & hdfs dfs -ls "$Path/part-*" 2>$null | Where-Object { $_ -match "part-" }
        $totalRecords = 0
        
        foreach ($line in $partFiles) {
            if ($line -match "(part-\w+-\d+)$") {
                $fileName = $matches[1]
                $filePath = "$Path/$fileName"
                try {
                    $records = (& hdfs dfs -cat $filePath | Measure-Object -Line).Lines
                    Write-Host "$fileName`: $records 条记录"
                    $totalRecords += $records
                }
                catch {
                    Write-Host "无法读取文件: $fileName" -ForegroundColor $Colors.Yellow
                }
            }
        }
        
        Write-Host "总记录数: $totalRecords"
        Write-Host ""
    }
    catch {
        Write-Host "错误: 无法获取统计信息" -ForegroundColor $Colors.Red
    }
}

# 查看结果内容
function Show-Results {
    param([string]$Path)
    
    Write-Host "=== 作业结果内容 ===" -ForegroundColor $Colors.Blue
    
    try {
        # 检查是否有part文件
        $partFiles = & hdfs dfs -ls "$Path/part-*" 2>$null
        if (-not $partFiles) {
            Write-Host "未找到结果文件 (part-*)" -ForegroundColor $Colors.Red
            return
        }
        
        Write-Host "前20行结果:"
        Write-Host "----------------------------------------"
        
        # 显示前20行
        $content = & hdfs dfs -cat "$Path/part-*" 2>$null | Select-Object -First 20
        $content | ForEach-Object { Write-Host $_ }
        
        Write-Host "----------------------------------------"
        
        # 统计总行数
        $totalLines = (& hdfs dfs -cat "$Path/part-*" 2>$null | Measure-Object -Line).Lines
        if ($totalLines -gt 20) {
            Write-Host "注意: 总共有 $totalLines 行结果，上面仅显示前20行" -ForegroundColor $Colors.Yellow
            Write-Host "要查看完整结果，请使用: hdfs dfs -cat $Path/part-*" -ForegroundColor $Colors.Yellow
        }
        Write-Host ""
    }
    catch {
        Write-Host "错误: 无法读取结果文件" -ForegroundColor $Colors.Red
    }
}

# 下载结果到本地
function Download-Results {
    param([string]$Path)
    
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $localDir = ".\mapreduce_results_$timestamp"
    
    Write-Host "=== 下载结果到本地 ===" -ForegroundColor $Colors.Blue
    Write-Host "下载路径: $localDir"
    
    try {
        & hdfs dfs -get $Path $localDir
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ 下载成功" -ForegroundColor $Colors.Green
            Write-Host "本地路径: $(Get-Location)\$localDir"
            
            # 显示本地文件列表
            Write-Host ""
            Write-Host "本地文件列表:"
            Get-ChildItem $localDir | Format-Table Name, Length, LastWriteTime -AutoSize
        }
        else {
            Write-Host "✗ 下载失败" -ForegroundColor $Colors.Red
        }
    }
    catch {
        Write-Host "✗ 下载过程中发生错误: $($_.Exception.Message)" -ForegroundColor $Colors.Red
    }
    Write-Host ""
}

# 显示Web UI链接
function Show-WebLinks {
    param([string]$Path)
    
    Write-Host "=== Web UI 链接 ===" -ForegroundColor $Colors.Blue
    Write-Host "HDFS Web UI: http://10.132.144.24:9870/explorer.html#$Path"
    Write-Host "YARN Web UI: http://10.132.144.24:8088"
    Write-Host ""
}

# 主函数
function Main {
    # 检查是否需要显示帮助
    if ($Help -or [string]::IsNullOrEmpty($OutputPath)) {
        Show-Help
        return
    }
    
    # 检查hdfs命令是否可用
    try {
        & hdfs version >$null 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "hdfs command not found"
        }
    }
    catch {
        Write-Host "错误: hdfs 命令未找到" -ForegroundColor $Colors.Red
        Write-Host "请确保 Hadoop 已正确安装并配置环境变量" -ForegroundColor $Colors.Yellow
        return
    }
    
    Write-Host "MapReduce 结果查看工具" -ForegroundColor $Colors.Green
    Write-Host "输出路径: $OutputPath"
    Write-Host ""
    
    # 检查路径是否存在
    if (-not (Test-HDFSPath $OutputPath)) {
        Write-Host "错误: 路径 '$OutputPath' 不存在" -ForegroundColor $Colors.Red
        Write-Host "提示: 请检查路径是否正确，或者作业是否已完成" -ForegroundColor $Colors.Yellow
        return
    }
    
    # 检查作业是否成功
    Test-JobSuccess $OutputPath
    
    # 根据选项执行不同操作
    if ($List) {
        Show-DirectoryListing $OutputPath
    }
    elseif ($Download) {
        Show-DirectoryListing $OutputPath
        Download-Results $OutputPath
    }
    elseif ($Stats) {
        Show-DirectoryListing $OutputPath
        Show-Statistics $OutputPath
    }
    else {
        # 默认：显示所有信息
        Show-DirectoryListing $OutputPath
        Show-Statistics $OutputPath
        Show-Results $OutputPath
        Show-WebLinks $OutputPath
    }
}

# 执行主函数
Main