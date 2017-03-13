package cn.daxi.jira.JiraTools.common;

import cn.daxi.jira.JiraTools.App;
import cn.daxi.jira.JiraTools.utils.Base64Utils;
import cn.daxi.jira.JiraTools.utils.PropertiesUtils;

import java.text.MessageFormat;

public class Const {
	public static int CONNECT_TIMEOUT = 30000;
	// JIRA
	public static String JIRA_BASIC_AUTH = "Basic ";
	// FishEye
	public static String FISHEYE_BASIC_AUTH = "Basic ";

	// Common
	public static String MSG_SUCCESS = "success";
	public static String MSG_ERROR = "error with {0}";
	public static String DEPLOY_LOG_DIR = "/home/deploy/";

	static {
		try {
            String jiraUser = PropertiesUtils.get("jira_user");
            String jiraPassword = PropertiesUtils.get("jira_password");
            JIRA_BASIC_AUTH += Base64Utils.base64Encode(jiraUser + ":" + jiraPassword);

            String fisheyeUser = PropertiesUtils.get("fisheye_user");
            String fisheyePassword = PropertiesUtils.get("fisheye_password");
			FISHEYE_BASIC_AUTH += Base64Utils.base64Encode(fisheyeUser + ":" + fisheyePassword);

            String filePath = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if (filePath.endsWith(".jar")) {// 可执行jar包运行的结果里包含".jar"
                // 截取路径中的jar包名
                filePath = filePath.substring(0, filePath.lastIndexOf("/"));
            }
            DEPLOY_LOG_DIR = filePath + "/deploy/";
		} catch (Exception e) {
            e.printStackTrace();
        }
	}
}
