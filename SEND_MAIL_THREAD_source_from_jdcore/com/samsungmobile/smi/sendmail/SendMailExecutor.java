package com.samsungmobile.smi.sendmail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;

public class SendMailExecutor
{
  private String from = MailSendProperties.getProperties("mail.from");
  private String user = MailSendProperties.getProperties("mail.user");
  private String admins = MailSendProperties.getProperties("mail.admin");
  
  private final int BLOCK_SIZE = 20;
  private final int MAIL_TO_SLEEP = 45000;
  private final int TIME_SLEEP = 30;
  private int MAIL_NUMBER;
  private int checkSuccessfully = -1;
  private int checkFailed = 0;
  private int counter = 1;
  
  ScheduledExecutorService executor = Executors.newScheduledThreadPool(20);
  ScheduledExecutorService executorSleep = Executors.newSingleThreadScheduledExecutor();
  ScheduledExecutorService executorNotice = Executors.newSingleThreadScheduledExecutor();
  
  private Logger logger = MailSend.logger;
  
  private Session session;
  private String content;
  private String subject;
  private static final int MAX_FAIL = 3;
  private static final int RE_SEND_TIME = 2;
  ArrayList<HashMap<String, String>> memberList;
  ArrayList<String> validatedFail = new ArrayList();
  ArrayList<String> sendFail = new ArrayList();
  
  public SendMailExecutor(Session session, String content, String subject, ArrayList<HashMap<String, String>> memberList)
  {
    this.session = session;
    this.content = content;
    this.subject = subject;
    this.memberList = memberList;
    MAIL_NUMBER = memberList.size();
  }
  
  public void sendEMailExcutor() {
    try {
      shutdownIfCompleteTasks();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }
  

  SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");
  
  class EmailTask implements Runnable
  {
    private int failCounter = 0;
    
    private ScheduledExecutorService executor;
    
    private Message msg;
    
    private String to;
    
    private String name;
    private int index;
    
    public EmailTask() {}
    
    public EmailTask(ScheduledExecutorService executor, int index)
    {
      this.executor = executor;
      this.index = index;
    }
    
    public void run()
    {
      if (checkValidateMail()) {
        String logStr = "sended to " + name + "(" + to + ")";
        logger.info("mail-send-start " + subject + "[" + index + "]" + "(" + to + ")");
        try
        {
          Transport.send(msg);
          logger.info(subject + "\n" + logStr + "\nmail-send-end");
          SendMailExecutor.this.shutdownIfCompleteTasks();
        } catch (Exception e) {
          failCounter += 1;
          if (failCounter == 3)
          {
            String logError = "Error when send to " + name + "(" + to + ")";
            logger.info(logError);
            logger.error(e.getMessage(), e);
            

            noticeAdmin(to, e);
            
            SendMailExecutor.this.shutdownIfCompleteTasks();
          } else {
            logger.info("Re-send to " + name + "(" + to + ") after " + 2 + " seconds");
            executor.schedule(this, 2L, java.util.concurrent.TimeUnit.SECONDS);
          }
        }
      }
      else {
        SendMailExecutor.this.shutdownIfCompleteTasks();
      }
    }
    
    boolean checkValidateMail()
    {
      boolean checkValidate = false;
      
      HashMap<String, String> map = (HashMap)memberList.get(index);
      String mbrId = (String)map.get("MBR_ID");
      if (!MailSendProperties.getProperties("send.test").equals("false")) {
        to = MailSendProperties.getProperties("send.test.to");
      } else {
        to = ((String)map.get("EMAIL"));
      }
      try
      {
        if (!SendMailExecutor.this.isValidEmail(to)) {
          checkValidate = true;
          
          name = ((String)map.get("FIRST_NAME"));
          
          content = content.replaceAll("@USERNAME", "'" + mbrId + "'");
          content = content.replaceAll("userIndex=0", "userIndex=" + (index + 1));
          
          msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress(from, user));
          msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
          msg.setSubject(subject);
          msg.setSentDate(new Date());
          
          msg.setContent(content, "text/html;charset=UTF-8");
          
          checkValidate = true;
        } else {
          checkValidate = false;
          addMailFailValidate(to);
          logger.info("#################################################################");
          logger.info("                   to Email Failed Validate : " + to);
          logger.info("#################################################################");
        }
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
      } finally {
        content = content.replaceAll("'" + mbrId + "'", "@USERNAME");
        content = content.replaceAll("userIndex=" + (index + 1), "userIndex=0");
      }
      
      return checkValidate;
    }
  }
  
