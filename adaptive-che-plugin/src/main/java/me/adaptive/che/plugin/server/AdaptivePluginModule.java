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

package me.adaptive.che.plugin.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import me.adaptive.che.plugin.server.builder.AdaptiveBuilder;
import me.adaptive.che.plugin.server.project.AdaptiveProjectGenerator;
import me.adaptive.che.plugin.server.project.AdaptiveProjectType;
import org.eclipse.che.api.builder.internal.Builder;
import org.eclipse.che.api.project.server.ProjectImporter;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.inject.DynaModule;

/**
 * Created by panthro on 26/06/15.
 */
@DynaModule
public class AdaptivePluginModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<ProjectType> projectTypeMultibinder = Multibinder.newSetBinder(binder(), ProjectType.class);
        projectTypeMultibinder.addBinding().to(AdaptiveProjectType.class);

        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(AdaptiveProjectGenerator.class);
        Multibinder.newSetBinder(binder(), ProjectImporter.class).addBinding().to(AdaptiveProjectGenerator.class);

        Multibinder<Builder> builderMultibinder = Multibinder.newSetBinder(binder(), Builder.class);
        builderMultibinder.addBinding().to(AdaptiveBuilder.class);

    }
}
