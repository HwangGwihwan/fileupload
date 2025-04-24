package com.example.fileupload.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardfileRepository;

@Controller
public class BoardfileController {
	@Autowired
	BoardfileRepository boardfileRepository;
	
	@GetMapping("/removeBoardfile")
	public String removeBoardfile(@RequestParam int fno
								 ,@RequestParam int bno) {
		Boardfile boardfile = boardfileRepository.findById(fno).orElse(null);
		
		// 파일 삭제 후
		File f = new File("c:/project/upload/" + boardfile.getFname() + "." + boardfile.getFext());
		if (f.exists()) {
			f.delete();	
		}
		
		// 테이블 Row 삭제
		boardfileRepository.deleteById(fno);
		return "redirect:/boardOne?bno=" + bno;
	}
}
