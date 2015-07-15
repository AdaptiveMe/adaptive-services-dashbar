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

package me.adaptive.infra.client.api;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by panthro on 13/07/15.
 */
public interface BuilderApi {

    @POST("/api/builder/build/{taskId}/{workspaceId}/{projectName}/{platform}")
    String build(@Path("taskId") long taskId,
                 @Path("workspaceId") String workspaceId,
                 @Path("projectName") String projectName,
                 @Path("platform") String platform,
                 @Body BuildRequestBody body);

    @GET("/api/builder/logs/{taskId}/{startLine}")
    Response logs(@Path("taskId") long taskId, @Path("startLine") int startLine);

    @GET("/api/builder/logs/{taskId}/{fromChar}/{toChar}")
    Character[] logs(@Path("taskId") long taskId, @Path("fromChar") long fromChar, @Path("toChar") long toChar);

    @GET("/api/builder/status/{taskId}")
    String status(@Path("taskId") long taskId);
}
