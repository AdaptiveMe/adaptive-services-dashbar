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

package me.adaptive.che.plugin.server.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.che.api.core.util.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by panthro on 22/07/15.
 */
public abstract class CommandLineBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(CommandLineBuilder.class);

    public abstract String[] getParameters();

    public abstract String getCommand();

    public String build() {
        StringBuilder builder = new StringBuilder(getCommand()).append(' ');
        String[] params = getParameters();
        if (params != null && params.length > 0) {
            for (String param : params) {
                if (!StringUtils.isEmpty(param)) {
                    param = org.springframework.util.StringUtils.containsWhitespace(param) ? org.springframework.util.StringUtils.quote(param) : param;
                    builder.append(param);
                    builder.append(' ');
                }
            }
        }
        LOG.debug("command generated: {}", builder.toString());
        return builder.toString();
    }

    public CommandLine buildCommandLine() {
        List<String> params = new ArrayList<>();
        params.add(getCommand());
        params.addAll(Arrays.asList(getParameters()));
        return new CommandLine(params.toArray(new String[params.size()]));
    }
}
