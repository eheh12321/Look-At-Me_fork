package com.lookatme.server.likes.repository;

import com.lookatme.server.board.entity.Board;
import com.lookatme.server.likes.entity.Likes;
import com.lookatme.server.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {
    Likes findByBoardAndMember(Board board, Member member);

    List<Likes> findByMember(Member member);

    boolean existsByBoardAndMember(Board board, Member member);
}
