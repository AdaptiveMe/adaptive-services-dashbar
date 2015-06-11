
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

package me.adaptive.che.infrastructure.dao;

import me.adaptive.core.data.api.ProfileEntityService;
import me.adaptive.core.data.domain.ProfileEntity;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("adaptiveProfileDao")
public class AdaptiveProfileDao implements UserProfileDao {
    private static final Logger LOG = LoggerFactory.getLogger(AdaptiveProfileDao.class);
    @Autowired
    private ProfileEntityService profileEntityService;


    @Override
    public void create(Profile profile) {
        profileEntityService.save(profileEntityService.toProfileEntity(profile, Optional.<ProfileEntity>empty()));
    }

    @Override
    public void update(Profile profile) throws NotFoundException {
        Optional<ProfileEntity> profileEntity = profileEntityService.findByProfileId(profile.getId());
        if (!profileEntity.isPresent()) {
            throw new NotFoundException(String.format("Profile not found %s", profile.getId()));
        }
        profileEntityService.save(profileEntityService.toProfileEntity(profile, profileEntity));
    }

    @Override
    public void remove(String id) throws NotFoundException {
        Optional<ProfileEntity> profileEntity = profileEntityService.findByProfileId(id);
        if (!profileEntity.isPresent()) {
            throw new NotFoundException(String.format("Profile not found %s", id));

        }
        profileEntityService.delete(profileEntity.get());
    }

    @Override
    public Profile getById(String id) throws NotFoundException {
        Optional<ProfileEntity> profileEntity = profileEntityService.findByProfileId(id);
        if (!profileEntity.isPresent()) {
            throw new NotFoundException(String.format("Profile not found %s", id));
        }
        return profileEntityService.toProfile(profileEntity.get());
    }
}
