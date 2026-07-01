package com.matvey.objectaccountingservice.mapper;

import com.matvey.objectaccountingservice.dto.request.EmployeeRequestDto;
import com.matvey.objectaccountingservice.dto.response.EmployeeResponseDto;
import com.matvey.objectaccountingservice.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    Employee toEntity(EmployeeRequestDto dto);

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.name", target = "customerName")
    EmployeeResponseDto toResponseDto(Employee entity);

    void updateEntityFromDto(EmployeeRequestDto dto, @MappingTarget Employee entity);
}
