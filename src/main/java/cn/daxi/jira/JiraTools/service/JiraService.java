package cn.daxi.jira.JiraTools.service;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.daxi.jira.JiraTools.utils.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.daxi.jira.JiraTools.bean.HttpClientReturn;
import cn.daxi.jira.JiraTools.common.Const;
import cn.daxi.jira.JiraTools.utils.HttpClientUtils;

/**
 * https://docs.atlassian.com/jira/REST/6.3.6/
 * @author xiyt
 *
 */
public class JiraService {
	
	private String projectName;
	
	public JiraService(String projectName) {
		this.projectName = projectName;
	}
	
	public JSONObject queryIssue(String jql) {
		JSONObject jsObj = new JSONObject();
		try {
			String url = PropertiesUtils.get("jira_rest_base_url") + "/search?jql=" + jql;
			
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", Const.JIRA_BASIC_AUTH);
			headers.put("Content-Type", "application/json");
			HttpClientReturn result = HttpClientUtils.executeHttpRequest("GET", url, headers, null, null, null);
			jsObj = JSON.parseObject(result.getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsObj;
	}
	
	/**
	 * 查询待发布的Jira对应的Key数组
	 * status changed from verify to "Ready for Deploy" AFTER "2016/12/28 18:30"
	 * @lastTime yyyyMMddHHmm
	 * @return
	 */
	public String[] queryIssueKeyForNextDeploy(String lastTime, String statusFrom, String statusTo) {
		String[] keys = new String[]{};
		
		if (StringUtils.isEmpty(lastTime)) {
			System.out.println(MessageFormat.format(Const.MSG_ERROR, "the parameter lastTime is empty!"));
			return keys; 
		}
		
		try {
			// 因为使用after关键字，需要把时间减去1分钟
			Date lastDate = DateUtils.parseDate(lastTime, "yyyyMMddHHmm");
			lastDate = DateUtils.addMinutes(lastDate, -1);
			lastTime = cn.daxi.jira.JiraTools.utils.DateUtils.formatDate(lastDate, "yyyy/MM/dd HH:mm");
			
			// 查询待发布的Jira对应的Key
			String jql = "project=''{0}'' and status changed from ''{1}'' to ''{2}'' after ''{3}''&fields=id,key";
			jql = MessageFormat.format(jql, projectName, statusFrom, statusTo, lastTime);
			jql = jql.replace(" ", "+");
			JSONObject jsObj = queryIssue(jql);
			if (jsObj.containsKey("issues")) {
				JSONArray issues = jsObj.getJSONArray("issues");
				
				keys = new String[issues.size()];
				for (int i = 0; i < issues.size(); i++) {
					keys[i] = issues.getJSONObject(i).getString("key");
				}
			} else {
				System.out.println(MessageFormat.format(Const.MSG_ERROR, jsObj.getJSONArray("errorMessages")));
			}
		} catch (Exception e) {
			System.out.println(MessageFormat.format(Const.MSG_ERROR, e.getMessage()));
		}
		return keys;
	}
}
