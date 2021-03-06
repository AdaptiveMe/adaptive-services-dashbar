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

import me.adaptive.core.data.domain.BuildRequestEntity;
import me.adaptive.infra.client.ApiClient;
import org.eclipse.che.api.builder.BuildStatus;
import org.eclipse.che.api.builder.internal.BuildLogger;
import org.eclipse.che.api.builder.internal.BuilderEvent;
import org.eclipse.che.api.core.notification.EventService;
import org.slf4j.LoggerFactory;
import retrofit.RetrofitError;
import retrofit.client.Response;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.*;

/**
 * The Builder Looger for adaptive builds.
 * <p>
 * Created by panthro on 08/07/15.
 */
public class AdaptiveBuilderLogger implements BuildLogger {

    private ApiClient apiClient;
    private BuildRequestEntity requestEntity;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private File myFile;
    private Future<Boolean> populatorTask;
    private EventService eventService;

    /**
     * Constructor
     *
     * @param requestEntity the requestEntity
     * @param builder       the adaptiveBuilder
     * @param apiClient     an already authenticated apiClient
     */
    public AdaptiveBuilderLogger(BuildRequestEntity requestEntity, AdaptiveBuilder builder, ApiClient apiClient, EventService eventService) {
        this.requestEntity = requestEntity;
        File root = builder.getBuildsRoot(requestEntity.getWorkspace().getWorkspaceId(), requestEntity.getProjectName(), requestEntity.getId());
        myFile = new File(root, builder.getBuildLogName());
        this.apiClient = apiClient;
        this.eventService = eventService;
        //TODO find a way to populate the log as it happens or somehow have a better Reader()
        populatorTask = executor.submit(new FileLogPopulator());

    }


    /**
     * the Reader instance that will be used to print the logs to the clients
     * TODO need to figure out a way to make this reder work during the build
     *
     * @return the Reader instance
     * @throws IOException case the reader cannot be created
     */
    @Override
    public Reader getReader() throws IOException {
        //writeLine("[WARNING] Could not get logs from builder");
        //writeLine("[WARNING] This does not means your build has failed, check the status.");
        return Files.newBufferedReader(myFile.toPath());
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public File getFile() {
        return myFile;
    }

    @Override
    public void writeLine(String line) throws IOException {
        //Shouldnt write anything actually
    }

    @Override
    public void close() throws IOException {
        executor.shutdown();
        try {
            if (!populatorTask.isDone()) {
                populatorTask.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


    }


    /**
     * Used to populate the log file while the build process is still running
     */
    private class FileLogPopulator implements Callable<Boolean> {

        BuildStatus status = BuildStatus.IN_QUEUE;
        int lastReadLine;

        @Override
        public Boolean call() throws Exception {

            while (status.equals(BuildStatus.IN_QUEUE) || status.equals(BuildStatus.IN_PROGRESS)) {
                if (BuildStatus.IN_PROGRESS.equals(status)) {
                    readAndWriteLines();
                }
                Thread.sleep(500);
                updateStatus();
            }
            if (status.equals(BuildStatus.SUCCESSFUL) || status.equals(BuildStatus.FAILED)) {
                readAndWriteLines();
            }
            return true;
        }

        /**
         * Read the lines from the API and write to the log file.
         *
         * @throws IOException
         */
        private void readAndWriteLines() throws IOException {
            try {
                Response response = apiClient.getBuilderApi().logs(requestEntity.getId(), lastReadLine);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody().in()));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    eventService.publish(BuilderEvent.messageLoggedEvent(requestEntity.getId(), requestEntity.getWorkspace().getWorkspaceId(), requestEntity.getProjectName(), new BuilderEvent.LoggedMessage(line, lastReadLine)));
                    lastReadLine++;
                    writeLine(line);
                }
            } catch (Exception e) {
                if (e instanceof RetrofitError) {
                    if (((RetrofitError) e).getResponse().getStatus() < 400) { //log errors only if response wasn't bad
                        LoggerFactory.getLogger(AdaptiveBuilderLogger.class).info("Could not get update status from remote builder", e);
                    }
                }
            }

        }

        /**
         * update the status from the API
         */
        private void updateStatus() {
            try {
                status = BuildStatus.valueOf(apiClient.getBuilderApi().status(requestEntity.getId()));
            } catch (Exception e) {
                if (e instanceof RetrofitError) {
                    if (((RetrofitError) e).getResponse().getStatus() < 400) { //log errors only if response wasn't bad
                        LoggerFactory.getLogger(AdaptiveBuilderLogger.class).info("Could not get update status from remote builder", e);
                    }
                }
            }
        }
    }


}
