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

import me.adaptive.core.data.api.UserEntityService;
import me.adaptive.core.data.domain.UserEntity;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author Eugene Voevodin
 */
@Service("adaptivePreferenceDao")
public class AdaptivePreferenceDao implements PreferenceDao {
    private static final Logger LOG = LoggerFactory.getLogger(AdaptivePreferenceDao.class);

    @Autowired
    private UserEntityService userEntityService;
    @Override
    public void setPreferences(String userId, Map<String, String> preferences) throws ServerException, NotFoundException {
        UserEntity user = findUserOrThrow(userId);
        user.setPreferences(preferences);
        userEntityService.save(user);
    }

    @Override
    public Map<String, String> getPreferences(String userId) throws ServerException {
        try {
            UserEntity user = findUserOrThrow(userId);
            return user.getPreferences();
        } catch (NotFoundException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public Map<String, String> getPreferences(String userId, String filter) throws ServerException {
        return filter(getPreferences(userId), filter);
    }

    private Map<String, String> filter(Map<String, String> preferences, String filter) {
        final Map<String, String> filtered = new HashMap<>();
        final Pattern pattern = Pattern.compile(filter);
        preferences.entrySet().stream().filter(entry -> pattern.matcher(entry.getKey()).matches()).forEach(entry -> {
            filtered.put(entry.getKey(), entry.getValue());
        });
        return filtered;
    }

    @Override
    public void remove(String userId) throws ServerException {
        try {
            UserEntity userEntity = findUserOrThrow(userId);
            userEntity.getPreferences().clear();
            userEntityService.save(userEntity);
        } catch (NotFoundException e) {
            throw new ServerException(e);
        }
    }

    private UserEntity findUserOrThrow(String userId) throws NotFoundException {
        Optional<UserEntity> user = userEntityService.findByUserId(userId);
        if (!user.isPresent()) {
            throw new NotFoundException(String.format("User with id %s not found", userId));
        }
        return user.get();
    }
}
