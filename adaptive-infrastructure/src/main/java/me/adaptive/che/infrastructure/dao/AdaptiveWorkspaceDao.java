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
import me.adaptive.core.data.domain.WorkspaceEntity;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service("adaptiveWorkspaceDao")
public class AdaptiveWorkspaceDao implements WorkspaceDao {
    private static final Pattern WS_NAME = Pattern.compile("[\\w][\\w\\.\\-]{1,18}[\\w]");

    @Autowired
    private WorkspaceEntityService workspaceEntityService;

    @Override
    public void create(Workspace workspace) throws ConflictException {
        validateWorkspaceName(workspace.getName());
        if (workspace.getId() != null && workspaceEntityService.findByWorkspaceId(workspace.getId()).isPresent()) {
            throw new ConflictException(String.format("Workspace with id %s already exists.", workspace.getId()));
        }

        if (workspace.getName() != null && workspaceEntityService.findByName(workspace.getName()).isPresent()) {
            throw new ConflictException(String.format("Workspace with name %s already exists.", workspace.getName()));
        }
        workspaceEntityService.create(workspaceEntityService.toWorkspaceEntity(workspace, Optional.<WorkspaceEntity>empty()));
    }

    @Override
    public void update(Workspace workspace) throws NotFoundException, ConflictException {
        Optional<WorkspaceEntity> workspaceEntity = workspaceEntityService.findByWorkspaceId(workspace.getId());

        if (!workspaceEntity.isPresent()) {
            throw new NotFoundException(String.format("Workspace not found %s", workspace.getId()));

        }
        workspaceEntityService.update(workspaceEntityService.toWorkspaceEntity(workspace, workspaceEntity));
    }

    @Override
    public void remove(String id) throws NotFoundException {
        Optional<WorkspaceEntity> workspaceEntity = workspaceEntityService.findByWorkspaceId(id);
        if (!workspaceEntity.isPresent()) {
            throw new NotFoundException(String.format("Workspace not found %s", id));

        }
        workspaceEntityService.delete(workspaceEntity.get());
    }

    @Override
    public Workspace getById(String id) throws NotFoundException {
        Optional<WorkspaceEntity> entity = workspaceEntityService.findByWorkspaceId(id);
        if (!entity.isPresent()) {
            throw new NotFoundException(String.format("Workspace not found %s", id));
        }
        return workspaceEntityService.toWorkspace(entity.get());
    }

    @Override
    public Workspace getByName(String name) throws NotFoundException {
        Optional<WorkspaceEntity> entity = workspaceEntityService.findByName(name);
        if (!entity.isPresent()) {
            throw new NotFoundException(String.format("Workspace not found %s", name));
        }
        return workspaceEntityService.toWorkspace(entity.get());
    }

    @Override
    public List<Workspace> getByAccount(String accountId) {
        return workspaceEntityService.toWorkspaceList(workspaceEntityService.findByAccountId(accountId));
    }

    private void validateWorkspaceName(String workspaceName) throws ConflictException {
        if (workspaceName == null) {
            throw new ConflictException("Workspace name required");
        }
        if (!WS_NAME.matcher(workspaceName).matches()) {
            throw new ConflictException("Incorrect workspace name");
        }
    }
}