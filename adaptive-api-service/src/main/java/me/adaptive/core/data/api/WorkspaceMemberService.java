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

import me.adaptive.core.data.domain.WorkspaceEntity;
import me.adaptive.core.data.domain.WorkspaceMemberEntity;
import me.adaptive.core.data.repo.WorkspaceMemberRepository;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.che.api.workspace.server.dao.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public List<WorkspaceMemberEntity> findByUserEmailAndRolesContains(String email, String role) {
        return workspaceMemberRepository.findByUserEmailAndRolesContains(email, role);
    }

    public List<WorkspaceMemberEntity> findAll() {
        return workspaceMemberRepository.findAll();
    }

    public List<WorkspaceMemberEntity> findByUserEmail(String userEmail) {
        return workspaceMemberRepository.findByUserEmail(userEmail);
    }

    public List<WorkspaceMemberEntity> findByUserId(Long id) {
        return workspaceMemberRepository.findByUserId(id);
    }

    public void delete(Iterable<WorkspaceMemberEntity> entities) {
        workspaceMemberRepository.delete(entities);
    }

    public void delete(WorkspaceMemberEntity entity) {
        workspaceMemberRepository.delete(entity);
    }

    public Member toMember(WorkspaceMemberEntity workspaceMemberEntity){
        List<String> roles = new ArrayList<String>(workspaceMemberEntity.getRoles().size());
        CollectionUtils.addAll(roles, workspaceMemberEntity.getRoles().iterator());
        return new Member().withWorkspaceId(workspaceMemberEntity.getWorkspace().getId().toString()).withUserId(workspaceMemberEntity.getUser().getEmail()).withRoles(roles);
    }


    public List<Member> toMemberList(List<WorkspaceMemberEntity> workspaceMemberEntities){
        List<Member> members = new ArrayList<Member>(workspaceMemberEntities.size());
        for(WorkspaceMemberEntity workspaceMemberEntity : workspaceMemberEntities){
            members.add(toMember(workspaceMemberEntity));
        }
        return members;
    }

    public List<WorkspaceMemberEntity> findByWorkspace(WorkspaceEntity workspaceEntity) {
        return workspaceMemberRepository.findByWorkspace(workspaceEntity);
    }

    public WorkspaceMemberEntity findByUserIdAndWorkspaceId(Long userId, Long workspaceId) {
        return workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspaceId);
    }

    public WorkspaceMemberEntity save(WorkspaceMemberEntity workspaceMemberEntity){
        return workspaceMemberRepository.save(workspaceMemberEntity);
    }

    public WorkspaceMemberEntity toWorkspaceMemberEntity(Member member){
        WorkspaceMemberEntity workspaceMemberEntity = new WorkspaceMemberEntity();
        CollectionUtils.addAll(workspaceMemberEntity.getRoles(),member.getRoles().iterator());
        workspaceMemberEntity.setUser(userService.findOne(Long.valueOf(member.getUserId())));
        workspaceMemberEntity.setWorkspace(workspaceEntityService.findOne(Long.valueOf(member.getWorkspaceId())));
        return workspaceMemberEntity;
    }
}
