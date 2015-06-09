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
 */ge me.adaptive.dashbar.api.assembly;

import me.adaptive.core.data.SpringContextHolder;
import org.eclipse.che.inject.CodenvyBootstrap;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;

/**
 * Created by panthro on 03/06/15.
 */
public class CheSpringContextListener extends ContextLoaderListener {

    CodenvyBootstrap codenvyBootstrap = new CodenvyBootstrap();

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        super.contextDestroyed(sce);
        codenvyBootstrap.contextDestroyed(sce);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);
        SpringContextHolder.initialize(getCurrentWebApplicationContext());
        codenvyBootstrap.contextInitialized(sce);
    }
}
