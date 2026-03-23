package com.prj.learnvocabularybe.util;

import com.prj.learnvocabularybe.entity.UserEntity;

public class SecurityUtil {
    public static UserEntity getCurrentUser() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("khanh");
        user.setPassword("1");
        user.setAvatarUrl("https://res.cloudinary.com/dlm5gmhxs/image/upload/v1768923438/product-reviews/oflwrnpksrndf8ukgizm.jpg");
        user.setEmail("khanh@gmail.com");
        return user;
    }
}
