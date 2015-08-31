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
 *
 *
 */

package me.adaptive.core.data.api;

import me.adaptive.core.data.config.JpaConfiguration;
import me.adaptive.core.data.domain.UserEntity;
import me.adaptive.notification.EmailConfiguration;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by panthro on 31/08/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {JpaConfiguration.class, EmailConfiguration.class})
public class UserRegistrationServiceTest {

    @Autowired
    UserRegistrationService userRegistrationService;
    @Autowired
    UserEntityService userEntityService;

    @Test
    public void testValidateToken() throws NotFoundException, ConflictException {
        String email = "rafael@adaptive.me";
        Optional<UserEntity> user = userEntityService.findByEmail(email);
        assertTrue(user.isPresent());
        String token = userRegistrationService.generateTemporaryValidationToken(user.get());
        assertNotNull(userRegistrationService.validateToken(token));


    }
}