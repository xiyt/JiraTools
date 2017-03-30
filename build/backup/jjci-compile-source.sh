#!/bin/bash
# Jenkins中项目的workspace路径：请修改
readonly project_workspace="${JENKINS_HOME}/workspace/new-blackcat"
# JiraTools的目录：请修改，默认放到项目根目录
readonly jiratools_path="${project_workspace}/JiraTools"

echo "-----开始编译代码-----"
mvn  -Dmaven.test.skip=true -f ${project_workspace}/pom.xml clean compile

# 将代码清单写入待发布清单
cat ${jiratools_path}/deploy/code-list-execute.txt >> ${jiratools_path}/deploy/code-list-deploy.txt
: > ${jiratools_path}/deploy/code-list-execute.txt
echo "-----代码编译完成！-----"
exit 0