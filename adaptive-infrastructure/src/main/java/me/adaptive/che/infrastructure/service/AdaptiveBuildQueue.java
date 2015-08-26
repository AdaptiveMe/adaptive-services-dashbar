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

import me.adaptive.core.data.api.UserEntityService;
import me.adaptive.core.data.api.WorkspaceEntityService;
import me.adaptive.core.data.domain.BuildRequestEntity;
import me.adaptive.core.data.domain.WorkspaceEntity;
import me.adaptive.core.data.domain.types.BuildRequestStatus;
import me.adaptive.core.data.repo.BuildRequestRepository;
import org.eclipse.che.api.builder.*;
import org.eclipse.che.api.builder.dto.*;
import org.eclipse.che.api.builder.internal.BuildTask;
import org.eclipse.che.api.builder.internal.Builder;
import org.eclipse.che.api.builder.internal.BuilderEvent;
import org.eclipse.che.api.builder.internal.BuilderRegistry;
import org.eclipse.che.api.core.*;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.ContentTypeGuesser;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.project.server.*;
import org.eclipse.che.api.project.shared.dto.BuilderConfiguration;
import org.eclipse.che.api.project.shared.dto.BuildersDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.vfs.impl.fs.LocalFSMountStrategy;
import org.everrest.guice.GuiceUriBuilderImpl;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.MoreObjects.firstNonNull;
import static me.adaptive.core.data.domain.types.BuildRequestStatus.*;

/**
 * Created by panthro on 21/08/15.
 */
@Singleton
public class AdaptiveBuildQueue implements BuildQueue {

    private final List<BuildRequestStatus> FINISHED_STATUSES = Arrays.asList(CANCELLED, SUCCESSFUL, FAILED);

    private static final String PLATFORM_OPTION = "platform";

    @Named("buildRequestRepository")
    @Inject
    private BuildRequestRepository buildRequestRepository;

    @Named("workspaceEntityService")
    @Inject
    private WorkspaceEntityService workspaceEntityService;

    @Named("userEntityService")
    @Inject
    private UserEntityService userEntityService;

    @Named("api.endpoint")
    @Inject
    private String baseProjectApiUrl;
    @Inject
    private LocalFSMountStrategy mountStrategy;

    @Named("adaptive.build.result.root")
    @Inject
    private String buildsRoot;
    @Named("adaptive.build.log.name")
    @Inject
    private String buildLogName;

    @Inject
    private EventService eventService;


    @Inject
    private DefaultProjectManager projectManager;


    @Inject
    private BuilderRegistry builderRegistry;

    private ExecutorService executor;


    @Override
    public int getTotalNum() {
        return 0;
    }

    @Override
    public int getWaitingNum() {
        return 0;
    }

    @Override
    public List<RemoteBuilderServer> getRegisterBuilderServers() {
        return Collections.emptyList();
    }

    @Override
    public boolean registerBuilderServer(BuilderServerRegistration registration) throws BuilderException {
        return false;
    }

    @Override
    public boolean unregisterBuilderServer(BuilderServerLocation location) throws BuilderException {
        return false;
    }

