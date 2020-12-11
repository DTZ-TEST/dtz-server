package com.sy.redpack.textmsg.util;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.StringTokenizer;

public class ParseParams {
    public static void ParseParams(String paramString){
        StringTokenizer token = new StringTokenizer(paramString,"&");
        while(token.hasMoreTokens()){
            String keyValue = token.nextToken();
            String[] keyValues = keyValue.split("=");

        }
    }

}
