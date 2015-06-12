
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
import com.google.inject.name.Names;
import me.adaptive.core.data.SpringContextHolder;
import me.adaptive.core.data.api.WorkspaceEntityService;
import me.adaptive.core.data.domain.WorkspaceEntity;
import org.eclipse.che.api.account.server.AccountService;
import org.eclipse.che.api.analytics.AnalyticsModule;
import org.eclipse.che.api.auth.AuthenticationService;
import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.builder.BuilderAdminService;
import org.eclipse.che.api.builder.BuilderSelectionStrategy;
import org.eclipse.che.api.builder.BuilderService;
import org.eclipse.che.api.builder.LastInUseBuilderSelectionStrategy;
import org.eclipse.che.api.builder.internal.BuilderModule;
import org.eclipse.che.api.builder.internal.SlaveBuilderService;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.CoreRestModule;
import org.eclipse.che.api.factory.FactoryModule;
import org.eclipse.che.api.project.server.BaseProjectModule;
import org.eclipse.che.api.project.server.ProjectService;
import org.eclipse.che.api.runner.LastInUseRunnerSelectionStrategy;
import org.eclipse.che.api.runner.RunnerAdminService;
import org.eclipse.che.api.runner.RunnerSelectionStrategy;
import org.eclipse.che.api.runner.RunnerService;
import org.eclipse.che.api.runner.internal.RunnerModule;
import org.eclipse.che.api.runner.internal.SlaveRunnerService;
import org.eclipse.che.api.user.server.UserProfileService;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.vfs.server.VirtualFileSystemModule;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.docs.DocsModule;
import org.eclipse.che.everrest.CodenvyAsynchronousJobPool;
import org.eclipse.che.everrest.ETagResponseFilter;
import org.eclipse.che.generator.archetype.ArchetypeGeneratorModule;
import org.eclipse.che.ide.ext.java.jdi.server.DebuggerService;
import org.eclipse.che.ide.ext.java.server.format.FormatService;
import org.eclipse.che.ide.ext.ssh.server.KeyService;
import org.eclipse.che.ide.ext.ssh.server.SshKeyStore;
import org.eclipse.che.ide.ext.ssh.server.UserProfileSshKeyStore;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.jdt.JavaNavigationService;
import org.eclipse.che.jdt.JavadocService;
import org.eclipse.che.jdt.RestNameEnvironment;
import org.eclipse.che.security.oauth.OAuthAuthenticationService;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProvider;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProviderImpl;
import org.eclipse.che.security.oauth.OAuthAuthenticatorTokenProvider;
import org.eclipse.che.vfs.impl.fs.*;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.PathKey;

import java.util.ArrayList;
import java.util.List;

@DynaModule
public class ApiModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ApiInfoService.class);

        bind(AuthenticationService.class);
        bind(WorkspaceService.class);
        bind(ProjectService.class);

        bind(AccountService.class); //TODO Implement Account/Subscription/Plan
        bind(ETagResponseFilter.class);

        bind(UserService.class);
        bind(UserProfileService.class);

        //TODO find a better way to do this
        List<WorkspaceEntity> workspaces = SpringContextHolder.getApplicationContext().getBean(WorkspaceEntityService.class).findAll();
        List<String> ids = new ArrayList<>(workspaces.size());
        workspaces.stream().forEach(workspaceEntity -> ids.add(workspaceEntity.getWorkspaceId()));
        bind(String[].class).annotatedWith(Names.named("vfs.local.id")).toInstance(ids.toArray(new String[ids.size()]));


        bind(LocalFileSystemRegistryPlugin.class);

        //Important it will create automatically a workspace folder upon workspace creation
        bind(LocalFSMountStrategy.class).to(WorkspaceHashLocalFSMountStrategy.class);
        bind(WorkspaceToDirectoryMappingService.class);

        bind(BuilderSelectionStrategy.class).to(LastInUseBuilderSelectionStrategy.class);
        bind(BuilderService.class);
        bind(BuilderAdminService.class);
        bind(SlaveBuilderService.class);

        bind(RunnerSelectionStrategy.class).to(LastInUseRunnerSelectionStrategy.class);
        bind(RunnerService.class);
        bind(RunnerAdminService.class);
        bind(SlaveRunnerService.class);

        bind(DebuggerService.class);
        bind(FormatService.class);

        bind(KeyService.class);
        bind(SshKeyStore.class).to(UserProfileSshKeyStore.class);

        bind(OAuthAuthenticationService.class);
        bind(OAuthTokenProvider.class).to(OAuthAuthenticatorTokenProvider.class);
        bind(OAuthAuthenticatorProvider.class).to(OAuthAuthenticatorProviderImpl.class);


        bind(RestNameEnvironment.class);
        bind(JavadocService.class);
        bind(JavaNavigationService.class);
        bind(AsynchronousJobPool.class).to(CodenvyAsynchronousJobPool.class);
        bind(new PathKey<>(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        install(new ArchetypeGeneratorModule());

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
