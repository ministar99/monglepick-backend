package com.monglepick.monglepickbackend.domain.community.controller;

import com.monglepick.monglepickbackend.domain.community.dto.PostCreateRequest;
import com.monglepick.monglepickbackend.domain.community.dto.PostResponse;
import com.monglepick.monglepickbackend.domain.community.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 컨트롤러
 *
 * <p>커뮤니티 게시글의 CRUD API를 제공합니다.
 * GET 요청은 비로그인 사용자도 접근 가능하며,
 * 게시글 작성, 수정, 삭제는 인증이 필요합니다.</p>
 *
 * <p>API 목록:</p>
 * <ul>
 *   <li>GET /api/v1/posts - 게시글 목록 조회</li>
 *   <li>GET /api/v1/posts/{id} - 게시글 상세 조회</li>
 *   <li>POST /api/v1/posts - 게시글 작성 (인증 필요)</li>
 *   <li>PUT /api/v1/posts/{id} - 게시글 수정 (작성자만)</li>
 *   <li>DELETE /api/v1/posts/{id} - 게시글 삭제 (작성자만)</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 목록 조회 API
     *
     * @param category 게시글 카테고리 필터 (선택: FREE, DISCUSSION, RECOMMENDATION, NEWS)
     * @param pageable 페이징 정보 (기본: 15건, 최신순)
     * @return 200 OK + 페이지 단위의 게시글 목록
     */
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @RequestParam(required = false) String category,
            @PageableDefault(size = 15, page = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<PostResponse> posts = postService.getPosts(category, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 상세 조회 API
     *
     * <p>조회 시 조회수가 1 증가합니다.</p>
     *
     * @param id 게시글 ID
     * @return 200 OK + 게시글 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        PostResponse post = postService.getPost(id);
        return ResponseEntity.ok(post);
    }

    /**
     * 게시글 작성 API (인증 필요)
     *
     * @param request 게시글 작성 요청 (제목, 내용, 카테고리)
     * @paramuserId JWT에서 추출한 사용자 ID
     * @return 201 Created + 생성된 게시글 정보
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostCreateRequest request)
        {// @AuthenticationPrincipal String userId) {

           String userId = "test-user-001";
        log.info("게시글 작성 요청 - userId: {}, category: {}", userId, request.category());
        PostResponse post = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    /**
     * 게시글 수정 API (작성자만 가능)
     *
     * @param id 게시글 ID
     * @param request 수정할 내용 (제목, 내용, 카테고리)
     * @param userId JWT에서 추출한 사용자 ID
     * @return 200 OK + 수정된 게시글 정보
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal String userId) {

        log.info("게시글 수정 요청 - postId: {}, userId: {}", id, userId);
        PostResponse post = postService.updatePost(id, request, userId);
        return ResponseEntity.ok(post);
    }
    /**
     * 게시글 삭제 API (작성자만 가능)
     *
     * @param id 게시글 ID
     * @param userId JWT에서 추출한 사용자 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal String userId) {

        log.info("게시글 삭제 요청 - postId: {}, userId: {}", id, userId);
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }
    /**임시저장작성
    @PostMapping("/drafts")
    public PostResponse createDraft(
            @RequestBody PostCreateRequest request,
            @RequestHeader("userId") String userId) {

        return postService.createDraft(request, userId);
    }*/
    @PostMapping("/drafts")  // 테스트용
    public ResponseEntity<PostResponse> createDraft(
            @RequestBody PostCreateRequest request) {

        String userId = "test-user-001"; // 테스트용

        PostResponse post = postService.createDraft(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }
    /**임시저장목록*/
    @GetMapping("/drafts")
    public Page<PostResponse> getDrafts(
            Pageable pageable,
            @RequestHeader("userId") String userId) {

        return postService.getDrafts(userId, pageable);
    }

    /**임시저장수정*/
    @PutMapping("/drafts/{id}")
    public PostResponse updateDraft(
            @PathVariable Long id,
            @RequestBody PostCreateRequest request,
            @RequestHeader("userId") String userId) {

        return postService.updateDraft(id, request, userId);
    }
    /**임시저장 삭제*/
    @DeleteMapping("/drafts/{id}")
    public void deleteDraft(
            @PathVariable Long id,
            @RequestHeader("userId") String userId) {

        postService.deleteDraft(id, userId);
    }
    /**임시저장 → 게시*/
    @PostMapping("/drafts/{id}/publish")
    public PostResponse publishDraft(
            @PathVariable Long id,
            @RequestHeader("userId") String userId) {

        return postService.publishDraft(id, userId);
    }
}
