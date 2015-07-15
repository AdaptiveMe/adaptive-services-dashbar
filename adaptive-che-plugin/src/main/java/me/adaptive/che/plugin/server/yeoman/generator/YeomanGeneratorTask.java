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

package me.adaptive.che.plugin.server.yeoman.generator;

import org.eclipse.che.api.project.server.type.AttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by panthro on 02/07/15.
 */
public class YeomanGeneratorTask implements Callable<YeomanGeneratorTask.TaskResult> {

    //public static final String

    private Map<String, AttributeValue> attributes;
    private Map<String, String> options;

    /**
     * TODO call yeoman and generate the project
     *
     * @param attributes
     * @param options
     */
    public YeomanGeneratorTask(Map<String, AttributeValue> attributes, Map<String, String> options) {
        this.attributes = attributes == null ? new HashMap<>() : attributes;
        this.options = options == null ? new HashMap<>() : options;
    }

    public class TaskResult {
    }

    @Override
    public TaskResult call() throws Exception {
        return null;
    }
}
