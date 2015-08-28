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

package me.adaptive.che.plugin.server.builder;

import me.adaptive.che.infrastructure.vfs.WorkspaceIdLocalFSMountStrategy;
import me.adaptive.core.data.domain.BuildRequestEntity;
import me.adaptive.core.data.domain.types.BuildRequestStatus;
import me.adaptive.core.data.repo.BuildRequestRepository;
import me.adaptive.infra.client.ApiClient;
import me.adaptive.infra.client.api.BuildRequestBody;
import org.eclipse.che.api.builder.BuildStatus;
import org.eclipse.che.api.builder.BuilderException;
import org.eclipse.che.api.builder.dto.BaseBuilderRequest;
import org.eclipse.che.api.builder.dto.BuildRequest;
import org.eclipse.che.api.builder.internal.*;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * This is the class responsible for invoking the build in one of the adaptive builder servers.
 *
 * @author panthro
 *         Created by panthro on 26/06/15.
 */
public class AdaptiveBuilder extends Builder {

    public static final String BUILDER_NAME = "adaptive";
    public static final String PLATFORM_OPTION = "platform";
    public static final BuildStatus[] FINISHED_STATUSES = {BuildStatus.CANCELLED, BuildStatus.SUCCESSFUL, BuildStatus.FAILED};

    private ApiClient apiClient;
    private File buildsRoot;
    private String buildLogName;
    @Inject
    private EventService eventService;

    @Inject
    @Named("buildRequestRepository")
    private BuildRequestRepository buildRequestRepository;

    /**
     * Default constructor.
     *
     * @param rootDirectory         the directory where we can store data
     * @param numberOfWorkers       the number of workers
     * @param queueSize             the size of the queue
     * @param cleanBuildResultDelay delay
     */
    @Inject
    public AdaptiveBuilder(@Named(Constants.BASE_DIRECTORY) java.io.File rootDirectory,
                           @Named(Constants.NUMBER_OF_WORKERS) int numberOfWorkers,
                           @Named(Constants.QUEUE_SIZE) int queueSize,
                           @Named(Constants.KEEP_RESULT_TIME) int cleanBuildResultDelay,
                           @Named("adaptive.api.client.endpoint") String endpoint,
                           @Named("adaptive.api.client.token") String token,
                           @Named("adaptive.build.result.root") String buildsRoot,
                           @Named("adaptive.build.log.name") String buildLogName,
                           EventService eventService) {
        super(rootDirectory, numberOfWorkers, queueSize, cleanBuildResultDelay, eventService);
        apiClient = new ApiClient(endpoint, token);
        this.buildsRoot = new File(buildsRoot);
        this.buildLogName = buildLogName;
    }


    @Override
    public String getName() {
        return BUILDER_NAME;
    }

    @Override
    public String getDescription() {
        return "Adaptive Builder";
    }

    /**
     * Builds the {@code BuildResult} accordingly with the result from the given task
     *
     * @param task       the task to build the result
     * @param successful a boolean if the task was successful
     * @return the {@code BuildResult} representing the result of the build
     * @throws BuilderException
     */
    @Override
    protected BuildResult getTaskResult(FutureBuildTask task, boolean successful) throws BuilderException {
        //TODO analyze how che choose which file to display when building from the cli
        File resultRoot = getBuildResultRoot(task.getConfiguration().getRequest());
        return new BuildResult(successful, getBuildArtifacts(resultRoot), new File(resultRoot, buildLogName));
    }

    /**
     * The build result
     *
     * @param request the request
     * @return the File pointing to the buildResultRoot
     */
    public File getBuildResultRoot(BaseBuilderRequest request) {
        return getBuildsRoot(request.getWorkspace(), request.getProject(), request.getId());
    }

    public File getBuildsRoot(String workspaceId, String projectName, Long buildId) {
        return new File(buildsRoot, getWorkspaceFolderName(workspaceId)
                + File.separator
                + (projectName.startsWith("/") ? projectName.substring(1) : projectName)
                + File.separator
                + buildId);
    }

    /**
     * We are not using the command line, this is for builds that need a command line.
     *
     * @param config the build config
     * @return should return a command line to prevent NullPointerExceptions but it won't do anything
     * @throws BuilderException
     */
    @Override
    protected CommandLine createCommandLine(BuilderConfiguration config) throws BuilderException {
        return new CommandLine("date"); //adding a date to the logs :P
    }


    /**
     * Utility method to get the workspace folder name
     *
     * @param wsId the workspace id
     * @return the folder name
     */
    private String getWorkspaceFolderName(String wsId) {
        return WorkspaceIdLocalFSMountStrategy.getWorkspaceFolderName(wsId);
    }


    @Override
    protected BuildLogger createBuildLogger(BuilderConfiguration buildConfiguration, File logFile) throws BuilderException {
        return new AdaptiveBuilderLogger(buildRequestRepository.findOne(buildConfiguration.getRequest().getId()), this, apiClient, eventService);
    }

