#!/bin/bash
# Jenkins中项目的workspace路径
readonly project_workspace="${JENKINS_HOME}/workspace/tianjin"
readonly jiratools_path="${project_workspace}/JiraTools"
# 每个项目目录下都需要创建一个deploy-log目录，记录该项目的发布信息
readonly deploy_info_file="${jiratools_path}/deploy/deploy.info"
readonly code_list_file_path="${jiratools_path}/deploy/deploy-logs"

echo "-----project workspace path is ${project_workspace}"
echo "-----jira tools path is ${jiratools_path}"

# 获取当前时间
current_date=$(date "+%Y%m%d%H%M")
echo "-----current date is ${current_date}"
# 从文件获取上次发布时间
last_deploy_date=$(tail -1 $deploy_info_file)
if [ "$last_deploy_date" = "" ]
	then last_deploy_date="198501290000"
fi
echo "-----last deploy date is ${last_deploy_date}"

# 调用Java将从上次发布时间开始到现在，
# jira状态从from变为to的单子关联的代码保持成代码清单文件
echo "-----the jira issue key is ${JIRA_ISSUE_KEY}"
result=$(java -jar ${jiratools_path}/JiraTools.jar "$JIRA_ISSUE_KEY" $current_date $last_deploy_date)
echo "-----the result of the JiraTools is ${result}"
if [ "$result" = "success" ]
	then
		code_list_file="${code_list_file_path}/code-list-${current_date}-${last_deploy_date}.txt"
		sql_file_list_file="${code_list_file_path}/sql-file-list-${current_date}-${last_deploy_date}.txt"
	# 更新代码清单
	echo "-----updating the code list..."
	for line in $(cat $code_list_file)
	do
		svn update ${project_workspace}/$line
	done

	# 更新sql文件清单
	echo "-----updating the sql file list..."
	for line in $(cat $sql_file_list_file)
	do
		svn update ${project_workspace}/$line
	done

	# 执行sql文件清单
	echo "-----execute the sql file..."
	for line in $(cat $sql_file_list_file)
	do
		mysql --defaults-file=/app/mgr/mysql/mysql.cnf -ujenkins -pjenkins123 < "${project_workspace}/${line}"
		if [ $? != 0 ]
		then 
			exit 1
		fi
	done

	# 复制本次发布的代码和脚本清单文件，用作邮件通知的附件
	echo "-----create the file for mail notification"
	yes | cp -rf "${code_list_file_path}/code-list-${current_date}-${last_deploy_date}.txt" "${code_list_file_path}/code-list-current.txt"
	yes | cp -rf "${code_list_file_path}/sql-file-list-${current_date}-${last_deploy_date}.txt" "${code_list_file_path}/sql-file-list-current.txt"

	# 更新最后发布时间
	echo $current_date >> $deploy_info_file 

	# 执行Jmeter测试
	echo "-----execute the test case"
	ant -buildfile ${jiratools_path}/deploy/test-build.xml -Dbuild.number=${BUILD_NUMBER}
	#cp /app/mgr/.jenkins/workspace/JmeterResult/html/TestReport-${BUILD_NUMBER}.html /app/mgr/.jenkins/jobs/tianjin/htmlreports/Jmeter_Test_Report
	echo "<a href=\"TestReport-${BUILD_NUMBER}.html\">TestReport-${BUILD_NUMBER}.html</a></br>" >> ${jiratools_path}/deploy/test-result/html/index.html
else
	echo $result
fi
exit 0