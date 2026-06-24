package com.matvey.objectaccountingservice.mapper;

import com.matvey.objectaccountingservice.dto.request.PprRequestDto;
import com.matvey.objectaccountingservice.dto.response.PprResponseDto;
import com.matvey.objectaccountingservice.entity.Ppr;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PprMapper {

    Ppr toEntity(PprRequestDto dto);

    PprResponseDto toResponseDto(Ppr entity);

    void updateEntityFromDto(PprRequestDto dto, @MappingTarget Ppr entity);
}
