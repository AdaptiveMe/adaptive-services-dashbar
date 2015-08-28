
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
package me.adaptive.dashbar.api.assembly;

import com.google.inject.AbstractModule;
import com.google.inject.spring.SpringIntegration;
import me.adaptive.che.infrastructure.api.MetricsModule;
import me.adaptive.che.infrastructure.api.RegistrationModule;
import me.adaptive.che.infrastructure.service.AdaptiveBuildQueue;
import me.adaptive.che.infrastructure.vfs.WorkspaceIdLocalFSMountStrategy;
import me.adaptive.core.data.api.UserRegistrationService;
import org.eclipse.che.api.account.server.AccountService;
import org.eclipse.che.api.analytics.AnalyticsModule;
import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.BuilderSelectionStrategy;
import org.eclipse.che.api.builder.BuilderService;
import org.eclipse.che.api.builder.LastInUseBuilderSelectionStrategy;
import org.eclipse.che.api.builder.internal.BuilderModule;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.CoreRestModule;
import org.eclipse.che.api.factory.FactoryModule;
import org.eclipse.che.api.project.server.BaseProjectModule;
import org.eclipse.che.api.project.server.ProjectService;
import org.eclipse.che.api.runner.internal.RunnerModule;
import org.eclipse.che.api.user.server.UserProfileService;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.vfs.server.VirtualFileSystemModule;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.docs.DocsModule;
import org.eclipse.che.everrest.CodenvyAsynchronousJobPool;
import org.eclipse.che.everrest.ETagResponseFilter;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.vfs.impl.fs.AutoMountVirtualFileSystemRegistry;
import org.eclipse.che.vfs.impl.fs.LocalFSMountStrategy;
import org.eclipse.che.vfs.impl.fs.VirtualFileSystemFSModule;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.PathKey;

@DynaModule
public class ApiModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(ApiExceptionMapper.class);

        bind(ApiInfoService.class);

        bind(AuthenticationService.class);
        bind(WorkspaceService.class);
        bind(ProjectService.class);

        bind(AccountService.class); //TODO Implement Account/Subscription/Plan
        bind(ETagResponseFilter.class);

        bind(UserService.class);
        bind(UserRegistrationService.class).toProvider(SpringIntegration.fromSpring(UserRegistrationService.class, "userRegistrationService"));
        bind(RegistrationModule.class);
        bind(MetricsModule.class);
        bind(UserProfileService.class);

        bind(VirtualFileSystemRegistry.class).to(AutoMountVirtualFileSystemRegistry.class);

        //Important it will create automatically a workspace folder upon workspace creation
        bind(LocalFSMountStrategy.class).toProvider(SpringIntegration.fromSpring(WorkspaceIdLocalFSMountStrategy.class, "workspaceIdLocalFSMountStrategy"));
        //bind(WorkspaceToDirectoryMappingService.class);

        bind(BuilderSelectionStrategy.class).to(LastInUseBuilderSelectionStrategy.class);
        bind(BuilderService.class);
        bind(BuildQueue.class).to(AdaptiveBuildQueue.class);
        //bind(BuilderAdminService.class);
        //bind(SlaveBuilderService.class);

        //bind(RunnerSelectionStrategy.class).to(LastInUseRunnerSelectionStrategy.class);
        //bind(RunnerService.class);
        //bind(RunnerAdminService.class);
        //bind(SlaveRunnerService.class);

        //bind(DebuggerService.class);
        //bind(FormatService.class);

        //bind(KeyService.class);
        //bind(SshKeyStore.class).to(UserProfileSshKeyStore.class);

        //bind(OAuthAuthenticationService.class);
        //bind(OAuthTokenProvider.class).to(OAuthAuthenticatorTokenProvider.class);
        //bind(OAuthAuthenticatorProvider.class).to(OAuthAuthenticatorProviderImpl.class);


        //bind(RestNameEnvironment.class);
        //bind(JavadocService.class);
        //bind(JavaNavigationService.class);
        bind(AsynchronousJobPool.class).to(CodenvyAsynchronousJobPool.class);
        bind(new PathKey<>(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        //install(new ArchetypeGeneratorModule());

        install(new CoreRestModule());
        install(new AnalyticsModule());
        install(new BaseProjectModule());
        install(new BuilderModule());
        install(new RunnerModule());
        install(new VirtualFileSystemModule());
        install(new VirtualFileSystemFSModule());
        install(new FactoryModule());
        install(new DocsModule());
    }
}
