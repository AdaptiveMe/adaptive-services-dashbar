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

import org.eclipse.che.api.account.server.dao.PlanDao;
import org.eclipse.che.api.account.server.dto.DtoServerImpls;
import org.eclipse.che.api.account.shared.dto.Plan;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Created by panthro on 12/06/15.
 */
@Service("adaptivePlanDao")
public class AdaptivePlanDao implements PlanDao {
    @Override
    public Plan getPlanById(String planId) throws NotFoundException, ServerException {
        return new DtoServerImpls.PlanImpl().withId(planId);
    }

    @Override
    public List<Plan> getPlans() throws ServerException {
        return Collections.emptyList();
    }
}
