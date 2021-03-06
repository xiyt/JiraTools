package cn.daxi.jira.JiraTools.service;

import cn.daxi.jira.JiraTools.bean.HttpClientReturn;
import cn.daxi.jira.JiraTools.common.Const;
import cn.daxi.jira.JiraTools.utils.HttpClientUtils;
import cn.daxi.jira.JiraTools.utils.PropertiesUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * 根据JQL语句查询jira问题
     * @param jql
     * @return
     */
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
			String issueTypes = PropertiesUtils.get("issue_types_for_code_list");
			
			// 查询待发布的Jira对应的Key
			String jql = "project=''{0}'' and type in ({1}) and status=''{2}'' and status changed from ''{3}'' to ''{4}'' after ''{5}''&fields=id,key";
			jql = MessageFormat.format(jql, projectName, issueTypes, statusTo, statusFrom, statusTo, lastTime);
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

    /**
     * 根据Jira key从jira任务备注中获取清单列表
     * @return
     */
//	public String[] queryCodeListFromCommentByKeys(String[] jiraKeys) {
//        String[] codeList = new String[]{};
//        try {
//            for (String key : jiraKeys) {
//                String url = PropertiesUtils.get("jira_rest_base_url") + "/issue/"+key+"/comment";
//
//                Map<String, String> headers = new HashMap<String, String>();
//                headers.put("Authorization", Const.JIRA_BASIC_AUTH);
//                headers.put("Content-Type", "application/json");
//                HttpClientReturn result = HttpClientUtils.executeHttpRequest("GET", url, headers, null, null, null);
//                JSONObject jsObj = JSON.parseObject(result.getContent());
//
//                if (jsObj != null && jsObj.containsKey("comments")) {
//                    JSONArray commentArr = jsObj.getJSONArray("comments");
//                    for (int i = 0; i < commentArr.size(); i++) {
//                        JSONObject comment = commentArr.getJSONObject(i);
//                        String body = comment.getString("body");
//                        // 判断是否为代码清单备注
//                        if (StringUtils.isNotEmpty(body) && body.startsWith(PropertiesUtils.get("jira_comment_code_list_prefix"))) {
//                            String[] bodyLineArr = body.split("\\r\\n");
//                            codeList = ArrayUtils.addAll(codeList, ArrayUtils.subarray(bodyLineArr, 1, bodyLineArr.length));
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return codeList;
//    }

    /**
     * 从通过查询特定jira获取到描述信息中的jira key，对应配置文件中的jira_key_for_specific_deploy
     * @return
     */
    public String[] queryIssueKeyForSpecificDeploy() {
        String[] jiraKeys = new String[]{};

        String optJiraKey = PropertiesUtils.get("jira_key_for_specific_deploy");
        if (StringUtils.isNotEmpty(optJiraKey)) {
            try {
                String url = PropertiesUtils.get("jira_rest_base_url") + "/issue/"+optJiraKey+"?fields=description";

                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", Const.JIRA_BASIC_AUTH);
                headers.put("Content-Type", "application/json");
                HttpClientReturn getResult = HttpClientUtils.executeHttpRequest("GET", url, headers, null, null, null);
                JSONObject jsObj = JSON.parseObject(getResult.getContent());

                if (jsObj != null && jsObj.containsKey("fields")) {
                    JSONObject fieldObj = jsObj.getJSONObject("fields");
                    // 从描述信息中获取Jira key
                    String description = fieldObj.getString("description");
                    if (StringUtils.isNotEmpty(description)) {
                        jiraKeys = description.split(",");

                        // 添加备注
                        String commentUrl = PropertiesUtils.get("jira_rest_base_url") + "/issue/" + optJiraKey + "/comment";
                        JSONObject comment = new JSONObject();
                        comment.put("body", "Jenkins has received the keys: " + description);
                        HttpClientReturn addCommentResult = HttpClientUtils.executeHttpRequest("POST", commentUrl, headers, null, comment.toJSONString(),null);
                        if (addCommentResult.getStatusCode() == 201) {// 添加备注成功

                            // 更新描述信息为空
                            String descriptionUrl = PropertiesUtils.get("jira_rest_base_url") + "/issue/" + optJiraKey;
                            JSONObject fields = new JSONObject();
                            fields.put("description", "");
                            JSONObject update = new JSONObject();
                            update.put("fields", fields);
                            HttpClientReturn updateResult = HttpClientUtils.executeHttpRequest("PUT", descriptionUrl, headers, null, update.toJSONString(), null);

                            if (updateResult.getStatusCode() != 200 && updateResult.getStatusCode() != 204) {// 修改描述信息失败
                                System.out.println(MessageFormat.format(Const.MSG_ERROR, "update the description for jira " + optJiraKey));
                            }
                        } else {
                            System.out.println(MessageFormat.format(Const.MSG_ERROR, "add the comment for jira " + optJiraKey));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jiraKeys;
    }

    /**
     * 根据Jira key从jira任务的代码列表字段中获取清单列表
     * @return
     */
    public String[] queryCodeListFromCustomFieldByKeys(String[] jiraKeys) {
        String[] codeList = new String[]{};
        // 自定义代码列表字段的字段ID
        String codeListField = PropertiesUtils.get("code_list_field");
        try {
            for (String key : jiraKeys) {
                String url = PropertiesUtils.get("jira_rest_base_url") + "/issue/"+key+"?fields=" + codeListField;

                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", Const.JIRA_BASIC_AUTH);
                headers.put("Content-Type", "application/json");
                HttpClientReturn result = HttpClientUtils.executeHttpRequest("GET", url, headers, null, null, null);
                JSONObject jsObj = JSON.parseObject(result.getContent());

                if (jsObj != null && jsObj.containsKey("fields")) {
                    JSONObject fieldObj = jsObj.getJSONObject("fields");
                    // 自定义字段：代码列表
                    String body = fieldObj.getString(codeListField);
                    // 判断是否为代码清单备注
                    if (StringUtils.isNotEmpty(body)) {
                        codeList = ArrayUtils.addAll(codeList, body.split("\\r\\n"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return codeList;
    }
}
