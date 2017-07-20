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
import java.text.SimpleDateFormat;
import java.util.Date;

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
		      System.out.println("[Baw Service] Baw Service HTTP API�� �����մϴ�.");
			  try {
				this.data = get_data();
			} catch (IOException e) {
			    System.out.println("[Baw Service] Baw Service HTTP API�� ������ �Ұ����մϴ�.");
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
			          System.out.println(command);
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
				          System.out.println("[Baw Service] Baw Service���κ��� ��ɾ� ���� ��û�� ���޹޾ҽ��ϴ�.");
			            }
			          }
			          else if (Main.config.getString("setting.id").equals(id))
			          {
			            System.out.println("[Baw Service] Baw Service API KEY�� ��ġ���� �ʽ��ϴ�. ��ɾ� ���� ��û�� �����մϴ�. �������� ���۹��� APIKEY:" + api_key);
			          }
			          else
			          {
			            System.out.println("[Baw Service] Baw Service ID�� ��ġ���� �ʽ��ϴ�. ��ɾ� ��û�� �����մϴ�. �������� ���۹��� ID:" + id);
			          }
			        }
			      }
			  }
		          
	      }
	    }
	    , 0L, 1200L);
	    
	    
	    this.ver = getOpenStreamHTML("https://baws.kr/api/versioncheckerHTTP.php");

	    if (!this.ver.equals(this.getDescription().getVersion()))
	    {
	      System.out.println("[Baw Service] Baw Service HTTP API ������Ʈ ���� �߰�! ������Ʈ�� �ݵ�� ������ ����ϰ� ������Ʈ�ϼ���.");
	      System.out.println("[Baw Service] ���ο� ������Ʈ�� https://baws.kr/ ���� ������ �� �ֽ��ϴ�.");
	      System.out.println("[Baw Service] ���� ����: " + this.getDescription().getVersion());
	      System.out.println("[Baw Service] ���ο� ����: " + this.ver);
	    }
	    config = getConfig();
	    config.addDefault("lastcommand", "BawServiceCommand");
	    config.options().copyDefaults(true);
	    saveConfig();
	    saveDefaultConfig();
	    config.addDefault("setting.id", "BawServiceID");
	    config.addDefault("setting.api-key", "BawServiceAPI_KEY");
	    saveConfig();
	    Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Baw Service" + ChatColor.AQUA + "] Baw Service HTTP API v" + this.getDescription().getVersion() + "�� Ȱ��ȭ���Դϴ�. ȯ���մϴ�. " + config.getString("setting.id") + "��");
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
    	URL url = new URL("https://baws.kr/api/getlist.php");
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        
        conn.setDoOutput(true);
    
        try (OutputStream out = conn.getOutputStream()) {
            out.write(("id=" + URLEncoder.encode(config.getString("setting.id"),"UTF-8")).getBytes());
            out.write("&".getBytes());
            out.write(("api_key=" + URLEncoder.encode(config.getString("setting.api-key"),"UTF-8")).getBytes());
            out.write("&".getBytes());
        }

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
          writer.println(stringBuffer.toString() + "[" + frmtdDate + "] " + "BawService ���� �ܼ� ��ɾ� ����: " + fla);
          writer.close();
        }
        catch (FileNotFoundException localFileNotFoundException) {}catch (UnsupportedEncodingException localUnsupportedEncodingException) {}
        return;
      }
      catch (IOException localIOException1) {}
    }
}
