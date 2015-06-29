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
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.ide.Constants;

import java.util.Arrays;

/**
 * Created by panthro on 26/06/15.
 */
@Singleton
public class AdaptiveProjectType extends ProjectType {

    public AdaptiveProjectType() {
        super("adaptive", "Adaptive Project", true, false);
        addConstantDefinition(Constants.LANGUAGE, Constants.LANGUAGE, "javascript");
        addConstantDefinition(Constants.FRAMEWORK, Constants.FRAMEWORK, "Adaptive");
        setDefaultBuilder("adaptive");
        addRunnerCategories(Arrays.asList("javascript"));
    }
}
