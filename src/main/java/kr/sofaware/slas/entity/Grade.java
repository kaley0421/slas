package kr.sofaware.slas.entity;

import lombok.Getter;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
public class Grade implements Serializable {
    @Id
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;            // 학생

    @Id
    @ManyToOne
    @JoinColumn(name = "syllabus_id")
    private Syllabus syllabus;          // 강의

    private int ranking;                // 석차
    private double gradeAvg;            // 전체 평점
    private double majorGradeAvg;       // 전공 학점
    private int totalCredit;            // 신청 학점

    @Override
    public int hashCode() {
        return Objects.hash(student, syllabus);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || Hibernate.getClass(this) != Hibernate.getClass(obj)) return false;

        Grade o = (Grade) obj;
        return Objects.equals(student, o.getStudent()) &&
                Objects.equals(syllabus, o.getSyllabus());
    }
}