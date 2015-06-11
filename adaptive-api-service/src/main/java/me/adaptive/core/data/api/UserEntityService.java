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

import me.adaptive.core.data.domain.UserEntity;
import me.adaptive.core.data.repo.UserRepository;
import me.adaptive.core.data.util.PasswordHash;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.che.api.user.server.dao.User;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

/**
 * Created by panthro on 04/06/15.
 */
@Service
public class UserEntityService {

    @Autowired
    private UserRepository userRepository;

    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByAliasesContains(email);
    }

    public UserEntity save(UserEntity entity) {
        return userRepository.save(entity);
    }

    public void delete(UserEntity entity) {
        userRepository.delete(entity);
    }

    public User toUser(UserEntity userEntity){
        User user = new User().withId(userEntity.getUserId()).withEmail(userEntity.getAliases().stream().findFirst().get()).withPassword(userEntity.getPasswordHash());
        CollectionUtils.addAll(user.getAliases(), userEntity.getAliases().iterator());
        return user;
    }

    public UserEntity toUserEntity(User user, Optional<UserEntity> userEntity) {
        UserEntity entity = userEntity.isPresent() ? userEntity.get() : new UserEntity();
        entity.getAliases().add(user.getEmail());
        entity.setUserId(user.getId());
        CollectionUtils.addAll(entity.getAliases(), user.getAliases().iterator());
        return entity;
    }

    public String generatePasswordHash(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        try {
            return PasswordHash.createHash(password);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            LoggerFactory.getLogger(UserEntityService.class).warn("Error generating password", e);
            return null;
        }
    }

    public boolean validatePassword(String password, String hash) {
        try {
            return PasswordHash.validatePassword(password, hash);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            LoggerFactory.getLogger(UserEntityService.class).warn("Error validating password", e);
            return false;
        }
    }

    public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String password = "rafael";
        System.out.println("password = " + password);
        String passwordHash = new UserEntityService().generatePasswordHash(password);
        System.out.println("passwordHash = " + passwordHash);
        boolean validationSuccess = new UserEntityService().validatePassword(password, passwordHash);
        System.out.println("validationSuccess = " + validationSuccess);
    }

    public Optional<UserEntity> findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }
}
