package cn.daxi.jira.JiraTools.service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.daxi.jira.JiraTools.bean.HttpClientReturn;
import cn.daxi.jira.JiraTools.common.Const;
import cn.daxi.jira.JiraTools.utils.FileUtils;
import cn.daxi.jira.JiraTools.utils.HttpClientUtils;

/**
 * 
 * @author xiyt
 * https://docs.atlassian.com/fisheye-crucible/latest/wadl/fisheye.html
 * https://confluence.atlassian.com/display/FISHEYE038/EyeQL+reference+guide
 */
public class FisheyeService {
	private String fisheyeRepoName;
	
	public FisheyeService(String fisheyeRepoName) {
		this.fisheyeRepoName = fisheyeRepoName;
	} 
	
	/**
	 * 查询Fisheye
	 * @param eyeql
	 * @return
	 */
	public JSONObject queryFisheye(String eyeql) {
		JSONObject jsObj = new JSONObject();
		try {
			String url = Const.FISHEYE_REST_BASE_URL + "/search-v1/query/" + fisheyeRepoName + "?query=" + eyeql;
			
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", Const.FISHEYE_BASIC_AUTH);
			headers.put("Content-Type", "application/json");
			headers.put("Accept", "application/json");
			HttpClientReturn result = HttpClientUtils.executeHttpRequest("GET", url, headers, null, null, null);
			jsObj = JSON.parseObject(result.getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsObj;
	}
	
	/**
	 * 查询JIra管理的代码清单
	 * @param jiraKeys
	 * @return
	 */
	public String[] queryCodeListByJiraKeys(String[] jiraKeys) {
		if (jiraKeys.length == 0) {
			System.out.println(MessageFormat.format(Const.MSG_ERROR, "not found the jira keys!"));
			return new String[]{};
		} else {
			StringBuilder sb = new StringBuilder("select revisions where");
			for (String key : jiraKeys) {
				sb.append(" comment matches " + key + " or");
			}
			sb.replace(sb.length() - 3, sb.length(), "");
			sb.append(" group by changeset");
			
			JSONObject jsObj = queryFisheye(sb.toString().replace(" ", "+"));
			JSONArray fileRevisionKey = jsObj.getJSONArray("fileRevisionKey");
			
			String[] codeList = new String[fileRevisionKey.size()];
			for (int i = 0; i < fileRevisionKey.size(); i++) {
				codeList[i] = fileRevisionKey.getJSONObject(i).getString("path");
			}
			return codeList;
		}
	}
	
	/**
	 * 保存文件列表到文件
	 * @param codeList
	 * @return
	 */
	public String saveCodeListToFile(String[] codeList, String currentDate, String lastDeployDate) {
		if (codeList == null || codeList.length == 0) {
			return MessageFormat.format(Const.MSG_ERROR, "the code list is empty!");
		} else {
			try {
				// 代码清单文件
				String codeListFileName = Const.DEPLOY_LOG_DIR + "/code-list-" + currentDate + "-" + lastDeployDate + ".txt";
				// sql文件清单文件
				String sqlListFileName = Const.DEPLOY_LOG_DIR + "/sql-file-list-" + currentDate + "-" + lastDeployDate + ".txt";
				for (String string : codeList) {
					String fileExt = string.substring(string.lastIndexOf(".") + 1);
					if (StringUtils.isNotEmpty(fileExt) && fileExt.toLowerCase().equals("sql")) {
						FileUtils.writeToFile(sqlListFileName, string+"\n", true);
					} else {
						FileUtils.writeToFile(codeListFileName, string+"\n", true);
					}
				}
				
			} catch (Exception e) {
				return MessageFormat.format(Const.MSG_ERROR, e.getMessage());
			}
			return Const.MSG_SUCCESS;
		}
	}
}
