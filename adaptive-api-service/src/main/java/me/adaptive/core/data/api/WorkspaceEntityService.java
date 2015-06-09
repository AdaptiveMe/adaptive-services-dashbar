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
import me.adaptive.core.data.repo.WorkspaceEntityRepository;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panthro on 08/06/15.
 */
@Service
public class WorkspaceEntityService {

    @Autowired
    private WorkspaceEntityRepository workspaceEntityRepository;
    @Autowired
    private AccountEntityService accountEntityService;

    public WorkspaceEntity findByName(String name) {
        return workspaceEntityRepository.findByName(name);
    }

    public List<WorkspaceEntity> findByAccountId(Long accountId) {
        return workspaceEntityRepository.findByAccountId(accountId);
    }

    public List<WorkspaceEntity> findAll() {
        return workspaceEntityRepository.findAll();
    }

    public WorkspaceEntity findOne(Long aLong) {
        return workspaceEntityRepository.findOne(aLong);
    }

    public boolean exists(Long aLong) {
        return workspaceEntityRepository.exists(aLong);
    }

    public WorkspaceEntity toWorkspaceEntity(Workspace workspace){
        WorkspaceEntity entity = new WorkspaceEntity();
        entity.setName(workspace.getName());
        entity.setId(workspace.getId() != null ? Long.valueOf(workspace.getId()) : null);
        entity.setTemporary(workspace.isTemporary());
        entity.setAttributes(workspace.getAttributes());
        entity.setAccount(workspace.getAccountId() != null ? accountEntityService.findOne(Long.valueOf(workspace.getAccountId())) : null);
        return entity;
    }

    public WorkspaceEntity create(WorkspaceEntity workspaceEntity) {
        return workspaceEntityRepository.save(workspaceEntity);
    }

    public WorkspaceEntity upate(WorkspaceEntity workspaceEntity) {
        return workspaceEntityRepository.save(workspaceEntity);
    }

    public void delete(Long id) {
        workspaceEntityRepository.delete(id);
    }

    public Workspace toWorkspace(WorkspaceEntity entity) {
        return new Workspace().withId(entity.getId() == null ? null : entity.getId().toString())
                .withName(entity.getName())
                .withAccountId(entity.getAccount() == null ? null : entity.getAccount().getId().toString())
                .withAttributes(entity.getAttributes())
                .withTemporary(entity.isTemporary());
    }

    public List<Workspace> toWorkspaceList(List<WorkspaceEntity> workspaceEntities) {
        List<Workspace> workspaces = new ArrayList<Workspace>(workspaceEntities.size());
        for(WorkspaceEntity entity : workspaceEntities){
            workspaces.add(toWorkspace(entity));
        }
        return workspaces;
    }
}
