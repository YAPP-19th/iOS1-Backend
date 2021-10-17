package com.yapp.project.aux.test.mission;

import com.yapp.project.account.domain.Account;
import com.yapp.project.mission.domain.Mission;
import com.yapp.project.mission.domain.dto.MissionDto;
import com.yapp.project.organization.domain.Organization;
import com.yapp.project.routine.domain.Week;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MissionTemplate {
    private MissionTemplate(){
    }
    private static final Long MISSION_ID = 1000L;
    public static final LocalDate START_DATE = LocalDate.now();
    public static final String STR_START_DATE = LocalDate.now().toString();
    public static final String STR_FINISH_DATE = START_DATE.plusDays(7L).toString();
    public static final LocalDate FINISH_DATE = START_DATE.plusDays(7L);
    protected static final List<Week> WEEKS = new ArrayList<>();

    static {
        WEEKS.add(Week.MON);
        WEEKS.add(Week.WED);
        WEEKS.add(Week.THU);
    }

    public static Mission makeMission(Account account, Organization organization,
                                      LocalDate startDate, LocalDate finishDate){
        return Mission.builder().account(account).organization(organization).startDate(startDate).finishDate(finishDate).build();
    }

    public static Mission makeMission(Account account, Organization organization){
        return makeMission(account,organization,START_DATE,FINISH_DATE);
    }

    public static MissionDto.MissionRequest makeMissionRequest(){
        return MissionDto.MissionRequest.builder().id(MISSION_ID).startDate(STR_START_DATE).finishDate(STR_FINISH_DATE).weeks(WEEKS).build();
    }

}
