package com.sparta.post.service;

import com.sparta.post.dto.PostRequestDto;
import com.sparta.post.dto.PostResponseDto;
import com.sparta.post.entity.Post;
import com.sparta.post.jwt.JwtUtil;
import com.sparta.post.repository.PostRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final JwtUtil jwtUtil;

    public PostResponseDto createPost(PostRequestDto requestDto) {
        String username = getUsername();
        Post post = new Post(requestDto, username);
        System.out.println("username = " + username);
        postRepository.save(post);
        return new PostResponseDto(post);
    }

    public List<PostResponseDto> getPosts() {
        return postRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PostResponseDto::new)
                .toList();
    }

    public PostResponseDto getPost(Long id) {
        return new PostResponseDto(findPost(id));
    }

    @Transactional
    public PostResponseDto updatePost(Long id, PostRequestDto requestDto, String token) {

        String substringToken = jwtUtil.substringToken(token);
        boolean isTokenValid = jwtUtil.validateToken(substringToken);
        Post post = findPost(id);

        if (isTokenValid) {
            post.update(requestDto);
            return new PostResponseDto(post);
        }
        return null;
    }

    public PostResponseDto deletePost(Long id, String token, PostRequestDto requestDto) {
        Post post = findPost(id);
        if (isTokenValid(token, post)) {
            postRepository.delete(post);
        }
        return new PostResponseDto(post);
    }

    private Post findPost(Long id) {
        return postRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("헤당 게시물은 존재하지 않습니다.")
        );
    }
    private boolean isTokenValid(String token, Post post) {

        String username = getUsernameFromJwt(token);

        if(username.equals(post.getUsername())) {
            return true;
        }
        return false;
    }


    private String getUsernameFromJwt(String tokenValue) {

        String token = jwtUtil.substringToken(tokenValue);

        if (!jwtUtil.validateToken(tokenValue)) {

            throw new IllegalArgumentException("Token Error");
        }

        // 토큰에서 사용자 정보 가져오기
        Claims info = jwtUtil.getUserInfoFromToken(token);
        String username = info.getSubject();
        return username;
    }

    public String getUsername() {
        System.out.println("이름 받아오는중");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // SecurityContextHolder내부에 SecurityContext내부에 있는 Authentication을 받아옴
        System.out.println("authentication = " + authentication);
        System.out.println("authentication.getName() = " + authentication.getName());
        if (authentication != null) {
            return authentication.getName(); // Authentication에서 이름을 받아온다.
        }
        return null;
    }
}



