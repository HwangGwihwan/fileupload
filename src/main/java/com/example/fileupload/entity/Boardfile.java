package com.example.fileupload.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data

@Entity
@Table(name="boardfile")
public class Boardfile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int fno;
	
	@Column(name="ftype")
	private String ftype;
	
	@Column(name="foriginname")
	private String foriginname;
	
	@Column(name="fname")
	private String fname;
	
	@Column(name="fext")
	private String fext;
	
	@Column(name="fsize")
	private long fsize;

	@Column(name="bno")
	private int bno;
	
	// 자식에서 부모로 단방향 관계설정 O
	// 불필요한 연관 관계는 조인등으로 인해 시스템 부하를 가져옴 -> 조인이 필요한 비지니스로직이 있을때만 연관 관계를 설정
//	@ManyToOne // 관계설정
//	@JoinColumn(name="bno") // FK
//	private Board board;
}
