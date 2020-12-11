package com.sy.util.weixin;

import com.sy.mainland.util.PropertiesFileLoader;
import com.sy.util.PropUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;

import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyStore;

public final class PayUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayUtil.class);

    public static final String PAY_URL = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
    public static final String QUERY_URL = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo";

    public static String post(String mchid, String url,String postContent) {
    	try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream instream = new FileInputStream(new File(PropertiesFileLoader.getClassPath() + "certs/weixin/apiclient_cert.p12"));
            char[] pw = mchid.toCharArray();
            try {
                keyStore.load(instream, pw);
            } finally {
                instream.close();
            }

            // Trust own CA and all self-signed certs
            SSLContext sslcontext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, pw)
                    .build();
            // Allow TLSv1 protocol only
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[]{"TLSv1"},
                    null,
                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();
            try {

                HttpPost http = new HttpPost(url);
                http.setEntity(EntityBuilder.create().setContentType(ContentType.create("application/x-www-form-urlencoded", Charset.forName("UTF-8"))).setContentEncoding("UTF-8").setText(postContent).build());

                CloseableHttpResponse response = httpclient.execute(http);
                try {
                    HttpEntity entity = response.getEntity();

                    StringBuilder resultBuilder = new StringBuilder();
                    if (entity != null) {
                        BufferedInputStream bis = new BufferedInputStream(entity.getContent(), 1024);

                        byte[] buf = new byte[1024];

                        int length;
                        while ((length = bis.read(buf)) != -1) {
                            resultBuilder.append(new String(buf, 0, length, "UTF-8"));
                        }

                        bis.close();
                    }
                    String result = resultBuilder.toString();

                    LOGGER.info("url:{},content:{},status:{},length={},result={}", url, postContent,response.getStatusLine(), entity == null ? 0 : entity.getContentLength(), result);
                    EntityUtils.consume(entity);

                    return result;
                } catch (Exception e) {
                    LOGGER.error("Exception:" + e.getMessage(), e);
                } finally {
                    response.close();
                }
            } catch (Exception e) {
                LOGGER.error("Exception:" + e.getMessage(), e);
            } finally {
                httpclient.close();
            }
        } catch (Exception e) {
            LOGGER.error("Exception:" + e.getMessage(), e);
        }

        return null;
    }

}
