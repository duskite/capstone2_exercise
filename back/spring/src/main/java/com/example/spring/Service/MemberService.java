package com.example.spring.Service;

import com.example.spring.DTO.Member;
import com.example.spring.DTO.OpenMatchDTO;
import com.example.spring.DTO.Profile;
import com.example.spring.Repository.MemberRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Transactional
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String hashingPw(String plainPw, String salt){
        String hashedPw = null;
        hashedPw = BCrypt.hashpw(plainPw, salt);

        return hashedPw;
    }

    public Profile setOrUpdateProfile(Profile profile){

        try{
            Optional<Profile> tmpProfile = memberRepository.findProfileByUserId(profile.getUserId());
            profile.setIdx(tmpProfile.get().getIdx());
        }catch (Exception e){
            //조회가 안되면 프로필을 새로 생성하는 유저
            System.out.println("오류: " + e.getMessage());
            return memberRepository.setOrUpdateProfile(profile, false);
        }
        //기존 프로필을 업데이트 하는 유저
        return memberRepository.setOrUpdateProfile(profile, true);
    }

    public Profile findProfileByNickname(String nickname){
        return memberRepository.findProfileByNickname(nickname).get();
    }

    public String findUserIdByNickname(String nickName){
        Profile profile = memberRepository.findUserIdByNickname(nickName).get();
        return profile.getUserId();
    }


    public Profile findProfileByUserId(String userId){
        return memberRepository.findProfileByUserId(userId).get();
    }

    /**
     * 회원가입
     * @param member
     * @return
     */
    public Long join(Member member){


        //비밀번호 해싱한후 디비에 넣음
        String salt = BCrypt.gensalt();
        member.setUserPw(hashingPw(member.getUserPw(), salt));
        member.setSalt(salt);

        //중복회원 있을 경우 사용자에게 리턴해서 알려줘야함
        try{
            validateDuplicateMember(member); //중복 회원 검증
            memberRepository.save(member);
            return member.getId();
        }catch (Exception e){
            System.out.println(e.getMessage());
            //중복회원 있을때
            return -1L;
        }
    }

    public boolean changePw(String userId, String newPw){
        Optional<Member> member = memberRepository.findByUserId(userId);
        if(member.isPresent()){
            String salt = member.get().getSalt();
            String newPwHashed = hashingPw(newPw, salt);

            return memberRepository.changePw(member.get().getId(), newPwHashed);

        } else {
            return false;
        }
    }

    public boolean isExistNickname(Profile profile){

        Optional<Profile> requestProfile = memberRepository.findProfileByUserId(profile.getUserId());
        Optional<Profile> optionalProfile = memberRepository.findProfileByNickname(profile.getNickname());
        //처음 생성하는 유저는 requestProfile이 조회되지 않음
        //이미 있던 유저가 프로필 업데이트 하는 과정
        if(requestProfile.isPresent()){
            if(optionalProfile.isPresent()){
                //이미 있는 닉네임이 내꺼인지 체크
                if(requestProfile.get().getIdx() == optionalProfile.get().getIdx()){
                    //내꺼면 그대로 닉네임 설정 가능하도록 함
                    return false;
                }
                //내가 쓰던 닉네임이 아니면 못바꿈
                return true;
            }else {
                return false;
            }
        }else {
            //처음 생성하는 유저가 타는 부분
            //그런데 이미 있는 닉네임이면 생성 불가
            if(optionalProfile.isPresent()){
                return true;
            }else {
                return false;
            }
        }
    }

    public boolean isMember(Member member){

        try{
            // 해당 아이디가 db에 있는지 확인
            // 조회가 안되면 예외 던짐
            Member memberInDB = memberRepository.findByUserId(member.getUserId()).get();
            System.out.println(memberInDB.getUserId());
            System.out.println("솔트:" + memberInDB.getSalt());

            String hashedPw = hashingPw(member.getUserPw(), memberInDB.getSalt());

            //비밀번호 체크 테스트
            System.out.println(hashedPw);
            System.out.println(memberInDB.getUserPw());

            // 아이디가 있다면 db에 저장된 비밀번호와 동일한지 확인
            // 디비에서 솔트 가져와서 사용자가 입력한 비번 해싱하고 디비에 있는 값과 비교함
            if(hashedPw.equals(memberInDB.getUserPw())){
                System.out.println("비밀번호 일치");
                return true;
            }

            System.out.println("비밀번호 불일치");
            return false;

        //조회가 안될때. 즉 회원이 아닐때
        }catch (NoSuchElementException e){
            return false;
        }
    }

    private void validateDuplicateMember(Member member) {
        //같은 이름이 있는 중복 회원x
        //Optional 반환되는거라 바로 .ifPresent 썼음
        memberRepository.findByUserId(member.getUserId())
                .ifPresent(m ->{
                    throw new IllegalStateException("이미 존재하는 회원입니다.");
                });
    }

    //이미 디비에 생성된 프로필이 있는지 체크
    private void validateDuplicateProfile(Profile profile) {
        memberRepository.findProfileByUserId(profile.getUserId())
                .ifPresent(m ->{
                    throw new IllegalStateException("이미 프로필이 존재하는 회원, 업데이트로 처리");
                });
    }

    /**
     * 전체 회원 조회
     * @return
     */
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    public Optional<Member> findOne(Long memberId){
        return memberRepository.findById(memberId);
    }
    public Optional<Member> findOne(String userId){return memberRepository.findByUserId(userId);}

    public boolean delete(Long id){
        Optional<Member> optionalMember = memberRepository.findById(id);
        if(optionalMember.isPresent()) {
            return memberRepository.delete(optionalMember.get());
        }
        return false;
    }

}
