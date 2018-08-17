package zon2008.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "app_permission", uniqueConstraints = {
		@UniqueConstraint(name = "USER_ROLE_UK", columnNames = { "user_id", "role_id" }) })

public class AppPermissionEntity implements Serializable {

	private static final long serialVersionUID = 4422307740900339016L;

	@Id
	@GeneratedValue
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUserEntity appUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id", nullable = false)
	private AppRoleEntity appRole;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AppUserEntity getAppUser() {
		return appUser;
	}

	public void setAppUser(AppUserEntity appUser) {
		this.appUser = appUser;
	}

	public AppRoleEntity getAppRole() {
		return appRole;
	}

	public void setAppRole(AppRoleEntity appRole) {
		this.appRole = appRole;
	}

}
