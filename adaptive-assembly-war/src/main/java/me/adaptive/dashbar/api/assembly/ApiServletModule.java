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
 */
package me.adaptive.dashbar.api.assembly;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spring.SpringIntegration;
import me.adaptive.che.infrastructure.filter.AdaptiveEnvironmentFilter;
import org.eclipse.che.everrest.CodenvyEverrestWebSocketServlet;
import org.eclipse.che.inject.DynaModule;
import org.everrest.guice.servlet.GuiceEverrestServlet;
import org.everrest.websockets.WSConnectionTracker;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@DynaModule
public class ApiServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        getServletContext().addListener(new WSConnectionTracker());

        bind(AdaptiveEnvironmentFilter.class)
                .toProvider(SpringIntegration.fromSpring(AdaptiveEnvironmentFilter.class, "adaptiveEnvironmentFilter"))
                .in(Singleton.class);

        Filter corsFilter = getCORSFilter();


        filter("/*").through(corsFilter); //TODO remove this filter in PRODUCTION
        filter("/*").through(AdaptiveEnvironmentFilter.class);
        serve("/api/ws/*").with(CodenvyEverrestWebSocketServlet.class);
        serve("/api/*").with(GuiceEverrestServlet.class);


    }

    private Filter getCORSFilter() {
        return new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {

            }

            @Override
            public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
                HttpServletResponse response = (HttpServletResponse) res;
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setHeader("Access-Control-Allow-Headers", "token, x-requested-with");
                chain.doFilter(req, response);
            }

            @Override
            public void destroy() {

            }
        };
    }


}
