package com.socialchat.mapper;

import com.socialchat.dto.CommentDto;
import com.socialchat.dto.PageResponse;
import com.socialchat.dto.PostDto;
import com.socialchat.entity.Comment;
import com.socialchat.entity.Post;
import com.socialchat.repository.LikeRepository;
import org.springframework.data.domain.Page;

public class PostMapper {
    public static PostDto convertToDto(Post post, Long currentUserId, LikeRepository likeRepository) {
        boolean isLiked = currentUserId != null && likeRepository.existsByUserIdAndPostId(currentUserId, post.getId());
        return PostDto.builder()
                .id(post.getId())
                .authorId(post.getAuthor().getId())
                .authorUsername(post.getAuthor().getUsername())
                .authorDisplayName(post.getAuthor().getDisplayName())
                .authorProfilePhotoUrl(post.getAuthor().getProfilePhotoUrl())
                .text(post.getText())
                .imageUrl(post.getImageUrl())
                .isPublic(post.getIsPublic())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isLikedByCurrentUser(isLiked)
                .createdAt(post.getCreatedAt())
                .build();
    }

    public static CommentDto convertCommentToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .authorId(comment.getAuthor().getId())
                .authorUsername(comment.getAuthor().getUsername())
                .authorDisplayName(comment.getAuthor().getDisplayName())
                .authorProfilePhotoUrl(comment.getAuthor().getProfilePhotoUrl())
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public static <T> PageResponse<T> convertPageToResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
