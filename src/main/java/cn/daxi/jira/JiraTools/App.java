package cn.daxi.jira.JiraTools;

import cn.daxi.jira.JiraTools.common.Const;
import cn.daxi.jira.JiraTools.service.FisheyeService;
import cn.daxi.jira.JiraTools.service.JiraService;
import cn.daxi.jira.JiraTools.utils.PropertiesUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

/**
 * JiraTools，Jira+Jenkins+Fisheye集成工具
 * 当jira_key不为空时，发布指定jira任务
 * 当jira_key为空时，发布从上次发布时间到当前时间期间的jira任务
 * arg0: jira_key，多个用逗号分隔
 * agr1:last_deploy_date,yyyyMMddHHmm
 */
public class App {
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println(MessageFormat.format(Const.MSG_ERROR, "the count of the args is wrong!"));
			return;
		}
		String jiraKeys = args[0];
		// 上次发布时间
		String lastDeployDate = args[1];

		JiraService js = new JiraService(PropertiesUtils.get("jira_poject_key"));
		FisheyeService fs = new FisheyeService(PropertiesUtils.get("fisheye_repo_name"));

		// 根据状态查询Jira keys
		String[] jiraKeyArr = null;
		if (StringUtils.isNotEmpty(jiraKeys)) {
            jiraKeyArr = jiraKeys.split(",");
		} else {
		    // 查询需要发布的Jira key
            jiraKeyArr = js.queryIssueKeyForNextDeploy(lastDeployDate, PropertiesUtils.get("jira_status_from"), PropertiesUtils.get("jira_status_to"));
		}
		// 根据Jira keys获取关联的代码清单
        String[] codeList = new String[]{};

		// Fisheye方式方式获取代码清单
		if ("0".equals(PropertiesUtils.get("code_list_mode")) || "1".equals(PropertiesUtils.get("code_list_mode"))) {
		    String[] codeListByFisheye = fs.queryCodeListByJiraKeys(jiraKeyArr);
            codeList = ArrayUtils.addAll(codeList, codeListByFisheye);
        }

        // 代码列表字段方式获取代码清单
        if ("0".equals(PropertiesUtils.get("code_list_mode")) || "2".equals(PropertiesUtils.get("code_list_mode"))) {
            String[] codeListByComment = js.queryCodeListFromCustomFieldByKeys(jiraKeyArr);
            codeList = ArrayUtils.addAll(codeList, codeListByComment);
        }
		String result = fs.saveCodeListToFile(codeList);
		System.out.println(result);
	}
}