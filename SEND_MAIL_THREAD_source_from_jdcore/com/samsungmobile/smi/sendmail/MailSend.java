package com.samsungmobile.smi.sendmail;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import org.apache.log4j.Logger;

public class MailSend
{
  public MailSend() {}
  
  static Logger logger = Logger.getLogger(new MailSend().getClass());
  
  public static void main(String[] args) {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    java.net.URL log4j_props_url = cl.getResource("log4j.properties");
    org.apache.log4j.PropertyConfigurator.configure(log4j_props_url);
    
    String port = MailSendProperties.getProperties("mail.port");
    

    String templateFile = MailSendProperties.getProperties("mail.html.template");
    String contentPath = MailSendProperties.getProperties("mail.html.upload.path");
    
    String userAuth = "AKIAIGNVY2HS3V52WIVA";
    String passwordAuth = "ArKqo5YbwxorlviTIeXfsOrHuFHqIZCvbbSGXMHgDkbW";
    
    String subject = "";
    String subjectFile = null;
    String content = "";
    String contentFile = null;
    String sendListFile = null;
    

    String target = null;
    String[] targetText = { "- Send File List [" + contentPath + args[1] + "_sendlist.txt ]", "- Individual Hub", "- Company Admin", "- Individual/Company Core", "- Member activation" };
    boolean inputSuccess = true;
    boolean isTransport = false;
    try
    {
      target = args[0];
      sendListFile = args[1] + "_sendlist.txt";
      subjectFile = args[2] + "_subject.html";
      contentFile = args[2] + "_content.html";
      
      System.out.println("sendListFile : " + sendListFile);
      System.out.println("subjectFile : " + subjectFile);
      System.out.println("contentFile : " + contentFile);
      
      if (args.length >= 4) {
        if ("debug".equals(args[3])) {
          isTransport = false;
        } else if ("send".equals(args[3])) {
          isTransport = true;
        }
      } else {
        System.out.println("Debug mode (send Mode \" Arg 4 => send\")");
      }
      
      String confirmText = "--------------------------------------------------------------------\n";
      confirmText = confirmText + "- sendListFile  " + sendListFile + "\n";
      confirmText = confirmText + "- subjectFile  " + subjectFile + "\n";
      confirmText = confirmText + "- contentFile  " + contentFile + "\n";
      confirmText = confirmText + "--------------------------------------------------------------------\n";
      System.out.println("\n\n" + confirmText + "\n");
      
      if (("0".equals(target)) || ("1".equals(target)) || ("2".equals(target)) || ("3".equals(target)) || ("4".equals(target))) {
        confirmText = confirmText + "[List Type]" + targetText[Integer.parseInt(target)] + "\n";
      } else {
        logger.error("잘못된 값 입력(발송대상자)");
        throw new Exception();
      }
      

      if (!"".equals(subjectFile)) {
        BufferedReader fr = null;
        try {
          fr = new BufferedReader(new FileReader(contentPath + subjectFile));
          String line = null;
          while ((line = fr.readLine()) != null) {
            subject = subject + line;
          }
        } catch (FileNotFoundException e) {
          System.out.println("메일 제목 HTML 파일이 존재하지 않음");
          logger.error("메일 제목 HTML 파일이 존재하지 않음");
          logger.error(e.getMessage(), e);
          throw new Exception();
        } catch (IOException e) {
          System.out.println(e.getMessage());
          logger.error(e.getMessage(), e);
          throw new Exception();
        } finally {
          fr.close();
        }
      }
      else {
        System.out.println("\n\n입력된 값 없음(파일제목 HTML)\n\n");
        logger.error("입력된 값 없음(파일제목 HTML)");
        throw new Exception();
      }
      
      if (!"".equals(subject)) {
        confirmText = confirmText + "[메일 제목 : " + subject + " ]\n";
      } else {
        System.out.println("\n\n잘못된 값 입력(메일제목)\n\n");
        logger.error("잘못된 값 입력(메일제목)");
        throw new Exception();
      }
      

      if (!"".equals(contentFile)) {
        BufferedReader fr = null;
        try {
          content = "";
          fr = new BufferedReader(new FileReader(templateFile));
          
          String line = null;
          while ((line = fr.readLine()) != null) {
            content = content + line + "\n";
          }
        } catch (FileNotFoundException e) {
          System.out.println("메일 템플릿이 존재하지 않음");
          logger.error("메일 템플릿이 존재하지 않음");
          logger.error(e.getMessage(), e);
          throw new Exception();
        } catch (IOException e) {
          System.out.println(e.getMessage());
          logger.error(e.getMessage(), e);
          throw new Exception();
        } finally {
          fr.close();
        }
        try
        {
          fr = new BufferedReader(new FileReader(contentPath + contentFile));
          
          String line = "";
          String contentHtml = "";
          int j = 0;
          while ((line = fr.readLine()) != null) {
            if (j > 0) {
              contentHtml = contentHtml + "\n";
            }
            contentHtml = contentHtml + line;
          }
          try {
            content = content.replace("<@ CONTENTS>", contentHtml);
          }
          catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error(e.getMessage(), e);
          }
        }
        catch (FileNotFoundException e) {
          System.out.println("메일 내용 HTML 파일이 존재하지 않음");
          logger.error("메일 내용 HTML 파일이 존재하지 않음");
          logger.error(e.getMessage(), e);
          throw new Exception();
        } catch (IOException e) {
          System.out.println(e.getMessage());
          logger.error(e.getMessage(), e);
          throw new Exception();
        } finally {
          fr.close();
        }
      }
      System.out.println("\n\n입력된 값 없음(파일내용 HTML)\n\n");
      logger.error("입력된 값 없음(파일내용 HTML)");
      throw new Exception();

    }
    catch (IOException e)
    {

      System.out.println(e.getMessage());
      logger.error(e.getMessage(), e);
      inputSuccess = false;
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.getMessage(), e);
      inputSuccess = false;
    }
    try
    {
      if (!isTransport) {
        System.out.println("--------------------------------------------------------------------");
      }
      if (inputSuccess) {
        ArrayList<HashMap<String, String>> memberList = null;
        if ("0".equals(target)) {
          memberList = new ArrayList();
          BufferedReader fr = null;
          try
          {
            fr = new BufferedReader(new FileReader(contentPath + sendListFile));
            String line = null;
            int j = 0;
            while ((line = fr.readLine()) != null) {
              j++;
              HashMap<String, String> map = new HashMap();
              map.put("FIRST_NAME", "[ " + j + " ]");
              map.put("LAST_NAME", "[ Last Name " + j + " ]");
              
              String[] lines = line.split(",");
              
              if (lines.length >= 2)
              {
                map.put("EMAIL", lines[0]);
                map.put("MBR_ID", lines[1]);
                
                memberList.add(map);
              }
            }
          } catch (FileNotFoundException e) {
            System.out.println("명단파일없음");
            logger.error("명단파일이 존재하지 않음");
            logger.error(e.getMessage(), e);
            throw new Exception();
          } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("명단파일에러");
            logger.error("명단파일에 오류가 있습니다");
            logger.error(e.getMessage(), e);
            throw new Exception();
          } catch (IOException e) {
            System.out.println(e.getMessage());
            logger.error(e.getMessage(), e);
            throw new Exception();
          } finally {
            fr.close();
          }
        }
        else {
          MemberDao dao = new MemberDao();
          memberList = dao.getMemberList(target);
        }
        if (memberList == null) {
          logger.error("대상이 없습니다.");
          throw new Exception();
        }
        

        Properties props = new Properties();
        props.put("mail.smtp.host", "email-smtp.eu-west-1.amazonaws.com");
        props.put("mail.smtp.port", Integer.valueOf(Integer.parseInt(port)));
        
        props.put("mail.smtp.auth", "true");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug.auth", "true");
        
        Session sess = Session.getDefaultInstance(props, 
          new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication("AKIAIGNVY2HS3V52WIVA", "ArKqo5YbwxorlviTIeXfsOrHuFHqIZCvbbSGXMHgDkbW");
            }
          });
        sess.setDebug(false);
        
        SendMailExecutor sendMailExecutor = new SendMailExecutor(sess, content, subject, memberList);
        sendMailExecutor.sendEMailExcutor();
      }
      
      if (!isTransport)
      {
        System.out.println("--------------------------------------------------------------------");
      }
    } catch (MessagingException ex) {
      ex.printStackTrace();
      System.out.println("mail send error : " + ex.getMessage());
      logger.error(ex.getMessage(), ex);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("error : " + e.getMessage());
      logger.error(e.getMessage(), e);
    }
  }
  
  public static boolean isValidEmail(String toEmail) throws Exception {
    boolean err = false;
    
    String regex = "^[_a-zA-Z0-9-]+(.[_a-zA-Z0-9-]+)*@(?:\\w[^_]+\\.)+\\w[^_-]+$";
    
    String[] arr = toEmail.split("@");
    
    if (arr.length != 2) {
      err = true;
    }
    else
    {
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(toEmail);
      if (!m.matches()) {
        err = true;
      }
    }
    return err;
  }
}
