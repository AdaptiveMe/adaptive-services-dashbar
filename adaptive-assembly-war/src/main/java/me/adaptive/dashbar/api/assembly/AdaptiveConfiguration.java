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

package me.adaptive.dashbar.api.assembly;

import me.adaptive.core.data.config.JpaConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Repository;

/**
 * Created by panthro on 10/08/15.
 */
@Configuration
@ComponentScan(basePackages = "me.adaptive", excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Repository.class))
@Import(JpaConfiguration.class)
public class AdaptiveConfiguration {


}
