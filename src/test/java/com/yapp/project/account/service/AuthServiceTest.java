package com.yapp.project.account.service;

import com.yapp.project.account.domain.Account;
import com.yapp.project.account.domain.dto.AccountRequestDto;
import com.yapp.project.account.domain.dto.AccountResponseDto;
import com.yapp.project.account.domain.dto.TokenDto;
import com.yapp.project.account.domain.dto.TokenRequestDto;
import com.yapp.project.account.domain.repository.AccountRepository;
import com.yapp.project.aux.Message;
import com.yapp.project.aux.StatusEnum;
import com.yapp.project.aux.test.account.AccountTemplate;
import com.yapp.project.config.exception.Content;
import com.yapp.project.config.exception.account.DuplicateException;
import com.yapp.project.config.exception.account.NotFoundUserInformationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@WithMockUser(value = AccountTemplate.EMAIL,password = AccountTemplate.PASSWORD)
class AuthServiceTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AuthService authService;

    @Test
    @Transactional
    void signupSuccess() {
        AccountRequestDto accountRequestDto = AccountTemplate.makeAccountRequestDto("hello@example.com");
        AccountResponseDto accountResponseDto = authService.signup(accountRequestDto);
        assertThat(accountResponseDto.getEmail()).isEqualTo(accountRequestDto.getEmail());
    }

    @Test
    @Transactional
    void signupFail() {
        Account account = AccountTemplate.makeTestAccount();
        AccountRequestDto accountRequestDto = AccountTemplate.makeAccountRequestDto();
        accountRepository.save(account);
        assertThatThrownBy(() -> authService.signup(accountRequestDto)).isInstanceOf(DuplicateException.class)
                .hasMessage(Content.EMAIL_DUPLICATE);
    }

    @Test
    @Transactional
    void loginSuccess() {
        Account account = AccountTemplate.makeTestAccount();
        accountRepository.save(account);
        AccountRequestDto accountRequestDto = AccountTemplate.makeAccountRequestDto();
        TokenDto tokenDto = authService.login(accountRequestDto);
        assertThat(tokenDto.getAccessToken()).isNotNull();
        assertThat(tokenDto.getRefreshToken()).isNotNull();
    }

    @Test
    @Transactional
    void loginFail(){
        AccountRequestDto accountRequestDto = AccountTemplate.makeAccountRequestDto("fail@example.com"
                ,"fail");
        assertThatThrownBy(() ->authService.login(accountRequestDto)).isInstanceOf(NotFoundUserInformationException.class)
                .hasMessage(Content.NOT_FOUND_USER_INFORMATION);
    }

    @Test
    @Transactional
    void reissue() throws InterruptedException {
        AccountRequestDto accountRequestDto = AccountTemplate.makeAccountRequestDto();
        accountRepository.save(AccountTemplate.makeTestAccount());
        TokenDto tokenDto = authService.login(accountRequestDto);
        TokenRequestDto tokenRequestDto = new TokenRequestDto(tokenDto.getRefreshToken());
        TimeUnit.MICROSECONDS.sleep(1);
        TokenDto reissueToken = authService.reissue(tokenRequestDto);
        assertThat(tokenDto.getAccessTokenExpiresIn()-reissueToken.getAccessTokenExpiresIn()).isNegative();
    }

    @Test
    @Transactional
    void logout(){
        Account account = AccountTemplate.makeTestAccount();
        accountRepository.save(account);
        Message message = authService.logout();
        assertThat(message.getStatus()).isEqualTo(StatusEnum.OK);
    }
}