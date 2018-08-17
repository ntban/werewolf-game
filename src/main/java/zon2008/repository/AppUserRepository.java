package zon2008.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import zon2008.entity.AppUserEntity;
@Repository
public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {

	@Query("SELECT u FROM AppUserEntity u WHERE u.username = ?1")
	public AppUserEntity findByName(String name);
}
