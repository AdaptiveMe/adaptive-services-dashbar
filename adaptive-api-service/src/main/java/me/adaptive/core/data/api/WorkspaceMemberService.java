/*
 * Copyright 2014-2015. Adaptive.me.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.adaptive.core.data.api;

import me.adaptive.core.data.domain.UserEntity;
import me.adaptive.core.data.domain.WorkspaceEntity;
import me.adaptive.core.data.domain.WorkspaceMemberEntity;
import me.adaptive.core.data.repo.WorkspaceMemberRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.che.api.workspace.server.dao.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by panthro on 08/06/15.
 */
@Service
public class WorkspaceMemberService {
    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;
    @Autowired
    private WorkspaceEntityService workspaceEntityService;
    @Autowired
    private UserEntityService userService;

    public Set<WorkspaceMemberEntity> findByUserId(String id) {
        return workspaceMemberRepository.findByUserUserId(id);
    }

    public void delete(Iterable<WorkspaceMemberEntity> entities) {
        workspaceMemberRepository.delete(entities);
    }

    public void delete(WorkspaceMemberEntity entity) {
        workspaceMemberRepository.delete(entity);
    }

    public Member toMember(WorkspaceMemberEntity workspaceMemberEntity){
        List<String> roles = new ArrayList<>(workspaceMemberEntity.getRoles().size());
        CollectionUtils.addAll(roles, workspaceMemberEntity.getRoles().iterator());
        return new Member().withWorkspaceId(workspaceMemberEntity.getWorkspace().getWorkspaceId()).withUserId(workspaceMemberEntity.getUser().getUserId()).withRoles(roles);
    }


    public List<Member> toMemberList(Set<WorkspaceMemberEntity> workspaceMemberEntities) {
        List<Member> members = new ArrayList<>(workspaceMemberEntities.size());
        members.addAll(workspaceMemberEntities.stream().map(this::toMember).collect(Collectors.toList()));
        return members;
    }

    public Set<WorkspaceMemberEntity> findByWorkspace(WorkspaceEntity workspaceEntity) {
        return workspaceMemberRepository.findByWorkspace(workspaceEntity);
    }

    public Optional<WorkspaceMemberEntity> findByUserIdAndWorkspaceId(String userId, String workspaceId) {
        return workspaceMemberRepository.findByUserUserIdAndWorkspaceWorkspaceId(userId, workspaceId);
    }

    public WorkspaceMemberEntity save(WorkspaceMemberEntity workspaceMemberEntity){
        return workspaceMemberRepository.save(workspaceMemberEntity);
    }

    public WorkspaceMemberEntity toWorkspaceMemberEntity(Member member, Optional<WorkspaceMemberEntity> workspaceMemberEntity) {
        WorkspaceMemberEntity entity = workspaceMemberEntity.isPresent() ? workspaceMemberEntity.get() : new WorkspaceMemberEntity();
        CollectionUtils.addAll(entity.getRoles(), member.getRoles().iterator());
        entity.setUser(member.getUserId() == null ? null : userService.findByUserId(member.getUserId()).get());
        entity.setWorkspace(member.getWorkspaceId() == null ? null : workspaceEntityService.findByWorkspaceId(member.getWorkspaceId()).get());
        return entity;
    }

    public Set<WorkspaceMemberEntity> findByWorkspaceId(String workspaceId) {
        return workspaceMemberRepository.findByWorkspaceWorkspaceId(workspaceId);
    }

    public WorkspaceMemberEntity create(Member member) {
        Optional<UserEntity> userEntity = userService.findByUserId(member.getUserId());
        WorkspaceMemberEntity workspaceMemberEntity = new WorkspaceMemberEntity();
        CollectionUtils.addAll(workspaceMemberEntity.getRoles(), member.getRoles().iterator());
        Optional<WorkspaceEntity> workspace = workspaceEntityService.findByWorkspaceId(member.getWorkspaceId());
        workspaceMemberEntity.setUser(userEntity.get());
        workspaceMemberEntity.setWorkspace(workspace.get());
        return workspaceMemberRepository.save(workspaceMemberEntity);
    }
}
