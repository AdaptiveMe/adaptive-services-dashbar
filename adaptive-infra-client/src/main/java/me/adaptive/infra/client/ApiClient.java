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

package me.adaptive.infra.client;

import me.adaptive.infra.client.api.BuilderApi;
import retrofit.RestAdapter;

/**
 * Created by panthro on 13/07/15.
 */
public class ApiClient {

    private static final String TOKEN_HEADER = "apiKey";

    private final String endpoint;
    private final String token;


    /**
     * Creates a reusable ApiClient
     *
     * @param endpoint the endpoint URL that this client should point to
     * @param token    the authentication toke, can be null if calling Anonymous APIs
     */
    public ApiClient(String endpoint, String token) {
        this.endpoint = endpoint;
        this.token = token == null ? "Temp Token" : token;
    }

    public BuilderApi getBuilderApi() {
        return adapter().create(BuilderApi.class);
    }

    private RestAdapter adapter() {
        RestAdapter.Builder builder = new RestAdapter.Builder();
        if (token != null) {
            builder.setRequestInterceptor(request -> request.addHeader(TOKEN_HEADER, token));
        }
        return builder.setEndpoint(endpoint).build();
    }
}
