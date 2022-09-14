package com.ll.exam.damda.form.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;


@Getter
@Setter
public class UserCreateForm {

    @NotEmpty(message = "아이디를 입력해주세요")
    private String username;

    @NotEmpty(message = "닉네임을 입력해주세요")
    private String nickname;

    @NotEmpty(message = "비밀번호를 입력해주세요")
    private String password;

    @NotEmpty(message = "비밀번호를 다시 입력해주세요")
    private String password_check;

    @NotEmpty(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식으로 입력해주세요")
    private String email;
}