    @Override
    public BuildTaskDescriptor scheduleBuild(String wsId, String projectName, ServiceContext serviceContext, BuildOptions buildOptions) throws BuilderException {

        /**
         * FOR TESTING
         */
        Map<String, String> options = new HashMap<>(1);
        options.put(PLATFORM_OPTION, "android");
        buildOptions = buildOptions == null ? DtoFactory.getInstance().createDto(BuildOptions.class).withOptions(options) : buildOptions;


        if (buildOptions == null || buildOptions.getOptions() == null || !buildOptions.getOptions().containsKey(PLATFORM_OPTION)) {
            throw new BuilderException(PLATFORM_OPTION + " not specified");
        }
        //TODO check target (release, debug, etc)

        WorkspaceEntity workspaceEntity = workspaceEntityService.findByWorkspaceId(wsId).orElseThrow(() -> new BuilderException("Could not find workspace " + wsId));


        Project project;
        try {
            project = projectManager.getProject(wsId, projectName);
            if (project == null) {
                throw new BuilderException("Project " + projectName + " not found");
            }
            //TODO check if project has builder
        } catch (ForbiddenException e) {
            throw new BuilderException("User has no permissions to build the project");
        } catch (ServerException e) {
            throw new BuilderException("Error getting project", e);
        }
        User user = EnvironmentContext.getCurrent().getUser();
        if (user == null) {
            throw new BuilderException("No user found in the current context");
        }
        BuildRequestEntity buildRequestEntity = new BuildRequestEntity();
        buildRequestEntity.setPlatform(buildOptions.getOptions().get(PLATFORM_OPTION));
        buildRequestEntity.setAttributes(buildOptions.getOptions());
        buildRequestEntity.setProjectName(projectName);
        buildRequestEntity.setWorkspace(workspaceEntity);

        buildRequestEntity.setRequester(userEntityService.findByUserId(user.getId()).orElseThrow(() -> new BuilderException("User " + user.getId() + "not found")));

        buildRequestEntity.setStatus(IN_QUEUE);

        buildRequestEntity = buildRequestRepository.saveAndFlush(buildRequestEntity);
        final Builder builder = getBuilder(project);

        final ProjectDescriptor descriptor = getProjectDescription(wsId, projectName);
        final BuildRequest request = (BuildRequest) DtoFactory.getInstance().createDto(BuildRequest.class)
                .withBuilder(builder.getName())
                .withId(buildRequestEntity.getId())
                .withOptions(buildOptions.getOptions())
                .withProject(projectName)
                .withTargets(buildOptions.getTargets())
                .withWorkspace(project.getWorkspace())
                .withUserId(user.getId())
                .withProjectDescriptor(descriptor);
        fillRequestFromProjectDescriptor(descriptor, request);

        eventService.publish(BuilderEvent.queueStartedEvent(request.getId(), wsId, projectName));
        executor.submit(() -> builder.perform(request));

        //we have to publish events to the eventservice when request becomes IN_PROGRESS
        executor.submit(new BuildChangesNotifier(buildRequestEntity));

        try {
            return getDescriptor(builder, buildRequestEntity, serviceContext);
        } catch (NotFoundException e) {
            throw new BuilderException(e);
        }
    }

    private Builder getBuilder(Project project) throws BuilderException {
        final Builder builder;
        try {
            builder = builderRegistry.get(project.getConfig().getBuilders().getDefault());
        } catch (ProjectTypeConstraintException | ServerException | ValueStorageException | InvalidValueException e) {
            throw new BuilderException(e);
        }
        return builder;
    }

    @Override
    public BuildTaskDescriptor scheduleDependenciesAnalyze(String wsId, String project, String type, ServiceContext serviceContext, BuildOptions buildOptions) throws BuilderException {
        return null;
    }

    @Override
    public BuildTaskDescriptor getTask(Long id) throws NotFoundException, ForbiddenException {
        return getTask(id, null);
    }

    @Override
    public BuildTaskDescriptor getTask(Long id, ServiceContext context) throws NotFoundException, ForbiddenException {
        BuildRequestEntity entity = getBuildRequestEntity(id);
        try {
            Project project = projectManager.getProject(entity.getWorkspace().getWorkspaceId(), entity.getProjectName());
            return getDescriptor(getBuilder(project), entity, context);
        } catch (ServerException e) {
            throw new RuntimeException(e);
        }
    }

    private BuildRequestEntity getBuildRequestEntity(Long id) throws NotFoundException {
        BuildRequestEntity entity = buildRequestRepository.findOne(id);
        if (entity == null) {
            throw new NotFoundException("Could not find task " + id);
        }
        return entity;
    }

