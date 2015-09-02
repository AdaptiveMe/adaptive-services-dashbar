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
import me.adaptive.core.data.domain.BuildRequestEntity;
import me.adaptive.core.data.domain.MetricServerEntity;
import me.adaptive.core.data.domain.UserEntity;
import me.adaptive.core.data.repo.BuildRequestRepository;
import me.adaptive.core.data.repo.MetricServerRepository;
import me.adaptive.core.data.repo.UserRepository;
import org.apache.commons.collections.bidimap.TreeBidiMap;
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
import java.time.LocalDate;
import java.time.ZoneId;
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


    @Named("userEntityService")
    @Inject
    UserEntityService userEntityService;

    @Named("metricServerRepository")
    @Inject
    MetricServerRepository metricServerRepository;

    @Named("buildRequestRepository")
    @Inject
    BuildRequestRepository buildRequestRepository;

    @Named("userRepository")
    @Inject
    UserRepository userRepository;


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

    // TODO: javadoc
    // TODO: apiresponses and exceptions

    /**
     * Returns the number of user's builds per platform or the total number of builds
     *
     * @param platform Platform: ios, android or total
     * @return Return the number of builds
     * @throws NotFoundException When the platform is not found
     */
    @ApiOperation(value = "Build metrics",
            notes = "Returns the build metrics information requested",
            response = Map.class,
            position = 2)
    @ApiResponses({@ApiResponse(code = 404, message = "Platform Not Found")})
    @GET
    @Path("/build/{metric}/{aggregation}/{startDate}/{endDate}")
    @GenerateLink(rel = "build metrics")
    @Produces(APPLICATION_JSON)
    public Map<String, Double> buildMetrics(
            @ApiParam(value = "platform", required = false)
            @QueryParam("platform")
            String platform,
            @ApiParam(value = "user_id", required = false)
            @QueryParam("user_id")
            String user_id,
            @ApiParam(value = "metric", required = true)
            @PathParam("metric")
            String metric,
            @ApiParam(value = "aggregation", required = true)
            @PathParam("aggregation")
            String aggregation,
            @ApiParam(value = "startDate", required = true)
            @PathParam("startDate")
            long startDate,
            @ApiParam(value = "endDate", required = true)
            @PathParam("endDate")
            long endDate) throws NotFoundException {

        Map<String, Double> map = new TreeMap<>();
        Map<String, Double> dayOccurrences = new TreeMap<>();

        LocalDate start = new Date(startDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = new Date(endDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Fill the map with the values of the x-axis
        switch (aggregation) {
            case "sum":
                map.put("sum", 0.0);
                break;
            case "day":
                for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
                    map.put(date.toString(), 0.0);
                    dayOccurrences.put(date.toString(), 0.0);
                }
                break;
            default:
                throw new NotFoundException("The aggregation {"+aggregation+"} is not found in the system. " +
                        "Should be: [sum,day]");
        }

        Set<BuildRequestEntity> values;

        if (user_id != null && platform != null) {
            Optional<UserEntity> user = userRepository.findByUserId(user_id);
            values = buildRequestRepository.findByPlatformAndRequesterAndStartTimeBetween(platform, user.get(), new Date(startDate), new Date(endDate));
        } else if (user_id == null && platform != null) {
            values = buildRequestRepository.findByPlatformAndStartTimeBetween(platform, new Date(startDate), new Date(endDate));
        } else if (user_id != null && platform == null) {
            Optional<UserEntity> user = userRepository.findByUserId(user_id);
            values = buildRequestRepository.findByRequesterAndStartTimeBetween(user.get(), new Date(startDate), new Date(endDate));
        } else {
            values = buildRequestRepository.findByStartTimeBetween(new Date(startDate), new Date(endDate));
        }

        // Depending on the metric
        switch (metric) {
            case "total":

                switch (aggregation) {
                    case "sum":
                        map.put("sum", (double) values.size());
                        break;
                    case "day":
                        for (BuildRequestEntity event : values) {
                            String eventDate = event.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                            map.put(eventDate, map.get(eventDate) + 1);
                        }
                        break;
                    default:
                        throw new NotFoundException("The aggregation {"+aggregation+"} is not found in the system. " +
                                "Should be: [sum,day]");
                }
                break;
            case "time":

                switch (aggregation) {
                    case "sum":
                        double sum = 0;
                        for (BuildRequestEntity event : values) {
                            System.out.println(event.getEndTime().getTime() - event.getStartTime().getTime() + "ms");
                            sum += (event.getEndTime().getTime() - event.getStartTime().getTime());
                        }
                        if (values.size()>0){
                            map.put("sum", sum/values.size());
                        }
                        break;
                    case "day":
                        for (BuildRequestEntity event : values) {
                            String eventDate = event.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                            map.put(eventDate, map.get(eventDate) + (event.getEndTime().getTime() - event.getStartTime().getTime()));
                            dayOccurrences.put(eventDate, dayOccurrences.get(eventDate) + 1);
                        }

                        for (Map.Entry<String, Double> entry : map.entrySet()) {
                            if (dayOccurrences.get(entry.getKey()) > 0) {
                                map.put(entry.getKey(), (entry.getValue() / dayOccurrences.get(entry.getKey())));
                            }
                        }

                        break;
                    default:
                        throw new NotFoundException("The aggregation {"+aggregation+"} is not found in the system. " +
                                "Should be: [sum,day]");
                }
                break;
            default:
                throw new NotFoundException("The metric {"+metric+"} is not found in the system. Should be: [total,time]");
        }

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
            position = 3)
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
