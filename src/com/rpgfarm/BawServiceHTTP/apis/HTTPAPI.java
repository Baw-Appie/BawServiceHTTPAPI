package com.rpgfarm.BawServiceHTTP.apis;

import com.rpgfarm.BawServiceHTTP.Main;
import com.rpgfarm.BawServiceHTTP.Utils;
import com.rpgfarm.BawServiceHTTP.events.MonetizePaymentEvent;
import org.bukkit.Bukkit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPAPI {
    public static void runWebCheck() {
        Logger logger = Main.singleInstance.getLogger();
        String data;
        try {
            logger.log(Level.OFF, "Minehub Monetize API 서버에 연결 중 입니다...");
            data = getData(Main.singleInstance.config.getString("setting.api-key"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Minehub Monetize HTTP API 서비스에 연결할 수 없습니다.");
            e.printStackTrace();
            return;
        }
        if(data.equals("ERROR")) {
            logger.log(Level.WARNING, "Minehub Monetize API에 잘못된 API 키가 설정되었습니다. config.yml 파일을 확인하세요.");
            return;
        }
        String[] datas = data.split("\n");
        for(String command : datas){
            if(command.isEmpty()) continue;
            logger.log(Level.FINE, "Minehub Monetize API 명령어 실행: "+command);
            Bukkit.getServer().getScheduler().runTask(Main.singleInstance, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                Bukkit.getPluginManager().callEvent(new MonetizePaymentEvent(command));
            });
            Utils.writeLog(command);
        }
    }

    protected static String getData(String apiKey) throws IOException {
        URL url = new URL("https://monetize.stella-api.dev/APILookup/"+apiKey);
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
}
