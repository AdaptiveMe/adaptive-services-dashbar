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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Service("adaptiveAuthenticationDao")
public class AdaptiveAuthenticationDao implements AuthenticationDao {

    @Autowired
    UserTokenEntityService userTokenService;

    @Autowired
    UserEntityService userService;

    @Override
    public Response login(Credentials credentials, Cookie tokenAccessCookie, UriInfo uriInfo) throws AuthenticationException {
        String token;
        if(credentials != null && credentials.getUsername() != null && credentials.getPassword() != null){

            UserEntity user = userService.findByEmail(credentials.getUsername());
            if(user == null){
                throw new AuthenticationException(String.format("User %s not found",credentials.getUsername()));
            }

            if(!userService.generatePasswordHash(credentials.getPassword()).equals(user.getPasswordHash())){
                throw new AuthenticationException("Invalid Credentials");
            }

            List<UserTokenEntity> tokens = userTokenService.findByUser(user);

            if(tokens.isEmpty()){
                token = userTokenService.generateTokenForUser(user).getToken();
            }else{
                token = tokens.get(0).getToken();
            }
        }else if(tokenAccessCookie!= null){
            token = userTokenService.findByToken(tokenAccessCookie.getValue()).getToken();
        }else{
            throw new AuthenticationException("No credentials provided");
        }

        return Response.ok().entity((DtoFactory.getInstance().createDto(Token.class).withValue(token))).build();
    }

    @Override
    public Response logout(String token, Cookie tokenAccessCookie, UriInfo uriInfo) {
        return Response.ok().build();
    }
}
