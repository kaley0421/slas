package kr.sofaware.slas.grade.controller;

import kr.sofaware.slas.entity.Board;
import kr.sofaware.slas.entity.Lecture;
import kr.sofaware.slas.entity.Member;
import kr.sofaware.slas.entity.Syllabus;
import kr.sofaware.slas.service.LectureService;
import kr.sofaware.slas.service.SyllabusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.*;

@RequiredArgsConstructor
@Controller
@RequestMapping("/p")
@Log4j2
public class GradeProfessorController {

    private final SyllabusService syllabusService; //강의 정보 가져올 때 사용
    private final LectureService lectureService;

    @GetMapping("grade")
    public String getProfessorGrade(Authentication authentication, Principal principal, Model model,
                                    @Nullable @RequestParam("year-semester") String yearSemester,
                                    @Nullable @RequestParam("syllabus-id") String syllabusId)
    {
        String professorId = principal.getName();
        // 사용자의 수강 학기와 과목들 조회
        Map<String, List<Syllabus>> lectures = syllabusService.mapAllByProfessorId(professorId);

        // 학기, 과목 선택 없는 요청이면 제일 최근 학기와 사전 순 빠른 학정번호를 선택
        if (syllabusId == null) {
            if (yearSemester == null) {
                ArrayList<String> yearSemesters = new ArrayList<>(lectures.keySet());
                // 이 사람이 들었던 수업이 없을 경우 그냥 리턴
                if (yearSemesters.isEmpty())
                    return "grade/pGradeMain";

                yearSemester = yearSemesters.get(0);
            }

            // 첫번째 과목
            syllabusId = lectures.get(yearSemester).get(0).getId();
        } else if (yearSemester == null) {
            yearSemester = syllabusId.substring(0, 4);
        }

        Map<String, String> formatYS = new TreeMap<>(Collections.reverseOrder());
        lectures.keySet().forEach(s -> formatYS.put(s, Syllabus.formatYearSemester(s)));

        // 학기 선택 리스트
        model.addAttribute("mapYS", formatYS);
        model.addAttribute("yearSemester", Syllabus.formatYearSemester(yearSemester));

        // 강의 선택 리스트
        model.addAttribute("syllabuses", lectures.get(yearSemester));
        String finalSyllabusId = syllabusId;
        model.addAttribute("selectedSyllabusName",
                lectures.get(yearSemester).stream().filter(s -> s.getId().equals(finalSyllabusId))
                        .findAny().get().getName());


        //1. 해당 syllabusId에 대한 Lecture 정보 가져오기
        List<Lecture> lecturesBySyllabusId = lectureService.findAllBySyllabusId(syllabusId);
        model.addAttribute("lecturesBySyllabusId", lecturesBySyllabusId);
        model.addAttribute("syllabusId", syllabusId);

        return "grade/pGradeMain";
    }

    @PostMapping("grade")
    public String putProfessorGrade(
                                    @RequestParam("student-id") String studentId,
                                    @RequestParam("syllabusId") String syllabusId,
                                    @RequestParam("grade") double grade)
    {
        //교수님이 입력한 학점 반양
        //lectureService.saveByPutMethod(lecture);

        log.error("ID : " + studentId);
        log.error("GRADE : " + grade);

        Optional<Lecture> lecture = lectureService.findByStudentIdAndSyllabusId(studentId, syllabusId);
/*
        //밑 방법이 더 가독성이 좋아서 교체
        Lecture.LectureBuilder lectureBuilder = Lecture.builder()
                .student(lecture.get().getStudent())
                .syllabus(lecture.get().getSyllabus())
                .grade(grade);

        Lecture lectureForSave = lectureBuilder.build();
        lectureService.saveByPutMethod(lectureForSave);
*/

        lecture.ifPresent(selectLecture -> {
            selectLecture.setGrade(grade);
            lectureService.saveByPutMethod(selectLecture);
        });


        //위 방법이 더 update에 특화된 코드이기에 새로 만들어주고 삭제하는 작업은 구조상 맞지 않다고 판단.
        //Lecture newLecture = new Lecture(lecture.getStudent(), lecture.getSyllabus(), grade);
        //기존꺼 제거
        //lectureService.deleteByStudentId(studentId);
        //새로운 학점 추가
        //lectureService.saveByPutMethod(newLecture);

        return "redirect:grade";
    }
}


