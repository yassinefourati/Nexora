package com.fourati.mapper;

import com.fourati.domain.UnderwritingNote;
import com.fourati.dto.request.CreateUnderwritingNoteRequest;
import com.fourati.dto.response.UnderwritingNoteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UnderwritingNoteMapper {

    @Mapping(target = "underwritingCaseId", source = "underwritingCase.id")
    UnderwritingNoteResponse toResponse(UnderwritingNote underwritingNote);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "underwritingCase", ignore = true)
    UnderwritingNote toEntity(CreateUnderwritingNoteRequest request);
}
