package com.matvey.objectaccountingservice.mapper;

import com.matvey.objectaccountingservice.dto.request.CustomerRequestDto;
import com.matvey.objectaccountingservice.dto.response.CustomerResponseDto;
import com.matvey.objectaccountingservice.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    Customer toEntity(CustomerRequestDto dto);

    CustomerResponseDto toResponseDto(Customer entity);

    void updateEntityFromDto(CustomerRequestDto dto, @MappingTarget Customer entity);
}
