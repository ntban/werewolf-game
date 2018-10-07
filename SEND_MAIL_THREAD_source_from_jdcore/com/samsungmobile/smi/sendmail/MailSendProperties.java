package com.samsungmobile.smi.sendmail;

import java.util.Properties;

public class MailSendProperties
{
  public MailSendProperties() {}
  
  public static String getProperties(String key)
  {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    java.io.InputStream props_stream = cl.getResourceAsStream("config.properties");
    
    Properties props = new Properties();
    String rtn = "";
    try
    {
      props.load(new java.io.BufferedInputStream(props_stream));
      
      rtn = props.getProperty(key);
      props_stream.close();
    } catch (java.io.FileNotFoundException e) {
      System.out.println(e.getMessage());
    } catch (java.io.IOException e) {
      System.out.println(e.getMessage());
    }
    
    return rtn;
  }
}
