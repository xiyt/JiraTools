#!/bin/bash
# Jenkins中项目的workspace路径：请修改
readonly project_workspace="${JENKINS_HOME}/workspace/new-blackcat"
# JiraTools的目录：请修改，默认放到项目根目录
readonly jiratools_path="${project_workspace}/JiraTools"

# 执行Jmeter测试
echo "-----开始执行自动化测试-----"
ant -buildfile ${jiratools_path}/deploy/test-build.xml -Dbuild.number=${BUILD_NUMBER}
echo "<a href=\"TestReport-${BUILD_NUMBER}.html\">TestReport-${BUILD_NUMBER}.html</a></br>" >> ${jiratools_path}/deploy/test-result/html/index.html
echo "-----自动化测试完成！-----"
exit 0