#!/bin/bash
# Jenkins中项目的workspace路径：请修改
readonly project_workspace="${JENKINS_HOME}/workspace/new-blackcat"
# JiraTools的目录：请修改，默认放到项目根目录
readonly jiratools_path="${project_workspace}/JiraTools"
# 该shell执行时所在的

# 停止Tomcat
bash ${JENKINS_HOME}/workspace/apache-tomcat-7.0.73/bin/shutdown.sh

# 部署war
rm -rf /app/mgr/.jenkins/workspace/apache-tomcat-7.0.73/project/blackcat-web/*
unzip /app/mgr/.jenkins/workspace/new-blackcat/blackcat-web/target/blackcat-web.war -d /app/mgr/.jenkins/workspace/apache-tomcat-7.0.73/project/blackcat-web
# 重启Tomcat
export BUILD_ID=dontKillMe
bash ${JENKINS_HOME}/workspace/apache-tomcat-7.0.73/bin/startup.sh
exit 0