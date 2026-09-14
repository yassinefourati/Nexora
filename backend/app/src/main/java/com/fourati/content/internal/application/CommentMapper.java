package com.fourati.content.internal.application;

import com.fourati.content.internal.domain.Comment;
import com.fourati.content.CreateCommentRequest;
import com.fourati.content.UpdateCommentRequest;
import com.fourati.content.CommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "parentCommentId", source = "parentComment.id")
    CommentResponse toResponse(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    Comment toEntity(CreateCommentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "entityType", ignore = true)
    @Mapping(target = "entityId", ignore = true)
    void updateEntityFromRequest(UpdateCommentRequest request, @MappingTarget Comment comment);
}
