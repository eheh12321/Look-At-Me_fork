package com.lookatme.server.board.service;

import com.lookatme.server.auth.dto.MemberPrincipal;
import com.lookatme.server.board.dto.BoardPatchDto;
import com.lookatme.server.board.dto.BoardPostDto;
import com.lookatme.server.board.dto.BoardResponseDto;
import com.lookatme.server.board.entity.Board;
import com.lookatme.server.board.entity.BoardProduct;
import com.lookatme.server.board.mapper.BoardMapper;
import com.lookatme.server.board.repository.BoardProductRepository;
import com.lookatme.server.board.repository.BoardRepository;
import com.lookatme.server.exception.ErrorCode;
import com.lookatme.server.exception.ErrorLogicException;
import com.lookatme.server.exception.board.BoardNotFoundException;
import com.lookatme.server.file.FileDirectory;
import com.lookatme.server.file.FileService;
import com.lookatme.server.likes.repository.LikesRepository;
import com.lookatme.server.likes.service.LikesService;
import com.lookatme.server.member.entity.Member;
import com.lookatme.server.member.repository.MemberRepository;
import com.lookatme.server.member.service.FollowService;
import com.lookatme.server.product.dto.ProductPatchDto;
import com.lookatme.server.product.dto.ProductPostDto;
import com.lookatme.server.product.entity.Category;
import com.lookatme.server.product.entity.Product;
import com.lookatme.server.product.repository.CategoryRepository;
import com.lookatme.server.product.repository.ProductRepository;
import com.lookatme.server.product.service.ProductService;
import com.lookatme.server.rental.dto.RentalPatchDto;
import com.lookatme.server.rental.entity.Rental;
import com.lookatme.server.rental.repository.RentalRepository;
import com.lookatme.server.rental.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Transactional
@Service
public class BoardService {

    private final BoardMapper mapper;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final ProductRepository productRepository;
    private final BoardProductRepository boardProductRepository;
    private final CategoryRepository categoryRepository;
    private final RentalRepository rentalRepository;
    private final FileService fileService;
    private final FollowService followService;
    private final RentalService rentalService;
    private final ProductService productService;
    private final LikesRepository likesRepository;
    private final LikesService likesService;

    public BoardResponseDto createBoard(BoardPostDto post, long memberId) {
        // 1. 게시글 저장
        Board board = mapper.boardPostToBoard(post);
        board.setMember(findMember(memberId));
        String userImageUrl = fileService.upload(post.getUserImage(), FileDirectory.post);
        board.setUserImage(userImageUrl);
        Board savedBoard = boardRepository.save(board);

        if (post.getProducts() != null) {
            for (ProductPostDto postProduct : post.getProducts()) {
                // 2. 게시글 상품 저장
                String itemImageUrl = fileService.upload(postProduct.getProductImage(), FileDirectory.item);
                Product savedProduct = productService.createProduct(postProduct, itemImageUrl);
                BoardProduct boardProduct = BoardProduct.builder()
                        .board(board)
                        .product(savedProduct)
                        .price(postProduct.getPrice())
                        .link(postProduct.getLink()).build();
                board.getBoardProducts().add(boardProduct);

                // 3. 렌탈 정보 저장
                Rental rental = rentalService.createRental(
                        memberId,
                        savedProduct.getProductId(),
                        board,
                        postProduct.getSize(),
                        postProduct.getRentalPrice(),
                        postProduct.isRental()
                );
                savedProduct.getRentals().add(rental);
            }
        }
        return mapper.boardToBoardResponse(savedBoard);
    }

    public BoardResponseDto updateBoard(BoardPatchDto patch, long boardId, long memberId) {
        Board savedBoard = findBoardEntity(boardId);
        // 로그인 한 작성자가 아니면 수정할 수 없음
        if (savedBoard.getMember().getMemberId() != memberId) {
            throw new ErrorLogicException(ErrorCode.FORBIDDEN);
        }
        if (patch.getProducts() != null) {
            for (ProductPatchDto product : patch.getProducts()) {
                productService.updateProduct(product);
                for (BoardProduct boardProduct : savedBoard.getBoardProducts()) {
                    if (boardProduct.getProduct().getProductId() == product.getProductId()) {
                        boardProduct.updateProductInfo(
                                product.getLink(),
                                product.getPrice()
                        );
                        break;
                    }
                }
                rentalService.updateRental(
                        savedBoard.getBoardId(),
                        product.getProductId(),
                        new RentalPatchDto(
                                product.getRentalPrice(),
                                product.getSize(),
                                product.isRental()
                        )
                );
            }
        }
        // userImage가 없으면 수정하지 않음
        String userImageUrl = savedBoard.getUserImage();
        if (patch.getUserImage() != null) {
            userImageUrl = fileService.upload(patch.getUserImage(), FileDirectory.post);
        }
        savedBoard.updateBoard(userImageUrl, patch.getContent());
        if (likesService.isLikePost(savedBoard.getMember(), savedBoard)) {
            savedBoard.setLikeStatusTrue();
        }
        return mapper.boardToBoardResponse(savedBoard);
    }

    public void deleteBoard(long boardId) {
        boardRepository.delete(findBoardEntity(boardId));
    }

