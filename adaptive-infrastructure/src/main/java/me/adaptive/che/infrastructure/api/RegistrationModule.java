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

package me.adaptive.che.infrastructure.api;

import com.google.inject.Inject;
import com.wordnik.swagger.annotations.*;
import me.adaptive.core.data.api.UserRegistrationService;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.inject.DynaModule;
import org.springframework.util.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_CREATE_USER;

/**
 * Created by panthro on 10/08/15.
 */
@DynaModule
@Api(value = "/register", description = "User registration")
@Path("/register")
public class RegistrationModule extends Service {


    UserRegistrationService userRegistrationService;


    @Inject
    public RegistrationModule(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }


    /**
     * Creates new user and profile.
     *
     * @param email    the user email
     * @param username the username
     * @param password the user password
     * @return entity of created user
     * @throws ConflictException if the email or username is already used
     * @throws ServerException   when some error occurred while persisting user or user profile
     * @see UserDescriptor
     */
    @ApiOperation(value = "Register a new user",
            notes = "Register a new user in the system",
            response = UserDescriptor.class,
            position = 1)
    @ApiResponses({@ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 401, message = "Missed token parameter"),
            @ApiResponse(code = 409, message = "Invalid token"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @POST
    @Path("/create")
    @GenerateLink(rel = LINK_REL_CREATE_USER)
    @Produces(APPLICATION_JSON)
    public Response create(@ApiParam(value = "Email", required = true) @FormParam("email") @Required String email,
                           @ApiParam(value = "Username", required = true) @FormParam("username") @Required String username,
                           @ApiParam(value = "Password", required = true) @FormParam("password") @Required String password,
                           @Context SecurityContext context) throws UnauthorizedException,
            ConflictException,
            ServerException,
            NotFoundException {
        if (!userRegistrationService.validateEmail(email) || !userRegistrationService.validateUsername(username) || !userRegistrationService.validatePassword(password)) {
            throw new ConflictException("Invalid registration parameters");
        }

        User user = userRegistrationService.register(email, username, password);

        return Response.status(Response.Status.CREATED).entity(user).build();
    }

    @ApiOperation(value = "Validate email and username", notes = "Check if email or username are available for registration", response = Boolean.class, position = 1)
    @ApiResponses({@ApiResponse(code = 200, message = "Request OK check response"),
            @ApiResponse(code = 401, message = "Invalid parameters"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @Path("/validate")
    @POST
    @Produces(APPLICATION_JSON)
    public Response validate(@ApiParam(value = "Email", required = false) @QueryParam("email") String email, @ApiParam(value = "Username", required = false) @QueryParam("username") String username) throws ConflictException {
        if (StringUtils.isEmpty(email) && StringUtils.isEmpty(username)) {
            throw new ConflictException("At least one parameter needs to be specified");
        }

        if (!StringUtils.isEmpty(email)) {
            userRegistrationService.validateEmail(email);
            throw new ConflictException("Email not valid or already registered");
        }

        if (!StringUtils.isEmpty(username)) {
            userRegistrationService.validateUsername(username);
            throw new ConflictException("Username not valid or already taken");
        }

        return Response.ok().build();
    }


}
