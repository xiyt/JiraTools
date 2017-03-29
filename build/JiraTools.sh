#!/bin/bash
# 引入配置文件
source $(cd `dirname $0`; pwd)/JiraTools-Config-BC.sh

# 获取当前时间
current_date=$(date "+%Y%m%d%H%M")
echo "-----current_date：${current_date}-----"

# 更新代码
function updateSource() {
    if [ "${UPDATE_ALL_SOURCE}"x = "true"x ]
    then
        # 全量更新代码
        svn update ${PROJECT_WORKSPACE}
    else
        # 从文件获取上次代码的更新时间
        last_update_date=$(tail -1 ${JIRATOOLS_PATH}/deploy/deploy.info)
        if [ "$last_update_date" = "" ]
            then last_update_date="198501290000"
        fi
        echo "-----last_update_date：${last_update_date}-----"

        echo "-----JIRA_ISSUE_KEY：${JIRA_ISSUE_KEY}-----"

        # 调用Java将从上次发布时间开始到现在，
        # jira状态从from变为to的单子关联的代码保持成代码清单文件
        result=$(java -jar ${JIRATOOLS_PATH}/JiraTools.jar "$JIRA_ISSUE_KEY" $last_update_date)

        echo "-----The jira key and source list is-----"
        echo $result

        # 包含错误直接退出
        if [[ "$result" =~ "error" ]]
            then exit 1
        fi

        # 执行成功
        if [[ "$result" =~ "success" ]]
            then
            # 该文件中保存了需要更新的代码清单
            code_list_file="${JIRATOOLS_PATH}/deploy/code-list-update.txt"
            # 该文件中保存了需要更新的脚本清单
            sql_file_list_file="${JIRATOOLS_PATH}/deploy/sql-file-list-update.txt"
            # 更新代码清单
            echo "-----start to update the code list-----"
            for line in $(cat $code_list_file)
            do
                svn update ${PROJECT_WORKSPACE}/$line
            done
            # 将代码转入待执行/编译清单
            cat $code_list_file >> ${JIRATOOLS_PATH}/deploy/code-list-execute.txt
            : > $code_list_file;

            # 更新sql文件清单
            echo "-----start to update the sql file list-----"
            for line in $(cat $sql_file_list_file)
            do
                svn update ${PROJECT_WORKSPACE}/$line
            done
            # 将脚本转入待执行/编译清单
            cat $sql_file_list_file >> ${JIRATOOLS_PATH}/deploy/sql-file-list-execute.txt
            : > $sql_file_list_file;

            # 代码的最后更新时间
            if [[ "$result" =~ "JIRA_KEYS_MODE_AUTO" ]]
                then
                echo "-----update the last deploy date-----"
                echo $current_date >> ${JIRATOOLS_PATH}/deploy/deploy.info
            fi
            echo "-----update completed！-----"
        else
            echo $result
            exit 1
        fi
    fi
}

# 编译代码
function compileSource() {
    echo "-----start to compile the source-----"
    mvn -Dmaven.test.skip=true -f ${PROJECT_WORKSPACE}/pom.xml clean compile
    if [ $? != 0 ]
    then
        exit 1
    fi

    # 将代码清单写入待发布清单
    cat ${JIRATOOLS_PATH}/deploy/code-list-execute.txt >> ${JIRATOOLS_PATH}/deploy/code-list-deploy.txt
    : > ${JIRATOOLS_PATH}/deploy/code-list-execute.txt
    echo "-----compile completed!-----"
}

# 执行SQL脚本
function executeSql() {
    # 该文件中保存了需要执行的sql脚本文件路径
    sql_file_list_file="${JIRATOOLS_PATH}/deploy/sql-file-list-execute.txt"

    echo "-----start to execute the sql-----"
    for line in $(cat $sql_file_list_file)
    do
        mysql --defaults-file=${MYSQL_CNF_PATH} -u${MYSQL_USER} -p${MYSQL_PASSWORD} < "${PROJECT_WORKSPACE}/${line}"
        if [ $? != 0 ]
        then
            exit 1
        fi
    done

    # 将脚本清单写入待发布清单
    cat ${JIRATOOLS_PATH}/deploy/sql-file-list-execute.txt >> ${JIRATOOLS_PATH}/deploy/sql-file-list-deploy.txt
    : > ${JIRATOOLS_PATH}/deploy/sql-file-list-execute.txt
    echo "-----sql execute completed!-----"
    exit 0
}

# 发布到Tomcat
function deployToTomcat() {
    echo "-----start to install the application-----"
    mvn  -f ${PROJECT_WORKSPACE}/pom.xml -Dmaven.test.skip=true -Ptest clean install
    if [ $? != 0 ]
    then
        exit 1
    fi

    echo "-----start to shutdown the tomcat-----"
    # 停止Tomcat
    export BUILD_ID=dontKillMe
    bash ${TOMCAT_HOME}/bin/shutdown.sh

    echo "-----start to deploy the application-----"

    # 备份当前war
    mv -f ${DOC_BASE}/current.war ${DOC_BASE_BACKUP}/backup-${current_date}.war
    rm -rf ${DOC_BASE}/*

    # 部署新war
    yes | cp -rf ${DEPLOY_WAR} ${DOC_BASE}/current.war
    unzip -o ${DOC_BASE}/current.war -d ${DOC_BASE}

    # 重启Tomcat
    export BUILD_ID=dontKillMe
    bash ${TOMCAT_HOME}/bin/startup.sh
    echo "-----start to restart the tomcat-----"
}

# Jmeter 自动测试
function jmeterTest() {
    echo "-----start to jmeter test-----"
    ant -buildfile ${JIRATOOLS_PATH}/deploy/test-build.xml -Dbuild.number=${BUILD_NUMBER}
    echo "<a href=\"TestReport-${BUILD_NUMBER}.html\">TestReport-${BUILD_NUMBER}.html</a></br>" >> ${JIRATOOLS_PATH}/deploy/test-result/html/index.html
    echo "-----test completed！-----"
    exit 0
}

# 发布后的清理工作
function clean() {
    # 复制代码清单文件到日志目录
    echo "-----start to copy the code list to log directory-----"
    yes | cp -rf "${JIRATOOLS_PATH}/deploy/code-list-deploy.txt" "${JIRATOOLS_PATH}/deploy/deploy-logs/code-list-deploy-${BUILD_NUMBER}.txt"
    : > ${JIRATOOLS_PATH}/deploy/code-list-deploy.txt

    # 复制脚本清单文件到日志目录
    echo "-----start to copy the sql list to log directory-----"
    yes | cp -rf "${JIRATOOLS_PATH}/deploy/sql-file-list-deploy.txt" "${JIRATOOLS_PATH}/deploy/deploy-logs/sql-file-list-deploy-${BUILD_NUMBER}.txt"
    : > ${JIRATOOLS_PATH}/deploy/sql-file-list-deploy.txt

    echo "-----clean completed！-----"
}

if [ "$1"x = "update"x ]
then
    updateSource
    exit 0
elif [ "$1"x = "compile"x ]
then
    compileSource
    exit 0
elif [ "$1"x = "sql"x ]
then
    executeSql
    exit 0
elif [ "$1"x = "deploy"x ]
then
    deployToTomcat
    exit 0
elif [ "$1"x = "jmeter"x ]
then
    jmeterTest
    exit 0
elif [ "$1"x = "clean"x ]
then
    clean
    exit 0
else
    echo "param error"
    exit 1
fi
exit 0
