package com.senla.readingbooks.repository.jpa;

import com.senla.readingbooks.entity.Comment;
import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.EntityType;
import com.senla.readingbooks.projection.CommentReplyCountProjection;
import com.senla.readingbooks.projection.CommentWithFirstReplyProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Modifying
    @Query("""
            UPDATE Comment c
            SET c.updatedAt = :now
            WHERE c.id = :id
            """)
    void updateComment(Long id, Instant now);

    @Query("""
            SELECT c
            FROM Comment c
            WHERE c.entityId = :entityId
              AND c.entityType = :entityType
              AND c.isPinned = true
            """)
    Optional<Comment> findPinnedComment(Long entityId, EntityType entityType);

    @Query(value = """
            SELECT c.id AS id,
                   c.user_id AS userId,
                   c.entity_id AS entityId,
                   c.entity_type AS entityType,
                   c.parent_id AS parentId,
                   c.likes_count AS likesCount,
                   c.dislikes_count AS dislikesCount,
                   c.is_pinned AS isPinned,
                   c.created_at AS createdAt,
                   c.updated_at AS updatedAt,
                   c.content_url AS contentUrl,
                   firstReply.id AS replyId,
                   firstReply.user_id AS replyUserId,
                   firstReply.entity_id AS replyEntityId,
                   firstReply.entity_type AS replyEntityType,
                   firstReply.parent_id AS replyParentId,
                   firstReply.likes_count AS replyLikesCount,
                   firstReply.dislikes_count AS replyDislikesCount,
                   firstReply.is_pinned AS replyIsPinned,
                   firstReply.created_at AS replyCreatedAt,
                   firstReply.updated_at AS replyUpdatedAt,
                   firstReply.content_url AS replyContentUrl
            FROM comments c
            LEFT JOIN (
                SELECT r.*,
                       ROW_NUMBER() OVER (PARTITION BY r.parent_id ORDER BY r.created_at) AS rn
                FROM comments r
            ) firstReply ON firstReply.parent_id = c.id AND firstReply.rn = 1
            WHERE c.entity_id = :entityId
                  AND c.entity_type = :entityType
                  AND c.parent_id IS NULL
            """, nativeQuery = true)
    List<CommentWithFirstReplyProjection> findRootCommentsWithFirstReply(Long entityId, String entityType, Pageable pageable);

    @Query("""
            SELECT COUNT(c.id)
            FROM Comment c
            WHERE c.entityId = :entityId
                  AND c.entityType = :entityType
                  AND c.parent.id IS NULL
            """)
    int countRootComments(Long entityId, EntityType entityType);

    @Query("""
            SELECT c.id AS rootCommentId,
                   COUNT(r.id) AS replyCount
            FROM Comment c
            LEFT JOIN Comment r ON r.parent.id = c.id
            WHERE c.id IN (:commentIds) AND c.parent IS NULL
            GROUP BY c.id
            """)
    List<CommentReplyCountProjection> countRepliesForRootComments(List<Long> commentIds);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.id IN (SELECT DISTINCT c.user.id
                           FROM Comment c
                           WHERE c.id IN :commentIds
            )
            """)
    List<User> findUsersByCommentIds(List<Long> commentIds);

    @Query(value = """
            SELECT c.id AS id,
                   c.user_id AS userId,
                   c.entity_id AS entityId,
                   c.entity_type AS entityType,
                   c.parent_id AS parentId,
                   c.likes_count AS likesCount,
                   c.dislikes_count AS dislikesCount,
                   c.is_pinned AS isPinned,
                   c.created_at AS createdAt,
                   c.updated_at AS updatedAt,
                   c.content_url AS contentUrl,
                   firstReply.id AS replyId,
                   firstReply.user_id AS replyUserId,
                   firstReply.entity_id AS replyEntityId,
                   firstReply.entity_type AS replyEntityType,
                   firstReply.parent_id AS replyParentId,
                   firstReply.likes_count AS replyLikesCount,
                   firstReply.dislikes_count AS replyDislikesCount,
                   firstReply.is_pinned AS replyIsPinned,
                   firstReply.created_at AS replyCreatedAt,
                   firstReply.updated_at AS replyUpdatedAt,
                   firstReply.content_url AS replyContentUrl
            FROM comments c
            LEFT JOIN comments firstReply ON firstReply.parent_id = c.id
               AND firstReply.created_at = (SELECT MIN(r.created_at)
                                            FROM comments r
                                            WHERE r.parent_id = c.id)
            WHERE c.entity_id = :entityId
                  AND c.entity_type = :entityType
                  AND c.is_pinned = TRUE
            """, nativeQuery = true)
    CommentWithFirstReplyProjection findPinnedCommentWithFirstReply(Long entityId, String entityType);


    @Query(value = """
            SELECT c.id AS id,
                   c.user_id AS userId,
                   c.entity_id AS entityId,
                   c.entity_type AS entityType,
                   c.parent_id AS parentId,
                   c.likes_count AS likesCount,
                   c.dislikes_count AS dislikesCount,
                   c.is_pinned AS isPinned,
                   c.created_at AS createdAt,
                   c.updated_at AS updatedAt,
                   firstReply.id AS replyId,
                   firstReply.user_id AS replyUserId,
                   firstReply.entity_id AS replyEntityId,
                   firstReply.entity_type AS replyEntityType,
                   firstReply.parent_id AS replyParentId,
                   firstReply.likes_count AS replyLikesCount,
                   firstReply.dislikes_count AS replyDislikesCount,
                   firstReply.is_pinned AS replyIsPinned,
                   firstReply.created_at AS replyCreatedAt,
                   firstReply.updated_at AS updatedAt
            FROM comments c
            LEFT JOIN (
                SELECT r.*,
                       ROW_NUMBER() OVER (PARTITION BY r.parent_id ORDER BY r.created_at) AS rn
                FROM comments r
            ) firstReply ON firstReply.parent_id = c.id AND firstReply.rn = 1
            WHERE c.entity_id = :entityId
                  AND c.entity_type = :entityType
                  AND c.parent_id IS NULL
                  AND c.is_pinned = FALSE
            """, nativeQuery = true)
    List<CommentWithFirstReplyProjection> findRootCommentsWithFirstReplyExcludingPinned(Long entityId, String entityType, Pageable pageable);

    @Query(value = """
            SELECT COUNT(c.id) > 0
            FROM comments c
            WHERE c.id = :commentId AND c.user_id = :userId
            LIMIT 1
            """, nativeQuery = true)
    boolean userIsAuthorOfComment(Long commentId, Long userId);
}