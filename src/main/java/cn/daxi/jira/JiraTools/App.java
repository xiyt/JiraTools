package cn.daxi.jira.JiraTools;

import cn.daxi.jira.JiraTools.common.Const;
import cn.daxi.jira.JiraTools.service.FisheyeService;
import cn.daxi.jira.JiraTools.service.JiraService;
import cn.daxi.jira.JiraTools.utils.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

/**
 * JiraTools，Jira+Jenkins+Fisheye集成工具
 * 当jira_key不为空时，发布指定jira任务
 * 当jira_key为空时，发布从上次发布时间到当前时间期间的jira任务
 * arg0: jira_key，多个用逗号分隔
 * agr1:current_date,yyyyMMddHHmm
 * agr2:last_deploy_date,yyyyMMddHHmm
 */
public class App {
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println(MessageFormat.format(Const.MSG_ERROR, "the count of the args is wrong!"));
			return;
		}
		String jiraKeys = args[0];
		// bash执行时间
		String currentDate = args[1];
		// 上次发布时间
		String lastDeployDate = args[2];

		JiraService js = new JiraService(PropertiesUtils.get("jira_roject_key"));
		FisheyeService fs = new FisheyeService(PropertiesUtils.get("fisheye_repo_name"));

		// 根据状态查询Jira keys
		String[] jiraKeyArr = null;
		if (StringUtils.isNotEmpty(jiraKeys)) {
            jiraKeyArr = jiraKeys.split(",");
		} else {
            jiraKeyArr = js.queryIssueKeyForNextDeploy(lastDeployDate, PropertiesUtils.get("jira_status_from"), PropertiesUtils.get("jira_status_to"));
		}
		// 根据Jira keys获取关联的代码清单
		String[] codeList = fs.queryCodeListByJiraKeys(jiraKeyArr);
		String result = fs.saveCodeListToFile(codeList, currentDate, lastDeployDate);
		System.out.println(result);
	}
}