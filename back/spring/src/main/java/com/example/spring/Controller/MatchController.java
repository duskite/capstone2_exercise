package com.example.spring.Controller;


import com.example.spring.DTO.MatchingDTO;
import com.example.spring.DTO.Member;
import com.example.spring.DTO.OpenMatchDTO;
import com.example.spring.DTO.Profile;
import com.example.spring.Service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match")
public class MatchController {
    /*
        매칭 생성, 삭제, 참가 등의 처리를 지원하는 컨트롤러
    */

    private final MatchService matchService;

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    //매칭 생성
    @PostMapping("/openMatch")
    public OpenMatchDTO openMatch(@RequestBody OpenMatchDTO openMatchDTO){

        return matchService.create(openMatchDTO);
    }

    //매칭 수정
    @PostMapping("/update")
    public OpenMatchDTO updateOpenMatch(@RequestBody OpenMatchDTO openMatchDTO){
        return matchService.update(openMatchDTO);
    }

    //매칭 삭제
    @DeleteMapping("/delete/{openMatchIdx}")
    public boolean delete(@PathVariable Long openMatchIdx){
        return matchService.delete(openMatchIdx);
    }



    //매칭 참가, 사용자id와 오픈매치id를 가져와서 매칭db에 생성함
    @PostMapping("/joinMatch")
    public Long joinMatch(@RequestBody MatchingDTO matchingDTO){
        return matchService.joinMatch(matchingDTO);
    }
    //매칭 탈퇴, 매칭 떠나기
    @DeleteMapping("/leaveMatch/{matchingIdx}")
    public boolean leaveMatch(@PathVariable Long matchingIdx){
        return matchService.leaveMatch(matchingIdx);
    }


    //오픈 매치 리스트
    @GetMapping("/openMatchList")
    public List<OpenMatchDTO> matchList(){
        return matchService.findAll();
    }
    //오픈 매치 리스트 필터(종목으로)
    @GetMapping("/openMatchList/{sportType}")
    public List<OpenMatchDTO> matchList(@PathVariable String sportType){
        return matchService.findAll(sportType);
    }


    //유저가 참여중인 오픈매치 리스트
    //여기서는 실제 참여중인 오픈매치 리스트를 리턴하면 될 듯
    @GetMapping("/joinedOpenMatch/{userIdx}")
    public List<OpenMatchDTO> matchingList(@PathVariable Long userIdx){
        return matchService.findAllOpenMatchListByUserId(userIdx);
    }

    //내가 개설한 오픈매치 리스트 가져오기
    @GetMapping("/openMatchByMe/{userIdx}")
    public List<OpenMatchDTO> openMatchByMe(@PathVariable Long userIdx){
        return matchService.findAllOpenMatchListByMe(userIdx);
    }

    //현재 오픈매치에 참여중인 모든 유저 프로필 정보 가져오기
    @GetMapping("/joinedUserProfiles/{openMatchIdx}")
    public List<Profile> getJoinedUserProfiles(@PathVariable Long openMatchIdx){
        return matchService.getJoinedUserProfiles(openMatchIdx);
    }


}
