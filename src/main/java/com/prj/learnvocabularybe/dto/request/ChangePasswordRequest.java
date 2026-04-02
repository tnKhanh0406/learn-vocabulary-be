package com.prj.learnvocabularybe.dto.request;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword
) {}
