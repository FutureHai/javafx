package com.hai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class：ADSLUtil
 * desc：
 * author: haihui.zhang
 */
public class ADSLUtil {
    private static final String reg = "(127|10|172|192)\\.([0-1][0-9]{0,2}|[2][0-5]{0,2}|[3-9][0-9]{0,1})\\.([0-1][0-9]{0,2}|[2][0-5]{0,2}|[3-9][0-9]{0,1})\\.([0-1][0-9]{0,2}|[2][0-5]{0,2}|[3-9][0-9]{0,1})";

    /**
     * 执行CMD命令,并返回String字符串 
     *
     * @param strCmd
     * @return
     * @throws Exception
     */
    private static String executeCmd(String strCmd) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(strCmd);
        process.waitFor();
        StringBuilder sbCmd = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            sbCmd.append(line).append("\n");
        }
        return sbCmd.toString();
    }

    /**
     * 连接ADSL
     *
     * @param account
     * @param password
     * @return
     * @throws Exception
     */
    static boolean connectAdsl(String account, String password) throws Exception {
        String connectCmd = "rasdial.exe 宽带连接 " + account + " " + password;
        String tempCmd = executeCmd(connectCmd);
        // 判断是否连接成功  
        return tempCmd.indexOf("已连接") > 0;
    }

    /**
     * 断开ADSL
     *
     * @return
     * @throws Exception
     */
    static boolean disconnectAdsl() throws Exception {
        String disconnectCmd = "rasdial.exe 宽带连接 /disconnect";
        String result = executeCmd(disconnectCmd);
        return !result.contains("没有连接");
    }

    /**
     * 检测连接
     *
     * @return
     */
    public static boolean isConnect() throws Exception {
        boolean connect = false;
        String result = executeCmd("ping " + "www.baidu.com");
        if (!result.trim().equals("")) {
            connect = result.indexOf("TTL") > 0;
        }
        return connect;
    }

    /**
     * 获取ip地址
     *
     * @return
     * @throws SocketException
     */
    static String getIp() throws SocketException {
        Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
        while (b.hasMoreElements()) {
            for (InterfaceAddress f : b.nextElement().getInterfaceAddresses()) {
                InetAddress inetAddress = f.getAddress();
                if (inetAddress instanceof Inet4Address && !isInner(inetAddress.getHostAddress())) {
                    return inetAddress.getHostAddress();
                }
            }
        }
        return null;
    }

    private static boolean isInner(String ip) {
        Pattern p = Pattern.compile(reg);
        Matcher matcher = p.matcher(ip);
        return matcher.find();
    }
}
