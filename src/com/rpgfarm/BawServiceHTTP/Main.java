package com.rpgfarm.BawServiceHTTP;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import com.rpgfarm.BawServiceHTTP.events.MonetizePaymentEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin implements Listener {
    public static FileConfiguration config;
    Thread serverThread;

    public void onEnable() {
        Logger logger = getLogger();
        logger.info("Baw Service API 플러그인 사용을 환영합니다.");
        config = getConfig();
        config.addDefault("lastcommand", "BawServiceCommand");
        config.addDefault("setting.api-key", "BawServiceAPI_KEY");
        config.addDefault("setting.port", "21080");
        config.options().copyDefaults(true);
        saveConfig();
        saveDefaultConfig();
        logger.info("현재 Baw Service HTTP API " + this.getDescription().getVersion() + "(을)를 사용하고 있습니다.");

        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, this::runWebCheck, 0L, 1200L);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        startSocketThread();
    }

    public void startSocketThread() {
        this.serverThread = new Thread(() -> {
            int port = Integer.parseInt(config.getString("setting.port"));
            getLogger().info("Baw Service API 서버를 시작합니다. ("+port+")");
            try (ServerSocket server = new ServerSocket(port)) {
                while (true) {
                    Socket client = server.accept();
                    OutputStreamWriter osr = new OutputStreamWriter(client.getOutputStream());
                    BufferedWriter bw = new BufferedWriter(osr);
                    PrintWriter pw = new PrintWriter(bw);
                    pw.println("OK");
                    pw.flush();
                    client.close();
                    runWebCheck();
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Baw Service API에 오류가 있습니다.");
                e.printStackTrace();
            }
        });
        this.serverThread.start();
    }

    public void stopSocketThread() {
        getLogger().info("Baw Service API 서버를 종료합니다.");
        this.serverThread.interrupt();
    }

    public void onDisable() {
        stopSocketThread();
        getLogger().info("Baw Service API를 이용해주셔서 감사합니다.");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.isOp()) {
            sender.sendMessage("관리자만 사용할 수 있습니다.");
            return false;
        }
        if (command.getName().equalsIgnoreCase("bawservice")) {
            if (args.length == 0) {
                sender.sendMessage("/bawservice reload: 설정을 다시 불러옵니다.");
            } else if (args[0].equalsIgnoreCase("reload")) {
                config = getConfig();
                stopSocketThread();
                startSocketThread();
                sender.sendMessage("설정을 다시 불러왔습니다.");
                getLogger().info("설정을 다시 불러왔습니다.");
            }
        }
        return false;
    }

    public void runWebCheck() {
        Logger logger = getLogger();
        String data;
        try {
            logger.log(Level.OFF, "Baw Service API 서버에 연결 중 입니다...");
            data = getData();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Baw Service HTTP API 서비스에 연결할 수 없습니다.");
            e.printStackTrace();
            return;
        }
        if(data.equals("ERROR")) {
            logger.log(Level.WARNING, "Baw Service API에 잘못된 API 키가 설정되었습니다. config.yml 파일을 확인하세요.");
            return;
        }
        String[] datas = data.split("\n");
        for(String command : datas){
            if(command.isEmpty()) continue;
            logger.log(Level.FINE, "Baw Service API 명령어 실행: "+command);
            getConfig().set("lastcommand", command);
            saveConfig();
            Bukkit.getServer().getScheduler().runTask(Main.this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            Bukkit.getPluginManager().callEvent(new MonetizePaymentEvent(command));
            saver(command);
        }
    }

    public String getData() throws IOException {
        URL url = new URL("https://monetize.stella-api.dev/APILookup/"+config.getString("setting.api-key"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);

        // SSL Let's Encrypt 오류 수정
        TrustManager[] trustAllCerts = {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try (InputStream in = conn.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024 * 8];
            int length;
            while ((length = in.read(buf)) != -1) out.write(buf, 0, length);
            return out.toString("UTF-8");
        }
    }

    public void saver(String fla) {
        File f = new File("plugins/BawService/log.log");
        if (!f.exists()) {
            new File("plugins/BawService/").mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
        Date date = new Date();
        try {
            File file = new File("plugins/BawService/log.log");
            FileReader fileReader = new FileReader(file);
            BufferedReader in = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get("plugins/BawService/log.log")), StandardCharsets.UTF_8));
            StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
            fileReader.close();
            String frmtdDate = dateFormat.format(date);
            try {
                PrintWriter writer = new PrintWriter("plugins/BawService/log.log", "UTF-8");
                writer.println(stringBuffer + "[" + frmtdDate + "] " + "Baw Service 원격 명령어 실행: " + fla);
                writer.close();
            } catch (FileNotFoundException | UnsupportedEncodingException ignored) {}
        } catch (IOException ignored) {}
    }
}