    public void deleteBoards() {
        boardRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public BoardResponseDto findBoard(long boardId) {
        return mapper.boardToBoardResponse(findBoardEntity(boardId));
    }

    @Transactional(readOnly = true)
    public BoardResponseDto findBoard(long boardId, long loginMemberId) {
        Board board = findBoardEntity(boardId);
        Member writer = board.getMember();
        if (loginMemberId != -1) {
            Member loginMember = findMember(loginMemberId);
            // 게시글 작성자가 내가 팔로우 한 사람인지 유무 체크
            if (followService.isFollowee(loginMemberId, writer.getMemberId())) {
                writer.setStatusToFollowingMember();
            }
            // 내가 좋아요를 누른 게시글인지 유무 체크
            if (likesService.isLikePost(loginMember, board)) {
                board.setLikeStatusTrue();
            }
        }
        return mapper.boardToBoardResponse(board);
    }

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> findBoards(int page, int size) {
        return findBoards(page, size, -1); // 팔로우 유무 체크하지 않음
    }

    @Transactional(readOnly = true)
    public Page<BoardResponseDto> findBoards(int page, int size, long memberId) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Board> boardPage = boardRepository.findAll(pageRequest);
        if (memberId != -1) {
            Member loginMember = findMember(memberId);
            Set<Long> followMemberIdList = followService.getFollowMemberIdSet(memberId); // 현재 로그인 한 회원이 팔로우 중인 회원 id list
            Set<Long> likeBoardIdSet = likesService.getLikeBoardIdSet(loginMember);
            boardPage.getContent().forEach(
                    board -> {
                        Member member = board.getMember();
                        if (followMemberIdList.contains(member.getMemberId())) {
                            member.setStatusToFollowingMember();
                        }
                        if (likeBoardIdSet.contains(board.getBoardId())) {
                            board.setLikeStatusTrue();
                        }
                    });
        }
        List<BoardResponseDto> response = mapper.boardsToBoardResponseDtos(boardPage.getContent());
        return new PageImpl<>(response, boardPage.getPageable(), boardPage.getTotalElements());
    }

    public Page<BoardResponseDto> findBoardsByCategoryName(String name, int page, int size) {

        Page<BoardProduct> productPage = boardProductRepository.findByProduct_Category(findCategory(name), PageRequest.of(page, size, Sort.by("createdDate").descending()));
        List<BoardProduct> boardProducts = productPage.getContent();
        List<Board> boardList = new ArrayList<>();

        for (BoardProduct boardProduct : boardProducts) {
            Board board = boardProduct.getBoard();
            boardList.add(board);
        }

        List<BoardResponseDto> response = mapper.boardsToBoardResponseDtos(boardList);
        return new PageImpl<>(response, productPage.getPageable(), productPage.getTotalElements());
    }

    public Page<BoardResponseDto> findBoardsByproductName(String name, int page, int size) {

        Page<BoardProduct> productPage = boardProductRepository.findByProduct_ProductName(name, PageRequest.of(page, size, Sort.by("createdDate").descending()));
        List<BoardProduct> boardProducts = productPage.getContent();

        List<Board> boardList = new ArrayList<>();

        for (BoardProduct boardProduct : boardProducts) {
            Board board = boardProduct.getBoard();
            boardList.add(board);
        }

        List<BoardResponseDto> response = mapper.boardsToBoardResponseDtos(boardList);
        return new PageImpl<>(response, productPage.getPageable(), productPage.getTotalElements());
    }


    public Page<BoardResponseDto> findBoardsByRentalAvailable(int page, int size) {

        Page<Rental> rentalPage = rentalRepository.findByAvailableTrue(PageRequest.of(page, size, Sort.by("createdDate").descending()));
        List<Rental> boardRentals = rentalPage.getContent();

        List<Board> boardList = new ArrayList<>();
        List<Long> idList = new ArrayList<>();

        for (Rental rental : boardRentals) {
            Board board = rental.getBoard();
            if (idList.contains(board.getBoardId())) {
                continue;
            }

            else {
                idList.add(board.getBoardId());
                boardList.add(board);
            }

        }

        List<BoardResponseDto> response = mapper.boardsToBoardResponseDtos(boardList);
        return new PageImpl<>(response, rentalPage.getPageable(), rentalPage.getTotalElements());
    }

    @Transactional
    public BoardResponseDto likeBoard(final MemberPrincipal memberPrincipal, final long boardId) {
        Member member = findMember(memberPrincipal.getMemberId());
        Board board = findBoardEntity(boardId);

        if (likesRepository.findByBoardAndMember(board, member) == null) {
            board.setLikeCnt(board.getLikeCnt() + 1);
            likesService.like(member, board);
            board.setLikeStatusTrue();
        } else {
            board.setLikeCnt(board.getLikeCnt() - 1);
            likesService.unlike(member, board);
        }

        return mapper.boardToBoardResponse(board);
    }

    // 내부에서 사용하는 용도 //
    private Member findMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ErrorLogicException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Product findProduct(long productId) {
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new ErrorLogicException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Category findCategory(String categoryName) {

        return categoryRepository.findByName(categoryName)
                .orElseThrow(() ->  new ErrorLogicException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Board findBoardEntity(long boardId) {
        return boardRepository.findByBoardId(boardId)
                .orElseThrow(BoardNotFoundException::new);
    }
}
