package com.prj.learnvocabularybe.util;

import com.prj.learnvocabularybe.entity.UserEntity;

public class SecurityUtil {
    public static UserEntity getCurrentUser() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("khanh");
        user.setPassword("1");
        user.setEmail("khanh@gmail.com");
        return user;
    }
}
