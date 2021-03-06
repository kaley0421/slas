package kr.sofaware.slas.mainpage.controller;

import kr.sofaware.slas.entity.Assignment;
import kr.sofaware.slas.entity.Member;
import kr.sofaware.slas.entity.Syllabus;
import kr.sofaware.slas.mainpage.dto.*;
import kr.sofaware.slas.service.AssignmentService;
import kr.sofaware.slas.service.MemberService;
import kr.sofaware.slas.service.SyllabusService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@RequiredArgsConstructor
@Controller
@RequestMapping("/p")
public class ProfessorMainPageController {

    private final SyllabusService syllabusService;
    private final MemberService memberService;
    private final AssignmentService assignmentService;

    //교수 메인페이지
    @GetMapping("main")
    public String professorMain(Model model, @RequestParam("year-semester") @Nullable String yearSemester) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();       //현재 로그인한 사용자의 id 받아오기
        UserDetails user = (UserDetails) principal;
        String username=((UserDetails) principal).getUsername();

        Member professor=memberService.loadUserByUsername(username);                                   //현재 로그인한 학생 찾기

        //교수가 강의한 강의들 조회
        Map<String, List<Syllabus>> lectures = syllabusService.mapAllByProfessorId(username);

        //학기 선택이 없는 요청이면 제일 최근 학기 시간표
        if(yearSemester == null) {
            ArrayList<String> yearSemesters = new ArrayList<>(lectures.keySet()); //모든 key 값
            // 이 사람이 강의한 수업이 없을 경우 그냥 리턴
            if (yearSemesters.isEmpty())
                return "mainpage/professor-mainpage";

            yearSemester = yearSemesters.get(0); //reverseOrder이기 때문에 get(0)이 가장 최근 학기
        }

        //학기 선택 리스트
        Map<String, String> formatYS = new TreeMap<>(Collections.reverseOrder());
        lectures.keySet().forEach(s -> formatYS.put(s, Syllabus.formatYearSemester(s)));

        model.addAttribute("mapYS", formatYS);
        model.addAttribute("yearSemester", Syllabus.formatYearSemester(yearSemester));

        //년도, 학기를 바탕으로 이 교수가 해당 학기에 수업한 Syllabus 들의 리스트를 view 에 전달하기 위한 syllabusDto 로 변환
        List<SyllabusDtoForProf> syllabusDtoList = new ArrayList<>();
        for(Syllabus s : lectures.get(yearSemester)){
            syllabusDtoList.add(new SyllabusDtoForProf(s));
        }

        List<SyllabusDto> listForCreatingCellDtoList=new ArrayList<>();                                        // List<SyllabusDto> 에 List<SyllabusDtoForProf> 를 넣어줌.. 둘은 상속 관계.. 이 방법밖에 없나.. 마음에 안 드렁 ㅠㅠㅠ 왜 바로 안 들어가는 거 ㅜㅜ
        for(SyllabusDtoForProf s : syllabusDtoList)
            listForCreatingCellDtoList.add(s);

        List<ArrayList<CellDto>> cellDtoList=CellDto.createCellDtoList(listForCreatingCellDtoList);            // 시간표 출력 위한 cellDtoList 생성

        // ↓ ↓ ↓  syllabusDtoList 의 각각의 syllabus 들의 assignmentDtoList 에 아직 마감기한 지나지 않은 과제들을 제출 마감일 빠른 순으로 출력 => 최대 얼만큼까지 출력해줄지는 프론트에서 처리
        for(SyllabusDtoForProf s : syllabusDtoList) {
            List<Assignment> assignmentList = assignmentService.findBySyllabus_IdSubmitEndAfterOrderBySubmitEndAsc(s.getId(),new Date());

            List<AssignmentDto> assignmentDtoList=new ArrayList<>();                                        // dto 로 변환해서 syllabusDto 내부에 set
            assignmentList.forEach(assignment -> assignmentDtoList.add(new AssignmentDto(assignment)));
            s.setAssignmentDtoList(assignmentDtoList);
        }

        model.addAttribute("MainPageDto", ProfessorMainPageDto.builder()                       // ProfessorMainPageDto 를 view 에 전달
                                                                        .id(username)
                                                                        .name(professor.getName())
                                                                        .noLectures(syllabusDtoList.isEmpty())
                                                                        .syllabusDtoList(syllabusDtoList)
                                                                        .cellDtoList(cellDtoList)
                                                                        .build());

        return "mainpage/professor-mainpage";
    }
}
