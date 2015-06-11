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

import me.adaptive.core.data.domain.AccountEntity;
import me.adaptive.core.data.domain.AccountMemberEntity;
import me.adaptive.core.data.domain.UserEntity;
import me.adaptive.core.data.repo.AccountMemberRepository;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.che.api.account.server.dao.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by panthro on 04/06/15.
 */
@Service
public class AccountMemberEntityService {

    @Autowired
    private AccountMemberRepository accountMemberRepository;
    @Autowired
    private AccountEntityService accountService;
    @Autowired
    private UserEntityService userService;

    public Set<AccountMemberEntity> findByUserEmailAndRolesContains(String email, String role) {
        return accountMemberRepository.findByUserAliasesContainsAndRolesContains(email, role);
    }


    public Set<AccountMemberEntity> findByUserEmail(String userEmail) {
        return accountMemberRepository.findByUserAliasesContains(userEmail);
    }

    public void delete(Iterable<AccountMemberEntity> entities) {
        accountMemberRepository.delete(entities);
    }

    public void delete(AccountMemberEntity entity) {
        accountMemberRepository.delete(entity);
    }

    public Member toMember(AccountMemberEntity accountMemberEntity){
        List<String> roles = new ArrayList<>(accountMemberEntity.getRoles().size());
        CollectionUtils.addAll(roles, accountMemberEntity.getRoles().iterator());
        return new Member().withAccountId(accountMemberEntity.getAccount().getAccountId()).withUserId(accountMemberEntity.getUser().getUserId()).withRoles(roles);
    }


    public List<Member> toMemberList(Set<AccountMemberEntity> accountMemberEntityList) {
        List<Member> members = new ArrayList<>(accountMemberEntityList.size());
        members.addAll(accountMemberEntityList.stream().map(this::toMember).collect(Collectors.toList()));
        return members;
    }

    public Set<AccountMemberEntity> findByAccount(AccountEntity accountEntity) {
        return accountMemberRepository.findByAccount(accountEntity);
    }

    public Set<AccountMemberEntity> findByAccountId(String accountId) {
        return accountMemberRepository.findByAccountAccountId(accountId);
    }

    public Optional<AccountMemberEntity> findByUserIdAndAccountId(String userEmail, String accountId) {
        return accountMemberRepository.findByUserUserIdAndAccountAccountId(userEmail, accountId);
    }

    public AccountMemberEntity create(Member member){
        Optional<UserEntity> userEntity = userService.findByUserId(member.getUserId());
        AccountMemberEntity accountMemberEntity = new AccountMemberEntity();
        CollectionUtils.addAll(accountMemberEntity.getRoles(),member.getRoles().iterator());
        Optional<AccountEntity> account = accountService.findByAccountId(member.getAccountId());
        accountMemberEntity.setUser(userEntity.get());
        accountMemberEntity.setAccount(account.get());
        return accountMemberRepository.save(accountMemberEntity);
    }

}
