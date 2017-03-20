package cn.daxi.jira.JiraTools.utils;

import cn.daxi.jira.JiraTools.App;
import cn.daxi.jira.JiraTools.common.Const;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Created by xiyt on 2017/3/11.
 */
public class PropertiesUtils {

    private  static ResourceBundle resource = null;

    static {
        if (null == resource) {
            try {

                String filePath = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                if (filePath.endsWith(".jar")) {// 可执行jar包运行的结果里包含".jar"
                    // 截取路径中的jar包名
                    filePath = filePath.substring(0, filePath.lastIndexOf("/"));
                }
                String propertiesFilePath = filePath + "/JiraTools.properties";
                Reader reader = new InputStreamReader(new FileInputStream(propertiesFilePath), "UTF-8");
                resource = new PropertyResourceBundle(reader);
            } catch (Exception e) {
                System.out.println(MessageFormat.format(Const.MSG_ERROR, "load properties file!"));
            }
        }
    }

    public static String get(String key) {
        return resource.getString(key);
    }
}
