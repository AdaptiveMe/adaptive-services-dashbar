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
import me.adaptive.core.data.domain.*;
import me.adaptive.core.data.domain.types.NotificationChannel;
import me.adaptive.core.data.domain.types.NotificationEvent;
import me.adaptive.core.data.domain.types.NotificationStatus;
import me.adaptive.core.data.repo.NotificationRepository;
import me.adaptive.core.data.util.SystemSettingHolder;
import me.adaptive.core.data.util.UserPreferences;
import me.adaptive.services.notification.NotificationSender;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.Constants;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.commons.lang.NameGenerator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by panthro on 10/08/15.
 */
@Service("userRegistrationService")
public class UserRegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationService.class);

    private static final Set<String> DEFAULT_USER_ROLES = Sets.newHashSet("user");
    private static final List<String> DEFAULT_ACCOUNT_ROLES = Lists.newArrayList("account/owner");
    private static final List<String> DEFAULT_WORKSPACE_ROLES = Lists.newArrayList("workspace/admin", "workspace/developer");
    private static final String FORGOT_PASSWORD_SALT_KEY = "forgot_password_token_salt";


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
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserTokenEntityService userTokenEntityService;

    @Autowired
    NotificationSender notificationSender;

    public boolean validateEmail(String email) {
        return isValidEmail(email) && !userEntityService.findByEmail(email).isPresent();
    }

    public boolean validateUsername(String username) {
        return isValidUsername(username) && !userEntityService.findByUsername(username).isPresent();
    }

    private boolean isValidUsername(String username) {
        return !StringUtils.isEmpty(username) && !StringUtils.containsWhitespace(username) && username.length() > 3 && username.length() < 15;
    }

    private boolean isValidEmail(String email) {
        return !StringUtils.isEmpty(email) && !StringUtils.containsWhitespace(email) && email.contains("@"); //TODO do a better check
    }

    public boolean validatePassword(String password) {
        //TODO check password strength maybe using http://stacoverflow.com/questions/3200292/password-strength-checking-library
        return !StringUtils.isEmpty(password) && password.length() >= 6;
    }

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
            userEntity.setUserId(NameGenerator.generate("user-", Constants.ID_LENGTH));
            userEntity.getPreferences().put(UserPreferences.Notification.EMAIL, email);
            userEntity = userEntityService.saveAndFlush(userEntity);

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
            account.setAccountId(NameGenerator.generate("acc-", org.eclipse.che.api.account.server.Constants.ID_LENGTH));
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
            workspaceEntity.setWorkspaceId(NameGenerator.generate(Workspace.class.getSimpleName().toLowerCase(), org.eclipse.che.api.workspace.server.Constants.ID_LENGTH));
            workspaceEntity.setName(username + "-ws");
            workspaceEntity.setTemporary(false);
            workspaceEntity = workspaceEntityService.create(workspaceEntity);
            workspaceMemberService.create(new org.eclipse.che.api.workspace.server.dao.Member().
                    withUserId(userEntity.getUserId())
                    .withWorkspaceId(workspaceEntity.getWorkspaceId())
                    .withRoles(DEFAULT_WORKSPACE_ROLES));

            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setStatus(NotificationStatus.CREATED);
            notificationEntity.setUserNotified(userEntity);
            notificationEntity.setEvent(NotificationEvent.USER_REGISTERED);
            notificationEntity.setChannel(NotificationChannel.EMAIL);
            notificationEntity.setDestination(email);
            notificationEntity = notificationRepository.save(notificationEntity);
            notificationSender.releaseNotification(notificationEntity);
            return userEntityService.toUser(userEntity);
        } catch (Exception e) {
            LOGGER.warn("Error registering user", e);
            throw new ConflictException("Error creating user");
        }
    }

    /**
     * Requests a forgot password activation link
     *
     * @param email
     */
    public void forgotPassword(String email) throws NotFoundException {
        Optional<UserEntity> user = userEntityService.findByEmail(email);
        if (!user.isPresent()) {
            throw new NotFoundException("Could not find user for given email " + email);
        }
        NotificationEntity notification = new NotificationEntity();
        notification.setDestination(email);
        notification.setEvent(NotificationEvent.FORGOT_PASSWORD);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setUserNotified(user.get());
        Map<String, Object> model = new HashMap<>();
        model.put("forgotToken", generateTemporaryValidationToken(user.get()));
        notificationSender.releaseNotification(notification, model);
    }

    /**
     * This will generate a token with the following format:
     * base64(id:sha1(currentPassword:SALT):base64(timestamp))
     *
     * @param user
     * @return the token string
     */
    String generateTemporaryValidationToken(UserEntity user) {

        return Base64.encodeBase64URLSafeString(
                (user.getId().toString()
                        + ':'
                        + getValidationHash(user)
                        + ':'
                        + Base64.encodeBase64String(String.valueOf(System.currentTimeMillis()).getBytes())).getBytes()
        );
    }


    /**
     * Validate a token generate previously
     *
     * @param tokenOrigin the token string
     * @return the user associated with the token
     * @throws NotFoundException in case no user can be found by the token
     * @throws ConflictException in case token is invalid OR the token hash is invalid OR the token is expired
     */
    public UserTokenEntity validateToken(String tokenOrigin) throws NotFoundException, ConflictException {
        String token = new String(Base64.decodeBase64(tokenOrigin));

        String[] parts = token.split(":");
        if (parts.length != 3) {
            throw new ConflictException("Invalid token format");
        }
        Long id = Long.valueOf(parts[0]);
        String validationHash = parts[1];
        Long timestamp = Long.valueOf(new String(Base64.decodeBase64(parts[2])));
        Optional<UserEntity> user = userEntityService.findOne(id);
        if (!user.isPresent()) {
            throw new NotFoundException("Could not get the user from the token");
        }
        if (!validationHash.equals(getValidationHash(user.get()))) {
            throw new ConflictException("Invalid token");
        }
        if (DateTime.now().minusHours(24).isAfter(timestamp)) {
            throw new ConflictException("Expired token");
        }
        Optional<UserTokenEntity> userToken = userTokenEntityService.findByUser(user.get()).stream().findAny();
        if (!userToken.isPresent()) {
            return userTokenEntityService.generateTokenForUser(user.get());
            //throw new ConflictException("User does not have an access token.");
        }

        return userToken.get();


    }

    protected String getValidationHash(UserEntity user) {
        return DigestUtils.sha1Hex(user.getPasswordHash() + ':' + SystemSettingHolder.getSettingByKey(FORGOT_PASSWORD_SALT_KEY).get().getValue());
    }
}
