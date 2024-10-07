package com.tenco.blog_v1.board;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller // IoC
public class BoardController {

    // DI
    // @Autowired
    private final BoardNativeRepository boardNativeRepository;

//    public BoardController(BoardNativeRepository boardNativeRepository) {
//        this.boardNativeRepository = boardNativeRepository;
//    }

    /**
     * index 화면
     *
     * @param model
     * @return
     */
    @GetMapping("/")
    public String index(Model model) {
        List<Board> boardList = boardNativeRepository.findAll();
        model.addAttribute("boardList", boardList);
        log.warn("여기까지 작동하나?");
        return "index";
    }

    /**
     * 게시글 작성 화면
     * 주소 설계 : http://localhost:8080/board/save-form
     *
     * @return
     */
    @GetMapping("board/save-form")
    public String saveForm() {
        return "board/save-form";
    }

    /**
     * 게시글 작성 기능
     *
     * @param title
     * @param content
     * @return
     */
    @PostMapping("board/save")
    public String save(@RequestParam(name = "title") String title, @RequestParam(name = "content") String content) {
        // 파라미터가 올바르게 전달 되었는지 확인
        log.warn("save 실행 : 제목={}, 내용={}", title, content);
        boardNativeRepository.save(title, content);
        return "redirect:/";
    }

    /**
     * 특정 게시글 요청 화면
     * 주소 설계 : http://localhost:8080/board/10
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("board/{id}")
    public String detail(@PathVariable(name = "id") Integer id, HttpServletRequest request) {
        Board board = boardNativeRepository.findById(id);
        request.setAttribute("board", board);
        return "board/detail";
    }

    /**
     * 게시글 삭제 기능
     * 주소 설계 : http://localhost:8080/board/10/delete (form 활용이기 때문에 동사로 delete 선언)
     * form 태그에서는 GET, POST 방식만 지원하기 때문 (JS로 PUT, DELETE 활용 가능)
     *
     * @param id
     * @return
     */
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable(name = "id") Integer id) {
        boardNativeRepository.deleteById(id);
        return "redirect:/";
    }

    /**
     * 게시글 수정 화면
     * board/id/update
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("board/{id}/update-form")
    public String updateForm(@PathVariable(name = "id") Integer id, HttpServletRequest request) {
        Board board = boardNativeRepository.findById(id);
        request.setAttribute("board", board);
        return "board/update-form"; // src/main/resources/templates/board/update-form.xxx
    }

    // 게시글 수정 기능
    @PostMapping("board/{id}/update")
    public String update(@PathVariable(name = "id") Integer id, @RequestParam(name = "title") String title, @RequestParam(name = "content") String content) {
        boardNativeRepository.updateById(id, title, content);
        return "redirect:/board/" + id;
    }

}
