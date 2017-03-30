package cn.daxi.jira.JiraTools.common;

import cn.daxi.jira.JiraTools.App;
import cn.daxi.jira.JiraTools.utils.PropertiesUtils;

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

	// 优先级：DEPLOY_MODE_ARGS > DEPLOY_MODE_SPECIFIC > DEPLOY_MODE_AUTO > DEPLOY_MODE_ALL
    // 发布模式：全量
    public static String DEPLOY_MODE_FULL = "full";
    // 发布模式：增量
    public static String DEPLOY_MODE_INCREMENT = "increment";

	static {
		try {
            JIRA_BASIC_AUTH += PropertiesUtils.get("jira_auth");
			FISHEYE_BASIC_AUTH += PropertiesUtils.get("fisheye_auth");

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
