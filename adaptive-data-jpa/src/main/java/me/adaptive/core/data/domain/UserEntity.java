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

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by panthro on 04/06/15.
 */
@Entity
@Table(name = "user")
public class UserEntity extends BaseEntity {

    @NotNull
    @Size(min = 3, max = 100, message = "User id can't have more than 100 characters and less than 3")
    @Column(name = "user_id", unique = true)
    private String userId;

    @NotNull
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<String>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_aliases", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    @Column(name = "alias")
    private Set<String> aliases = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "user_preferences", joinColumns = @JoinColumn(name = "user_id"))
    private Map<String, String> preferences;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
    }

    public Map<String, String> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity)) return false;
        if (!super.equals(o)) return false;

        UserEntity that = (UserEntity) o;

        if (getUserId() != null ? !getUserId().equals(that.getUserId()) : that.getUserId() != null) return false;
        if (getPasswordHash() != null ? !getPasswordHash().equals(that.getPasswordHash()) : that.getPasswordHash() != null)
            return false;
        if (getRoles() != null ? !getRoles().equals(that.getRoles()) : that.getRoles() != null) return false;
        return !(getAliases() != null ? !getAliases().equals(that.getAliases()) : that.getAliases() != null);

    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "userId='" + userId + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", roles=" + roles +
                ", aliases=" + aliases +
                '}';
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getUserId() != null ? getUserId().hashCode() : 0);
        result = 31 * result + (getPasswordHash() != null ? getPasswordHash().hashCode() : 0);
        result = 31 * result + (getRoles() != null ? getRoles().hashCode() : 0);
        result = 31 * result + (getAliases() != null ? getAliases().hashCode() : 0);
        return result;
    }

}
