package com.credibanco.bancinc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDTO {
    private int statusCode;
    private Object data;
}
