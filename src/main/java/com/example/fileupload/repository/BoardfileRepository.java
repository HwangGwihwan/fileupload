package com.example.fileupload.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fileupload.entity.Boardfile;

import jakarta.transaction.Transactional;

@Repository
public interface BoardfileRepository extends JpaRepository<Boardfile, Integer>{
	List<Boardfile> findByBno(int bno);
	
	// PK 한행삭제
	// void deleteById(int id);
	
	// FK 여러행 삭제(Board 삭제 시 같이 삭제 : transaction)
	@Transactional
	@Modifying
	@Query(nativeQuery = true
			   , value = "delete from boardfile where bno =:bno")
	void deleteByBno(int bno);
}
