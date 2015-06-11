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

package me.adaptive.core.data.repo;

import me.adaptive.core.data.domain.WorkspaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Created by panthro on 04/06/15.
 */
@Repository
public interface WorkspaceEntityRepository extends JpaRepository<WorkspaceEntity, Long>, JpaSpecificationExecutor<WorkspaceEntity> {

    Optional<WorkspaceEntity> findByName(String name);

    Set<WorkspaceEntity> findByAccountAccountId(String accountId);

    Optional<WorkspaceEntity> findByWorkspaceId(String workspaceId);
}
