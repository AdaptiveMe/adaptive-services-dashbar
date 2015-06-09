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

package me.adaptive.core.data.api;


import me.adaptive.core.data.domain.ProfileEntity;
import me.adaptive.core.data.domain.UserEntity;
import me.adaptive.core.data.repo.ProfileEntityRepository;
import org.eclipse.che.api.user.server.dao.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Created by panthro on 08/06/15.
 */
@Service
public class ProfileEntityService {
    @Autowired
    private ProfileEntityRepository profileEntityRepository;
    @Autowired
    private UserEntityService userService;

    public ProfileEntity findByUser(UserEntity user) {
        return profileEntityRepository.findByUser(user);
    }

    public ProfileEntity save(ProfileEntity profile) {
        return profileEntityRepository.save(profile);
    }

    public ProfileEntity findOne(Long aLong) {
        return profileEntityRepository.findOne(aLong);
    }

    public void delete(Long aLong) {
        profileEntityRepository.delete(aLong);
    }

    public boolean exists(Long aLong) {
        return profileEntityRepository.exists(aLong);
    }

    public ProfileEntity toProfileEntity(Profile profile){
        ProfileEntity entity = new ProfileEntity();
        entity.setAttributes(profile.getAttributes());
        entity.setId(profile.getId() == null ? null : Long.valueOf(profile.getId()));
        entity.setUser(profile.getUserId() == null ? null : userService.findOne(Long.valueOf(profile.getId())));
        return entity;
    }

    public Profile toProfile(ProfileEntity entity){
        return new Profile().withId(entity.getId() == null ? null : entity.getId().toString())
                .withAttributes(entity.getAttributes())
                .withUserId(entity.getUser() == null ? null : entity.getUser().getId().toString());
    }
}
