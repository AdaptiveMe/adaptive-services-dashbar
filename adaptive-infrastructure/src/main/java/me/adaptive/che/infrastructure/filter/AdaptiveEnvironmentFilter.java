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
import me.adaptive.core.data.api.WorkspaceEntityService;
import me.adaptive.core.data.api.WorkspaceMemberService;
import me.adaptive.core.data.domain.UserTokenEntity;
import me.adaptive.core.data.domain.WorkspaceEntity;
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
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by panthro on 08/06/15.
 */
@Service("adaptiveEnvironmentFilter")
public class AdaptiveEnvironmentFilter implements Filter {

    private static final String TOKEN_PARAM = "token";

    public static final String COOKIE_NAME = TOKEN_PARAM;

    public static final Pattern WORKSPACE_ID_PATTERN = Pattern.compile(".*\\/(workspace\\w{16}).*");

    public static final Logger LOG = LoggerFactory.getLogger(AdaptiveEnvironmentFilter.class);

    @Autowired
    private UserTokenEntityService userTokenEntityService;

    @Autowired
    private WorkspaceMemberService workspaceMemberService;

    @Autowired
    private WorkspaceEntityService workspaceEntityService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        Optional<String> tokenString;
        try {
            tokenString = getToken(servletRequest);
            if (tokenString.isPresent() && !tokenString.get().equals(AdaptiveAuthenticationDao.COOKIE_DELETE_VALUE)) {
                Optional<UserTokenEntity> token = userTokenEntityService.findByToken(tokenString.get());
                if (token.isPresent()) {

                    EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
                    Set<String> roles = new HashSet<>(token.get().getUser().getRoles());
                    //TODO Left it commented out so we can have a reference of the roles
                    //Collections.addAll(roles, new String[]{"workspace/admin", "workspace/developer", "system/admin", "system/manager", "user"});
                    //TODO Check how to set the correct workspace to the context

                    Optional<WorkspaceEntity> workspaceEntityOptional = getWorkspaceIdFromRequest(servletRequest);
                    Optional<WorkspaceMemberEntity> workspaceMemberEntity = Optional.empty();
                    if (workspaceEntityOptional.isPresent()) {
                        workspaceMemberEntity = workspaceMemberService.findByUserIdAndWorkspaceId(token.get().getUser().getUserId(), workspaceEntityOptional.get().getWorkspaceId());
                    } else {
                        Set<WorkspaceMemberEntity> workspaceMemberEntities = workspaceMemberService.findByUserId(token.get().getUser().getUserId());
                        if (!workspaceMemberEntities.isEmpty()) {
                            workspaceMemberEntity = Optional.ofNullable(workspaceMemberEntities.stream().findFirst().get());
                            //TODO add Account roles to the context
                        }
                    }
                    if (workspaceMemberEntity.isPresent()) {
                        roles.addAll(workspaceMemberEntity.get().getRoles());
                        addWorkspaceToEnvironment(workspaceMemberEntity.get().getWorkspace(), environmentContext);
                    }

                    User user = new UserImpl(token.get().getUser().getAliases().stream().filter(s -> !s.contains("@")).findFirst().get(), token.get().getUser().getUserId(), token.get().getToken(), roles, false);
                    environmentContext.setUser(user);
                    servletRequest = this.addUserInRequest((HttpServletRequest) servletRequest, user);
                }
            }
        } finally {
            filterChain.doFilter(servletRequest, servletResponse);
            EnvironmentContext.reset();
        }
    }

    /**
     * //TODO this is a quick and dirty fix to work around this issue https://github.com/codenvy/everrest/issues/7
     *
     * @param servletRequest
     * @return
     */
    private boolean isBuggyRequest(ServletRequest servletRequest) {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            return "POST".equals(request.getMethod()) && request.getContentType() != null && request.getContentType().contains(MediaType.APPLICATION_FORM_URLENCODED);
        }
        return false;
    }

    private Optional<String> getToken(ServletRequest request) {
        /**
         * Tries to get the token from several places in the following order
         * Request Param
         * Session Attribute
         * Cookie
         */

        /**
         * Request Param
         */
        Optional<String> token = !isBuggyRequest(request) ? Optional.ofNullable(request.getParameter(TOKEN_PARAM)) : Optional.<String>empty();
        if (!token.isPresent()) {
            /**
             * Session
             */
            Object sessionToken = ((HttpServletRequest) request).getSession().getAttribute(TOKEN_PARAM);
            if (sessionToken != null) {
                token = Optional.of(sessionToken.toString());
            }
            if (!token.isPresent()) {
                /**
                 * Cookie
                 */
                if (((HttpServletRequest) request).getCookies() != null) {
                    Optional<Cookie> cookie = Arrays.asList(((HttpServletRequest) request).getCookies())
                            .stream()
                            .filter(c -> c.getName() != null && COOKIE_NAME.equals(c.getName()))
                            .findFirst();
                    if (cookie.isPresent()) {
                        token = Optional.of(cookie.get().getValue());
                    }
                }
            }
        }

        /**
         * Save the token to the session
         */
        if (token.isPresent() && ((HttpServletRequest) request).getSession().getAttribute(TOKEN_PARAM) == null) {
            ((HttpServletRequest) request).getSession().setAttribute(TOKEN_PARAM, token.get());
        }
        return token;
    }


    protected Optional<WorkspaceEntity> getWorkspaceIdFromRequest(ServletRequest request) {
        if (request instanceof HttpServletRequest) {
            String requestUri = ((HttpServletRequest) request).getRequestURI();
            Matcher matcher = WORKSPACE_ID_PATTERN.matcher(requestUri);
            if (matcher.find()) {
                return workspaceEntityService.findByWorkspaceId(matcher.group(1));
            }
        }
        return Optional.empty();
    }

    protected void addWorkspaceToEnvironment(WorkspaceEntity workspaceEntity, EnvironmentContext environmentContext) {
        environmentContext.setWorkspaceName(workspaceEntity.getName());
        environmentContext.setWorkspaceId(workspaceEntity.getWorkspaceId());
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
