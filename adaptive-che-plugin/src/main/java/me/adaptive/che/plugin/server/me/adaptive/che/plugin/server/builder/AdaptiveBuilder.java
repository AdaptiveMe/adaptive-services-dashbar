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

package me.adaptive.che.plugin.server.me.adaptive.che.plugin.server.builder;

import org.eclipse.che.api.builder.BuilderException;
import org.eclipse.che.api.builder.internal.BuildResult;
import org.eclipse.che.api.builder.internal.Builder;
import org.eclipse.che.api.builder.internal.BuilderConfiguration;
import org.eclipse.che.api.builder.internal.Constants;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.CommandLine;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by panthro on 26/06/15.
 */
public class AdaptiveBuilder extends Builder {

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
                           EventService eventService) {
        super(rootDirectory, numberOfWorkers, queueSize, cleanBuildResultDelay, eventService);
    }


    @Override
    public String getName() {
        return "adaptive";
    }

    @Override
    public String getDescription() {
        return "Adaptive Builder";
    }

    @Override
    protected BuildResult getTaskResult(FutureBuildTask task, boolean successful) throws BuilderException {
        return null;
    }

    @Override
    protected CommandLine createCommandLine(BuilderConfiguration config) throws BuilderException {
        return null;
    }
}
