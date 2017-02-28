package cn.daxi.jira.JiraTools.common;

import cn.daxi.jira.JiraTools.utils.Base64Utils;

public class Const {
	public static int CONNECT_TIMEOUT = 30000;
	// JIRA
	public static String JIRA_REST_BASE_URL = "http://10.1.236.88:8080/rest/api/2";
	public static String JIRA_BASIC_AUTH = "Basic ";
	// FishEye
	public static String FISHEYE_REST_BASE_URL = "http://10.1.236.88:8060/rest-service-fe";
	public static String FISHEYE_BASIC_AUTH = "Basic ";
	
	// Common
	public static String MSG_SUCCESS = "success";
	public static String MSG_ERROR = "error with {0}";
	public static String DEPLOY_LOG_DIR = "/app/mgr/.jenkins/workspace/tianjin/deploy/deploy-logs";

	static {
		try {
			JIRA_BASIC_AUTH += Base64Utils.base64Encode("admin:admin123456");
			FISHEYE_BASIC_AUTH += Base64Utils.base64Encode("admin:admin123456");
		} catch (Exception e) {
		}
	}
}
