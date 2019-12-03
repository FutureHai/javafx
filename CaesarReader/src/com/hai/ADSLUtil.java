package com.hai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

/**
 * class：ADSLUtil
 * desc：
 * author: haihui.zhang
 */
public class ADSLUtil {
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
    static String getIp() throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("ipconfig");
        process.waitFor();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        boolean start = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("宽带连接")) {
                start = true;
                continue;
            }
            if (start) {
                if (line.contains("IPv4 地址")) {
                    String[] s = line.split(":");
                    return s[1].trim();
                }
            }
        }

        return null;
    }
}
