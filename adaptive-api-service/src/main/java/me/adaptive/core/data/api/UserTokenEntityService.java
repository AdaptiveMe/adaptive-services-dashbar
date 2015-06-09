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
import me.adaptive.core.data.domain.UserTokenEntity;
import me.adaptive.core.data.repo.UserTokenRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by panthro on 05/06/15.
 */
@Service
public class UserTokenEntityService {

    private static final String SALT = "yyxFHMF6zYwcm8ePH7Uk";

    @Autowired
    private UserTokenRepository userTokenRepository;

    public UserTokenEntity generateTokenForUser(UserEntity userEntity){
        return userTokenRepository.save(new UserTokenEntity(userEntity,getToken(userEntity)));
    }

    private String getToken(UserEntity user){
        StringBuilder builder = new StringBuilder(user.getId().toString());

        builder.append(user.getEmail())
                .append(user.getPasswordHash())
                .append(System.nanoTime())
                .append(SALT);
        return String.valueOf(DigestUtils.sha256Hex(builder.toString()));
    }

    public UserTokenEntity findByToken(String token) {
        return userTokenRepository.findByToken(token);
    }

    public List<UserTokenEntity> findByUser(UserEntity user) {
        return userTokenRepository.findByUserAndActiveTrue(user);
    }
}
