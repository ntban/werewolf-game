package com.samsungmobile.smi.sendmail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class MemberDao
{
  public MemberDao() {}
  
  java.sql.Connection conn = null;
  
  public ArrayList<HashMap<String, String>> getMemberList(String memberType) {
    ArrayList<HashMap<String, String>> memberList = new ArrayList();
    
    conn = getConnection();
    String query = getQuery(memberType);
    
    System.out.println(query);
    try
    {
      java.sql.PreparedStatement pstmt = conn.prepareStatement(query);
      ResultSet rs = pstmt.executeQuery();
      
      while (rs.next()) {
        HashMap<String, String> map = new HashMap();
        map.put("FIRST_NAME", rs.getString("FIRST_NAME"));
        map.put("LAST_NAME", rs.getString("LAST_NAME"));
        map.put("EMAIL", rs.getString("EMAIL"));
        memberList.add(map);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return memberList;
  }
  
  private java.sql.Connection getConnection() {
    String jdbc_url = MailSendProperties.getProperties("jdbc.url");
    String jdbc_id = MailSendProperties.getProperties("jdbc.username");
    String jdbc_pwd = MailSendProperties.getProperties("jdbc.password");
    try {
      Class.forName("oracle.jdbc.driver.OracleDriver");
      conn = java.sql.DriverManager.getConnection(jdbc_url, jdbc_id, jdbc_pwd);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    return conn;
  }
  
  private String getQuery(String memberType) {
    StringBuffer query = new StringBuffer();
    query.append("\t SELECT \n");
    query.append("\t     FIRST_NAME, \n");
    query.append("\t     LAST_NAME, \n");
    query.append("\t     EMAIL, \n");
    query.append("\t     MBR_ID, \n");
    query.append("\t     NATION_CODE \n");
    query.append("\t FROM \n");
    query.append("\t     MBR_MEMBER_VW \n");
    query.append("\t WHERE MAIL_RCV_FLAG = '1' \n");
    





















    return query.toString();
  }
}
