package com.splitwise.splitwise.backend.repository;

import com.splitwise.splitwise.backend.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember,Long> {
    List<GroupMember> findByGroupId(Long groupId);
}
