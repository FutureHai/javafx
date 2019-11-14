package com.hai;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.TouchScreen;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Controller implements Initializable {
    private static ChromeDriverService service;
    private static ChromeOptions chromeOptions;
    private static final String rootPath;
    //    private static ExecutorService cachedExecutor;
    private ChromeDriver driver;
    private boolean stopReadFlag = false;
    private boolean readingFlag = false;//正在阅读标识
    private boolean stopPPPOEFlag = false;
    //已完成文章篇数
    private int readNum = 0;
    // 剩余文章篇数
    private int unReadNum = 0;

    private static Integer pauseTimeFrom;
    private static Integer pauseTimeTo;
    private static Integer slipTimesFrom;
    private static Integer slipTimesTo;
    private static Integer pxFrom;
    private static Integer pxTo;

    @FXML
    private TextArea logTxt;
    @FXML
    private TextArea linkTxt;
    @FXML
    private TextField startTimeTxt;
    @FXML
    private TextField endTimeTxt;
    @FXML
    private TextField pauseTimeFromTxt;
    @FXML
    private TextField pauseTimeToTxt;
    @FXML
    private TextField slipTimesFromTxt;
    @FXML
    private TextField slipTimesToTxt;
    @FXML
    private TextField pxFromTxt;
    @FXML
    private TextField pxToTxt;
    @FXML
    private TextField chromeLocationTxt;

    @FXML
    private Button startReadBtn;
    @FXML
    private Button stopReadBtn;
    @FXML
    private Button configSaveBtn;
    @FXML
    private Button connectBtn;
    @FXML
    private Button disconnectBtn;
    @FXML
    private TextField accountTxt;
    @FXML
    private TextField passwordTxt;
    @FXML
    private Text statusTxt;
    @FXML
    private Text ipTxt;

    static {
        chromeOptions = new ChromeOptions();
        List<String> arguments = new ArrayList<>();
        arguments.add("--window-size=360,640");
        arguments.add("--disable-gpu");
        arguments.add("--hide-scrollbars");
        arguments.add("--disable-javascript");
        arguments.add("--disable-infobars");
        chromeOptions.addArguments(arguments);

        Map<String, Object> deviceMetrics = new HashMap<>();
        deviceMetrics.put("width", 360);
        deviceMetrics.put("height", 640);
        deviceMetrics.put("pixelRatio", 3.0);

        Map<String, Object> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceMetrics", deviceMetrics);
        mobileEmulation.put("userAgent", "Mozilla/5.0 (Linux; Android 4.2.1; en-us; Nexus 5 Build/JOP40D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");

        chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        chromeOptions.setExperimentalOption("w3c", false);

        rootPath = new File(System.getProperty("user.dir")).getParent();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initConfig();

        initService();
    }

    private void initConfig() {
        List<String> configList = readFromTextFile(rootPath + "\\config.ini");
        if (null == configList || configList.size() == 0) {
            return;
        }

        String[] configs = configList.get(0).split("\\|");

        startTimeTxt.setText(configs[0]);
        endTimeTxt.setText(configs[1]);
        pauseTimeFromTxt.setText(configs[2]);
        pauseTimeToTxt.setText(configs[3]);
        slipTimesFromTxt.setText(configs[4]);
        slipTimesToTxt.setText(configs[5]);
        pxFromTxt.setText(configs[6]);
        pxToTxt.setText(configs[7]);
        chromeLocationTxt.setText(configs[8]);

        String s = configs[8].replace("\\", "\\\\");
        chromeOptions.setBinary(s);

        pauseTimeFrom = Integer.valueOf(configs[2]);
        pauseTimeTo = Integer.valueOf(configs[3]);
        slipTimesFrom = Integer.valueOf(configs[4]);
        slipTimesTo = Integer.valueOf(configs[5]);
        pxFrom = Integer.valueOf(configs[6]);
        pxTo = Integer.valueOf(configs[7]);
    }

    /**
     * 初始化全局配置
     */
    private void initService() {
        try {
            service = new ChromeDriverService.Builder()
                    .usingDriverExecutable(new File(rootPath + "\\chromedriver.exe"))
                    .usingAnyFreePort()
                    .build();
            service.start();
        } catch (Exception e) {
            appendFile(rootPath + "\\error.log", e.getMessage());
        }
    }

    /**
     * 保存配置按钮点击事件
     *
     * @param event
     */
    public void configSaveButtonPress(ActionEvent event) {
        String startTimeString = startTimeTxt.getText().trim();
        String endTimeString = endTimeTxt.getText().trim();
        String pauseTimeFromString = pauseTimeFromTxt.getText().trim();
        String pauseTimeToString = pauseTimeToTxt.getText().trim();
        String slipTimesFromString = slipTimesFromTxt.getText().trim();
        String slipTimesToString = slipTimesToTxt.getText().trim();
        String pxFromString = pxFromTxt.getText().trim();
        String pxToString = pxToTxt.getText().trim();
        String chromeLocationString = chromeLocationTxt.getText().trim();
        if (startTimeString.equals("") || endTimeString.equals("")
                || pauseTimeFromString.equals("") || pauseTimeToString.equals("")
                || slipTimesFromString.equals("") || slipTimesToString.equals("")
                || pxFromString.equals("") || pxToString.equals("")
                || chromeLocationString.equals("")) {
            return;
        }

        String configs = startTimeString + "|" + endTimeString + "|" + pauseTimeFromString + "|" + pauseTimeToString + "|" + slipTimesFromString + "|" + slipTimesToString + "|" + pxFromString + "|" + pxToString + "|" + chromeLocationString;
        writeTextFile(rootPath + "\\config.ini", configs);
        pauseTimeFrom = Integer.valueOf(pauseTimeFromString);
        pauseTimeTo = Integer.valueOf(pauseTimeToString);
        slipTimesFrom = Integer.valueOf(slipTimesFromString);
        slipTimesTo = Integer.valueOf(slipTimesToString);
        pxFrom = Integer.valueOf(pxFromString);
        pxTo = Integer.valueOf(pxToString);
    }

    /**
     * 开始阅读按钮点击事件
     *
     * @param event
     */
    public void startReadButtonPress(ActionEvent event) {
        if (readingFlag || stopPPPOEFlag) {
            return;
        }
        stopReadFlag = false;
        readingFlag = true;
        CompletableFuture.runAsync(() -> read(readAndDeleteFirstLine(rootPath + "\\unread.txt")));
    }

    /**
     * 停止阅读按钮点击事件
     *
     * @param event
     */
    public void stopReadButtonPress(ActionEvent event) {
        stopReadFlag = true;
        readingFlag = false;
    }

    /**
     * 开始拨号按钮点击事件
     *
     * @param event
     */
    public void connectButtonPress(ActionEvent event) {
        CompletableFuture.runAsync(() -> connect(accountTxt.getText().trim(), passwordTxt.getText().trim()));
    }

    /**
     * 断开连接按钮点击事件
     *
     * @param event
     */
    public void disconnectButtonPress(ActionEvent event) {
        try {
            printLog("正在断开连接");
            ADSLUtil.disconnectAdsl();
            printLog("连接已断开");
        } catch (Exception e) {
            printLog("断开连接失败!");
            appendFile(rootPath + "\\error.log", e.getMessage());
            return;
        }

        stopPPPOEFlag = true;
    }

    /**
     * 拨号
     */
    private void connect(String account, String password) {
        if ("".equals(account)) {
            printLog("账号不能为空");
            return;
        }
        if ("".equals(password)) {
            printLog("密码不能为空");
            return;
        }
        try {
            printLog("重新断开连接");
            ADSLUtil.disconnectAdsl();
            printLog("连接已断开");
        } catch (Exception e) {
            printLog("断开连接失败!" + e.getMessage());
            appendFile(rootPath + "\\error.log", e.getMessage());
            return;
        }

        try {
            printLog("3秒后开始拨号...");
            Thread.sleep(3000);
            printLog("开始拨号");
            statusTxt.setText("正在拨号");
            boolean status = ADSLUtil.connectAdsl(account, password);
            if (status) {
                printLog("拨号成功3秒后开始阅读");
                statusTxt.setText("拨号成功");
                stopPPPOEFlag = false;
                try {
                    ipTxt.setText(ADSLUtil.getIp());
                } catch (Exception e1) {
                    printLog("获取IP失败");
                    appendFile(rootPath + "\\error.log", e1.getMessage());
                }

                if (!stopReadFlag) {
                    Thread.sleep(3000);
                    //触发开始阅读
                    Event.fireEvent(startReadBtn, new ActionEvent());
                }
            } else {
                statusTxt.setText("拨号失败");
            }
        } catch (Exception e) {
            printLog("拨号异常");
            appendFile(rootPath + "\\error.log", e.getMessage());
        }
    }

    /**
     * 传入URL开始阅读
     *
     * @param url
     */
    private void read(String url) {
        if (null == url || url.trim().equals("")) {
            printLog("已全部阅读完成");
            readingFlag = false;
            return;
        }
        readNum = readNum + 1;
        printLog(String.format("开始阅读第 %s 篇，剩余 %s 篇", readNum, unReadNum));
        printLog(String.format("当前文章：%s", url));

        driver = new ChromeDriver(service, chromeOptions);
        driver.get(url);
        //下滑次数
        int hasNum = 0;
        try {
            int num = getRandomNumberInRange(slipTimesFrom, slipTimesTo);
            for (int i = 1; i <= num; i++) {
                if (stopReadFlag) {
                    break;
                }

                int holdTime = getRandomNumberInRange(pauseTimeFrom, pauseTimeTo);
                int px = getRandomNumberInRange(pxFrom, pxTo);
                printLog(String.format("第 %s 次下滑，等待 %s 秒, 下滑 %s 像素", i, holdTime, px));
                Thread.sleep(holdTime * 1000);
                TouchScreen touchScreen = driver.getTouch();
                touchScreen.scroll(0, px);
                hasNum = i;
            }
        } catch (Exception e) {
            appendFile(rootPath + "\\error.log", e.getMessage());
        }

        if (stopReadFlag) {
            printLog(String.format("第 %s 篇阅读未完成，共下滑 %s 次", readNum, hasNum));
            //将该url重新写入未读文件
            appendFile(rootPath + "\\unread.txt", url);
        } else {
            printLog(String.format("第 %s 篇阅读完成，共下滑 %s 次", readNum, hasNum));
            appendFile(rootPath + "\\read.txt", url);
        }

        if (Objects.nonNull(driver)) {
            driver.quit();
        }

        readingFlag = false;
        //触发重新拨号
        if (!stopReadFlag) {
            Event.fireEvent(connectBtn, new ActionEvent());
        }
    }

    /**
     * 写日志
     *
     * @param message
     */
    private void printLog(String message) {
        logTxt.appendText(message + "\n");
    }

    static ChromeDriverService getService() {
        return service;
    }

    ChromeDriver getDriver() {
        return driver;
    }

    /**
     * 获取随机数
     *
     * @param min
     * @param max
     * @return
     */
    private static int getRandomNumberInRange(int min, int max) {
        return new Random().ints(min, (max + 1)).limit(1).findFirst().getAsInt();
    }

    /**
     * 读取文件
     *
     * @param pathname
     * @return
     * @throws IOException
     */
    private ArrayList<String> readFromTextFile(String pathname) {
        ArrayList<String> strArray = new ArrayList<>();
        try {
            File filename = new File(pathname);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
            BufferedReader br = new BufferedReader(reader);
            String line;
            line = br.readLine();
            while (line != null) {
                strArray.add(line);
                line = br.readLine();
            }
        } catch (Exception e) {
            appendFile(rootPath + "\\error.log", e.getMessage());
        }

        return strArray;
    }

    /**
     * 写文件
     *
     * @param pathname
     * @param content
     */
    private void writeTextFile(String pathname, String content) {
        try {
            FileWriter writer = new FileWriter(pathname);
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            printLog("保存失败!");
            appendFile(rootPath + "\\error.log", e.getMessage());
            return;
        }

        printLog("保存成功!");
    }

    /**
     * 追加文件内容
     *
     * @param pathname
     * @param content
     */
    private void appendFile(String pathname, String content) {
        try {
            FileWriter writer = new FileWriter(pathname, true);
            writer.write(content + "\r\n");
            writer.close();
        } catch (Exception e) {
            printLog("保存失败!");
            appendFile(rootPath + "\\error.log", e.getMessage());
        }
    }

    /**
     * 读取删除第一行
     *
     * @param pathname
     * @return
     */
    private String readAndDeleteFirstLine(String pathname) {
        String firstLine = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(pathname));
            StringBuilder sb = new StringBuilder(4096);
            String temp;
            int line = 0;
            while ((temp = br.readLine()) != null) {
                line++;
                if (line == 1) {
                    firstLine = temp;
                    continue;
                }
                sb.append(temp).append("\r\n");
            }
            br.close();
            BufferedWriter bw = new BufferedWriter(new FileWriter(pathname));
            bw.write(sb.toString());
            bw.close();
            if (line > 0) {
                unReadNum = line - 1;
            }
        } catch (Exception e) {
            printLog("文章读取失败");
            appendFile(rootPath + "\\error.log", e.getMessage());
        }

        return firstLine;
    }
}
