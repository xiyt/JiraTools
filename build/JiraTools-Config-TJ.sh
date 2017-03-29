#!/bin/bash
# Jenkins中项目的workspace路径：请修改
readonly PROJECT_WORKSPACE="${JENKINS_HOME}/workspace/activity2.0"
# JiraTools的目录：请修改，默认放到项目根目录
readonly JIRATOOLS_PATH="${PROJECT_WORKSPACE}/JiraTools"
# 是否全量发布
readonly UPDATE_ALL_SOURCE="true"

# tomcat路径
readonly TOMCAT_HOME="/home/jenkins/apache-tomcat-7.0.76"
# war发布目录
readonly DOC_BASE="/home/web/actplat"
# war备份目录
readonly  DOC_BASE_BACKUP="/home/web/actplat-backup"
# 需要发布的war路径
readonly DEPLOY_WAR="${PROJECT_WORKSPACE}/activity-platform-new/target/activity-platform-new.war"

# Mysql配置文件路径
readonly MYSQL_CNF_PATH="/app/mgr/mysql/mysql.cnf"
# MYSQL 用户名
readonly MYSQL_USER="jenkins"
# MYSQL 密码
readonly MYSQL_PASSWORD="jenkins123"