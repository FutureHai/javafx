package com.hai;

import com.google.common.collect.Lists;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable {
    private static ChromeDriverService service;
    private static Map<String, Object> mobileEmulation;
    private static ChromeOptions chromeOptions;
    private static final String rootPath;
    private static ExecutorService executorService;
    private static final List<String> userAgents;
    private static List<String> urls = Lists.newArrayList();
    private static int totalNum = 0;//总文章篇数
    private ChromeDriver driver;
    private boolean stopReadFlag = false;
    private boolean readingFlag = false;//正在阅读标识
    private boolean stopPPPOEFlag = false;
    //当前文章
    private int currentNum = 0;

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
        userAgents = Lists.newArrayList(
                "Mozilla/5.0 (iPhone; CPU iPhone OS 6_1_3 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Mobile/10B329 MicroMessenger/5.0.1",
                "Mozilla/5.0 (Linux; Android 7.1.1; MI 6 Build/NMF26X; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.132 MQQBrowser/6.2 TBS/043807 Mobile Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Mobile/13F69 MicroMessenger/6.6.1 NetType/4G Language/zh_CN",
                "Mozilla/5.0 (Linux; Android 7.1.1; OD103 Build/NMF26F; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/4G Language/zh_CN",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 11_2_2 like Mac OS X) AppleWebKit/604.4.7 (KHTML, like Gecko) Mobile/15C202 MicroMessenger/6.6.1 NetType/4G Language/zh_CN",
                "Mozilla/5.0 (Linux; Android 6.0.1; SM919 Build/MXB48T; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 11_1_1 like Mac OS X) AppleWebKit/604.3.5 (KHTML, like Gecko) Mobile/15B150 MicroMessenger/6.6.1 NetType/WIFI Language/zh_CN",
                "Mozilla/5.0 (Linux; Android 5.1.1; vivo X6S A Build/LMY47V; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN",
                "Mozilla/5.0 (iphone x Build/MXB48T; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN",
                "Mozilla/5.0 (Linux; Android 5.1; HUAWEI TAG-AL00 Build/HUAWEITAG-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043622 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/4G Language/zh_CN"
        );
        chromeOptions = new ChromeOptions();
        List<String> arguments = new ArrayList<>();
        arguments.add("--window-size=360,640");
        arguments.add("--disable-gpu");
        arguments.add("--hide-scrollbars");
        arguments.add("--disable-javascript");
        arguments.add("--disable-infobars");
        arguments.add("--no-default-browser-check");
        chromeOptions.addArguments(arguments);

        Map<String, Object> deviceMetrics = new HashMap<>();
        deviceMetrics.put("width", 360);
        deviceMetrics.put("height", 640);
        deviceMetrics.put("pixelRatio", 3.0);
        mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceMetrics", deviceMetrics);

        chromeOptions.setExperimentalOption("w3c", false);

        rootPath = new File(System.getProperty("user.dir")).getParent();

        executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initConfig();

        initService();
    }

    private void initConfig() {
        urls = readFromTextFile(rootPath + "\\unread.txt");
        totalNum = urls.size();

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
        accountTxt.setText(configs[9]);
        passwordTxt.setText(configs[10]);

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
        String accountString = accountTxt.getText().trim();
        String passwordString = passwordTxt.getText().trim();
        if (startTimeString.equals("") || endTimeString.equals("")
                || pauseTimeFromString.equals("") || pauseTimeToString.equals("")
                || slipTimesFromString.equals("") || slipTimesToString.equals("")
                || pxFromString.equals("") || pxToString.equals("")
                || chromeLocationString.equals("")) {
            return;
        }

        String builder = startTimeString + "|" +
                endTimeString + "|" +
                pauseTimeFromString + "|" +
                pauseTimeToString + "|" +
                slipTimesFromString + "|" +
                slipTimesToString + "|" +
                pxFromString + "|" +
                pxToString + "|" +
                chromeLocationString + "|" +
                accountString + "|" +
                passwordString;

        writeTextFile(rootPath + "\\config.ini", builder);
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
        CompletableFuture.runAsync(() -> read(urls.get(currentNum)), executorService);
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
        CompletableFuture.runAsync(() -> connect(accountTxt.getText().trim(), passwordTxt.getText().trim()), executorService);
    }

    /**
     * 断开连接按钮点击事件
     *
     * @param event
     */
    public void disconnectButtonPress(ActionEvent event) {
        try {
            printLog("手动断开宽带连接！");
            ADSLUtil.disconnectAdsl();
            printLog("宽带连接已断开！");
        } catch (Exception e) {
            printLog("断开宽带连接异常，系统退出！");
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
            printLog("账号不能为空！");
            return;
        }
        if ("".equals(password)) {
            printLog("密码不能为空！");
            return;
        }
        try {
            printLog("正在断开宽带连接！");
            ADSLUtil.disconnectAdsl();
            printLog("宽带连接已断开！");
        } catch (Exception e) {
            printLog("断开宽带连接异常，系统退出！");
            appendFile(rootPath + "\\error.log", e.getMessage());
            return;
        }

        statusTxt.setText("");
        ipTxt.setText("");

        try {
            printLog("3秒后开始拨号...");
            Thread.sleep(3000);
            printLog("开始拨号！");
            statusTxt.setText("正在拨号");
            boolean status = ADSLUtil.connectAdsl(account, password);
            if (status) {
                printLog("拨号成功3秒后开始阅读！");
                statusTxt.setText("拨号成功");
                stopPPPOEFlag = false;
                try {
                    ipTxt.setText(ADSLUtil.getIp());
                } catch (Exception e1) {
                    appendFile(rootPath + "\\error.log", e1.getMessage());
                }

                if (!stopReadFlag) {
                    Thread.sleep(3000);
                    //触发开始阅读
                    Event.fireEvent(startReadBtn, new ActionEvent());
                }
            } else {
                statusTxt.setText("拨号失败");
                printLog("拨号失败，停止阅读！");
            }
        } catch (Exception e) {
            printLog("拨号异常！");
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
            printLog("文章已全部阅读完成！");
            readingFlag = false;
            return;
        }

        printLog(String.format("开始阅读第 %s 篇，剩余 %s 篇！", currentNum + 1, totalNum - currentNum - 1));
        printLog(String.format("正在加载文章：%s", url));

        //随机设置ua
        mobileEmulation.put("userAgent", userAgents.get(getRandomNumberInRange(0, 9)));
        chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);

        driver = new ChromeDriver(service, chromeOptions);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.get(url);
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        printLog("文章加载完成开始阅读！");
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
                printLog(String.format("第 %s 次下滑，等待 %s 秒, 下滑 %s 像素！", i, holdTime, px));
                Thread.sleep(holdTime * 1000);
                TouchScreen touchScreen = driver.getTouch();
                touchScreen.scroll(0, px);
                hasNum = i;
            }
        } catch (Exception e) {
            appendFile(rootPath + "\\error.log", e.getMessage());
        }

        if (stopReadFlag) {
            printLog(String.format("第 %s 篇文章未完成！", currentNum + 1));
        } else {
            printLog(String.format("第 %s 篇文章阅读完成，共下滑 %s 次！", currentNum + 1, hasNum));
            if (currentNum < totalNum - 1) {
                currentNum = currentNum + 1;
            } else {
                currentNum = 0;
            }
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
            printLog("保存全局配置失败！");
            appendFile(rootPath + "\\error.log", e.getMessage());
            return;
        }

        printLog("保存全局配置成功！");
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
            appendFile(rootPath + "\\error.log", e.getMessage());
        }
    }
}
