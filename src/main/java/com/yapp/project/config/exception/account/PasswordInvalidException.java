package com.yapp.project.config.exception.account;

import com.yapp.project.aux.StatusEnum;
import lombok.Getter;

@Getter
public class PasswordInvalidException extends IllegalArgumentException{
    private final StatusEnum status;
    public PasswordInvalidException(){
        super(AccountContent.NOT_VAILDATION_PASSWORD);
        this.status = StatusEnum.PASSWORD_BAD_REQUEST;
    }
}
