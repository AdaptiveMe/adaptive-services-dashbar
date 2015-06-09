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
 */ge me.adaptive.core.data.api;

import me.adaptive.core.data.domain.UserEntity;
import me.adaptive.core.data.repo.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.che.api.user.server.dao.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by panthro on 04/06/15.
 */
@Service
public class UserEntityService {

    private static final String PASSWORD_SALT = "1AKac7etAyhdV7aT2A8kf45Axpjy44rNs0mMnxYq";

    @Autowired
    private UserRepository userRepository;

    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserEntity findByAliasesContains(String alias) {
        return userRepository.findByAliasesContains(alias);
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public UserEntity save(UserEntity entity) {
        return userRepository.save(entity);
    }

    public UserEntity findOne(Long aLong) {
        return userRepository.findOne(aLong);
    }

    public boolean exists(Long aLong) {
        return userRepository.exists(aLong);
    }

    public long count() {
        return userRepository.count();
    }

    public void delete(Long aLong) {
        userRepository.delete(aLong);
    }

    public void delete(UserEntity entity) {
        userRepository.delete(entity);
    }

    public User toUser(UserEntity userEntity){
        User user = new User().withId(userEntity.getId().toString()).withEmail(userEntity.getEmail()).withPassword(userEntity.getPasswordHash());
        CollectionUtils.addAll(user.getAliases(),userEntity.getAliases().iterator());
        return user;
    }
    public UserEntity toUserEntity(User user){
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(user.getEmail());
        userEntity.setId(user.getId() != null ? Long.valueOf(user.getId()) : null);
        CollectionUtils.addAll(userEntity.getAliases(), user.getAliases().iterator());
        return userEntity;
    }

    public String generatePasswordHash(String password){
        return new String(DigestUtils.sha256Hex(new StringBuilder(password).append(PASSWORD_SALT).toString()));
    }

    public static void main(String[] args){
        System.out.println(new UserEntityService().generatePasswordHash("123456"));
    }
}
