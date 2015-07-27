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

package me.adaptive.che.plugin.server.project.generator;

import me.adaptive.che.plugin.server.util.CommandLineBuilder;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.che.api.project.server.type.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by panthro on 22/07/15.
 * <p>
 * Yeoman generator task runner, supports generator version 2.0.96
 */
public class GeneratorCommandBuilder extends CommandLineBuilder {


    /**
     * Sample Command
     * yo adaptiveme test latest false "Initializr Bootstrap" "ios,android" --ios-version=8.1 --android-version=5.1
     * <p>
     * yo adaptiveme:app [options] [<arg1>] [<arg2>] [<arg3>] [<arg4>] [<arg5>]
     * <p>
     * Options:
     * -h,   --help             # Print the generator's options and usage
     * --skip-cache       # Do not remember prompt answers                                                                    Default: false
     * --skip-install     # Skip dependencies installation                                                                    Default: false
     * --start-nibble     # Start the nibble emulator at the end of the generation. The nibble should be installed globally.  Default: false
     * --ios-version      # iOS version selected. ex: 8.1                                                                     Default: false
     * --android-version  # Android version selected. ex: 5.0                                                                 Default: false
     * <p>
     * Arguments:
     * arg1  # Your project name                                        Type: String   Required: false
     * arg2  # Adaptive Javascript Library version (defaults = latest)  Type: String   Required: false
     * arg3  # Add typescript support                                   Type: Boolean  Required: false
     * arg4  # Boilerplate for initialize application                   Type: String   Required: false
     * arg5  # Array of platforms selected. ex: [ios,android]           Type: Array    Required: false
     */

    public static final String PLATFORMS_SEPARATOR = ",";

    public static final String YEOMAN_COMMAND = "yo";
    public static final String GENERATOR_NAME = "adaptiveme";
    public static final String SKIP_INSTALL = "--skip-install";
    public static final String SKIP_CACHE = "--skip-cache";
    public static final String IOS_VERSION = "--ios-version";
    public static final String ANDROID_VERSION = "--android-version";

    public class Options {
        public static final String ADAPTIVE_VERSION = "adaptive";
        public static final String TYPESCRIPT = "typescript";
        public static final String BOILERPLATE = "boilerplate";
        public static final String PLATFORMS = "platforms";
        public static final String IOS_VERSION = "iosVersion";
        public static final String ANDROID_VERSION = "androidVersion";
    }

    //TODO check defaults
    private class DEFAULTS {
        private static final String ADAPTIVE = "latest";
        private static final String BOILERPLATE = "none";
        private static final String PLATFORMS = "android,ios";
        private static final String IOS_VERSION = "8.1";
        private static final String ANDROID_VERSION = "5.0";
        private static final boolean SKIP_INSTALL = true;
        private static final boolean SKIP_SERVER = true;
    }

    private String projectName;
    private String adaptiveVersion;
    private boolean typescriptSupport;
    private String boilerplate;
    private String[] platforms;
    private boolean skipInstall = DEFAULTS.SKIP_INSTALL;
    private boolean skipCache = DEFAULTS.SKIP_SERVER;
    private String iosVersion;
    private String androidVersion;


    public GeneratorCommandBuilder withProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public GeneratorCommandBuilder withAdaptiveVersion(String adaptiveVersion) {
        this.adaptiveVersion = adaptiveVersion;
        return this;
    }

    public GeneratorCommandBuilder withTypescriptSupport(boolean typescriptSupport) {
        this.typescriptSupport = typescriptSupport;
        return this;
    }

    public GeneratorCommandBuilder withBoilerplate(String boilerplate) {
        this.boilerplate = boilerplate;
        return this;
    }

    public GeneratorCommandBuilder witPlatforms(String[] platforms) {
        this.platforms = platforms;
        return this;
    }

    public GeneratorCommandBuilder withSkipInstall(boolean skipInstall) {
        this.skipInstall = skipInstall;
        return this;
    }

    public GeneratorCommandBuilder withSkipCache(boolean skipCache) {
        this.skipCache = skipCache;
        return this;
    }

