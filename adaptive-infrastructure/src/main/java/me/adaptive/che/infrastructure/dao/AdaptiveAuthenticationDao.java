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

package me.adaptive.che.infrastructure.dao;

import me.adaptive.che.infrastructure.filter.AdaptiveEnvironmentFilter;
import me.adaptive.core.data.api.UserEntityService;
import me.adaptive.core.data.api.UserTokenEntityService;
import me.adaptive.core.data.domain.UserEntity;
import me.adaptive.core.data.domain.UserTokenEntity;
import org.eclipse.che.api.auth.AuthenticationDao;
import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.dto.server.DtoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Service("adaptiveAuthenticationDao")
public class AdaptiveAuthenticationDao implements AuthenticationDao {

    public static final String COOKIE_DELETE_VALUE = "deleted-token";
    private static final int COOKIE_MAX_AGE = 3600 * 2; //2h in seconds

    @Autowired
    UserTokenEntityService userTokenService;

    @Autowired
    UserEntityService userService;

    @Override
    public Response login(Credentials credentials, Cookie tokenAccessCookie, UriInfo uriInfo) throws AuthenticationException {
        String token;
        if (credentials != null && credentials.getUsername() != null && credentials.getPassword() != null) {

            Optional<UserEntity> user = userService.findByEmail(credentials.getUsername());
            if (!user.isPresent()) {
                throw new AuthenticationException(String.format("User %s not found", credentials.getUsername()));
            }

            if (!userService.validatePassword(credentials.getPassword(), user.get().getPasswordHash())) {
                throw new AuthenticationException("Invalid Credentials");
            }

            Set<UserTokenEntity> tokens = userTokenService.findByUser(user.get());

            if (tokens.isEmpty()) {
                token = userTokenService.generateTokenForUser(user.get()).getToken();
            } else {
                token = tokens.stream().findAny().get().getToken();
            }
        } else {
            throw new AuthenticationException("No credentials provided");
        }

        return Response.ok().cookie(
                new NewCookie(new Cookie(AdaptiveEnvironmentFilter.COOKIE_NAME, token), "", COOKIE_MAX_AGE, true))
                .entity((DtoFactory.getInstance().createDto(Token.class).withValue(token))).build();
    }

    @Override
    public Response logout(String token, Cookie tokenAccessCookie, UriInfo uriInfo) {
        NewCookie newCookie = new NewCookie(AdaptiveEnvironmentFilter.COOKIE_NAME, COOKIE_DELETE_VALUE, "/", null, Cookie.DEFAULT_VERSION, null, 1, new Date(0), false, false);
        return Response.ok().cookie(newCookie).build();
    }
}
