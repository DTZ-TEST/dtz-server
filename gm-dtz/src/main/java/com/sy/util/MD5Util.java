package com.sy.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 采用MD5加密
 * 
 * @author relax
 */
public class MD5Util {

	public static String md5U(String inStr) {
		return md5Encode(inStr).toUpperCase();
	}

	/***
	 * MD5加密 生成32位md5码
	 * 
	 * @param 待加密字符串
	 * @return 返回32位md5码
	 */
	public static String md5Encode(String inStr) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		}

		byte[] byteArray;
		StringBuffer hexValue = new StringBuffer();
		try {
			byteArray = inStr.getBytes("UTF-8");
			byte[] md5Bytes = md5.digest(byteArray);

			for (int i = 0; i < md5Bytes.length; i++) {
				int val = ((int) md5Bytes[i]) & 0xff;
				if (val < 16) {
					hexValue.append("0");
				}
				hexValue.append(Integer.toHexString(val));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return hexValue.toString();
	}

	public static String md5For16U(String inStr) {
		return md5For16(inStr).toUpperCase();
	}

	public static String md5For16(String inStr) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		}

		byte[] byteArray;
		StringBuffer hexValue = new StringBuffer();
		try {
			byteArray = inStr.getBytes("UTF-8");
			byte[] md5Bytes = md5.digest(byteArray);

			for (int i = 0; i < md5Bytes.length; i++) {
				int val = ((int) md5Bytes[i]) & 0xff;
				if (val < 16) {
					hexValue.append("0");
				}
				hexValue.append(Integer.toHexString(val));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return hexValue.toString().substring(8, 24);
	}

    private static ThreadLocal<MessageDigest> threadLocal = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            MessageDigest messagedigest = null;
            try {
                messagedigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return messagedigest;
        }
    };

    public static String getStringMD5(String str) {
        byte[] buffer;
        try {
            buffer = str.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
        MessageDigest messagedigest = threadLocal.get();
        messagedigest.update(buffer);

        return byte2HexStr(messagedigest.digest());

    }

    private static String byte2HexStr(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String s = Integer.toHexString(b[i] & 0xFF);
            if (s.length() == 1) {
                sb.append("0");
            }

            sb.append(s.toLowerCase());
        }

        return sb.toString();
    }

	/**
	 * 测试主函数
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		String str = new String("amigoxiexiexingxing");
		System.out.println("原始：" + str);
		System.out.println("MD5后：" + md5Encode(str));
	}
}