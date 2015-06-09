/*
 *
 *  * Copyright 2014-2015. Adaptive.me.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package me.adaptive.core.data.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Created by panthro on 05/06/15.
 */
@Entity
@Table(name = "user_token")
public class UserTokenEntity extends BaseEntity {

    @ManyToOne
    @NotNull
    private UserEntity user;

    @NotNull
    @Column(name = "token", unique = true, updatable = false)
    private String token;

    @NotNull
    private Boolean active = true;

    public UserTokenEntity() {
        super();
    }

    public UserTokenEntity(UserEntity user, String token) {
        this.user = user;
        this.token = token;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
