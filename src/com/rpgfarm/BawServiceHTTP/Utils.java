package com.rpgfarm.BawServiceHTTP;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static void writeLog(String content) {
        File f = new File("plugins/MinehubMonetize/log.log");
        if (!f.exists()) {
            new File("plugins/MinehubMonetize/").mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
        Date date = new Date();
        try {
            File file = new File("plugins/MinehubMonetize/log.log");
            FileReader fileReader = new FileReader(file);
            BufferedReader in = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get("plugins/MinehubMonetize/log.log")), StandardCharsets.UTF_8));
            StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
            fileReader.close();
            String frmtdDate = dateFormat.format(date);
            try {
                PrintWriter writer = new PrintWriter("plugins/MinehubMonetize/log.log", "UTF-8");
                writer.println(stringBuffer + "[" + frmtdDate + "] " + "Minehub Monetize 원격 명령어 실행: " + content);
                writer.close();
            } catch (FileNotFoundException | UnsupportedEncodingException ignored) {
            }
        } catch (IOException ignored) {
        }
    }
}
