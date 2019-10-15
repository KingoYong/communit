package com.example.demo.mapper;

import com.example.demo.util.dto.ReportOffset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface ResponseAppIdMapper {

    ReportOffset findAppId(@Param("appId") String appId);

    int insert(ReportOffset reportOffset1);

    long findOffset(@Param("appId") String appId);

    void updateOffset(@Param("appId") String appId, @Param("offset") long offset);
}
