package com.mmo.microservice.token.repository;

import com.mmo.microservice.token.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TokenRepo extends JpaRepository<Token, Long> {
    @Query("SELECT t FROM Token t  WHERE t.r_page_id = :id")
    List<Token> getPageToken(@Param("id") long id);

    @Query("SELECT t FROM Token t  WHERE t.r_user_fb_id = :id")
    List<Token> getLstFBUserToken(@Param("id") long id);

    @Query("SELECT t FROM Token t  WHERE t.r_user_fb_id = :id")
    Token getFBUserToken(@Param("id") long id);

    @Query(value ="select * from r_token where type = 'PAGE' and r_page_id = :id order by created_date desc limit 1", nativeQuery = true)
    Token getLastPageToken(@Param("id") long id);

    @Query("select rt from Token rt where rt.r_user_fb_id = :id")
    Token getTokenByUFBID(@Param("id") long id);

    @Query("select rt from Token rt where (rt.r_user_fb_id = :u_id or :u_id is null) and (rt.r_page_id = :a_id or :a_id is null)")
    Token getTokenExisted(@Param("u_id") Long u_id, @Param("a_id") Long a_id);

    @Modifying
    @Query("delete from Token rt where rt.r_user_fb_id = :id")
    void deleteTokenByUFBID(@Param("id") long id);
}
