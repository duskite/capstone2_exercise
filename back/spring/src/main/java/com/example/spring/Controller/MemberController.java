package com.example.spring.Controller;

import com.example.spring.DTO.AuthDTO;
import com.example.spring.DTO.Member;
import com.example.spring.DTO.Profile;
import com.example.spring.Service.MemberService;
import com.example.spring.auth.ApiResponse;
import com.example.spring.auth.AuthService;
import com.example.spring.auth.ResponseMap;
import lombok.experimental.PackagePrivate;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.Multipart;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;

    @Autowired
    public MemberController(MemberService memberService, AuthService authService) {
        this.memberService = memberService;
        this.authService = authService;
    }

    //회원 가입
    @PostMapping("/signUp")
    public boolean signUp(@RequestBody Member member){
        Member newMember = new Member();
        newMember.setUserId(member.getUserId());
        newMember.setUserPw(member.getUserPw());
        newMember.setEmail(member.getEmail());

        try{
            long checkDuplicate = memberService.join(newMember);
            if(checkDuplicate == -1L){
                return false;
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

    //유저 삭제
    @DeleteMapping("/delete/{userIdx}")
    public boolean delete(@PathVariable Long userIdx){
        return memberService.delete(userIdx);
    }

    //비밀번호 변경
    @PostMapping("/changePw/{userId}/{newPw}")
    public boolean changePw(@PathVariable String userId, @PathVariable String newPw){
       return memberService.changePw(userId, newPw);
    }

    //프로필 생성, 수정
    @PostMapping("/setMyProfile")
    public boolean setMyProfile(@RequestBody Profile profile,
                                @RequestHeader Map<String, String> header){

        System.out.println("RequestBody:" + profile.getUserId());
        System.out.println("RequestBody:" + profile.getNickname());
        System.out.println("헤더:" + header.get("token"));

        // 내부적으로 검색 위해서 토큰 날아오는거 앞에 bearer 뗌
//        String[] result = header.get("token").toString().split(" ");
//        System.out.println("검색하려는 토큰: " + result[1]);

        //현재 프로필 수정으로 요청한 유저
        String requestUser = authService.findUserByToken(header.get("token"));
        //프로필 수정을 당하는 유저
        String willChangeUser = profile.getUserId();

        System.out.println("요청 유저: " + requestUser + ", 수정 유저: " + willChangeUser);
        //자기꺼만 바꿀수 있음
        if(requestUser.equals(willChangeUser)){

            //이미 동일한 닉네임 유저 있으면 프로필 생성 또는 업뎃 안됨
            //단 동일한 유저가 요청하는 경우에는 가능(닉네임 이외에 다른것만 변경하는 유저들)
            if(memberService.isExistNickname(profile)){
                return false;
            }

            memberService.setOrUpdateProfile(profile);
            System.out.println("프로필 업데이트 됨");

            return true;
        }
        System.out.println("프로필 업데이트 실패");
        return false;
    }

    //프로필 조회
    @GetMapping("/getUserProfile/{userId}")
    public Profile getUserProfile(@PathVariable String userId){
        return memberService.findProfileByUserId(userId);
    }

    //닉네임으로 프로필 이미지 조회
    @GetMapping("/getUserProfileImgByNickName/{nickName}")
    public String getUserProfileImgByNickName(@PathVariable String nickName){
        Profile profile = memberService.findProfileByNickname(nickName);
        String path = profile.getImage();
        return path;
    }



    //유저 인덱스 조회
    @GetMapping("/getUserIndexByUserId/{userId}")
    public Long getUserIndexByUserId(@PathVariable String userId){

        Long userIdx;

        Optional<Member> member = memberService.findOne(userId);
        if(member.isPresent()){
            userIdx = member.get().getId();
            return userIdx;
        }else {
            return -1l;
        }
    }


    //프로필 이미지 업로드
    @PostMapping("/upload/image")
    public ApiResponse uploadsProfileImg(@RequestParam(name="image") MultipartFile image) throws IOException {

        String fileName = UUID.randomUUID().toString();
        String absolutePath = new File("/home/ec2-user/downloads/img").getAbsolutePath()
                + "/" + fileName + ".jpg";

//        String absolutePath = new File("/Users/duskite/Downloads/img").getAbsolutePath()
//                + "/" + fileName + ".jpg";

        System.out.println(absolutePath);

        ResponseMap result = new ResponseMap();

        if(!image.isEmpty()){
            File file = new File(absolutePath);
            if(!file.exists()){
                file.mkdirs();
            }

            image.transferTo(file);
            //디비에는 이지미가 저장된 로컬 경로로 포워딩 해주는 경로로 저장
            result.setResponseData("image", "/imagePath/" + fileName + ".jpg");

            System.out.println("이미지 업로드 완료");
        }
        return result;
    }

}
