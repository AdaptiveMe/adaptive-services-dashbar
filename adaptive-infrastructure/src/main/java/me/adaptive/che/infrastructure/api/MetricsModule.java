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
import com.wordnik.swagger.annotations.*;
import me.adaptive.core.data.api.UserEntityService;
import me.adaptive.core.data.domain.MetricServerEntity;
import me.adaptive.core.data.repo.MetricServerRepository;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.inject.DynaModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.ws.rs.*;
import java.util.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * Module for exposing the metrics for the dashbar calls.
 * Created by ferranvila on 12/08/15.
 */
@DynaModule
@Api(value = "/metrics", description = "Metrics Module")
@Path("/metrics")
public class MetricsModule extends Service {


    /**
     * TODO: /metrics/builds/total/{platform} [android,ios,total]
     * TODO: /metrics/builds/time/{platform} [android,ios]
     */


    @Named("userEntityService")
    @Inject
    UserEntityService userEntityService;

    @Named("metricServerRepository")
    @Inject
    MetricServerRepository metricServerRepository;


    /**
     * Returns the total number of user of the system
     *
     * @return String with the total number of users
     * @throws ServerException Server Exception when some error is produced
     */
    @ApiOperation(value = "Total number of users",
            notes = "Returns the total number of users in the system",
            response = String.class,
            position = 1)
    @ApiResponses({@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/user/total")
    @GenerateLink(rel = "total users")
    @Produces(TEXT_PLAIN)
    public String totalUsers() throws ServerException, ConflictException {

        try {
            return String.valueOf(userEntityService.count());
        } catch (Exception e) {
            throw new ConflictException(e.getMessage());
        }
    }

    /**
     * Returns the number of user's builds per platform or the total number of builds
     *
     * @param platform Platform: ios, android or total
     * @return Return the number of builds
     * @throws ServerException   When some error is produced on the server
     * @throws ConflictException When there are issues with the parameters
     * @throws NotFoundException When the platform is not found
     */
    @ApiOperation(value = "Total number of user's build for platform",
            notes = "Returns the total number of user's builds for every platform",
            response = String.class,
            position = 2)
    @ApiResponses({@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "Platform Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/user/builds/{platform}/total")
    @GenerateLink(rel = "total user builds per platform")
    @Produces(TEXT_PLAIN)
    public String totalUserBuildsPlatform(
            @ApiParam(value = "platform", required = true)
            @PathParam("platform")
            String platform) throws ServerException, ConflictException, NotFoundException {

        if (!(platform.equals("android") || platform.equals("ios") || platform.equals("total"))) {
            throw new NotFoundException("The platform specified is not defined in the system");
        }

        // TODO: Query the tables with the build information. Not created yet.

        return "NOT IMPLEMENTED";
    }

    /**
     * Return the last time values for the build of one platform
     *
     * @param platform Platform: ios, android or total
     * @return The user time values for a build in a platform
     * @throws ServerException   When some error is produced on the server
     * @throws ConflictException When there are issues with the parameters
     * @throws NotFoundException When the platform is not found
     */
    @ApiOperation(value = "Last user's build time values for platform",
            notes = "Returns the last user's build time values for platform",
            response = Map.class,
            position = 3)
    @ApiResponses({@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "Platform Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/user/builds/{platform}/time")
    @GenerateLink(rel = "last user's build time values for platform")
    @Produces(APPLICATION_JSON)
    public Map<String, String> timeUserBuildsPlatform(
            @ApiParam(value = "platform", required = true)
            @PathParam("platform")
            String platform) throws ServerException, ConflictException, NotFoundException {

        if (!(platform.equals("android") || platform.equals("ios") || platform.equals("total"))) {
            throw new NotFoundException("The platform specified is not defined in the system");
        }

        Map<String, String> map = new HashMap<>();

        // TODO: Query the tables with the build information. Not created yet.

        map.put("key1", "NOT IMPLEMENTED");
        map.put("key2", "NOT IMPLEMENTED");

        return map;
    }

    /**
     * Returns the specified number of values for a specific metric on
     * a specific server
     *
     * @param server Hosname of the server to query
     * @param metric Metric to query
     * @param number Number of values
     * @return Metric's values
     * @throws ServerException   When some error is produced on the server
     * @throws ConflictException When there are issues with the parameters
     * @throws NotFoundException When the platform is not found
     */
    @ApiOperation(value = "Server metric last values",
            notes = "Returns the specified values for a metric in a concrete server",
            response = Map.class,
            position = 4)
    @ApiResponses({@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 401, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "Platform Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")})
    @GET
    @Path("/server/{server}/{metric}/{number}")
    @GenerateLink(rel = "server metric values")
    @Produces(APPLICATION_JSON)
    public Map<String, String> serverMetricValues(
            @ApiParam(value = "server", required = true)
            @PathParam("server")
            String server,
            @ApiParam(value = "metric", required = true)
            @PathParam("metric")
            String metric,
            @ApiParam(value = "number", required = false, defaultValue = "20")
            @PathParam("number")
            String number) throws ServerException, ConflictException, NotFoundException {

        Map<String, String> map = new TreeMap<>();

        Page<MetricServerEntity> values = metricServerRepository.findByHostnameAndAttributeKey(server, metric,
                new PageRequest(0, Integer.valueOf(number), new Sort(Sort.Direction.DESC, "createdAt")));

        for (MetricServerEntity ms : values){

            map.put(ms.getCreatedAt().toString(), ms.getAttributes().get(metric));
        }

        return map;
    }
}
