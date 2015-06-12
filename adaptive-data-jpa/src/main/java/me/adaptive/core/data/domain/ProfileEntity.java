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
import java.util.Map;

/**
 * Created by panthro on 08/06/15.
 */
@Entity
@Table(name = "profile")
public class ProfileEntity extends BaseEntity {


    @ManyToOne
    @NotNull
    private UserEntity user;

    @NotNull
    @Size(min = 3, max = 100, message = "Profile id can't have more than 100 characters and less than 3")
    @Column(name = "profile_id", unique = true)
    private String profileId;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "profile_attributes", joinColumns = @JoinColumn(name = "profile_id"))
    private Map<String, String> attributes;

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileEntity)) return false;
        if (!super.equals(o)) return false;

        ProfileEntity entity = (ProfileEntity) o;

        if (getUser() != null ? !getUser().equals(entity.getUser()) : entity.getUser() != null) return false;
        if (getProfileId() != null ? !getProfileId().equals(entity.getProfileId()) : entity.getProfileId() != null)
            return false;
        return !(getAttributes() != null ? !getAttributes().equals(entity.getAttributes()) : entity.getAttributes() != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getProfileId() != null ? getProfileId().hashCode() : 0);
        result = 31 * result + (getAttributes() != null ? getAttributes().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProfileEntity{" +
                "user=" + user +
                ", profileId='" + profileId + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
