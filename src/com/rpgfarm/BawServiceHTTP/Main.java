package com.rpgfarm.BawServiceHTTP;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin implements Listener {
	public static FileConfiguration config;

    public void onEnable() {
        Logger logger = getLogger();
	    logger.info("Baw Service API 플러그인 사용을 환영합니다.");
	    config = getConfig();
	    config.addDefault("lastcommand", "BawServiceCommand");
	    config.addDefault("setting.api-key", "BawServiceAPI_KEY");
	    config.options().copyDefaults(true);
	    saveConfig();
	    saveDefaultConfig();
	    logger.info("현재 Baw Service HTTP API " + this.getDescription().getVersion() + "(을)를 사용하고 있습니다.");

        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
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
                saver(command);
            }
        }, 0L, 1200L);
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
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    public void saver(String fla) {
        File f = new File("plugins\\BawService\\log.log");
        if (!f.exists()) {
            new File("plugins\\BawService\\").mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
        Date date = new Date();
        try {
            File file = new File("plugins\\BawService\\log.log");
            FileReader fileReader = new FileReader(file);
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("plugins\\BawService\\log.log"), StandardCharsets.UTF_8));
            StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
            fileReader.close();
            String frmtdDate = dateFormat.format(date);
            try {
                PrintWriter writer = new PrintWriter("plugins\\BawService\\log.log", StandardCharsets.UTF_8);
                writer.println(stringBuffer + "[" + frmtdDate + "] " + "Baw Service API "+this.getDescription().getVersion()+" 원격 명령어 실행: " + fla);
                writer.close();
            } catch (FileNotFoundException | UnsupportedEncodingException ignored) {}
        } catch (IOException ignored) {}
    }
}
