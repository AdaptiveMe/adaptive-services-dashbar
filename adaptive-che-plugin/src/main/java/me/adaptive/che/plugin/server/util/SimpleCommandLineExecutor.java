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

import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by panthro on 22/07/15.
 */
public class SimpleCommandLineExecutor {

    int result;
    boolean isFinished;

    @SuppressWarnings("ConstantConditions")
    public void execute(CommandLineBuilder builder, File workDir, File outputLog, final LineConsumer consumer) throws IOException, InterruptedException {
        String command = builder.build();
        if (StringUtils.isEmpty(command)) {
            throw new IllegalStateException("Command cannot be empty");
        }

        Process process = new ProcessBuilder(builder.buildCommandLine().toShellCommand())
                .redirectInput(outputLog)
                .redirectErrorStream(true)
                .directory(workDir).start();

        try {
            result = process.waitFor();
            isFinished = true;
        } catch (InterruptedException e) {
            ProcessUtil.kill(process);
            throw e;
        }
    }

    public boolean isSuccess() {
        return result == 0;
    }
}
