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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


import com.rpgfarm.BawServiceHTTP.Main;


public class Main extends JavaPlugin implements Listener {
	
	
	public static FileConfiguration config;
	  public Thread serverThread;
	  String ver;
	  public static String latestCommand = "";
	  public static int count = 0;
	  
	  public static String m(String message)
	  {
	    return ChatColor.translateAlternateColorCodes('&', message);
	  }
	  
	  public void onEnable()
	  {

	    Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable()
	    {
	      String data = "";
	      public void run()
	      {
			  try {
				this.data = get_data();
			} catch (IOException e) {
			    System.out.println("[Baw Service] 다음과 같은 오류로 Baw Service HTTP API 서비스에 연결할 수 없습니다.");
				e.printStackTrace();
			}
			  

			  String[] datas = data.split("'/");
			  for( String command : datas ){
			      if (command.indexOf(";") != -1)
			      {
			        if (command.split(";").length >= 2)
			        {
			          String api_key = command.split(";")[0];
			          String id = command.split(";")[1];
			          String[] commands = command.replace(api_key + ";" + id + ";", "").split(";");
			          if ((Main.config.getString("setting.id").equals(id)) && (Main.config.getString("setting.api-key").equals(api_key)))
			          {
			            String[] arrayOfString1;
			            int j = (arrayOfString1 = commands).length;
			            for (int i = 0; i < j; i++)
			            {
			              String str = arrayOfString1[i];
				          getConfig().set("lastcommand", str);
				          saveConfig();
				          Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str);
				          saver(str);
				          System.out.println("[Baw Service] Baw Service API 명령어 실행: "+str);
			            }
			          }
			          else if (Main.config.getString("setting.id").equals(id))
			          {
			            System.out.println("[Baw Service] 보안을 위하여 전송받은 데이터를 검증하였으나, 잘못된 Baw Service API KEY 입니다. 요청을 무시합니다.");
			          }
			          else
			          {
			            System.out.println("[Baw Service] 보안을 위하여 전송받은 데이터를 검증하였으나, 잘못된 Baw Service ID 입니다. 요청을 무시합니다.");
			          }
			        }
			      }
			  }
		          
	      }
	    }
	    , 0L, 1200L);
	    
	    
	    this.ver = getOpenStreamHTML("https://baws.kr/API/GetHTTPAPIVersion");

	    if (!this.ver.equals(this.getDescription().getVersion()))
	    {
	        System.out.println("[Baw Service Updater] Baw Service API 업데이트 버전 발견! 업데이트전 반드시 서버를 백업하고 업데이트하세요.");
	        System.out.println("[Baw Service Updater] Baw Service API는 되도록 최신 버전을 유지할 수 있도록 해주세요.");
	        System.out.println("[Baw Service Updater] 현재 버전: " + this.getDescription().getVersion());
	        System.out.println("[Baw Service Updater] 새로운 버전: " + this.ver);
	    }
	    System.out.println("[Baw Service] Baw Service API 플러그인 콘피그 초기화중");
	    config = getConfig();
	    config.addDefault("lastcommand", "BawServiceCommand");
	    config.addDefault("setting.id", "BawServiceID");
	    config.addDefault("setting.api-key", "BawServiceAPI_KEY");
	    config.options().copyDefaults(true);
	    saveConfig();
	    saveDefaultConfig();
	    System.out.println("[Baw Service] Baw Service API v" + this.getDescription().getVersion() + "가 활성화중입니다. 환영합니다. " + config.getString("setting.id") + "님");
	    System.out.println("[Baw Service] 현재 활성화중인 Baw Service API는 Socket 사용 버전입니다.");
	  }
	    
	    
	    
    public String getOpenStreamHTML(String urlToRead)
    {
        String result = "";
        try
        {
            URL url = new URL(urlToRead);
            InputStreamReader is = new InputStreamReader(url.openStream(), "UTF-8");
            int ch;
            while((ch = is.read()) != -1) 
                result = (new StringBuilder(String.valueOf(result))).append((char)ch).toString();
        }
        catch(MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }
    
    public String get_data() throws IOException
    {
    	URL url = new URL("https://baws.kr/API/GetList");
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        
        conn.setDoOutput(true);
    
        try (OutputStream out = conn.getOutputStream()) {
            out.write(("id=" + URLEncoder.encode(config.getString("setting.id"),"UTF-8")).getBytes());
            out.write("&".getBytes());
            out.write(("api_key=" + URLEncoder.encode(config.getString("setting.api-key"),"UTF-8")).getBytes());
            out.write("&".getBytes());
        }
        
        // SSL Let's Encrypt 오류 수정
        
        TrustManager[] trustAllCerts = { new X509TrustManager()
        {
          public X509Certificate[] getAcceptedIssuers()
          {
            return null;
          }
          
          public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException
          {}
          
          public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException
          {}
        } };
        SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("SSL");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			sc.init(null, trustAllCerts, new SecureRandom());
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

//        출처: http://bobr2.tistory.com/entry/SSL-인증서-없이-https-통신하는-법-예제 [나만의공간]
        
        try (InputStream in = conn.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024 * 8];
            int length = 0;
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
            return (new String(out.toByteArray(), "UTF-8"));
        }
    }
    
    public void saver(String fla)
    {
      File f = new File("plugins\\BawService\\log.log");
      if (!f.exists())
      {
        new File("plugins\\BawService\\").mkdirs();
        try
        {
          f.createNewFile();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
      SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
      Date date = new Date();
      try
      {
        File file = new File("plugins\\BawService\\log.log");
        FileReader fileReader = new FileReader(file);
        BufferedReader in = new BufferedReader(
          new InputStreamReader(
          new FileInputStream("plugins\\BawService\\log.log"), "UTF8"));
        StringBuffer stringBuffer = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null)
        {
          stringBuffer.append(line);
          stringBuffer.append("\n");
        }
        fileReader.close();
        String frmtdDate = dateFormat.format(date);
        try
        {
          PrintWriter writer = new PrintWriter("plugins\\BawService\\log.log", "UTF-8");
          writer.println(stringBuffer.toString() + "[" + frmtdDate + "] " + "Baw Service API "+this.getDescription().getVersion()+" 원격 명령어 실행: " + fla);
          writer.close();
        }
        catch (FileNotFoundException localFileNotFoundException) {}catch (UnsupportedEncodingException localUnsupportedEncodingException) {}
        return;
      }
      catch (IOException localIOException1) {}
    }
}
