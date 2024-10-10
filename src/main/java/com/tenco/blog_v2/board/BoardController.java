package com.tenco.blog_v2.board;

import com.tenco.blog_v2.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller // IoC
public class BoardController {

    // DI
    // @Autowired
    // 네이티브 쿼리 연습
    private final BoardNativeRepository boardNativeRepository;

    // JPA API, JPQL
    private final BoardRepository boardRepository;

    private final HttpSession session;


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
        // 1. 게시글 조회
        // Board board = boardNativeRepository.findById(id);
        Board board = boardRepository.findById(id);
        // 2. 요청 속성에 조회한 게시글 속성 및 값 추가
        request.setAttribute("board", board);
        // 뷰 리졸브 - 템플릿 반환
        return "board/update-form"; // src/main/resources/templates/board/update-form.xxx
    }

    /**
     * 게시글 수정 기능
     * @param id
     * @param reqDTO
     * @return
     */
    // board/{id}/update
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable(name = "id") Integer id, @ModelAttribute BoardDTO.UpdateDTO reqDto) {

        // 1. 데이터 바인딩 방식 수정

        // 2. 인증 검사 - 로그은 여부 판단
        User sessionUser = (User) session.getAttribute("sessionUser");
        if(sessionUser == null) {
            return "redirect:/login-form";
        }
        // 3. 권한 체크 - 내 글이 맞니?
        Board board = boardRepository.findById(id);
        if(board == null) {
            return "redirect:/";  // 게시글이 없다면 에러 페이지 추후 수정
        }
        if (!board.getUser().getId().equals(sessionUser.getId())) {
            return "redirect:/error-403"; // 권한이 없습니다. 추후 수정
        }
        // 4. 유효성 검사 - 생략

        // 5. 서비스 측 위임 (직접 구현) - 레파지토리 사용
        boardRepository.updateByIdJPA(id, reqDto.getTitle(), reqDto.getContent());

        // 6. 리다이렉트 처리

        return "redirect:/board/" + id;
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
    public String delete(@PathVariable(name = "id") Integer id, HttpSession session) {
        // 유효성, 인증 검사
        // 세션에서 로그인 사용자 정보 가져오기 -> 인증(로그인 여부), 인가(권한 - 내 글인지 아닌지)
        User sessionUser = (User) session.getAttribute("sessionUser");

        if (sessionUser == null) {
            return "redirect:/login-form";
        }

        // 인가(권한) 체크
        Board board = boardRepository.findById(id);
        if (board == null) {
            return "redirect:/error-404";
        }

        if (!board.getUser().getId().equals(sessionUser.getId())) {
            return "redirect:/error-403";
        }

        // 게시글 삭제
        // boardRepository.deleteByIdJPA(id); --> JPA API
        boardRepository.deleteById(id); // --> JPQL
        // boardNativeRepository.deleteById(id); --> 네이티브
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
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Integer id, HttpServletRequest request) {
        // JPA API 사용
        // Board board = boardRepository.findById(id);

        // JPQL FETCH join 사용
        Board board = boardRepository.findByIdJoinUser(id);
        request.setAttribute("board", board);
        return "board/detail";
    }


    /**
     * index 화면
     *
     * @param model
     * @return
     */
    @GetMapping("/")
    public String index(Model model) {
        // List<Board> boardList = boardNativeRepository.findAll();
        // 코드 수정
        List<Board> boardList = boardRepository.findAll();
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
    public String save(@ModelAttribute BoardDTO.saveDTO reqDTO) {
        User sessionUser = (User) session.getAttribute("sessionUser");

        if (sessionUser == null) {
            return "redirect:/login-form";
        }

        // 파라미터가 올바르게 전달 되었는지 확인
        log.warn("save 실행 : 제목={}, 내용={}", reqDTO.getTitle(), reqDTO.getContent());
        // boardNativeRepository.save(title, content);

        // saveDTO에서 toEntity를 사용해서 Board 엔티티로 변환하고 인수값으로 User 정보를 넣었다.
        // 결국 Board 엔티티로 반환이 된다.
        boardRepository.save(reqDTO.toEntity(sessionUser));
        return "redirect:/";
    }

}
