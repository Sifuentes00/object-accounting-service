package com.matvey.objectaccountingservice.mapper;

import com.matvey.objectaccountingservice.dto.request.ContractRequestDto;
import com.matvey.objectaccountingservice.dto.response.ContractResponseDto;
import com.matvey.objectaccountingservice.entity.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    Contract toEntity(ContractRequestDto dto);

    ContractResponseDto toResponseDto(Contract entity);

    void updateEntityFromDto(ContractRequestDto dto, @MappingTarget Contract entity);
}
