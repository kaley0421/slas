package kr.sofaware.slas.repository;

import kr.sofaware.slas.entity.Lecture;
<<<<<<< HEAD
import kr.sofaware.slas.entity.LecturePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.thymeleaf.standard.expression.GreaterOrEqualToExpression;
import java.util.Optional;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, LecturePK> {
    List<Lecture> findByStudent_Id(String id);      //student_id 로 lecture 불러오기

    @Query("SELECT l FROM Lecture l WHERE l.student.id = :id AND l.syllabus.id LIKE :yearSemester%")
    List<Lecture> findByIdAndYearSemester(String id, String yearSemester);

    List<Lecture> findAllByStudent_Id(@Param(value = "memberId") String id);
}
