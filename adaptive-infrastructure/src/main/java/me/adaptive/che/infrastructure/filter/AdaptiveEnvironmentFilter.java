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

package me.adaptive.che.infrastructure.filter;

import me.adaptive.core.data.api.UserTokenEntityService;
import me.adaptive.core.data.api.WorkspaceMemberService;
import me.adaptive.core.data.domain.UserTokenEntity;
import me.adaptive.core.data.domain.WorkspaceMemberEntity;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.commons.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Created by panthro on 08/06/15.
 */
@Service("adaptiveEnvironmentFilter")
public class AdaptiveEnvironmentFilter implements Filter {

    private static final String TOKEN_PARAM = "token";

    private static final String COOKIE_NAME = TOKEN_PARAM;

    @Autowired
    private UserTokenEntityService userTokenEntityService;

    @Autowired
    private WorkspaceMemberService workspaceMemberService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (getToken(servletRequest) != null) {
            Optional<UserTokenEntity> token = userTokenEntityService.findByToken(servletRequest.getParameter(TOKEN_PARAM));
            if (token.isPresent()) {

                EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
                //TODO Left it commented out so we can have a reference of the roles
                //Collections.addAll(roles, new String[]{"workspace/admin", "workspace/developer", "system/admin", "system/manager", "user"});
                User user = new UserImpl(token.get().getUser().getAliases().stream().findFirst().get(), token.get().getUser().getUserId(), token.get().getToken(), token.get().getUser().getRoles(), false);
                Set<WorkspaceMemberEntity> workspaces = workspaceMemberService.findByUserId(token.get().getUser().getUserId());
                //TODO Check how to set the correct workspace to the context
                try {
                    if (!workspaces.isEmpty()) {
                        WorkspaceMemberEntity workspaceEntity = workspaces.stream().findFirst().get();
                        environmentContext.setWorkspaceName(workspaceEntity.getWorkspace().getName());
                        environmentContext.setWorkspaceId(workspaceEntity.getWorkspace().getWorkspaceId());
                    }
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

    private String getToken(ServletRequest request) {
        String token = request.getParameter(TOKEN_PARAM);
        if (token == null) {
            Optional<Cookie> cookie = Arrays.asList(((HttpServletRequest) request).getCookies())
                    .stream()
                    .filter(c -> c.getName().equals(COOKIE_NAME))
                    .findFirst();
            if (cookie.isPresent()) {
                token = cookie.get().getValue();
            }
        }
        return token;
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
                return user::getName;
            }
        };
    }
}
