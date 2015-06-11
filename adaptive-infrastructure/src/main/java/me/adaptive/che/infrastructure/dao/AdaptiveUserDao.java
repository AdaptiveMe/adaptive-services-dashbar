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
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

@Service("UserDao")
public class AdaptiveUserDao implements UserDao {

    @Autowired
    private UserEntityService userEntityService;

    @Override
    public boolean authenticate(String alias, String password) throws NotFoundException {

        Optional<UserEntity> user = userEntityService.findByEmail(alias);
        if (!user.isPresent()) {
            throw new NotFoundException(String.format("User %s not found",alias));
        }
        return userEntityService.validatePassword(password, user.get().getPasswordHash());
    }

    @Override
    public void create(User user) throws ConflictException {
        Optional<UserEntity> userEntity = userEntityService.findByEmail(user.getEmail());
        if (!userEntity.isPresent()) {
            throw new ConflictException(String.format("Unable create new user '%s'. User email is already in use.", user.getEmail()));
        }

        userEntity = userEntityService.findByUserId(user.getId());
        if (userEntity.isPresent()) {
            throw new ConflictException(String.format("Unable create new user '%s'. User id is already in use.", user.getId()));
        }
        UserEntity newUser = userEntityService.toUserEntity(user, Optional.<UserEntity>empty());

        try {
            newUser.setPasswordHash(userEntityService.generatePasswordHash(user.getPassword()));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            LoggerFactory.getLogger(AdaptiveUserDao.class).warn("Error creating user password", e);
            throw new ConflictException("Error creating user password");
        }
        newUser.getRoles().add("user");
        userEntityService.save(newUser);
    }

    @Override
    public void update(User user) throws NotFoundException {
        Optional<UserEntity> userEntity = userEntityService.findByEmail(user.getEmail());
        if (!userEntity.isPresent()) {
            throw new NotFoundException(String.format("User not found %s", user.getEmail()));
        }
        userEntityService.toUserEntity(user, userEntity);
        try {
            if (user.getPassword() != null) {
                userEntity.get().setPasswordHash(userEntityService.generatePasswordHash(user.getPassword()));
            }
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            LoggerFactory.getLogger(AdaptiveUserDao.class).warn("Unable to generate user password", e);
        }
        userEntityService.save(userEntity.get());
    }

    @Override
    public void remove(String id) throws NotFoundException {
        Optional<UserEntity> userEntity = userEntityService.findByEmail(id);
        if (!userEntity.isPresent()) {
            throw new NotFoundException(String.format("User not found %s", id));
        }
        //TODO delete membership
        //TODO check workspaces, account, subscription, etc
        userEntityService.delete(userEntity.get());
    }

    @Override
    public User getByAlias(String alias) throws NotFoundException {
        Optional<UserEntity> userEntity = userEntityService.findByEmail(alias);
        if (!userEntity.isPresent()) {
            throw new NotFoundException(String.format("User not found %s", alias));
        }
        return userEntityService.toUser(userEntity.get());
    }

    @Override
    public User getById(String id) throws NotFoundException {
        Optional<UserEntity> userEntity = userEntityService.findByUserId(id);
        if (!userEntity.isPresent()) {
            throw new NotFoundException(String.format("User not found %s", id));
        }
        return userEntityService.toUser(userEntity.get());
    }
}
