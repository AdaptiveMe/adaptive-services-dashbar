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
 */ge me.adaptive.che.infrastructure;

import com.google.inject.AbstractModule;
import com.google.inject.spring.SpringIntegration;
import me.adaptive.che.infrastructure.dao.*;
import me.adaptive.core.data.SpringContextHolder;
import org.eclipse.che.api.account.server.dao.AccountDao;
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

@DynaModule
public class AdaptiveInfrastructureModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BeanFactory.class).toInstance(SpringContextHolder.getApplicationContext());
        bind(UserDao.class).toProvider(SpringIntegration.fromSpring(UserDao.class, "UserDao"));
        bind(WorkspaceDao.class).toProvider(SpringIntegration.fromSpring(AdaptiveWorkspaceDao.class,"adaptiveWorkspaceDao"));
        bind(UserProfileDao.class).toProvider(SpringIntegration.fromSpring(AdaptiveProfileDao.class,"adaptiveProfileDao"));
        bind(PreferenceDao.class).to(LocalPreferenceDaoImpl.class);
        bind(MemberDao.class).toProvider(SpringIntegration.fromSpring(WorkspaceMemberDao.class,"workspaceMemberDao"));
        bind(AccountDao.class).toProvider(SpringIntegration.fromSpring(AccountDao.class, "AccountDao"));
        bind(AuthenticationDao.class).toProvider(SpringIntegration.fromSpring(AdaptiveAuthenticationDao.class,"adaptiveAuthenticationDao"));
        bind(FactoryStore.class).to(InMemoryFactoryStore.class);
        bind(TokenValidator.class).toProvider(SpringIntegration.fromSpring(AdaptiveTokenValidator.class, "adaptiveTokenValidator"));
    }

}