    @Override
    public BuilderConfigurationFactory getBuilderConfigurationFactory() {
        return new AdaptiveBuilderConfigurationFactory(this);
    }

    @Override
    protected Callable<Boolean> createTaskFor(CommandLine commandLine, BuildLogger logger, long timeout, final BuilderConfiguration configuration) {
        return new AdaptiveBuilderTask(configuration);
    }

    public BuildResult getBuildResult(Long taskId) throws NotFoundException {
        BuildRequestEntity entity = buildRequestRepository.findOne(taskId);
        if (entity == null) {
            throw new NotFoundException("Task id " + taskId + " not found");
        }
        boolean success = BuildRequestStatus.SUCCESSFUL.equals(entity.getStatus());

        return new BuildResult(success, getBuildArtifacts(getBuildsRoot(entity.getWorkspace().getWorkspaceId(), entity.getProjectName(), entity.getId())));

    }

    private List<File> getBuildArtifacts(File resultRoot) {
        File[] resultFiles = resultRoot.listFiles((dir, name) -> !buildLogName.equals(name));
        return resultFiles == null ? Collections.emptyList() : Arrays.asList(resultFiles);
    }


    public String getBuildLogName() {
        return buildLogName;
    }

    @Override
    public BuildTask getBuildTask(Long id) throws NotFoundException {
        try {
            return super.getBuildTask(id);
        } catch (NotFoundException e) {
            BuildRequestEntity entity = buildRequestRepository.findOne(id);
            if (entity == null) {
                throw e;
            }
            return new DelegateBuildTask(entity);
        }
    }

    public class AdaptiveBuilderTask implements Callable<Boolean> {

        private BuilderConfiguration configuration;

        public AdaptiveBuilderTask(BuilderConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                apiClient.getBuilderApi().build(
                        configuration.getRequest().getId(),
                        getWorkspaceFolderName(configuration.getRequest().getWorkspace()),
                        configuration.getRequest().getProjectDescriptor().getName()
                        , configuration.getRequest().getOptions().getOrDefault(PLATFORM_OPTION, "android"),
                        new BuildRequestBody("debug")); //TODO build the correct body based on the configuration & request
            } catch (Exception e) {
                return false;
            }
            return true;
        }

    }


    private class DelegateBuildTask implements BuildTask {

        private final BuildRequestEntity entity;

        public DelegateBuildTask(BuildRequestEntity entity) {
            this.entity = entity;
        }

        @Override
        public Long getId() {
            return entity.getId();
        }

        @Override
        public CommandLine getCommandLine() {
            return new CommandLine("");
        }

        @Override
        public String getBuilder() {
            return "adaptive";
        }

        @Override
        public BuildLogger getBuildLogger() {
            return new AdaptiveBuilderLogger(entity, AdaptiveBuilder.this, apiClient, eventService);
        }

        @Override
        public boolean isStarted() {
            return BuildRequestStatus.IN_QUEUE.equals(entity.getStatus());
        }

        @Override
        public long getStartTime() {
            return entity.getStartTime() == null ? 0 : entity.getStartTime().getTime();
        }

        @Override
        public long getEndTime() {
            return entity.getEndTime() == null ? 0 : entity.getEndTime().getTime();
        }

        @Override
        public long getRunningTime() {
            if (isStarted()) {
                return getEndTime() > 0 ? getEndTime() - getStartTime() : System.currentTimeMillis() - getStartTime();
            } else {
                return 0;
            }

        }

        @Override
        public boolean isDone() {
            return Arrays.asList(FINISHED_STATUSES).contains(BuildStatus.valueOf(entity.getStatus().name()));
        }

        @Override
        public boolean isCancelled() {
            return BuildRequestStatus.CANCELLED.equals(entity.getStatus());
        }

        @Override
        public void cancel() throws BuilderException {
            LoggerFactory.getLogger(DelegateBuildTask.class).warn("Calling cancel from the delegate");
        }

        @Override
        public BuildResult getResult() throws BuilderException {
            try {
                return getBuildResult(this.getId());
            } catch (NotFoundException e) {
                throw new BuilderException(e);
            }
        }

        @Override
        public BuilderConfiguration getConfiguration() {
            BaseBuilderRequest request = DtoFactory.getInstance().createDto(BuildRequest.class)
                    .withUserId(entity.getRequester().getUserId())
                    .withWorkspace(entity.getWorkspace().getWorkspaceId())
                    .withProject(entity.getProjectName())
                    .withId(entity.getId())
                    .withBuilder(AdaptiveBuilder.BUILDER_NAME)
                    .withOptions(entity.getAttributes());
            try {
                return new AdaptiveBuilderConfigurationFactory(AdaptiveBuilder.this).createBuilderConfiguration(request);
            } catch (BuilderException e) {
                LoggerFactory.getLogger(DelegateBuildTask.class).error("Error creating configuration", e);
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return BUILDER_NAME;
    }

    @Override
    protected void cleanup(BuildTask task) {
        //Nope, there's nothing to cleanup
    }
}
