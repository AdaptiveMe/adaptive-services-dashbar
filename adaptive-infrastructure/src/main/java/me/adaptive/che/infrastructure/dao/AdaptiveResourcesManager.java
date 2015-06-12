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

package me.adaptive.che.infrastructure.dao;

import org.eclipse.che.api.account.server.ResourcesManager;
import org.eclipse.che.api.account.shared.dto.UpdateResourcesDescriptor;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by panthro on 12/06/15.
 */
@Service("adaptiveResourcesManager")
public class AdaptiveResourcesManager implements ResourcesManager {
    @Override
    public void redistributeResources(String accountId, List<UpdateResourcesDescriptor> updateResourcesDescriptors) throws NotFoundException, ServerException, ConflictException, ForbiddenException {

    }
}
