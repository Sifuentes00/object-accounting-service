package com.matvey.objectaccountingservice.mapper;

import com.matvey.objectaccountingservice.dto.request.ObjectRequestDto;
import com.matvey.objectaccountingservice.dto.response.ObjectResponseDto;
import com.matvey.objectaccountingservice.entity.Object;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ObjectMapper {

    Object toEntity(ObjectRequestDto dto);

    @Mapping(source = "customer", target = "customer")
    @Mapping(source = "customer.id", target = "customer.id")
    @Mapping(source = "customer.name", target = "customer.name")
    @Mapping(source = "responsibleEmployee", target = "responsibleEmployee")
    @Mapping(source = "responsibleEmployee.id", target = "responsibleEmployee.id")
    @Mapping(source = "responsibleEmployee.fullName", target = "responsibleEmployee.fullName")
    @Mapping(source = "responsibleEmployee.position", target = "responsibleEmployee.position")
    @Mapping(source = "responsibleEmployee.phoneNumber", target = "responsibleEmployee.phoneNumber")
    ObjectResponseDto toResponseDto(Object entity);

    void updateEntityFromDto(ObjectRequestDto dto, @MappingTarget Object entity);
}
