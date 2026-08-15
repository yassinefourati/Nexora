package com.fourati.mapper;

import com.fourati.domain.Comment;
import com.fourati.dto.request.CreateCommentRequest;
import com.fourati.dto.request.UpdateCommentRequest;
import com.fourati.dto.response.CommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    CommentResponse toResponse(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    Comment toEntity(CreateCommentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "entityType", ignore = true)
    @Mapping(target = "entityId", ignore = true)
    void updateEntityFromRequest(UpdateCommentRequest request, @MappingTarget Comment comment);
}
