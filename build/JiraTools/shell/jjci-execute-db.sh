#!/bin/bash
# Jenkins中项目的workspace路径：请修改
readonly project_workspace="${JENKINS_HOME}/workspace/new-blackcat"
# JiraTools的目录：请修改，默认放到项目根目录
readonly jiratools_path="${project_workspace}/JiraTools"

# 该文件中保存了需要执行的sql脚本文件路径
sql_file_list_file="${jiratools_path}/deploy/sql-file-list-update.txt"

echo "-----开始执行脚本文件-----"
for line in $(cat $sql_file_list_file)
do
	mysql --defaults-file=/app/mgr/mysql/mysql.cnf -ujenkins -pjenkins123 < "${project_workspace}/${line}"
	if [ $? != 0 ]
	then 
		exit 1
	fi
done
echo "-----执行脚本文件完成！-----"
exit 0