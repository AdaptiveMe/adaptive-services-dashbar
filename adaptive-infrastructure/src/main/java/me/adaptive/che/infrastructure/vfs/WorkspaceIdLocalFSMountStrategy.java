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
 *
 *
 */

package me.adaptive.che.infrastructure.vfs;

import me.adaptive.core.data.api.WorkspaceEntityService;
import me.adaptive.core.data.domain.WorkspaceEntity;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.vfs.impl.fs.LocalFSMountStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Optional;

/**
 * Created by panthro on 19/06/15.
 */
@Service("workspaceIdLocalFSMountStrategy")
public class WorkspaceIdLocalFSMountStrategy implements LocalFSMountStrategy {

    @Autowired
    private WorkspaceEntityService workspaceEntityService;

    @Value("#{environment.CHE_VFS_ROOT}")
    private String vfsRoot;
    private File root;

    @Override
    public File getMountPath(String workspaceId) throws ServerException {
        checkRoot();
        Optional<WorkspaceEntity> optional = workspaceEntityService.findByWorkspaceId(workspaceId);
        if (!optional.isPresent()) {
            throw new ServerException(String.format("Workspace %s does not exist", workspaceId));
        }
        return getMountPath(optional.get());
    }

    @Override
    public File getMountPath() throws ServerException {
        return getMountPath(EnvironmentContext.getCurrent().getWorkspaceId());
    }

    private File getMountPath(WorkspaceEntity workspaceEntity) {
        File mountPath = new File(root, DigestUtils.md5Hex(workspaceEntity.getId().toString()));
        return mountPath;
    }

    @PostConstruct
    private void init() {
        checkRoot();
    }

    private void checkRoot() {
        if (root == null) {
            if (vfsRoot == null) {
                throw new RuntimeException("CHE_VFS_ROOT not set. Please set CHE_VFS_ROOT environment variable. eg: export CHE_VFS_ROOT=/tmp/che-root");
            }
            root = new File(vfsRoot);
        }
        if (!root.exists()) {
            if (root.getParentFile().exists() && root.getParentFile().canWrite()) {
                root.mkdir();
            } else {
                throw new RuntimeException(String.format("VFS Root %s does not exist and cannot be created", vfsRoot));
            }
        } else {
            if (!root.canWrite()) {
                throw new RuntimeException(String.format("CHE_VFS_ROOT location %s is not writable", root.getAbsolutePath()));
            }
        }
    }
}
