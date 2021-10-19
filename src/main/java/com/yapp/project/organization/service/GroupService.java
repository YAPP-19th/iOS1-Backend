package com.yapp.project.organization.service;

import com.yapp.project.account.domain.Account;
import com.yapp.project.mission.domain.dao.MissionOrganization;
import com.yapp.project.mission.domain.repository.MissionRepository;
import com.yapp.project.organization.domain.Organization;
import com.yapp.project.organization.domain.dto.OrgDto;
import com.yapp.project.organization.domain.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final OrganizationRepository organizationRepository;
    private final MissionRepository missionRepository;



    @Transactional
    public List<OrgDto.OrgResponse> findAll(Account account){
        ArrayList<Long> excludeOrganization = getMyOrganizationId(account);

        List<Organization> organizations;
        if (!excludeOrganization.isEmpty())
            organizations = organizationRepository.findOrganizationsNotIn(excludeOrganization);
        else
            organizations = organizationRepository.findAll();

        return toOrgResponseList(organizations);
    }

    @Transactional
    public OrgDto.OrgDetailResponse detailGroup(Long id){
        Organization organization = organizationRepository.findById(id).orElse(null);
        assert organization!=null;
        return organization.toDetailResponseDto();
    }

    @Transactional
    public List<OrgDto.OrgResponse> findByCategory(String category, Account account){
        ArrayList<Long> excludeOrganization = getMyOrganizationId(account);

        List<Organization> organizations;
        if (!excludeOrganization.isEmpty())
            organizations= organizationRepository.findByCategoryAndMoreAndNotIn(category, excludeOrganization);
        else
            organizations = organizationRepository.findByCategoryAndMore(category);

        return toOrgResponseList(organizations);
    }

    private ArrayList<Long> getMyOrganizationId(Account account){
        ArrayList<MissionOrganization> missions = missionRepository.findMissionByAccountAndIsFinishIsFalse(account);
        return (ArrayList<Long>) missions.stream().map(x -> x.getOrganization().getId()).collect(Collectors.toList());
    }

    private List<OrgDto.OrgResponse> toOrgResponseList(List<Organization> organizations){
        return organizations.stream().map(Organization::toResponseDto).collect(Collectors.toList());
    }
}
