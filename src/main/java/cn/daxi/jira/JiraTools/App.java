package cn.daxi.jira.JiraTools;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import cn.daxi.jira.JiraTools.common.Const;
import cn.daxi.jira.JiraTools.service.FisheyeService;
import cn.daxi.jira.JiraTools.service.JiraService;

/**
 * Jira+Jenkins集成工具
 * arg0:jira_project_name
 * agr1:fisheye_repo_name
 * agr2:current_date,yyyyMMddHHmm
 * agr3:last_deploy_date,yyyyMMddHHmm
 * agr4:jira_status_from
 * agr5:jira_status_to
 * arg6:jira_key
 */
public class App {
	public static void main(String[] args) {
		if (args.length < 6) {
			System.out.println(MessageFormat.format(Const.MSG_ERROR, "the count of the args is wrong!"));
			return;
		}
		// jira项目名
		String jiraProjectName = args[0];
		// fisheye仓库名
		String fisheyeRepoName = args[1];
		// bash执行时间
		String currentDate = args[2];
		// 上次发布时间
		String lastDeployDate = args[3];
		// 开始结束的jira状态
		String jiraStatusFrom = args[4];
		String jiraStatusTo = args[5];
		String jiraKey = args[6];
		
		JiraService js = new JiraService(jiraProjectName);
		FisheyeService fs = new FisheyeService(fisheyeRepoName);
		
		// 根据状态查询Jira keys
		String[] jiraKeys = null;
		if (StringUtils.isNotEmpty(jiraKey)) {
			jiraKeys = new String[] { jiraKey };
		} else {
			jiraKeys = js.queryIssueKeyForNextDeploy(lastDeployDate, jiraStatusFrom, jiraStatusTo);
		}
		// 根据Jira keys获取关联的代码清单
		String[] codeList = fs.queryCodeListByJiraKeys(jiraKeys);
		String result = fs.saveCodeListToFile(codeList, currentDate, lastDeployDate);
		System.out.println(result);
	}
}