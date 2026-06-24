package com.matvey.objectaccountingservice.mapper;

import com.matvey.objectaccountingservice.dto.request.ObjectRequestDto;
import com.matvey.objectaccountingservice.dto.response.ObjectResponseDto;
import com.matvey.objectaccountingservice.entity.Object;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ObjectMapper {

    Object toEntity(ObjectRequestDto dto);

    ObjectResponseDto toResponseDto(Object entity);

    void updateEntityFromDto(ObjectRequestDto dto, @MappingTarget Object entity);
}
