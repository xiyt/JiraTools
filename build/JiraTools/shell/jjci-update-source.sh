#!/bin/bash
# Jenkins中项目的workspace路径：请修改
readonly project_workspace="${JENKINS_HOME}/workspace/new-blackcat"
# JiraTools的目录：请修改，默认放到项目根目录
readonly jiratools_path="${project_workspace}/JiraTools"


echo "-----项目工作目录：${project_workspace}-----"
echo "-----JiraTools安装目录：${jiratools_path}-----"

# 获取当前时间
current_date=$(date "+%Y%m%d%H%M")
echo "-----当前执行时间：${current_date}-----"

# 从文件获取上次代码的更新时间
last_update_date=$(tail -1 ${jiratools_path}/deploy/deploy.info)
if [ "$last_update_date" = "" ]
	then last_update_date="198501290000"
fi
echo "-----上次执行时间：${last_update_date}-----"

echo "-----触发此次构建的Jira Key：${JIRA_ISSUE_KEY}-----"

# 调用Java将从上次发布时间开始到现在，
# jira状态从from变为to的单子关联的代码保持成代码清单文件
result=$(java -jar ${jiratools_path}/JiraTools.jar "$JIRA_ISSUE_KEY" $last_update_date)

echo "-----获取Jira Key和代码清单结果如下-----"
echo $result

if [[ "$result" =~ "success" ]]
	then
		# 该文件中保存了需要更新的代码清单
		code_list_file="${jiratools_path}/deploy/code-list-update.txt"
		# 该文件中保存了需要更新的脚本清单
		sql_file_list_file="${jiratools_path}/deploy/sql-file-list-update.txt"
	# 更新代码清单
	echo "-----开始更新代码清单文件-----"
	for line in $(cat $code_list_file)
	do
		svn update ${project_workspace}/$line
	done
	# 将代码转入待执行/编译清单
	cat $code_list_file >> ${jiratools_path}/deploy/code-list-execute.txt
	: > $code_list_file;


	# 更新sql文件清单
	echo "-----开始更新脚本清单文件-----"
	for line in $(cat $sql_file_list_file)
	do
		svn update ${project_workspace}/$line
	done
	# 将脚本转入待执行/编译清单
	cat $sql_file_list_file >> ${jiratools_path}/deploy/sql-file-list-execute.txt
	: > $sql_file_list_file;

	# 代码的最后更新时间
	if [ "$JIRA_ISSUE_KEY" == "" ]
		then 
			echo "-----写入最后更新时间信息-----"
			echo $current_date >> ${jiratools_path}/deploy/deploy.info 
	fi

	echo "-----资源更新完成！-----"
else
	echo $result
	exit 1
fi
exit 0