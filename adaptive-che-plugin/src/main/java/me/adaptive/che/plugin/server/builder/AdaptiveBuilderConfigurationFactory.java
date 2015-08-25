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

import org.eclipse.che.api.builder.BuilderException;
import org.eclipse.che.api.builder.dto.BaseBuilderRequest;
import org.eclipse.che.api.builder.internal.BuilderConfiguration;
import org.eclipse.che.api.builder.internal.DefaultBuilderConfigurationFactory;

import java.io.File;

/**
 * Creates the BuilderConfiguration changing the work and build dir.
 *
 * @author panthro on 14/07/15.
 * @see DefaultBuilderConfigurationFactory
 */
public class AdaptiveBuilderConfigurationFactory extends DefaultBuilderConfigurationFactory {

    private AdaptiveBuilder myBuilder;
    private BaseBuilderRequest request;

    public AdaptiveBuilderConfigurationFactory(AdaptiveBuilder builder) {
        super(builder);
        this.myBuilder = builder;
    }

    @Override
    protected File createBuildDir() throws BuilderException {
        return this.myBuilder.getBuildResultRoot(request);
    }

    @Override
    public BuilderConfiguration createBuilderConfiguration(BaseBuilderRequest request) throws BuilderException {
        this.request = request;
        return super.createBuilderConfiguration(request);
    }

    @Override
    protected File createWorkDir(File parent, BaseBuilderRequest request) throws BuilderException {
        return this.myBuilder.getBuildResultRoot(request);
    }
}
