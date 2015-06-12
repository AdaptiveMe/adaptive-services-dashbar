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

package me.adaptive.che.infrastructure.service;

import org.eclipse.che.api.account.server.SubscriptionService;
import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.account.shared.dto.UsedAccountResources;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.springframework.stereotype.Service;

/**
 * Created by panthro on 12/06/15.
 */
@Service("adaptiveSubscriptionService")
public class AdaptiveSubscriptionService extends SubscriptionService {

    public AdaptiveSubscriptionService() {
        super("adaptive-service-id", "adaptive-display-name");
    }

    @Override
    public void beforeCreateSubscription(Subscription subscription) throws ConflictException, ServerException, ForbiddenException {

    }

    @Override
    public void afterCreateSubscription(Subscription subscription) throws ApiException {

    }

    @Override
    public void onRemoveSubscription(Subscription subscription) throws ApiException {

    }

    @Override
    public void onUpdateSubscription(Subscription oldSubscription, Subscription newSubscription) throws ApiException {

    }

    @Override
    public void onCheckSubscriptions() throws ApiException {

    }

    @Override
    public UsedAccountResources getAccountResources(Subscription subscription) throws ServerException {
        return null;
    }
}
