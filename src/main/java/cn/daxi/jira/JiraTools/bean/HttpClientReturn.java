package cn.daxi.jira.JiraTools.bean;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

public class HttpClientReturn {

	private String status;
	private Header[] requestHeaders;
	private Header[] responseHeaders;
	private String content;

	public HttpClientReturn(){}
	
	public HttpClientReturn(HttpRequestBase request, HttpEntity entity, CloseableHttpResponse response) throws Exception{
		try {
			this.content = EntityUtils.toString(entity);
			this.requestHeaders = request.getAllHeaders();
			this.responseHeaders = response.getAllHeaders();
			this.status = response.getStatusLine().toString();
			EntityUtils.consume(entity);
		} finally {
            response.close();
        }
	}
	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the headers
	 */
	public Header[] getResponseHeaders() {
		return responseHeaders;
	}
	/**
	 * @return the headers
	 */
	public String getResponseHeadersString() {
		StringBuilder sb = new StringBuilder();
		for (Header header : responseHeaders) {
			sb.append(header.getName()+":"+header.getValue()+"\n");
		}
		return sb.toString();
	}
	/**
	 * @return the headers
	 */
	public String getRequestHeadersString() {
		StringBuilder sb = new StringBuilder();
		for (Header header : requestHeaders) {
			sb.append(header.getName()+":"+header.getValue()+"\n");
		}
		return sb.toString();
	}
	
	/**
	 * @param headers the headers to set
	 */
	public void setResponseHeaders(Header[] headers) {
		this.responseHeaders = headers;
	}
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
}
