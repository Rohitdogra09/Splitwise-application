package com.splitwise.splitwise.backend.controller;

import com.splitwise.splitwise.backend.entity.Group;
import com.splitwise.splitwise.backend.entity.GroupMember;
import com.splitwise.splitwise.backend.entity.User;
import com.splitwise.splitwise.backend.repository.GroupMemberRepository;
import com.splitwise.splitwise.backend.repository.GroupRepository;
import com.splitwise.splitwise.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupController(GroupRepository groupRepository,
                           UserRepository userRepository,
                           GroupMemberRepository groupMemberRepository){
        this.groupRepository=groupRepository;
        this.userRepository=userRepository;
        this.groupMemberRepository=groupMemberRepository;
    }
    //create group
    @PostMapping
    public Group createGroup(@RequestBody Group group){
        group.setId(null);
        return groupRepository.save(group);
    }

    //List groups
    @GetMapping
    public List<Group> getAllGroups(){
        return groupRepository.findAll();

    }

    //Add member to groups
    //Post /api/groups/{groud Id}/members body:{"userId": 1}

    @PostMapping("/{groupId}/members")
    public GroupMember addMember(@PathVariable Long groupId, @RequestBody Map<String, Long>body){
        Long userId = body.get("userId");
        if(userId==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserId is required");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));

        GroupMember gm=GroupMember.builder()
                .group(group)
                .user(user)
                .build();
        return groupMemberRepository.save(gm);
    }

    //List members of a group

    @GetMapping("/{groupId}/members")
    public List<GroupMember> listMembers(@PathVariable Long groupId){
        return groupMemberRepository.findByGroupId(groupId);
    }
}
