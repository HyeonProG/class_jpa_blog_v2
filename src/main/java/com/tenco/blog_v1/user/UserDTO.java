package com.tenco.blog_v1.user;

import lombok.Data;

@Data
public class UserDTO {

    // 정적 내부 클래스로 모으자.
    @Data
    public static class LoginDTO {
        private String username;
        private String password;
    }

    // 정적 내부 클래스로 모으자.
    @Data
    public static class JoinDTO {
        private String username;
        private String password;
        private String email;

        /**
         * toEntity 메서드는 DTO에서 Entity로 변환하여,
         * 각 계층의 책임을 분리하고, 보안과 데이터 무결성을 보장하며,
         * 유연한 데이터 처리를 가능하게 하기 위해 사용됩니다.
         */
        public User toEntity() {
            return User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .build();
        }

    }

    @Data
    public static class UpdateDTO {
        private String password;
        private String email;
    }

}