  private synchronized void shutdownIfCompleteTasks()
  {
    checkSuccessfully += 1;
    
    if (checkSuccessfully == MAIL_NUMBER) {
      noticeCompleteMail();
      
      executorNotice.shutdown();
      executorNotice = null;
      executor.shutdown();
      executorSleep.shutdown();
      executor = null;
      executorSleep = null;
      return;
    }
    
    if (checkSuccessfully % 20 == 0) {
      if (checkSuccessfully / counter >= 45000) {
        counter += 1;
        executorSleep.schedule(new Runnable()
        {
          public void run()
          {
            SendMailExecutor.this.executeBlock();
          }
        }, 30L, java.util.concurrent.TimeUnit.SECONDS);
      } else {
        executeBlock();
      }
    }
  }
  
  private void noticeCompleteMail()
  {
    try {
      StringBuffer contentMail = new StringBuffer(memberList.size() + " mail sent / " + (checkFailed + validatedFail.size()) + " errors");
      contentMail.append("<br/><br/><br/>Mails validated fail:<br/>");
      for (String mail : validatedFail) {
        contentMail.append(mail + "<br/>");
      }
      contentMail.append("<br/><br/>Mails sended fail:<br/>");
      for (String mail : sendFail) {
        contentMail.append(mail + "<br/>");
      }
      
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(from, user));
      msg.setSubject("Send Completed - " + subject);
      
      msg.setContent(contentMail.toString(), "text/html;charset=UTF-8");
      msg.setSentDate(new Date());
      String[] admin = admins.split(",");
      for (int k = 0; k < admin.length; k++) {
        admin[k] = admin[k].trim();
      }
      for (int i = 0; i < admin.length; i++) {
        InternetAddress[] address = { new InternetAddress(admin[i].toLowerCase()) };
        msg.setRecipients(Message.RecipientType.TO, address);
        final Message message = msg;
        final String adminMail = admin[i];
        executorNotice.execute(new Runnable()
        {
          public void run()
          {
            try {
              Transport.send(message);
              logger.info("Sent to Completed Mail (" + adminMail + ")");
            } catch (Exception e) {
              logger.error(e.getMessage(), e);
            }
          }
        });
      }
      

      logger.info("#################################################################");
      logger.info("                   complete send mail : " + subject);
      logger.info("#################################################################");
      logger.info(memberList.size() + " mail sent / " + (checkFailed + validatedFail.size()) + " errors");
    } catch (Exception ex) {
      logger.error(ex);
    }
  }
  
  public synchronized void addMailFailValidate(String to) {
    validatedFail.add(to);
  }
  
  public synchronized void noticeAdmin(String emailError, Exception e) {
    checkFailed += 1;
    sendFail.add(emailError);
    try
    {
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(from, user));
      String tilte = "Notice Error Send Mail " + subject + " to " + emailError;
      msg.setSubject(tilte);
      msg.setContent(tilte + getStackTrace(e), "text/html;charset=UTF-8");
      msg.setSentDate(new Date());
      String[] admin = admins.split(",");
      for (int k = 0; k < admin.length; k++) {
        admin[k] = admin[k].trim();
      }
      for (int i = 0; i < admin.length; i++) {
        InternetAddress[] address = { new InternetAddress(admin[i].toLowerCase()) };
        msg.setRecipients(Message.RecipientType.TO, address);
        final Message message = msg;
        final String adminMail = admin[i];
        executor.execute(new Runnable()
        {
          public void run()
          {
            try
            {
              Transport.send(message);
              logger.info("Sent to Completed Notice Mail (" + adminMail + ")");
            } catch (Exception e) {
              logger.error(e.getMessage(), e);
            }
          }
        });
      }
    } catch (Exception ex) {
      logger.error(ex);
    }
  }
  
  private void executeBlock() {
    int bound = checkSuccessfully + 20;
    
    if (bound > MAIL_NUMBER) {
      bound = MAIL_NUMBER;
    }
    
    for (int i = checkSuccessfully + 1; i <= bound; i++) {
      executor.execute(new EmailTask(executor, i - 1));
    }
  }
  
  private static String getStackTrace(Throwable throwable) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw, true);
    throwable.printStackTrace(pw);
    return sw.getBuffer().toString();
  }
  
  private boolean isValidEmail(String toEmail) throws Exception {
    boolean err = false;
    
    String regex = "^[_a-zA-Z0-9-]+(.[_a-zA-Z0-9-]+)*@(?:\\w[^_]+\\.)+\\w[^_-]+$";
    
    String[] arr = toEmail.split("@");
    
    if (arr.length != 2) {
      err = true;
    } else {
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(toEmail);
      if (!m.matches()) {
        err = true;
      }
    }
    return err;
  }
}
