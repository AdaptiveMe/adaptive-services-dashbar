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
import com.google.inject.name.Named;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import me.adaptive.core.data.api.UserEntityService;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.inject.DynaModule;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * Module for exposing the metrics for the dashbar calls.
 * Created by ferranvila on 12/08/15.
 */
@DynaModule
@Api(value = "/metrics", description = "Metrics Module")
@Path("/metrics")
public class MetricsModule extends Service {

    @Named("userEntityService")
    @Inject
    UserEntityService userEntityService;

    /**
     * /metrics/user/builds/total/{platform} [android,ios,total]
     * /metrics/user/builds/time/{platform} [android,ios]
     *
     * /metrics/builds/total/{platform} [android,ios,total]
     * /metrics/builds/time/{platform} [android,ios]
     *
     * /metrics/users/total
     *
     * /metrics/server/{server}/{metric} [my.adaptive.me, infra1] [cpu,memory,file]
     */

    @ApiOperation(value = "Total number of users",
            notes = "Returns the total number of users in the system",
            response = String.class,
            position = 1)
    @ApiResponses({@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/users/total")
    @GenerateLink(rel = "total users")
    @Produces(TEXT_PLAIN)
    public String totalUsers() throws ServerException {

        return String.valueOf(userEntityService.count());
    }
}
