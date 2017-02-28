package cn.daxi.jira.JiraTools.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

public class Base64Utils {
	public static String base64Encode(String str) throws Exception {
		if (StringUtils.isEmpty(str)) {
			return "";
		}
		byte[] b = str.getBytes("utf-8");   
        String s = "";  
        if (b != null) {  
            s = Base64.encodeBase64String(b);  
        }
        return s; 
	}
	
	public static String base64Decode(String str) throws Exception {
		if (StringUtils.isEmpty(str)) {
			return "";
		}
        byte[] b = Base64.decodeBase64(str);  
        return new String(b, "utf-8");  
	}
	
	public static String base64EncodeFile(String filePath) throws Exception {
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new Exception("文件不存在");
		}
		FileInputStream inputFile = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];
		inputFile.read(buffer);
		inputFile.close();
		return Base64.encodeBase64String(buffer);
		
	}
	
	public static void base64DecodeFile(String str, String filePath) throws Exception {
		byte[] buffer = Base64.decodeBase64(str);
		FileOutputStream out = new FileOutputStream(filePath);
		out.write(buffer);
		out.close();
	}
}
