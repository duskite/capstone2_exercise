package com.example.spring.Controller;


import com.example.spring.DTO.AuthDTO;
import com.example.spring.DTO.Member;
import com.example.spring.Service.MemberService;
import com.example.spring.auth.ApiResponse;
import com.example.spring.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @Autowired
    public AuthController(AuthService authService, MemberService memberService){
        this.authService = authService;
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody AuthDTO.LoginDTO loginDTO){

        Member checkMember = new Member();
        checkMember.setUserId(loginDTO.getUserId());
        checkMember.setUserPw(loginDTO.getUserPw());

        //로그인 시도시 회원인지 아닌지 판단
        if(memberService.isMember(checkMember)){
            return authService.login(loginDTO);
        }else{
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(404);
            return apiResponse;
        }
    }

    @PostMapping("/refreshToken")
    public ApiResponse newAccessToken(@RequestBody AuthDTO.GetNewAccessTokenDTO getNewAccessTokenDTO, HttpServletRequest request){
        return authService.newAccessToken(getNewAccessTokenDTO, request);
    }

    //필터에서 토큰 유효성 검사하는데
    //여기 체크는 그냥 로그인 유지 시키려고
    @GetMapping("/tokenCheck")
    public boolean tokenCheck(){
        return true;
    }

}
