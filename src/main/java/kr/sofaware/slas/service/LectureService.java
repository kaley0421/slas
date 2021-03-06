package kr.sofaware.slas.service;

import kr.sofaware.slas.entity.Lecture;
import kr.sofaware.slas.entity.Syllabus;
import kr.sofaware.slas.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;

    /**
     * 학생 학번으로 들었던 수업들 '<년도>-<학기>' 형식으로 분류 후 HashMap 으로 반환해줌
     * @author 양경호
     * @param studentId 학번
     * @return {21-2=[{기업과경영}, {분석화학}, {생화학}, {열전달}, {유기화학}], 21-1=[{경영전략론}, {마케팅원론}]}
     */
    public Map<String, List<Syllabus>> mapAllByStudentId(String studentId) {
        Map<String, List<Syllabus>> map = new TreeMap<>(Collections.reverseOrder());

        lectureRepository.findAllByStudent_Id(studentId).forEach(lecture -> {
            String yearSemester = lecture.getSyllabus().getId().substring(0, 4);

            if (!map.containsKey(yearSemester))
                map.put(yearSemester, new ArrayList<>());

            map.get(yearSemester).add(lecture.getSyllabus());
        });

        // 강의명으로 정렬해서 반환
        map.forEach((s, syllabi) -> syllabi.sort(Comparator.comparing(Syllabus::getName)));
        return map;
    }

    /**
     * @author 김수헌
     */
    public List<Lecture> findAllByStudentId(String studentId){
        return lectureRepository.findAllByStudent_Id(studentId);
    }

    /**
     * 학생 학번으로 들었던 수업들 '<년도>-<학기>' 형식으로 분류 후 HashMap 으로 반환해줌
     * @author 김수헌
     * @param studentId
     * @return {21-2=[{기업과경영}, {분석화학}, {생화학}, {열전달}, {유기화학}], 21-1=[{경영전략론}, {마케팅원론}]}
     */
    public Map<String, List<Lecture>> mapLectureByStudentId(String studentId) {
        Map<String, List<Lecture>> map = new TreeMap<>(Collections.reverseOrder());

        lectureRepository.findAllByStudent_Id(studentId).forEach(lecture -> {
            String yearSemester = lecture.getSyllabus().getId().substring(0, 4);

            if (!map.containsKey(yearSemester))
                map.put(yearSemester, new ArrayList<>());

            map.get(yearSemester).add(lecture);
        });

        return map;
    }

    /**
     * @author 김수헌
     */
    public List<Lecture> findAllBySyllabusId(String syllabusId)
    {
        return lectureRepository.findAllBySyllabus_Id(syllabusId);
    }

    /**
     * @author 김수헌
     */
    @Transactional
    public List<Lecture> saveAllByPutMethod(List<Lecture> lectures)
    {
        return lectureRepository.saveAll(lectures);
    }

    /**
     * @author 김수헌
     */
    @Transactional
    public Lecture saveByPutMethod(Lecture lecture)
    {
        return lectureRepository.save(lecture);
    }

    /**
     * @author 김수헌
     */
    public Optional<Lecture> findByStudentIdAndSyllabusId(String studentId, String syllabusId)
    {
        return lectureRepository.findByStudent_IdAndSyllabus_Id(studentId, syllabusId);
    }

    /**
     * @author 김수헌
     */
    public Lecture deleteByStudentId(String studentId)
    {
        return lectureRepository.deleteByStudent_Id(studentId);
    }

    /**
     * 학생이 해당 수업을 듣는지 여부
     * @author 양경호
     * @param syllabusId 학정번호
     * @param studentId 학번
     * @return 내용의 참 거짓 값
     */
    public boolean existsBySyllabus_IdAndStudent_Id(String syllabusId, String studentId) {
        return lectureRepository.existsBySyllabus_IdAndStudent_Id(syllabusId, studentId);
    }

    /**
     * 학생이 듣는 강의 중 가장 처음 것 반환
     * @author 양경호
     * @param studentId 학번
     * @return 학생이 듣는 강의 중 가장 처음 것
     */
    public Optional<Syllabus> findFirstByStudent_IdOrderBySyllabusDesc(String studentId) {
        Optional<Lecture> oLecture = lectureRepository.findFirstByStudent_IdOrderBySyllabusDesc(studentId);
        return oLecture.isEmpty() ? Optional.empty() : Optional.of(oLecture.get().getSyllabus());
    }
}
