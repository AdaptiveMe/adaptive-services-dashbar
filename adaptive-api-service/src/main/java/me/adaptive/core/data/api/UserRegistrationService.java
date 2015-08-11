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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.adaptive.core.data.domain.AccountEntity;
import me.adaptive.core.data.domain.UserEntity;
import me.adaptive.core.data.domain.WorkspaceEntity;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.user.server.Constants;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.commons.lang.NameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by panthro on 10/08/15.
 */
@Service("userRegistrationService")
public class UserRegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationService.class);

    private static final Set<String> DEFAULT_USER_ROLES = Sets.newHashSet("user");
    private static final List<String> DEFAULT_ACCOUNT_ROLES = Lists.newArrayList("account/owner");
    private static final List<String> DEFAULT_WORKSPACE_ROLES = Lists.newArrayList("workspace/owner");


    @Autowired
    UserEntityService userEntityService;
    @Autowired
    AccountEntityService accountEntityService;
    @Autowired
    AccountMemberEntityService accountMemberEntityService;
    @Autowired
    WorkspaceEntityService workspaceEntityService;
    @Autowired
    WorkspaceMemberService workspaceMemberService;
    @Autowired
    ProfileEntityService profileEntityService;

    public boolean validateEmail(String email) {
        return isValidEmail(email) && !userEntityService.findByEmail(email).isPresent();
    }

    public boolean validateUsername(String username) {
        return isValidUsername(username) && !userEntityService.findByUsername(username).isPresent();
    }

    private boolean isValidUsername(String username) {
        return !StringUtils.isEmpty(username) && username.length() > 3 && username.length() < 15;
    }

    private boolean isValidEmail(String email) {
        return email.contains("@"); //TODO do a better check
    }

    public boolean validatePassword(String password) {
        //TODO check password strength maybe using http://stackoverflow.com/questions/3200292/password-strength-checking-library
        return !StringUtils.isEmpty(password) && password.length() >= 6;
    }

    @Transactional
    public User register(String email, String username, String password) throws ConflictException {
        try {
            /**
             * Creates the user
             */
            UserEntity userEntity = new UserEntity();
            userEntity.getAliases().add(email);
            userEntity.getAliases().add(username);
            userEntity.setPasswordHash(userEntityService.generatePasswordHash(password));
            userEntity.getRoles().addAll(DEFAULT_USER_ROLES);
            userEntity.setUserId(NameGenerator.generate("user", Constants.ID_LENGTH));
            userEntity = userEntityService.save(userEntity);

            /**
             * Profile
             */
            profileEntityService.save(profileEntityService.toProfileEntity(new Profile().
                    withId(userEntity.getUserId()).
                    withUserId(userEntity.getUserId()), Optional.empty()));

            /**
             * Account
             */
            AccountEntity account = new AccountEntity();
            account.setAccountId(NameGenerator.generate("acc", org.eclipse.che.api.account.server.Constants.ID_LENGTH));
            account.setName(username + "-account");
            account = accountEntityService.save(account);
            accountMemberEntityService.create(new Member()
                    .withAccountId(account.getAccountId())
                    .withUserId(userEntity.getUserId())
                    .withRoles(DEFAULT_ACCOUNT_ROLES));

            /**
             * Workspace
             */
            WorkspaceEntity workspaceEntity = new WorkspaceEntity();
            workspaceEntity.setAccount(account);
            workspaceEntity.setWorkspaceId(NameGenerator.generate("ws", org.eclipse.che.api.workspace.server.Constants.ID_LENGTH));
            workspaceEntity.setName(username + "ws");
            workspaceEntity.setTemporary(false);
            workspaceEntity = workspaceEntityService.create(workspaceEntity);
            workspaceMemberService.create(new org.eclipse.che.api.workspace.server.dao.Member().
                    withUserId(userEntity.getUserId())
                    .withWorkspaceId(workspaceEntity.getWorkspaceId())
                    .withRoles(DEFAULT_WORKSPACE_ROLES));

            return userEntityService.toUser(userEntity);

        } catch (Exception e) {
            LOGGER.warn("Error registering user", e);
            throw new ConflictException("Error creating user");
        }
    }
}