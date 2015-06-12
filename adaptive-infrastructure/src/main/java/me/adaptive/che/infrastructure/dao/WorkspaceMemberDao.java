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

package me.adaptive.che.infrastructure.dao;

import me.adaptive.core.data.api.WorkspaceEntityService;
import me.adaptive.core.data.api.WorkspaceMemberService;
import me.adaptive.core.data.domain.WorkspaceMemberEntity;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.workspace.server.dao.Member;
import org.eclipse.che.api.workspace.server.dao.MemberDao;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("workspaceMemberDao")
public class WorkspaceMemberDao implements MemberDao {

    @Autowired
    private WorkspaceDao workspaceDao;
    @Autowired
    private WorkspaceEntityService workspaceEntityService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private WorkspaceMemberService workspaceMemberService;

    @Override
    public void create(Member member) throws NotFoundException, ServerException, ConflictException {

        // Check workspace existence
        workspaceDao.getById(member.getWorkspaceId());
        // Check user existence
        userDao.getById(member.getUserId());
        if (workspaceMemberService.findByUserIdAndWorkspaceId(member.getUserId(), member.getWorkspaceId()).isPresent()) {
            throw new ConflictException(
                    String.format("Membership of user %s in workspace %s already exists. Use update method instead.",
                            member.getUserId(), member.getWorkspaceId()));
        }

        workspaceMemberService.save(workspaceMemberService.toWorkspaceMemberEntity(member, Optional.<WorkspaceMemberEntity>empty()));

    }

    @Override
    public void update(Member member) throws NotFoundException, ServerException {


        // Check workspace existence
        workspaceDao.getById(member.getWorkspaceId());
        // Check user existence
        userDao.getById(member.getUserId());
        Optional<WorkspaceMemberEntity> workspaceMemberEntity = workspaceMemberService.findByUserIdAndWorkspaceId(member.getUserId(), member.getWorkspaceId());
        if (!workspaceMemberEntity.isPresent()) {
            throw new NotFoundException(String.format("Unable to update membership: user %s has no memberships in workspace %s.",
                    member.getUserId(), member.getWorkspaceId()));
        }
        workspaceMemberService.save(workspaceMemberService.toWorkspaceMemberEntity(member, workspaceMemberEntity));
    }

    @Override
    public List<Member> getWorkspaceMembers(String wsId) {

        return workspaceMemberService.toMemberList(workspaceMemberService.findByWorkspaceId(wsId));

    }

    @Override
    public List<Member> getUserRelationships(String userId) {
        return workspaceMemberService.toMemberList(workspaceMemberService.findByUserId(userId));
    }

    @Override
    public Member getWorkspaceMember(String wsId, String userId) throws NotFoundException, ServerException {
        Optional<WorkspaceMemberEntity> workspaceMemberEntity = workspaceMemberService.findByUserIdAndWorkspaceId(userId, wsId);

        if (!workspaceMemberEntity.isPresent()) {
            throw new NotFoundException(String.format("User with id %s has no membership in workspace %s", userId, wsId));
        }

        return workspaceMemberService.toMember(workspaceMemberEntity.get());


    }

    @Override
    public void remove(Member member) throws NotFoundException {
        Optional<WorkspaceMemberEntity> workspaceMemberEntity = workspaceMemberService.findByUserIdAndWorkspaceId(member.getUserId(), member.getWorkspaceId());
        if (!workspaceMemberEntity.isPresent()) {
            throw new NotFoundException(String.format("Unable to update membership: user %s has no memberships in workspace %s.",
                    member.getUserId(), member.getWorkspaceId()));
        }
        workspaceMemberService.delete(workspaceMemberEntity.get());
    }
}
