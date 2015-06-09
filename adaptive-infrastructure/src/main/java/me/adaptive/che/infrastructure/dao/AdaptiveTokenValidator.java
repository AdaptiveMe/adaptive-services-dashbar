
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
import me.adaptive.core.data.api.UserTokenEntityService;
import me.adaptive.core.data.domain.UserTokenEntity;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.user.server.TokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Created by panthro on 05/06/15.
 */
@Service("adaptiveTokenValidator")
public class AdaptiveTokenValidator implements TokenValidator {

    @Autowired
    private UserTokenEntityService userTokenService;

    @Autowired
    private UserEntityService userService;

    @Override
    public String validateToken(String token) throws ConflictException {

        if(StringUtils.isEmpty(token)){
            throw  new ConflictException("Token param is null or empty");
        }
        UserTokenEntity userToken = userTokenService.findByToken(token);
        if(userToken == null){
            throw new ConflictException("User Token not found");
        }

        if(!userToken.isActive()){
            throw new ConflictException("User Token is not active");
        }
        return userToken.getUser().getEmail();
    }

}