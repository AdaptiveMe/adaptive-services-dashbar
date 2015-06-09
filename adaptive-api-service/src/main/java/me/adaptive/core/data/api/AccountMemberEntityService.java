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

    public List<AccountMemberEntity> findByUserEmailAndRolesContains(String email, String role) {
        return accountMemberRepository.findByUserEmailAndRolesContains(email, role);
    }

    public List<AccountMemberEntity> findAll() {
        return accountMemberRepository.findAll();
    }

    public List<AccountMemberEntity> findByUserEmail(String userEmail) {
        return accountMemberRepository.findByUserEmail(userEmail);
    }

    public List<AccountMemberEntity> findByUserId(Long id) {
        return accountMemberRepository.findByUserId(id);
    }

    public void delete(Iterable<AccountMemberEntity> entities) {
        accountMemberRepository.delete(entities);
    }

    public void delete(AccountMemberEntity entity) {
        accountMemberRepository.delete(entity);
    }

    public Member toMember(AccountMemberEntity accountMemberEntity){
        List<String> roles = new ArrayList<String>(accountMemberEntity.getRoles().size());
        CollectionUtils.addAll(roles, accountMemberEntity.getRoles().iterator());
        return new Member().withAccountId(accountMemberEntity.getAccount().getId().toString()).withUserId(accountMemberEntity.getUser().getEmail()).withRoles(roles);
    }


    public List<Member> toMemberList(List<AccountMemberEntity> accountMemberEntityList){
        List<Member> members = new ArrayList<Member>(accountMemberEntityList.size());
        for(AccountMemberEntity accountMemberEntity : accountMemberEntityList){
            members.add(toMember(accountMemberEntity));
        }
        return members;
    }

    public List<AccountMemberEntity> findByAccount(AccountEntity accountEntity) {
        return accountMemberRepository.findByAccount(accountEntity);
    }

    public List<AccountMemberEntity> findByUserEmailAndAccountId(String userEmail, Long accountId) {
        return accountMemberRepository.findByUserEmailAndAccountId(userEmail, accountId);
    }

    public AccountMemberEntity create(Member member){

        UserEntity userEntity = userService.findByEmail(member.getUserId());
        AccountMemberEntity accountMemberEntity = new AccountMemberEntity();
        CollectionUtils.addAll(accountMemberEntity.getRoles(),member.getRoles().iterator());
        AccountEntity account = accountService.findOne(Long.valueOf(member.getAccountId()));
        accountMemberEntity.setUser(userEntity);
        accountMemberEntity.setAccount(account);
        return accountMemberRepository.save(accountMemberEntity);
    }

}
