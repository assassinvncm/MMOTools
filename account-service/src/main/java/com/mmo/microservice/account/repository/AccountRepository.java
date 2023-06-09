package com.mmo.microservice.account.repository;

import com.mmo.microservice.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT u FROM Account u  WHERE u.facebook_id = :facebook_id")
    Account getFbUserByFBID(@Param("facebook_id") String facebook_id);
}
