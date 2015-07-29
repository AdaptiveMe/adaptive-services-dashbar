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
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.vfs.impl.fs.VirtualFileImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * The adaptive project generator that will be called when a project is generated with some params.
 * Created by panthro on 02/07/15.
 */
@Singleton
public class AdaptiveProjectGenerator implements CreateProjectHandler {


    private static Logger LOG = LoggerFactory.getLogger(AdaptiveProjectGenerator.class);

    public static final String OPTION_GENERATE = "generate";
    public static final String GENERATION_LOG = "generator.log";



    @Override
    public void onCreateProject(FolderEntry baseFolder, Map<String, AttributeValue> attributes, Map<String, String> options) throws ForbiddenException, ConflictException, ServerException {
        //TODO enable only when generate is passed
        //if (options != null && options.containsKey(OPTION_GENERATE)) {
            //TODO check if we should start it in another thread
            try {
                File workDir = ((VirtualFileImpl) baseFolder.getVirtualFile()).getIoFile();
                File generatorLog = ((VirtualFileImpl) baseFolder.createFile(GENERATION_LOG, new byte[0], "text/plain").getVirtualFile()).getIoFile();
                SimpleCommandLineExecutor executor = new SimpleCommandLineExecutor();
                executor.execute(new GeneratorCommandBuilder(baseFolder.getVirtualFile().getName()).withAttributes(attributes).withOptions(options), workDir, generatorLog);
                if (!executor.isSuccess()) {
                    LOG.warn("There was an error executing the generator command");
                }

            } catch (Exception e) {
                LOG.warn("Error executing the generator", e);
            }
        //}
    }

    @Override
    public String getProjectType() {
        return AdaptiveProjectType.TYPE;
    }
}
