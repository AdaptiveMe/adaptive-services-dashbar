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

import org.apache.commons.io.IOUtils;
import org.eclipse.che.api.core.util.LineConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;

/**
 * Created by panthro on 22/07/15.
 */
public class SimpleCommandLineExecutor {

    public static final Logger LOG = LoggerFactory.getLogger(SimpleCommandLineExecutor.class);


    int result;
    boolean isFinished;

    public void execute(CommandLineBuilder builder, File workDir, File outputLog, LineConsumer consumer) throws IOException, InterruptedException {
        String command = builder.build();
        if (StringUtils.isEmpty(command)) {
            throw new IllegalStateException("Command cannot be empty");
        }
        Process process = Runtime.getRuntime().exec(command, null, workDir);
        GeneratorOutputHandler outputCopier;
        GeneratorOutputHandler errorCopier;
        if (outputLog != null && outputLog.canWrite()) {
            errorCopier = new GeneratorOutputHandler(process.getErrorStream(), new FileOutputStream(outputLog), consumer);
            outputCopier = new GeneratorOutputHandler(process.getInputStream(), new FileOutputStream(outputLog), consumer);
            outputCopier.start();
            errorCopier.start();
        }
        result = process.waitFor();
        isFinished = true;
    }

    public boolean isSuccess() {
        return result == 0;
    }

    private class GeneratorOutputHandler extends Thread {
        InputStream is;
        OutputStream out;
        LineConsumer consumer;

        GeneratorOutputHandler(InputStream is, OutputStream out, LineConsumer consumer) {
            this.is = is;
            this.out = out;
            this.consumer = consumer;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                    if (consumer != null) {
                        consumer.writeLine(line);
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(out);
            }
        }
    }
}
