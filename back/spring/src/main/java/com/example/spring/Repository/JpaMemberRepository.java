package com.example.spring.Repository;

import com.example.spring.DTO.Member;
import com.example.spring.DTO.Profile;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class JpaMemberRepository implements MemberRepository {

    private final EntityManager em;
    public JpaMemberRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Member save(Member member) {
        em.persist(member);
        return member;
    }


    @Override
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    @Override
    public Optional<Member> findByUserId(String userId) {
        List<Member> result = em.createQuery("select m from Member m where m.userId = :userId", Member.class)
                .setParameter("userId", userId)
                .getResultList();

        return result.stream().findAny();
    }
    @Override
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    @Override
    public boolean changePw(Long id, String newPw){

        try{
            Member member = em.find(Member.class, id);
            member.setUserPw(newPw);
            em.flush();
        }catch (Exception e){
            return false;
        }
        return true;
    }

    @Override
    public Profile setOrUpdateProfile(Profile profile, boolean exist) {

        if(exist){
            Profile updateProfile = em.find(Profile.class, profile.getIdx());
            updateProfile.setNickname(profile.getNickname());
            updateProfile.setIntroduce(profile.getIntroduce());
            updateProfile.setImage(profile.getImage());
            updateProfile.setRegion(profile.getRegion());

            updateProfile.setFavMon(profile.isFavMon());
            updateProfile.setFavTue(profile.isFavTue());
            updateProfile.setFavWed(profile.isFavWed());
            updateProfile.setFavThu(profile.isFavThu());
            updateProfile.setFavFri(profile.isFavFri());
            updateProfile.setFavSat(profile.isFavSat());
            updateProfile.setFavSun(profile.isFavSun());

            updateProfile.setFavSoccer(profile.isFavSoccer());
            updateProfile.setFavFutsal(profile.isFavFutsal());
            updateProfile.setFavBaseball(profile.isFavBaseball());
            updateProfile.setFavBasketball(profile.isFavBasketball());
            updateProfile.setFavBadminton(profile.isFavBadminton());
            updateProfile.setFavCycle(profile.isFavCycle());
            em.flush();
        }else {
            em.persist(profile);
        }
        return profile;
    }

    @Override
    public Optional<Profile> findProfileByUserId(String userId) {
        List<Profile> result = em.createQuery("select m from Profile m where m.userId = :userId", Profile.class)
                .setParameter("userId", userId)
                .getResultList();
        return result.stream().findAny();
    }

    public Optional<Profile> findProfileById(Long id){
        Profile profile = em.find(Profile.class, id);
        return Optional.ofNullable(profile);
    }

    @Override
    public Optional<Profile> findUserIdByNickname(String nickname) {
        List<Profile> result = em.createQuery("select m from Profile m where m.nickname = :nickname", Profile.class)
                .setParameter("nickname", nickname)
                .getResultList();

        return result.stream().findAny();
    }

    @Override
    public Optional<Profile> findProfileByNickname(String nickname) {

        List<Profile> result = em.createQuery("select m from Profile m where m.nickname = :nickname", Profile.class)
                .setParameter("nickname", nickname)
                .getResultList();
        return result.stream().findAny();
    }

    @Override
    public boolean delete(Member member) {

        try{
            em.remove(member);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
