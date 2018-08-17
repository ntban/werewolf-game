package zon2008.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import zon2008.entity.AppPermissionEntity;

@Repository
public interface AppPermissionRepository extends JpaRepository<AppPermissionEntity, Long> {
	@Query("SELECT p.appRole.roleName FROM AppPermissionEntity p WHERE p.appUser.userId = ?1")
	public List<String> getRoleNames(Long userId);
}
