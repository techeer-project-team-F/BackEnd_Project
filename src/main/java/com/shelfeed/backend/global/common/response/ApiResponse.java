package com.shelfeed.backend.global.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor //클래스에 지정한 필드 개수 만큼 생성자를 자동 생성
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data; //T는 아무 값이나 들어 올 수 있음

    public static <T> ApiResponse<T> ok(T data){//static : 모든 객체가 공유, 객체 자동생성(객체를 생성하는 메서드 일 때 사용)
        return new ApiResponse<>(true, "OK", data);
    }// return 값은 ApiResponse<T>

    public static <T> ApiResponse<T> ok(String message, T data){
        return new ApiResponse<>(true, message, data);
    }

    public static ApiResponse<Void> fail(String message){//Void : 제네릭 안에 값이 안들어갈 때 문법상 사용해야 함
        return new ApiResponse<>(false, message, null);
    }
}
