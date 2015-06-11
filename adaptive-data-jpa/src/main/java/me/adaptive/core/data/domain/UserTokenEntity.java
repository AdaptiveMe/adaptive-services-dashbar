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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTokenEntity)) return false;
        if (!super.equals(o)) return false;

        UserTokenEntity that = (UserTokenEntity) o;

        if (getUser() != null ? !getUser().equals(that.getUser()) : that.getUser() != null) return false;
        if (getToken() != null ? !getToken().equals(that.getToken()) : that.getToken() != null) return false;
        return !(active != null ? !active.equals(that.active) : that.active != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getToken() != null ? getToken().hashCode() : 0);
        result = 31 * result + (active != null ? active.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserTokenEntity{" +
                "user=" + user +
                ", token='" + token + '\'' +
                ", active=" + active +
                '}';
    }
}
