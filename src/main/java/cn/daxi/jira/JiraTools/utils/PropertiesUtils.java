package cn.daxi.jira.JiraTools.utils;

import cn.daxi.jira.JiraTools.App;
import cn.daxi.jira.JiraTools.common.Const;

import java.io.FileInputStream;
import java.io.InputStream;
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
                InputStream is = new FileInputStream(propertiesFilePath);
                resource = new PropertyResourceBundle(is);
            } catch (Exception e) {
                System.out.println(MessageFormat.format(Const.MSG_ERROR, "load properties file!"));
            }
        }
    }

    public static String get(String key) {
        return resource.getString(key);
    }
}
