package kr.sofaware.slas.service;

import kr.sofaware.slas.entity.Board;
import kr.sofaware.slas.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LectureFileService implements BoardService{
    private final BoardRepository boardRepository;

    @Override
    public void create(Board board) {
        boardRepository.save(board);
    }

    @Override
    public Optional<Board> read(int boardId) {
        return boardRepository.findById(boardId);
    }

    @Override
    public void update(Board board) {

    }

    @Override
    public void delete(int boardId) {

    }

    @Override
    public List<Board> listAll() {
        return boardRepository.findAll();
    }

    @Override
    public List<Board> listAll(String SyllabusId) {
        return boardRepository.findAllByCategoryAndSyllabus_Id(Board.CATEGORY_NOTICE, SyllabusId);
    }

    @Override
    public void increaseViewCount(int boardId, HttpSession session) {

    }

    /**
     * 해당 과목의 강의 자료가 몇개 올라와 있는지 count ==> 뭔가 LectureFileService 로 일단 만들긴 했는데.. ㅎㅎ.. 나중에 다른데로 옮기던징...
     * @author 정지민
     * @param SyllabusId
     * @return 강의 자료들의 개수
     */
    public int countLectureFiles(String SyllabusId){
        return boardRepository.countAllByCategoryAndSyllabus_Id(Board.CATEGORY_FILES, SyllabusId);
    }
}
