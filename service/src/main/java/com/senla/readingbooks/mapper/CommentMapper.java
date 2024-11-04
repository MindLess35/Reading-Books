package com.senla.readingbooks.mapper;

import com.senla.readingbooks.dto.comment.CommentReadDto;
import com.senla.readingbooks.dto.comment.CommentSaveDto;
import com.senla.readingbooks.dto.comment.ReplyCommentWithUserDto;
import com.senla.readingbooks.dto.comment.RootCommentWithReplyAndUserDto;
import com.senla.readingbooks.entity.Comment;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.projection.CommentWithFirstReplyProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "parentId", source = "parent.id")
    CommentReadDto toDto(Comment comment);

    Comment toEntity(CommentSaveDto dto);

    Comment updateEntity(CommentSaveDto dto, @MappingTarget Comment parent);

    CommentReadDto mapRootComment(CommentWithFirstReplyProjection projection);

    @Mapping(target = "id", source = "replyId")
    @Mapping(target = "entityId", source = "replyEntityId")
    @Mapping(target = "entityType", source = "replyEntityType")
    @Mapping(target = "parentId", source = "replyParentId")
    @Mapping(target = "userId", source = "replyUserId")
    @Mapping(target = "likesCount", source = "replyLikesCount")
    @Mapping(target = "dislikesCount", source = "replyDislikesCount")
    @Mapping(target = "isPinned", source = "replyIsPinned")
    @Mapping(target = "createdAt", source = "replyCreatedAt")
    @Mapping(target = "updatedAt", source = "replyUpdatedAt")
    @Mapping(target = "contentUrl", source = "replyContentUrl")
    CommentReadDto mapReplyComment(CommentWithFirstReplyProjection projection);

    default List<CommentReadDto> toListDto(List<CommentWithFirstReplyProjection> projections) {
        return projections.stream()
                .map(this::mapRootComment)
                .collect(Collectors.toList());
    }

    default List<CommentReadDto> toListReplyDto(List<CommentWithFirstReplyProjection> projections) {
        return projections.stream()
                .map(this::mapReplyComment)
                .collect(Collectors.toList());
    }

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "author.authorId", source = "user.id")
    @Mapping(target = "author.username", source = "user.username")
    @Mapping(target = "author.avatarUrl", source = "user.avatarUrl")
    @Mapping(target = "createdAt", source = "dto.createdAt")
    @Mapping(target = "updatedAt", source = "dto.updatedAt")
    ReplyCommentWithUserDto toReplyWithUserDto(CommentReadDto dto, User user);

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "likesCount", source = "dto.likesCount")
    @Mapping(target = "dislikesCount", source = "dto.dislikesCount")
    @Mapping(target = "isPinned", source = "dto.isPinned")
    @Mapping(target = "createdAt", source = "dto.createdAt")
    @Mapping(target = "updatedAt", source = "dto.updatedAt")
    @Mapping(target = "contentUrl", source = "dto.contentUrl")
    @Mapping(target = "author.authorId", source = "user.id")
    @Mapping(target = "author.username", source = "user.username")
    @Mapping(target = "author.avatarUrl", source = "user.avatarUrl")
    RootCommentWithReplyAndUserDto toCommentWithUserDto(CommentReadDto dto, User user, ReplyCommentWithUserDto firstReply, Long otherRepliesCount);
}