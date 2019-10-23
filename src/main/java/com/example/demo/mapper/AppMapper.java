package com.example.demo.mapper;

import com.example.demo.util.dto.AppParamDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface AppMapper {
    void insert(AppParamDto appParamDto);
}
