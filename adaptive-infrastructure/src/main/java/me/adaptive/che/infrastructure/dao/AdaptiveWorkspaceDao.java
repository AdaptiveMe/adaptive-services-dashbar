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
import java.util.regex.Pattern;

@Service("adaptiveWorkspaceDao")
public class AdaptiveWorkspaceDao implements WorkspaceDao {
    private static final Pattern WS_NAME = Pattern.compile("[\\w][\\w\\.\\-]{1,18}[\\w]");

    @Autowired
    private WorkspaceEntityService workspaceEntityService;

    @Override
    public void create(Workspace workspace) throws ConflictException {
        validateWorkspaceName(workspace.getName());
        if(workspaceEntityService.exists(Long.valueOf(workspace.getId()))){
            throw new ConflictException(String.format("Workspace with id %s already exists.", workspace.getId()));
        }

        if (workspaceEntityService.findByName(workspace.getName()) != null) {
            throw new ConflictException(String.format("Workspace with name %s already exists.", workspace.getName()));
        }
        workspaceEntityService.create(workspaceEntityService.toWorkspaceEntity(workspace));
    }

    @Override
    public void update(Workspace workspace) throws NotFoundException, ConflictException {

        if(!workspaceEntityService.exists(Long.valueOf(workspace.getId()))){
            throw new NotFoundException(String.format("Workspace not found %s", workspace.getId()));

        }
        workspaceEntityService.upate(workspaceEntityService.toWorkspaceEntity(workspace));
    }

    @Override
    public void remove(String id) throws NotFoundException {
        if(!workspaceEntityService.exists(Long.valueOf(id))){
            throw new NotFoundException(String.format("Workspace not found %s", id));

        }
        workspaceEntityService.delete(Long.valueOf(id));
    }

    @Override
    public Workspace getById(String id) throws NotFoundException {
        WorkspaceEntity entity = workspaceEntityService.findOne(Long.valueOf(id));
        if(entity == null) {
            throw new NotFoundException(String.format("Workspace not found %s", id));
        }
        return workspaceEntityService.toWorkspace(entity);
    }

    @Override
    public Workspace getByName(String name) throws NotFoundException {
        WorkspaceEntity entity = workspaceEntityService.findByName(name);
        if(entity == null) {
            throw new NotFoundException(String.format("Workspace not found %s", name));
        }
        return workspaceEntityService.toWorkspace(entity);
    }

    @Override
    public List<Workspace> getByAccount(String accountId) {
        return workspaceEntityService.toWorkspaceList(workspaceEntityService.findByAccountId(Long.valueOf(accountId)));
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