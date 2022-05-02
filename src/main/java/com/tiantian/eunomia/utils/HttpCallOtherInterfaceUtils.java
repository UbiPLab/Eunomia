package com.tiantian.eunomia.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tiantian152
 */
public class HttpCallOtherInterfaceUtils {

    public static String doPost(String method, JSONObject jsonObject, HttpServletRequest request) {
        HttpClient client = HttpClients.createDefault();
        // 将接口地址和接口方法拼接起来
        String url = "http://localhost:8091/" + method;
        System.out.println(url);
        HttpPost post = new HttpPost(url);
        String result = null;
        try {
            StringEntity s = new StringEntity(jsonObject.toString(), "UTF-8");
            s.setContentType("application/json");
            post.setEntity(s);
            post.addHeader("content-type", "text/xml");
            // 调用Fa接口
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = res.getEntity();
                result = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
