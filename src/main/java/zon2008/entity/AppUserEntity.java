package zon2008.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "app_user", //
		uniqueConstraints = { //
				@UniqueConstraint(name = "APP_USER_NAME", columnNames = "username"),
				@UniqueConstraint(name = "APP_USER_IP", columnNames = "user_ip") })

public class AppUserEntity implements Serializable {
	private static final long serialVersionUID = 4668375551612644571L;

	@Id
	@GeneratedValue
	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "username", length = 36, nullable = false)
	private String username;
	
	 @Column(name = "nickname", length = 56, nullable = false)
	 private String nickname;
	 
	 @Column(name = "encryted_password", length = 128, nullable = false)
	 private String encrytedPassword;
	 
	 @Column(name = "user_ip", length = 32, nullable = false)
	 private String userIP;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return username;
	}

	public void setUserName(String userName) {
		this.username = userName;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEncrytedPassword() {
		return encrytedPassword;
	}

	public void setEncrytedPassword(String encrytedPassword) {
		this.encrytedPassword = encrytedPassword;
	}

	public String getUserIP() {
		return userIP;
	}

	public void setUserIP(String userIP) {
		this.userIP = userIP;
	}
	 
}
