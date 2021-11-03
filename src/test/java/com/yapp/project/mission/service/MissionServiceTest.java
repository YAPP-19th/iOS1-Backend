package com.yapp.project.mission.service;

import com.yapp.project.account.domain.Account;
import com.yapp.project.aux.Message;
import com.yapp.project.aux.StatusEnum;
import com.yapp.project.aux.common.DateUtil;
import com.yapp.project.aux.test.account.AccountTemplate;
import com.yapp.project.aux.test.mission.MissionTemplate;
import com.yapp.project.aux.test.organization.OrganizationTemplate;
import com.yapp.project.capture.domain.repository.CaptureImageRepository;
import com.yapp.project.config.exception.mission.AlreadyMissionExistException;
import com.yapp.project.aux.content.MissionContent;
import com.yapp.project.config.exception.mission.MissionNotFoundException;
import com.yapp.project.mission.domain.Mission;
import com.yapp.project.mission.domain.dto.MissionDto;
import com.yapp.project.mission.domain.repository.MissionRepository;
import com.yapp.project.organization.domain.Organization;
import com.yapp.project.organization.domain.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {
    @Mock
    private MissionRepository missionRepository;

    @Mock
    private CaptureImageRepository captureImageRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private MissionService missionService;


    @Test
    void test_미션_생성() {
        //given
        Account account = AccountTemplate.makeTestAccount();
        Organization organization = OrganizationTemplate.makeTestOrganization();
        MissionDto.MissionRequest request = MissionTemplate.makeMissionRequest();
        given(organizationRepository.getById(request.getId())).willReturn(organization);
        //when
        Message message = missionService.createMission(request,account);
        //then
        assertThat(message.getStatus()).isEqualTo(StatusEnum.MISSION_OK);
    }

    @Test
    void test_이미_유저_그룹_관계가_있을_때(){
        //given
        Account account = AccountTemplate.makeTestAccount();
        Organization organization = OrganizationTemplate.makeTestOrganization();
        MissionDto.MissionRequest request = MissionTemplate.makeMissionRequest();

        Mission mission = request.toMission(account,organization);
        given(missionRepository.findMissionByAccountAndOrganization_IdAndIsFinishIsFalseAndIsDeleteIsFalse(account, request.getId()))
                .willReturn(Optional.of(mission));
        //when -> then
        assertThatThrownBy(() -> missionService.createMission(request, account)).isInstanceOf(AlreadyMissionExistException.class)
                .hasMessage(MissionContent.MISSION_BAD_REQUEST);
    }

    @Test
    void test_현재_진행_중인_미션이_있을_때(){
        //given
        Account account = AccountTemplate.makeTestAccount();

        List<Organization> organizations = new ArrayList<>();
        organizations.add(OrganizationTemplate.makeTestOrganization("확언하기","미라클모닝"));
        organizations.add(OrganizationTemplate.makeTestOrganization("시각화","미라클모닝"));
        organizations.add(OrganizationTemplate.makeTestOrganization("감사한 일 쓰기","미라클모닝"));

        List<Mission> missions = new ArrayList<>();
        missions.add(MissionTemplate.makeMissionRequest().toMission(account,organizations.get(0)));
        missions.add(MissionTemplate.makeMissionRequest().toMission(account,organizations.get(1)));
        missions.add(MissionTemplate.makeMissionRequest().toMission(account,organizations.get(2)));

        given(missionRepository.findAllByAccountAndIsFinishIsFalseAndIsDeleteIsFalse(account)).willReturn(missions);

        //when
        MissionDto.MissionResponseMessage message = missionService.findAllIsDoing(account);

        //then
        assertThat(message.getMessage().getStatus()).isEqualTo(StatusEnum.MISSION_OK);
        assertThat(message.getData().get(0).getTitle()).isEqualTo("확언하기");
        assertThat(message.getData().get(1).getTitle()).isEqualTo("시각화");
        assertThat(message.getData().get(2).getTitle()).isEqualTo("감사한 일 쓰기");
    }


    @Test
    void test_미션이_완료된_것이_있을_때(){
        //given
        Account account = AccountTemplate.makeTestAccount();
        List<Organization> organizations = new ArrayList<>();
        organizations.add(OrganizationTemplate.makeTestOrganization("확언하기","미라클모닝"));
        organizations.add(OrganizationTemplate.makeTestOrganization("시각화","미라클모닝"));
        organizations.add(OrganizationTemplate.makeTestOrganization("감사한 일 쓰기","미라클모닝"));
        List<Mission> missions = new ArrayList<>();
        LocalDate yesterday = DateUtil.KST_LOCAL_DATE_YESTERDAY();
        missions.add(MissionTemplate.makeMissionRequest(yesterday.minusDays(7).toString(),yesterday.toString())
                .toMission(account,organizations.get(0)));
        missions.add(MissionTemplate.makeMissionRequest(yesterday.minusDays(14).toString(),yesterday.toString())
                .toMission(account,organizations.get(1)));
        missions.add(MissionTemplate.makeMissionRequest(yesterday.minusDays(7).toString(),yesterday.plusDays(7).toString())
                .toMission(account,organizations.get(2)));

        given(missionRepository.findAllByAccountAndIsDeleteIsFalseAndIsFinishIsTrue(account)).willReturn(missions.subList(0,2));

        //when
        MissionDto.MissionResponseMessage message = missionService.findAllAlreadyFinish(account);
        System.out.println(message);
        //then
        assertThat(message).isNotNull();
        assertThat(message.getMessage().getMsg()).isEqualTo(MissionContent.FIND_MY_MISSION_LISTS_FINISH);
        assertThat(message.getData().size()).isEqualTo(2);
    }



    @Test
    void test_미션_디테일_페이지_내용이_있을_때(){
        //given
        Account account = AccountTemplate.makeTestAccount();
        Organization organization = OrganizationTemplate.makeTestOrganization();
        Mission mission = MissionTemplate.makeMissionRequest().toMission(account,organization);
        given(missionRepository.findMissionByAccountAndId(account,1L))
                .willReturn(Optional.of(mission));
        //when
        MissionDto.MissionDetailResponseMessage responseMessage = missionService.findDetailMyMission(account,1L);
        //then
        assertThat(responseMessage.getMessage().getStatus()).isEqualTo(StatusEnum.MISSION_OK);
        assertThat(responseMessage.getData().getPeriod()).isEqualTo(7);
        assertThat(responseMessage.getData().getBeginTime()).isEqualTo(OrganizationTemplate.BEGIN_TIME);
        assertThat(responseMessage.getData().getEndTime()).isEqualTo(OrganizationTemplate.END_TIME);
    }

    @Test
    void test_미션_디테일_페이지_내용이_없을_때(){
        //given
        Account account = AccountTemplate.makeTestAccount();
        Organization organization = OrganizationTemplate.makeTestOrganization();
        given(missionRepository.findMissionByAccountAndId(account,1L)).willReturn(Optional.empty());
        //when -> then
        assertThatThrownBy(() ->missionService.findDetailMyMission(account,1L)).isInstanceOf(MissionNotFoundException.class)
                .hasMessage(MissionContent.MISSION_NOT_FOUND);
    }

    @Test
    void test_내_미션_삭제_했을_때(){
        //given
        Account account = AccountTemplate.makeTestAccount();
        Organization organization = OrganizationTemplate.makeTestOrganization();
        Mission mission = MissionTemplate.makeMission(account, organization);
        given(missionRepository.findById(mission.getId())).willReturn(Optional.of(mission));
        //when
        Message message = missionService.deleteMyMission(mission.getId());
        //then
        assertThat(message.getStatus()).isEqualTo(StatusEnum.MISSION_OK);
        assertThat(message.getMsg()).isEqualTo(MissionContent.MISSION_DELETE_SUCCESS);
    }
}