    public GeneratorCommandBuilder withOptions(Map<String, String> options) {
        if (options != null && !options.keySet().isEmpty()) {
            if (options.containsKey(Options.ADAPTIVE_VERSION)) {
                setAdaptiveVersion(options.get(Options.ADAPTIVE_VERSION));
            }
            if (options.containsKey(Options.TYPESCRIPT)) {
                setTypescriptSupport(true); //DO NOT SEND THE typescript if not supported
            }
            if (options.containsKey(Options.BOILERPLATE)) {
                setBoilerplate(options.get(Options.BOILERPLATE));
            }
            if (options.containsKey(Options.PLATFORMS)) {
                setPlatforms(options.get(Options.PLATFORMS).split(PLATFORMS_SEPARATOR));
            }
            if (options.containsKey(Options.IOS_VERSION)) {
                setIosVersion(options.get(Options.IOS_VERSION));
            }
            if (options.containsKey(Options.ANDROID_VERSION)) {
                setAndroidVersion(Options.ANDROID_VERSION);
            }
        }
        return this;
    }

    public GeneratorCommandBuilder withAttributes(Map<String, AttributeValue> attributes) {
        return this;
    }

    /**
     * Creates a generator instance with all parameters set
     *
     * @param projectName       the project name
     * @param adaptiveVersion   the version of adaptive to use
     * @param typescriptSupport should enable typescript
     * @param boilerplate       which boilerplate to use
     * @param platforms         which platforms
     * @param iosVersion        which ios version
     * @param androidVersion    which android version
     * @see <a href="https://www.npmjs.com/package/generator-adaptiveme">Generator Docs</a>
     */
    public GeneratorCommandBuilder(String projectName, String adaptiveVersion, boolean typescriptSupport, String boilerplate, String[] platforms, String iosVersion, String androidVersion) {
        this.projectName = projectName;
        this.adaptiveVersion = adaptiveVersion;
        this.typescriptSupport = typescriptSupport;
        this.boilerplate = boilerplate;
        this.platforms = platforms;
        this.iosVersion = iosVersion;
        this.androidVersion = androidVersion;
    }

    /**
     * Creates a generator with all default options
     *
     * @param projectName
     */
    public GeneratorCommandBuilder(String projectName) {
        this(projectName, DEFAULTS.ADAPTIVE, false, DEFAULTS.BOILERPLATE, DEFAULTS.PLATFORMS.split(PLATFORMS_SEPARATOR), DEFAULTS.IOS_VERSION, DEFAULTS.ANDROID_VERSION);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getAdaptiveVersion() {
        return adaptiveVersion;
    }

    public void setAdaptiveVersion(String adaptiveVersion) {
        this.adaptiveVersion = adaptiveVersion;
    }

    public Boolean isTypescriptSupport() {
        return typescriptSupport;
    }

    public void setTypescriptSupport(boolean typescriptSupport) {
        this.typescriptSupport = typescriptSupport;
    }

    public String getBoilerplate() {
        return boilerplate;
    }

    public void setBoilerplate(String boilerplate) {
        this.boilerplate = boilerplate;
    }

    public boolean isSkipInstall() {
        return skipInstall;
    }

    public void setSkipInstall(boolean skipInstall) {
        this.skipInstall = skipInstall;
    }

    public boolean isSkipCache() {
        return skipCache;
    }

    public void setSkipCache(boolean skipCache) {
        this.skipCache = skipCache;
    }

    public String[] getPlatforms() {
        return platforms;
    }

    public void setPlatforms(String[] platforms) {
        this.platforms = platforms;
    }


    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getIosVersion() {
        return iosVersion;
    }

    public void setIosVersion(String iosVersion) {
        this.iosVersion = iosVersion;
    }

    @Override
    public String[] getParameters() {
        List<String> paramList = new ArrayList<>();
        //Order MATTERS!
        paramList.add(GENERATOR_NAME);
        paramList.add(getProjectName());
        paramList.add(getAdaptiveVersion().toLowerCase());
        paramList.add(isTypescriptSupport().toString().toLowerCase());
        paramList.add(getBoilerplate());
        paramList.add(StringUtils.join(platforms, PLATFORMS_SEPARATOR));
        paramList.add(IOS_VERSION);
        paramList.add(iosVersion);
        paramList.add(ANDROID_VERSION);
        paramList.add(androidVersion);

        if (skipInstall) {
            paramList.add(SKIP_INSTALL);
        }

        if (skipCache) {
            paramList.add(SKIP_CACHE);
        }

        return paramList.toArray(new String[paramList.size()]);
    }

    @Override
    public String getCommand() {
        return YEOMAN_COMMAND;
    }


}
