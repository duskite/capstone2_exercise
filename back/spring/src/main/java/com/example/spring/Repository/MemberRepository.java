package com.example.spring.Repository;

import com.example.spring.DTO.Member;
import com.example.spring.DTO.Profile;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByUserId(String userId);
    List<Member> findAll();

    Profile setOrUpdateProfile(Profile profile, boolean exist);
    Optional<Profile> findProfileByUserId(String userId);
    Optional<Profile> findProfileByNickname(String nickname);

    Optional<Profile> findProfileById(Long id);
    Optional<Profile> findUserIdByNickname(String nickname);

    boolean delete(Member member);
    boolean changePw(Long id, String newPw);
}
