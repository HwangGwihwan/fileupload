package com.example.fileupload.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;

import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;

import jakarta.transaction.Transactional;

public interface BoardRepository extends JpaRepository<Board, Integer>{
	Page<BoardMapping> findAllBy(PageRequest pageable);
	BoardMapping findByBno(int bno);
	Board findByBnoAndPw(int bno, String pw);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true
		   , value = "update board set title =:title"
		  		+ " where bno =:bno and pw =:pw")
	int modifyBoard(int bno, String title, String pw);
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true
		   , value = "delete from board"
		   		+ " where bno =:bno and pw=:pw")
	int deleteBoard(int bno, String pw);
}
