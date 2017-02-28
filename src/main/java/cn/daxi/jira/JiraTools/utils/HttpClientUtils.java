package cn.daxi.jira.JiraTools.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import cn.daxi.jira.JiraTools.bean.HttpClientReturn;
import cn.daxi.jira.JiraTools.common.Const;

public class HttpClientUtils {
	/**
	 * 
	 * @param method GET POST PUT DELETE
	 * @param url
	 * @param headers
	 * @param froms
	 * @param content
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public static HttpClientReturn executeHttpRequest(String method, String url, Map<String, String> headers, Map<String, String> froms, String content, String timeout) throws Exception {
		HttpClientReturn hcr = new HttpClientReturn();
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpRequestBase httpRequest = new HttpGet(url);
		
		
		if ("POST".equals(method)) {
			httpRequest = new HttpPost(url);
		} else if ("PUT".equals(method)) {
			httpRequest = new HttpPut(url);
		} else if ("DELETE".equals(method)) {
				httpRequest = new HttpDelete(url);
		}

		// 设置超时
		int defaultTimeout = Const.CONNECT_TIMEOUT;
		if (StringUtils.isNotEmpty(timeout)) {
			defaultTimeout = Integer.valueOf(timeout);
		}
		RequestConfig rc = RequestConfig.custom()
				.setConnectTimeout(defaultTimeout)
				.setConnectionRequestTimeout(defaultTimeout)
				.setSocketTimeout(defaultTimeout).build();
		httpRequest.setConfig(rc);

		// Header
        if (null != headers && !headers.isEmpty()) {
        	Iterator<String> it = headers.keySet().iterator();
        	while (it.hasNext()) {
				String key = (String) it.next();
				httpRequest.addHeader(key, headers.get(key));
			}
        }
        
        // RequestBody
        if ("POST".equals(method) || "PUT".equals(method)) {
        	if (null != froms && !froms.isEmpty()) {
        		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        		Iterator<String> keys = froms.keySet().iterator();
        		while(keys.hasNext()) {
        			String key = keys.next();
        			nvps.add(new BasicNameValuePair(key, froms.get(key)));
        		}
        		((HttpEntityEnclosingRequestBase)httpRequest).setEntity(new UrlEncodedFormEntity(nvps));
        	} else if (StringUtils.isNotEmpty(content)) {
        		StringEntity entity = new StringEntity(content, ContentType.create("application/json", Consts.UTF_8));
        		((HttpEntityEnclosingRequestBase)httpRequest).setEntity(entity); 
        	}
        }

        CloseableHttpResponse response = httpclient.execute(httpRequest);
        try {
        	try {
	            HttpEntity entity = response.getEntity();
	            hcr = new HttpClientReturn(httpRequest, entity, response);
        	} finally {
	            response.close();
	        }
	    } finally {
	        httpclient.close();
	    }
        return hcr;
	}
}