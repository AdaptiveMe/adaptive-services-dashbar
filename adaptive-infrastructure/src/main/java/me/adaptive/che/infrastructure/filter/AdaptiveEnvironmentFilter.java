
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
 */ge me.adaptive.che.infrastructure.filter;

import me.adaptive.core.data.api.UserTokenEntityService;
import me.adaptive.core.data.domain.UserTokenEntity;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.commons.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.security.Principal;

/**
 * Created by panthro on 08/06/15.
 */
@Service("adaptiveEnvironmentFilter")
public class AdaptiveEnvironmentFilter implements Filter {

    private static final String TOKEN_PARAM = "token";

    @Autowired
    private UserTokenEntityService userTokenEntityService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(servletRequest.getParameter(TOKEN_PARAM)!= null){
            UserTokenEntity token = userTokenEntityService.findByToken(servletRequest.getParameter(TOKEN_PARAM));
            if(token != null){

                EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
                //TODO Left it commented out so we can have a reference of the roles
                //Collections.addAll(roles, new String[]{"workspace/admin", "workspace/developer", "system/admin", "system/manager", "user"});
                User user = new UserImpl(token.getUser().getEmail(), token.getUser().getId().toString(), token.getToken(), token.getUser().getRoles(), false);

                try {
                    //environmentContext.setWorkspaceName(this.wsName);
                    //environmentContext.setWorkspaceId(this.wsId);
                    environmentContext.setUser(user);
                    filterChain.doFilter(this.addUserInRequest((HttpServletRequest) servletRequest, user), servletResponse);
                } finally {
                    EnvironmentContext.reset();
                }
            }
        }else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }

    private HttpServletRequest addUserInRequest(final HttpServletRequest httpRequest, final User user) {
        return new HttpServletRequestWrapper(httpRequest) {
            public String getRemoteUser() {
                return user.getName();
            }

            public boolean isUserInRole(String role) {
                return user.isMemberOf(role);
            }

            public Principal getUserPrincipal() {
                return new Principal() {
                    public String getName() {
                        return user.getName();
                    }
                };
            }
        };
    }
}
