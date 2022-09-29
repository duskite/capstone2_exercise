package com.example.spring.Service;

import com.example.spring.VO.Member;
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

//        //중복회원 있을 경우 사용자에게 리턴해서 알려줘야함
//        try{
//            validateDuplicateMember(member); //중복 회원 검증
//        }catch (Exception e){
//
//        }

        memberRepository.save(member);
        return member.getId();
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
        memberRepository.findById(member.getId())
                .ifPresent(m ->{
                    throw new IllegalStateException("이미 존재하는 회원입니다.");
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

}