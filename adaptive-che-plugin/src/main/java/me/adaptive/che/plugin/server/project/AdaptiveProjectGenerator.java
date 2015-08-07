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

package me.adaptive.che.plugin.server.project;

import com.google.inject.Singleton;
import me.adaptive.che.plugin.server.project.generator.GeneratorCommandBuilder;
import me.adaptive.che.plugin.server.util.SimpleCommandLineExecutor;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectImporter;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.vfs.impl.fs.VirtualFileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * The adaptive project generator that will be called when a project is generated with some params.
 * Created by panthro on 02/07/15.
 */
@Singleton
public class AdaptiveProjectGenerator implements CreateProjectHandler, ProjectImporter {


    private static Logger LOG = LoggerFactory.getLogger(AdaptiveProjectGenerator.class);

    public static final String OPTION_GENERATE = "generate";
    public static final String GENERATION_LOG = "generator.log";



    @Override
    public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {
        //TODO enable only when generate is passed
        //if (options != null && options.containsKey(OPTION_GENERATE)) {
            //TODO check if we should start it in another thread
        generateProject(baseFolder, attributes, options, LineConsumerFactory.NULL);
        //}
    }

    private void generateProject(FolderEntry baseFolder, @Nullable Map<String, AttributeValue> attributes, Map<String, String> options, LineConsumerFactory lineConsumerFactory) {
        try {
            File workDir = ((VirtualFileImpl) baseFolder.getVirtualFile()).getIoFile();
            File generatorLog = ((VirtualFileImpl) baseFolder.createFile(GENERATION_LOG, new byte[0], "text/plain").getVirtualFile()).getIoFile();
            SimpleCommandLineExecutor executor = new SimpleCommandLineExecutor();
            LineConsumer consumer = lineConsumerFactory.newLineConsumer();
            executor.execute(new GeneratorCommandBuilder(baseFolder.getVirtualFile().getName()).withAttributes(attributes).withOptions(options), workDir, generatorLog, consumer);
            if (!executor.isSuccess()) {
                LOG.warn("There was an error executing the generator command");
            }

        } catch (Exception e) {
            LOG.warn("Error executing the generator", e);
        }
    }


    @Override
    public String getProjectType() {
        return AdaptiveProjectType.TYPE;
    }

    /**
     * IMPORTER
     */

    @Override
    public String getId() {
        return "adaptive";
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public ImporterCategory getCategory() {
        return ImporterCategory.SOURCE_CONTROL;
    }

    @Override
    public String getDescription() {
        return "Generates an Adaptive Project based on the parameters passed";
    }

    @Override
    public void importSources(FolderEntry baseFolder, String location, Map<String, String> parameters) throws ForbiddenException, ConflictException, UnauthorizedException, IOException, ServerException {
        generateProject(baseFolder, Collections.emptyMap(), parameters, LineConsumerFactory.NULL);
    }

    @Override
    public void importSources(FolderEntry baseFolder, String location, Map<String, String> parameters, LineConsumerFactory importOutputConsumerFactory) throws ForbiddenException, ConflictException, UnauthorizedException, IOException, ServerException {
        generateProject(baseFolder, Collections.emptyMap(), parameters, importOutputConsumerFactory);
    }
}
