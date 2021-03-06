package kr.sofaware.slas.mainpage.controller;

//import kr.sofaware.slas.service.ProfessorService;
import kr.sofaware.slas.entity.Assignment;
import kr.sofaware.slas.entity.Member;
import kr.sofaware.slas.entity.Syllabus;
import kr.sofaware.slas.mainpage.dto.*;
import kr.sofaware.slas.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RequiredArgsConstructor
@Controller
@RequestMapping("/s")
public class StudentMainPageController {

        //private final AssignmentBoardService assignmentBoardService;
        private final AssignmentService assignmentService;
        private final LectureService lectureService;
        private final MemberService memberService;
        private final NoticeService noticeService;

        //학생 메인페이지
        @GetMapping("main")
        public String studentMain(Model model, @RequestParam("year-semester") @Nullable String yearSemester){
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();       //현재 로그인한 사용자의 id 받아오기
            UserDetails user = (UserDetails) principal;
            String username=((UserDetails) principal).getUsername();

            Member student=memberService.loadUserByUsername(username);                                   //현재 로그인한 학생 찾기

            // 사용자의 수강 학기와 과목들 조회
            Map<String, List<Syllabus>> lectures = lectureService.mapAllByStudentId(username);

            if (yearSemester == null) {
                ArrayList<String> yearSemesters = new ArrayList<>(lectures.keySet());
                // 이 사람이 들었던 수업이 없을 경우 그냥 리턴
                if (yearSemesters.isEmpty())
                    return "mainpage/student-mainpage";

                yearSemester = yearSemesters.get(0);
            }

            Map<String, String> formatYS = new TreeMap<>(Collections.reverseOrder());
            lectures.keySet().forEach(s -> formatYS.put(s, Syllabus.formatYearSemester(s)));
            // 학기 선택 리스트
            model.addAttribute("mapYS", formatYS);
            model.addAttribute("yearSemester", Syllabus.formatYearSemester(yearSemester));

            List<SyllabusDtoForStu> syllabusDtoList = new ArrayList<>();                            //년도, 학기를 바탕으로 이 학생이 해당 학기에 들은 강의들의 syllabus 들을 받아와서 view 에 전달하기 위한 syllabusDto 들로 변환
            for(Syllabus s : lectures.get(yearSemester)){
                syllabusDtoList.add(new SyllabusDtoForStu(s));
            }

            List<SyllabusDto> listForCreatingCellDtoList=new ArrayList<>();                                        // List<SyllabusDto> 에 List<SyllabusDtoForStu> 를 넣어줌.. 둘은 상속 관계.. 이 방법밖에 없나.. 마음에 안 드렁 ㅠㅠㅠ 왜 바로 안 들어가는 거 ㅜㅜ
            for(SyllabusDtoForStu s : syllabusDtoList)
                listForCreatingCellDtoList.add(s);

            List<ArrayList<CellDto>> cellDtoList=CellDto.createCellDtoList(listForCreatingCellDtoList);            // 시간표 출력 위한 cellDtoList 생성

            // ↓ ↓ ↓ syllabusDtoList 의 각각의 syllabus 들의 noticeDtoList 에 최신 공지 3개 정도 add
            // board 테이블에서 ( 공지사항이고 && syllabus_id 는 syllabusDtoList.get(i).id ) 인 것들을 찾아서 등록일 빠른 순으로(datetime 내림차순) 정렬 => 위에서부터 레코드 3개만 가져오기 => noticeDtoList 로 결과 가져옴!!
            for(SyllabusDtoForStu s : syllabusDtoList) {
                s.setNoticeDtoList(noticeService.findFirst3ByCategoryAndSyllabus_IdOrderByDateDesc(s.getId()));
            }


            // ↓ ↓ ↓  syllabusDtoList 의 각각의 syllabus 들의 assignmentDtoList 에 아직 제출하지 않은 과제들을 제출 마감일 빠른 순으로 출력 => 최대 얼만큼까지 출력해줄지는 프론트에서 처리
            for(SyllabusDtoForStu s : syllabusDtoList) {
                List<Assignment> assignmentList = assignmentService.findBySyllabus_IdSubmitEndAfterOrderBySubmitEndAsc(s.getId(),new Date());

                for(Iterator<Assignment> iter = assignmentList.iterator(); iter.hasNext();){                      // 제출한 과제라면 목록에서 삭제
                    Assignment a=iter.next();
                    if(assignmentService.existsByAssignment_IdAndMember_Id(a.getId(),username))
                        iter.remove();
                }
                List<AssignmentDto> assignmentDtoList=new ArrayList<>();                                        // dto 로 변환해서 syllabusDto 내부에 set
                assignmentList.forEach(assignment -> assignmentDtoList.add(new AssignmentDto(assignment)));
                s.setAssignmentDtoList(assignmentDtoList);
            }


            // ↓ ↓ ↓  syllabusDtoList 의 각각의 syllabus 들의 videoLectureDtoList 에 아직 수강하지 않은 강의들을 마감일 빠른 순으로 (최대 5개? 3개? 까지) 출력
            // 이거는 근데 올라온 수업 강의 영상들을 시청했냐 안했냐 가 먼저 판단되야 해서 ***나중에*** 해야 할듯..
            // 근데 강의 시청했냐 아니냐를 어떻게 알지? 출석체크로 아나?? 출석->수강완료 / 지각->수강완료 / 결석->수강미완료 이렇게 처리?? 강의 몇퍼센트 시청했고 이런 것도 할 건가???!
            // 강의 영상 시청 여부를 board 의 칼럼으로 추가하는 방안은 ?!?!?!????!?

            model.addAttribute("MainPageDto", StudentMainPageDto.builder()                       // studentMainPageDto 를 view 에 전달
                                                                            .id(username)
                                                                            .name(student.getName())
                                                                            .noLectures(syllabusDtoList.isEmpty())
                                                                            .syllabusDtoList(syllabusDtoList)
                                                                            .cellDtoList(cellDtoList)
                                                                            .build());
            return "mainpage/student-mainpage";
        }
}
