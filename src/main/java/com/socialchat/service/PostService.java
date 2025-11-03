package com.socialchat.service;

import com.socialchat.dto.*;
import com.socialchat.entity.Comment;
import com.socialchat.entity.Like;
import com.socialchat.entity.Post;
import com.socialchat.entity.User;
import com.socialchat.mapper.PostMapper;
import com.socialchat.repository.CommentRepository;
import com.socialchat.repository.LikeRepository;
import com.socialchat.repository.PostRepository;
import com.socialchat.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public PostDto createPost(Long userId, CreatePostRequest request) {
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Post post = Post.builder()
                .author(user)
                .text(request.getText())
                .imageUrl(request.getImageUrl())
                .isPublic(request.getIsPublic())
                .build();

        post = postRepository.save(post);
        return PostMapper.convertToDto(post, userId, likeRepository);
    }

    public PostDto getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return PostMapper.convertToDto(post, currentUserId, likeRepository);
    }

    public PageResponse<PostDto> getPublicTimeline(int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable);
        Page<PostDto> postDtos = posts.map(post -> PostMapper.convertToDto(post, currentUserId, likeRepository));
        return PostMapper.convertPageToResponse(postDtos);
    }

    public PageResponse<PostDto> getUserPosts(Long userId, int page, int size, Long currentUserId) {
        userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable);
        Page<PostDto> postDtos = posts.map(post -> PostMapper.convertToDto(post, currentUserId, likeRepository));
        return PostMapper.convertPageToResponse(postDtos);
    }

    @Transactional
    public PostDto likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {
            Like like = Like.builder()
                    .user(user)
                    .post(post)
                    .build();
            likeRepository.save(like);
            
            if (!post.getAuthor().getId().equals(userId)) {
                notificationService.createPostLikeNotification(post.getAuthor().getId(), userId, postId);
            }
        }

        post = postRepository.findById(postId).get();
        return PostMapper.convertToDto(post, userId, likeRepository);
    }

    @Transactional
    public PostDto unlikePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        likeRepository.findByUserIdAndPostId(userId, postId)
                .ifPresent(likeRepository::delete);

        post = postRepository.findById(postId).get();
        return PostMapper.convertToDto(post, userId, likeRepository);
    }

    @Transactional
    public CommentDto addComment(Long postId, Long userId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Comment comment = Comment.builder()
                .post(post)
                .author(user)
                .text(request.getText())
                .build();

        comment = commentRepository.save(comment);
        
        if (!post.getAuthor().getId().equals(userId)) {
            notificationService.createPostCommentNotification(post.getAuthor().getId(), userId, postId);
        }
        
        return PostMapper.convertCommentToDto(comment);
    }

    public PageResponse<CommentDto> getPostComments(Long postId, int page, int size) {
        postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);

        Page<CommentDto> commentDtos = comments.map(PostMapper::convertCommentToDto);
        return PostMapper.convertPageToResponse(commentDtos);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to delete this post");
        }

        postRepository.delete(post);
    }
}
