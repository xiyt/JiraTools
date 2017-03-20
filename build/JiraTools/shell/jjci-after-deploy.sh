#!/bin/bash
# Jenkins中项目的workspace路径：请修改
readonly project_workspace="${JENKINS_HOME}/workspace/new-blackcat"
# JiraTools的目录：请修改，默认放到项目根目录
readonly jiratools_path="${project_workspace}/JiraTools"
# 该shell执行时所在的

# 复制代码清单文件到日志目录
echo "-----复制代码清单文件到日志目录-----"
yes | cp -rf "${jiratools_path}/deploy/code-list-update.txt" "${jiratools_path}/deploy/deploy-logs/code-list-deploy-${BUILD_NUMBER}.txt"
: > ${jiratools_path}/deploy/code-list-update.txt

# 复制脚本清单文件到日志目录
echo "-----复制脚本清单文件到日志目录-----"
yes | cp -rf "${jiratools_path}/deploy/sql-file-list-update.txt" "${jiratools_path}/deploy/deploy-logs/sql-file-list-deploy-${BUILD_NUMBER}.txt"
: > ${jiratools_path}/deploy/sql-file-list-update.txt

echo "-----发布后续工作完成！-----"
exit 0