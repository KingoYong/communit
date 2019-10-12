package com.example.demo.mapper;

import com.example.demo.util.response.ReportResponse;
import com.example.demo.util.response.ReportResponseReturn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ResponseMapper {

    void insertByBatch(List<ReportResponse> reportResponseList);

    List<ReportResponseReturn> findByAppIdAndOffset(@Param("appId") String appId, @Param("offset") long offset,
                                                    @Param("number") int number);


    List<ReportResponse> findAll();
}
