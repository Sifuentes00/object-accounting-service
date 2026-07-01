package com.matvey.objectaccountingservice.mapper;

import com.matvey.objectaccountingservice.dto.request.CustomerRequestDto;
import com.matvey.objectaccountingservice.dto.response.CustomerResponseDto;
import com.matvey.objectaccountingservice.dto.response.EmployeeResponseDto;
import com.matvey.objectaccountingservice.entity.Customer;
import com.matvey.objectaccountingservice.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    Customer toEntity(CustomerRequestDto dto);

    @org.mapstruct.Mapping(target = "employees", source = "employees", qualifiedByName = "mapEmployees")
    CustomerResponseDto toResponseDto(Customer entity);

    void updateEntityFromDto(CustomerRequestDto dto, @MappingTarget Customer entity);

    @Named("mapEmployees")
    default List<EmployeeResponseDto> mapEmployees(List<Employee> employees) {
        if (employees == null) {
            return null;
        }
        return employees.stream()
                .map(this::mapEmployee)
                .collect(Collectors.toList());
    }

    default EmployeeResponseDto mapEmployee(Employee employee) {
        if (employee == null) {
            return null;
        }
        return EmployeeResponseDto.builder()
                .id(employee.getId())
                .phoneNumber(employee.getPhoneNumber())
                .fullName(employee.getFullName())
                .position(employee.getPosition())
                .customerId(employee.getCustomer() != null ? employee.getCustomer().getId() : null)
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .version(employee.getVersion())
                .build();
    }
}
