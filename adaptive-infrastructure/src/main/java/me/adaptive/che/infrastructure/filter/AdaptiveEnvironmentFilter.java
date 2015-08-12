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

import me.adaptive.che.infrastructure.dao.AdaptiveAuthenticationDao;
import me.adaptive.core.data.api.UserTokenEntityService;
import me.adaptive.core.data.api.WorkspaceMemberService;
import me.adaptive.core.data.domain.UserTokenEntity;
import me.adaptive.core.data.domain.WorkspaceMemberEntity;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.commons.user.UserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Created by panthro on 08/06/15.
 */
@Service("adaptiveEnvironmentFilter")
public class AdaptiveEnvironmentFilter implements Filter {

    private static final String TOKEN_PARAM = "token";

    public static final String COOKIE_NAME = TOKEN_PARAM;

    public static final Logger LOG = LoggerFactory.getLogger(AdaptiveEnvironmentFilter.class);

    @Autowired
    private UserTokenEntityService userTokenEntityService;

    @Autowired
    private WorkspaceMemberService workspaceMemberService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        String tokenString;
        try {
            tokenString = getToken(servletRequest);
            if (tokenString != null && !tokenString.equals(AdaptiveAuthenticationDao.COOKIE_DELETE_VALUE)) {
                Optional<UserTokenEntity> token = userTokenEntityService.findByToken(tokenString);
                if (token.isPresent()) {

                    EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
                    Set<String> roles = new HashSet<>(token.get().getUser().getRoles());
                    //TODO Left it commented out so we can have a reference of the roles
                    //Collections.addAll(roles, new String[]{"workspace/admin", "workspace/developer", "system/admin", "system/manager", "user"});
                    //TODO Check how to set the correct workspace to the context
                    Set<WorkspaceMemberEntity> workspaces = workspaceMemberService.findByUserId(token.get().getUser().getUserId());
                    if (!workspaces.isEmpty()) {
                        WorkspaceMemberEntity workspaceEntity = workspaces.stream().findFirst().get();
                        roles.addAll(workspaceEntity.getRoles());
                        //TODO add Account roles to the context
                        environmentContext.setWorkspaceName(workspaceEntity.getWorkspace().getName());
                        environmentContext.setWorkspaceId(workspaceEntity.getWorkspace().getWorkspaceId());
                    }

                    User user = new UserImpl(token.get().getUser().getAliases().stream().findFirst().get(), token.get().getUser().getUserId(), token.get().getToken(), roles, false);
                    environmentContext.setUser(user);
                    servletRequest = this.addUserInRequest((HttpServletRequest) servletRequest, user);
                }
            }
        } finally {
            filterChain.doFilter(servletRequest, servletResponse);
            EnvironmentContext.reset();
        }
    }

    private String getToken(ServletRequest request) {
        /**
         * Tries to get the token from several places in the following order
         * Request Param
         * Session Attribute
         * Cookie
         */

        /**
         * Request Param
         */
        //TODO this is a quick and dirty fix to work around this issue https://github.com/codenvy/everrest/issues/7
        String token = !"POST".equals(((HttpServletRequest) request).getMethod()) ? request.getParameter(TOKEN_PARAM) : null;
        if (token == null) {
            /**
             * Session
             */
            Object sessionToken = ((HttpServletRequest) request).getSession().getAttribute(TOKEN_PARAM);
            if (sessionToken != null) {
                token = sessionToken.toString();
            }
            if (token == null) {
                /**
                 * Cookie
                 */
                if (((HttpServletRequest) request).getCookies() != null) {
                    Optional<Cookie> cookie = Arrays.asList(((HttpServletRequest) request).getCookies())
                            .stream()
                            .filter(c -> c.getName() != null && COOKIE_NAME.equals(c.getName()))
                            .findFirst();
                    if (cookie.isPresent()) {
                        token = cookie.get().getValue();
                    }
                }
            }
        }

        /**
         * Save the token to the session
         */
        if (token != null && ((HttpServletRequest) request).getSession().getAttribute(TOKEN_PARAM) == null) {
            ((HttpServletRequest) request).getSession().setAttribute(TOKEN_PARAM, token);
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
