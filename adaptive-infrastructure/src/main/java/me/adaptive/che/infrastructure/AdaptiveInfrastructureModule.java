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

package me.adaptive.che.infrastructure;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.spring.SpringIntegration;
import me.adaptive.che.infrastructure.dao.*;
import me.adaptive.core.data.SpringContextHolder;
import org.eclipse.che.api.account.server.ResourcesManager;
import org.eclipse.che.api.account.server.SubscriptionService;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.PlanDao;
import org.eclipse.che.api.auth.AuthenticationDao;
import org.eclipse.che.api.factory.FactoryStore;
import org.eclipse.che.api.user.server.TokenValidator;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.api.workspace.server.dao.MemberDao;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.inject.DynaModule;
import org.springframework.beans.factory.BeanFactory;

import java.util.Map;

@DynaModule
public class AdaptiveInfrastructureModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BeanFactory.class).toInstance(SpringContextHolder.getApplicationContext());
        bind(UserDao.class).toProvider(SpringIntegration.fromSpring(UserDao.class, "UserDao"));
        bind(WorkspaceDao.class).toProvider(SpringIntegration.fromSpring(AdaptiveWorkspaceDao.class,"adaptiveWorkspaceDao"));
        bind(UserProfileDao.class).toProvider(SpringIntegration.fromSpring(AdaptiveProfileDao.class,"adaptiveProfileDao"));
        bind(PreferenceDao.class).toProvider(SpringIntegration.fromSpring(AdaptivePreferenceDao.class, "adaptivePreferenceDao"));

        //TODO find a batter way
        Multibinder<SubscriptionService> multiBinder = Multibinder.newSetBinder(binder(), SubscriptionService.class);
        Map<String, SubscriptionService> services = SpringContextHolder.getApplicationContext().getBeansOfType(SubscriptionService.class);
        services.entrySet().forEach(entry -> multiBinder.addBinding().toInstance(entry.getValue()));


        bind(MemberDao.class).toProvider(SpringIntegration.fromSpring(WorkspaceMemberDao.class, "workspaceMemberDao"));
        bind(ResourcesManager.class).toProvider(SpringIntegration.fromSpring(AdaptiveResourcesManager.class, "adaptiveResourcesManager"));
        bind(PlanDao.class).toProvider(SpringIntegration.fromSpring(AdaptivePlanDao.class, "adaptivePlanDao"));
        bind(AccountDao.class).toProvider(SpringIntegration.fromSpring(AccountDao.class, "AccountDao"));
        bind(AuthenticationDao.class).toProvider(SpringIntegration.fromSpring(AdaptiveAuthenticationDao.class,"adaptiveAuthenticationDao"));
        bind(FactoryStore.class).to(InMemoryFactoryStore.class);
        bind(TokenValidator.class).toProvider(SpringIntegration.fromSpring(AdaptiveTokenValidator.class, "adaptiveTokenValidator"));

        SpringIntegration.bindAll(binder(), SpringContextHolder.getApplicationContext());
    }

}
