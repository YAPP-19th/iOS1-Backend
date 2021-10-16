package com.yapp.project.saying.service;

import static com.yapp.project.saying.domain.dto.SayingDto.*;

import com.yapp.project.account.domain.Account;
import com.yapp.project.config.exception.saying.AlreadyFoundException;
import com.yapp.project.config.exception.saying.OverFlowSayingIdException;
import com.yapp.project.saying.domain.Saying;
import com.yapp.project.saying.domain.SayingRecord;
import com.yapp.project.saying.domain.repository.SayingRecordRepository;
import com.yapp.project.saying.domain.repository.SayingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SayingService {
    private final SayingRepository sayingRepository;
    private final SayingRecordRepository sayingRecordRepository;

    public Saying randomSaying(Account account, int id){
        Saying saying =  sayingRepository.findById((long) id)
                .orElseThrow(OverFlowSayingIdException::new);

        SayingRecord lastRecord = sayingRecordRepository.findTopByAccount_IdOrderByIdDesc(account.getId())
                .orElse(null);

        if (lastRecord == null || lastRecord.getCreatedAt().toLocalDate().isBefore(LocalDateTime.now().toLocalDate())){
            return saying;
        }else{
            throw new AlreadyFoundException();
        }
    }

    @Transactional
    public SayingResponse checkResult(SayingAccess request, Account account){
        Saying saying = sayingRepository.findById(request.getId())
                .orElseThrow(OverFlowSayingIdException::new);
        if (saying.getContent().equalsIgnoreCase(request.getContent())){
            SayingRecord sayingRecord = SayingRecord.builder().account(account).saying(saying).build();
            sayingRecordRepository.save(sayingRecord);
            return SayingResponse.builder().id(request.getId()).result(true).build();
        }else{
            return SayingResponse.builder().id(request.getId()).result(false).build();
        }
    }

}
