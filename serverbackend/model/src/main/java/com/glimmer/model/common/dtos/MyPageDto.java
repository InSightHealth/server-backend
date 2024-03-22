package com.glimmer.model.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyPageDto<T> implements Serializable {
    private Integer currentPage;
    private Integer size;
    private Integer total;

    private T data;
}
