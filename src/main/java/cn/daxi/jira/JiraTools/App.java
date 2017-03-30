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

		// 保存需要发布的Jira key
		String[] jiraKeyArr = new String[]{};
        // 根据Jira keys获取关联的代码清单
        String[] codeList = new String[]{};

		if (StringUtils.isNotEmpty(jiraKeys)) {
		    // 1、从命令参数中的key
            jiraKeyArr = jiraKeys.split(",");
            System.out.println("JIRA_KEYS_MODE_ARGS：" + ArrayUtils.toString(jiraKeyArr));
		} else {
		    // 2、jira_key_for_specific_deploy方式
            jiraKeyArr = js.queryIssueKeyForSpecificDeploy();
            if (ArrayUtils.isNotEmpty(jiraKeyArr)) {
                System.out.println("JIRA_KEYS_MODE_SPECIFIC：" + ArrayUtils.toString(jiraKeyArr));
            } else {
                String deployMode = PropertiesUtils.get("deploy_mode");
                if (Const.DEPLOY_MODE_INCREMENT.equals(deployMode)) {
                    // 3、根据状态查询需要发布的Jira key
                    jiraKeyArr = js.queryIssueKeyForNextDeploy(lastDeployDate, PropertiesUtils.get("jira_status_from"), PropertiesUtils.get("jira_status_to"));
                    System.out.println("JIRA_KEYS_MODE_AUTO：" + ArrayUtils.toString(jiraKeyArr));

                } else if (Const.DEPLOY_MODE_FULL.equals(deployMode)) {
                    // 4、全量发布
                    codeList = new String[]{"/"};
                    System.out.println("JIRA_KEYS_MODE_ALL：ALL");
                } else {
                    System.out.println(MessageFormat.format(Const.MSG_ERROR, "deploy mode parameter error"));
                }
            }
		}

        String result = Const.MSG_SUCCESS;
		if (ArrayUtils.isNotEmpty(jiraKeyArr)) {
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
        }

        // 生产代码清单列表
        if (ArrayUtils.isNotEmpty(codeList)) {
            result = fs.saveCodeListToFile(codeList);
        }
		System.out.println(result);
	}
}