    @Override
    public BuildTaskDescriptor cancel(Long id, ServiceContext context) throws NotFoundException, ForbiddenException {
        BuildRequestEntity buildRequestEntity = getBuildRequestEntity(id);
        try {
            Builder builder = getBuilder(projectManager.getProject(buildRequestEntity.getWorkspace().getWorkspaceId(), buildRequestEntity.getProjectName()));
            builder.getBuildTask(id).cancel();
            return getDescriptor(builder, buildRequestEntity, context);
        } catch (ServerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Response writeLog(Long id) throws NotFoundException, ForbiddenException, ServerException {
        return readFile(id, buildLogName);
    }

    @Override
    public Response readFile(Long id, String path) throws NotFoundException, ForbiddenException, ServerException {
        File file = findFile(id, path);
        return Response.ok(file).type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @Override
    public Response downloadFile(Long id, String path) throws NotFoundException, ForbiddenException, ServerException {
        File file = findFile(id, path);
        return Response.status(200)
                .header("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()))
                .type(ContentTypeGuesser.guessContentType(file))
                .entity(file)
                .build();

    }

    @Override
    public Response downloadResultArchive(Long id, String arch) throws NotFoundException, ForbiddenException, ServerException {
        return Response.serverError().entity("Not implemented yet").type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @PostConstruct
    void init() {
        executor = Executors.newCachedThreadPool(new CustomizableThreadFactory("BUILD-QUEUE-"));
        eventService.subscribe(new BuildStatusMessenger(this));
    }

    @PreDestroy
    void stop() {
        executor.shutdown();
        ;
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

    }

    private File findFile(Long id, String path) throws ServerException, ForbiddenException, NotFoundException {
        BuildRequestEntity request = buildRequestRepository.findOne(id);
        if (request == null) {
            throw new NotFoundException("Build " + id + " was not found");
        }
        return findFile(request, path);
    }

    private File findFile(BuildRequestEntity request, String path) throws ServerException, ForbiddenException, NotFoundException {
        //this is to check access writes
        projectManager.getProject(request.getWorkspace().getWorkspaceId(), request.getProjectName());
        File returnFile = new File(getBuildFolder(request.getWorkspace().getWorkspaceId(), request.getProjectName(), request.getId()), path);
        if (!returnFile.exists()) {
            throw new NotFoundException("Could not find file " + path + "for build id " + request.getId());
        }
        return returnFile;
    }

    private File getBuildFolder(String workspaceId, String projectName, Long taskId) throws ServerException, NotFoundException {
        File workspaceBuildsRoot = new File(buildsRoot, mountStrategy.getMountPath(workspaceId).getName());
        if (!workspaceBuildsRoot.exists()) {
            throw new NotFoundException("Could not find any builds for the given build id " + taskId);
        }
        File projectBuildsRoot = new File(workspaceBuildsRoot, projectName);
        if (!projectBuildsRoot.exists()) {
            throw new NotFoundException("Could not find any builds for the given build id " + taskId);
        }

        File taskBuildRoot = new File(projectBuildsRoot, String.valueOf(taskId));
        if (!taskBuildRoot.exists()) {
            throw new NotFoundException("Could not find any builds for the given build id " + taskId);
        }
        return taskBuildRoot;
    }

    public BuildTaskDescriptor getDescriptor(Builder builder, BuildRequestEntity buildRequestEntity, ServiceContext context) throws BuilderException, NotFoundException {
        UriBuilder uriBuilder = null;
        if (context != null) {
            uriBuilder = context.getServiceUriBuilder();
        }

        if (uriBuilder == null) {
            uriBuilder = new GuiceUriBuilderImpl();
        }

        final DtoFactory dtoFactory = DtoFactory.getInstance();
        Long id = buildRequestEntity.getId();
        String workspace = buildRequestEntity.getWorkspace().getWorkspaceId();
        BuildTaskDescriptor descriptor = dtoFactory.createDto(BuildTaskDescriptor.class);
        descriptor.withCreationTime(buildRequestEntity.getCreatedAt().getTime())
                .withStartTime(buildRequestEntity.getStartTime() != null ? buildRequestEntity.getStartTime().getTime() : -1)
                .withEndTime(buildRequestEntity.getEndTime() != null ? buildRequestEntity.getEndTime().getTime() : -1)
                .withStatus(BuildStatus.valueOf(buildRequestEntity.getStatus().name()))
                .withProject(buildRequestEntity.getProjectName())
                .withTaskId(id)
                .withWorkspace(workspace);
        final List<Link> links = new ArrayList<>();
        switch (buildRequestEntity.getStatus()) {
            case IN_QUEUE:
            case IN_PROGRESS:
                links.add(dtoFactory.createDto(Link.class)
                        .withRel(org.eclipse.che.api.builder.internal.Constants.LINK_REL_GET_STATUS)
                        .withHref(uriBuilder.path(BuilderService.class, "getStatus").build(workspace, id)
                                .toString())
                        .withMethod("GET")
                        .withProduces(MediaType.APPLICATION_JSON));
                links.add(dtoFactory.createDto(Link.class)
                        .withRel(org.eclipse.che.api.builder.internal.Constants.LINK_REL_CANCEL)
                        .withHref(uriBuilder.path(BuilderService.class, "cancel").build(workspace, id).toString())
                        .withMethod("POST")
                        .withProduces(MediaType.APPLICATION_JSON));
                break;
            case SUCCESSFUL:
                links.add(dtoFactory.createDto(Link.class)
                        .withRel(org.eclipse.che.api.builder.internal.Constants.LINK_REL_VIEW_LOG)
                        .withHref(uriBuilder.path(BuilderService.class, "getLogs").build(workspace, id).toString())
                        .withMethod("GET")
                        .withProduces(MediaType.TEXT_PLAIN));
                links.add(dtoFactory.createDto(Link.class)
                        .withRel(org.eclipse.che.api.builder.internal.Constants.LINK_REL_BROWSE)
                        .withHref(uriBuilder.path(BuilderService.class, "browseDirectory").queryParam("path", "/")
                                .build(workspace, id).toString())
                        .withMethod("GET")
                        .withProduces(MediaType.TEXT_HTML));

                BuildTask buildTask = builder.getBuildTask(id);
                final List<File> results = buildTask.getResult().getResults();
                for (java.io.File ru : results) {
                    if (ru.isFile()) {
                        String relativePath = buildTask.getConfiguration().getWorkDir().toPath().relativize(ru.toPath()).toString();
                        if (SystemInfo.isWindows()) {
                            relativePath = relativePath.replace("\\", "/");
                        }
                        links.add(dtoFactory.createDto(Link.class)
                                .withRel(org.eclipse.che.api.builder.internal.Constants.LINK_REL_DOWNLOAD_RESULT)
                                .withHref(uriBuilder.path(BuilderService.class, "downloadFile")
                                        .queryParam("path", relativePath).build(workspace, id).toString())
                                .withMethod("GET")
                                .withProduces(ContentTypeGuesser.guessContentType(ru)));
                    }
                }
                /* not using downloadResultArchive for now
                if (!results.isEmpty()) {
                    links.add(dtoFactory.createDto(Link.class)
                            .withRel(org.eclipse.che.api.builder.internal.Constants.LINK_REL_DOWNLOAD_RESULTS_TARBALL)
                            .withHref(context.getBaseUriBuilder().path(BuilderServer.class, "downloadResultArchive")
                                    .queryParam("arch", "tar")
                                    .build(builder, id).toString())
                            .withMethod("GET"));
                    links.add(dtoFactory.createDto(Link.class)
                            .withRel(org.eclipse.che.api.builder.internal.Constants.LINK_REL_DOWNLOAD_RESULTS_ZIPBALL)
                            .withHref(context.getBaseUriBuilder().path(BuilderServer.class, "downloadResultArchive")
                                    .queryParam("arch", "zip")
                                    .build(builder, id).toString())
                            .withMethod("GET"));
                }*/

        }
        descriptor.withLinks(links);
        return descriptor;
    }

    private ProjectDescriptor getProjectDescription(String workspace, String project)
            throws BuilderException {
        final UriBuilder baseProjectUriBuilder = UriBuilder.fromUri(baseProjectApiUrl);
        final String projectUrl = baseProjectUriBuilder.path(ProjectService.class)
                .path(ProjectService.class, "getProject")
                .build(workspace, project.startsWith("/") ? project.substring(1) : project)
                .toString();
        try {
            return HttpJsonHelper.get(ProjectDescriptor.class, projectUrl);
        } catch (IOException e) {
            throw new BuilderException(e);
        } catch (ServerException | UnauthorizedException | ForbiddenException | NotFoundException | ConflictException e) {
            throw new BuilderException(e.getServiceError());
        }
    }

    private void fillRequestFromProjectDescriptor(ProjectDescriptor descriptor, BaseBuilderRequest request) throws BuilderException {
        String builder = request.getBuilder();
        final BuildersDescriptor builders = descriptor.getBuilders();
        if (builder == null) {
            if (builders != null) {
                builder = builders.getDefault();
                //if builder not set in request we will use builder that set in ProjectDescriptor
                request.setBuilder(builder);
                //fill build configuration from ProjectDescriptor for default builder
                fillBuildConfig(request,
                        builder,
                        firstNonNull(builders.getConfigs(), Collections.<String, BuilderConfiguration>emptyMap()));
            }
            if (builder == null) {
                throw new BuilderException("Name of builder is not specified, be sure corresponded property of project is set");
            }

        } else {
            //fill build configuration from ProjectDescriptor for builder from request
            fillBuildConfig(request,
                    builder,
                    firstNonNull(builders.getConfigs(), Collections.<String, BuilderConfiguration>emptyMap()));
        }
        request.setProjectDescriptor(descriptor);
        request.setProjectUrl(descriptor.getBaseUrl());
        final Link zipballLink = descriptor.getLink(org.eclipse.che.api.project.server.Constants.LINK_REL_EXPORT_ZIP);
        if (zipballLink != null) {
            final String zipballLinkHref = zipballLink.getHref();
            final String token = getAuthenticationToken();
            request.setSourcesUrl(token != null ? String.format("%s?token=%s", zipballLinkHref, token) : zipballLinkHref);
        }
    }

    private void fillBuildConfig(BaseBuilderRequest request, String builder, Map<String, BuilderConfiguration> buildersConfigs) {
        //here we going to check is ProjectDescriptor have some setting for giving builder form ProjectDescriptor
        BuilderConfiguration builderConfig = buildersConfigs.get(builder);
        if (builderConfig != null) {
            request.setOptions(firstNonNull(builderConfig.getOptions(), Collections.<String, String>emptyMap()));
            request.setTargets(firstNonNull(builderConfig.getTargets(), Collections.<String>emptyList()));
        }
    }

    private String getAuthenticationToken() {
        User user = EnvironmentContext.getCurrent().getUser();
        if (user != null) {
            return user.getToken();
        }
        return null;
    }

    @Override
    public List<BuildTaskDescriptor> getTasks(String workspace, String project) {
        return Collections.emptyList();
    }


    public class BuildChangesNotifier implements Runnable {

        BuildRequestEntity request;

        public BuildChangesNotifier(BuildRequestEntity request) {
            this.request = request;
        }

        private void updateRequest() {
            this.request = buildRequestRepository.findOne(request.getId());
        }

        @Override
        public void run() {
            BuildRequestStatus lastStatus = request.getStatus();
            boolean finished;
            do {
                finished = FINISHED_STATUSES.contains(lastStatus);
                if (!lastStatus.equals(request.getStatus())) {
                    switch (request.getStatus()) {
                        case IN_PROGRESS:
                            eventService.publish(BuilderEvent.buildTimeStartedEvent(request.getId(), request.getWorkspace().getWorkspaceId(), request.getProjectName(),
                                    request.getStartTime().getTime()));
                            eventService.publish(BuilderEvent.beginEvent(request.getId(), request.getWorkspace().getWorkspaceId(), request.getProjectName()));
                            break;
                        case CANCELLED:
                        case SUCCESSFUL:
                        case FAILED:
                            eventService.publish(BuilderEvent.doneEvent(request.getId(), request.getWorkspace().getWorkspaceId(), request.getProjectName()));
                            break;
                    }
                }
                try {
                    Thread.sleep(1000L);
                    lastStatus = request.getStatus();
                    updateRequest();
                } catch (InterruptedException e) {
                    finished = true;
                }
            } while (!finished);
        }
    }
}
