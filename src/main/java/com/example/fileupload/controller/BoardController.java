package com.example.fileupload.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.fileupload.dto.BoardForm;
import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;
import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardRepository;
import com.example.fileupload.repository.BoardfileRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class BoardController {
	@Autowired
	BoardRepository boardRepository;
	@Autowired
	BoardfileRepository boardfileRepository;

	// 입력폼
	@GetMapping("/addBoard")
	public String addBoard() {
		return "addBoard";
	}
	
	// 입력액션
	@PostMapping("/addBoard")
	@Transactional
	public String addBoard(BoardForm boardForm) {
		log.debug(boardForm.toString());
		// 파일을 첨부하지 않아도 fileSize 는 1이다
		log.debug("Size" + boardForm.getFileList().size());
		
		Board board = new Board();
		board.setTitle(boardForm.getTitle());
		board.setPw(boardForm.getPw());
		boardRepository.save(board); // board 저장
		int bno = board.getBno(); // board insert 후 bno 변경되었는지
		log.debug("bno: " + bno);
		
		// 파일 분리
		List<MultipartFile> fileList = boardForm.getFileList();
		long firstFileSize = fileList.get(0).getSize();
		log.debug("firstFileSize: " + firstFileSize);
		
		// 파일을 첨부하지 않아도 fileSize 는 1이다
		if (firstFileSize > 0) { // 첫번째 파일사이즈가 0이상이다 -> 첨부된 파일이 있다
			// 업로드 파일 유효성 검증 코드 구현
			for (MultipartFile f : fileList) {
				if (f.getContentType().equals("application/octet-stream")) {
					log.debug("실행파일 업로드 불가");
					return "redirect:/addBoard"; // Msg추가
				}
			}
			
			// 파일 업로드 진행 코드
			for (MultipartFile f : fileList) {
				log.debug("파일타입: " + f.getContentType());
				log.debug("원본이름: " + f.getOriginalFilename());
				log.debug("파일용량: " + f.getSize());
				// 확장자 추출
				String ext = f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf(".") + 1);
				log.debug("확장자: " + ext);
				
				String saveName = UUID.randomUUID().toString().replace("-", "");
				log.debug("저장파일이름: " + saveName);
				
				File emptyFile = new File("c:/project/upload/" + saveName + "." + ext);
				
				// f의 byte -> emptyFile 복사
				try {
					f.transferTo(emptyFile);
				} catch (IllegalStateException | IOException e) {
					log.error("파일저장실패!");
					e.printStackTrace();
				}
				
				// boardfile테이블에도 파일정보 저정
				Boardfile boardfile = new Boardfile();
				boardfile.setBno(board.getBno());
				boardfile.setFext(ext);
				boardfile.setFname(saveName);
				boardfile.setForiginname(f.getOriginalFilename());
				boardfile.setFsize(f.getSize());
				boardfile.setFtype(f.getContentType());
				boardfileRepository.save(boardfile);
			}
		}
		
		return "redirect:/boardList";
	}
	
	@GetMapping("/boardList")
	public String boardList(Model model
						, @RequestParam(defaultValue = "0") int currentPage
						, @RequestParam(defaultValue = "8") int rowPerPage) {
		
		Sort sort = Sort.by("bno").descending();
		PageRequest pageable = PageRequest.of(currentPage, rowPerPage, sort);
		Page<BoardMapping> list = boardRepository.findAllBy(pageable);
		
		model.addAttribute("list", list);
		model.addAttribute("prePage", list.getNumber()-1);
		model.addAttribute("nextPage", list.getNumber()+1);
		return "boardList";
	}
	
	@GetMapping("/boardOne")
	public String boardOne(Model model, @RequestParam int bno) {
		BoardMapping board = boardRepository.findByBno(bno);

		List<Boardfile> fileList = boardfileRepository.findByBno(bno);
		log.debug("size:" + fileList.size());
		model.addAttribute("board", board);
		model.addAttribute("fileList", fileList);
		return "boardOne";
	}
	
	@GetMapping("/modifyBoard")
	public String modifyBoard(Model model, @RequestParam int bno) {
		BoardMapping board = boardRepository.findByBno(bno);
		model.addAttribute("board", board);
		return "modifyBoard";
	}
	
	@PostMapping("/modifyBoard")
	public String modifyBoard(BoardForm boardform, @RequestParam int bno
							, RedirectAttributes rda) {
		int row = boardRepository.modifyBoard(bno, boardform.getTitle(), boardform.getPw());
		
		if (row == 0) { // 수정실패
			rda.addFlashAttribute("msg", "비밀번호가 일치하지 않습니다");
			return "redirect:/modifyBoard?bno=" + bno;
		}	
		return "redirect:/boardOne?bno=" + bno;
	}
	
	@GetMapping("/removeBoard")
	public String removeBoard(Model model, @RequestParam int bno
							, RedirectAttributes rda) {
		List<Boardfile> boardfile = boardfileRepository.findByBno(bno);
		if (boardfile.size() > 0) { // 파일이 있으므로 삭제 불가
			rda.addFlashAttribute("msg", "첨부파일을 먼저 삭제해주세요");
			return "redirect:/boardOne?bno=" + bno;
		}
		
		BoardMapping board = boardRepository.findByBno(bno);
		model.addAttribute("board", board);
		return "removeBoard";
	}
	
	@PostMapping("/removeBoard")
	public String removeBoard(BoardForm boardform, @RequestParam int bno
							, RedirectAttributes rda) {
				
		Board board = boardRepository.findByBnoAndPw(bno, boardform.getPw());
		
		if (board == null) { // 삭제실패
			rda.addFlashAttribute("msg", "비밀번호가 일치하지 않습니다");
			return "redirect:/removeBoard?bno=" + bno;
		}
		
		boardfileRepository.deleteByBno(bno);
		boardRepository.delete(board);
		return "redirect:/boardList";
	}
}
