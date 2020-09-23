package com.arqaam.logframelab.service.auth;

import com.arqaam.logframelab.controller.dto.auth.GroupDto;
import com.arqaam.logframelab.model.persistence.auth.Group;
import com.arqaam.logframelab.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;
    @InjectMocks
    private GroupService groupService;

    @Test
    void findByGroupNameTest() {
        Group expected = new Group();
        String groupName = "GROUP NAME";
        when(groupRepository.findByName(groupName)).thenReturn(expected);

        Group result = groupService.findByGroupName(groupName);
        verify(groupRepository).findByName(groupName);
        assertEquals(expected, result);
    }

    @Test
    void getAllGroupsTest() {
        Set<GroupDto> expected = Collections.singleton(new GroupDto(1, "GROUP NAME"));
        when(groupRepository.findAllGroupsBy()).thenReturn(expected);

        Set<GroupDto> result = groupService.getAllGroups();
        verify(groupRepository).findAllGroupsBy();
        assertEquals(expected, result);
    }
}