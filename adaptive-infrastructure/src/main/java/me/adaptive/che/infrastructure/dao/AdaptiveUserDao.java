
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
 */ge me.adaptive.che.infrastructure.dao;

import me.adaptive.core.data.api.UserEntityService;
import me.adaptive.core.data.domain.UserEntity;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("UserDao")
public class AdaptiveUserDao implements UserDao {

    @Autowired
    private UserEntityService userEntityService;

    @Override
    public boolean authenticate(String alias, String password) throws NotFoundException {

        UserEntity user = userEntityService.findByEmail(alias);
        if(user == null){
            user = userEntityService.findByAliasesContains(alias);
        }
        if(user == null){
            throw new NotFoundException(String.format("User %s not found",alias));
        }
        return userEntityService.generatePasswordHash(password).equals(user.getPasswordHash());
    }

    @Override
    public void create(User user) throws ConflictException {
        UserEntity userEntity = userEntityService.findByEmail(user.getEmail());
        if(userEntity != null){
            throw new ConflictException(String.format("Unable create new user '%s'. User email is already in use.", user.getEmail()));
        }
        userEntity = userEntityService.toUserEntity(user);
        //TODO check how we should handle this ID thing
        userEntity.setId(null);
        userEntity.setPasswordHash(user.getPassword());
        userEntity.getRoles().add("user");
        userEntityService.save(userEntity);
    }

    @Override
    public void update(User user) throws NotFoundException {
        UserEntity userEntity = userEntityService.findByEmail(user.getEmail());
        if(userEntity == null) {
            throw new NotFoundException(String.format("User not found %s", user.getEmail()));
        }
        CollectionUtils.addAll(userEntity.getAliases(),user.getAliases().iterator());
        userEntity.setPasswordHash(user.getPassword());
        userEntityService.save(userEntity);
    }

    @Override
    public void remove(String id) throws NotFoundException {
        UserEntity userEntity = userEntityService.findByEmail(id);
        if(userEntity == null) {
            throw new NotFoundException(String.format("User not found %s", id));
        }
        //TODO delete membership
        //TODO check workspaces, account, subscription, etc
        userEntityService.delete(userEntity);
    }

    @Override
    public User getByAlias(String alias) throws NotFoundException {
        UserEntity userEntity = userEntityService.findByAliasesContains(alias);
        if(userEntity == null){
            throw new NotFoundException(String.format("User not found %s", alias));
        }
        return userEntityService.toUser(userEntity);
    }

    @Override
    public User getById(String id) throws NotFoundException {
        UserEntity userEntity = userEntityService.findOne(Long.valueOf(id));
        if(userEntity ==null){
            throw new NotFoundException(String.format("User not found %s", id));
        }
        return userEntityService.toUser(userEntity);
    }
}
