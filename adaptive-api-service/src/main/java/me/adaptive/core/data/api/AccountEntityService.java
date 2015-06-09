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
import me.adaptive.core.data.repo.AccountRepository;
import org.eclipse.che.api.account.server.dao.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by panthro on 04/06/15.
 */
@Service
public class AccountEntityService {

    @Autowired
    AccountRepository accountRepository;

    public List<AccountEntity> findAll() {
        return accountRepository.findAll();
    }

    public AccountEntity findByName(String name) {
        return accountRepository.findByName(name);
    }

    public AccountEntity findOne(Long aLong) {
        return accountRepository.findOne(aLong);
    }

    public long count() {
        return accountRepository.count();
    }

    public void delete(AccountEntity entity) {
        accountRepository.delete(entity);
    }

    public AccountEntity findOne(Specification<AccountEntity> spec) {
        return accountRepository.findOne(spec);
    }

    public AccountEntity createAccountEntity(Account account){
        AccountEntity accountEntity = StringUtils.hasText(account.getId()) ? accountRepository.findOne(Long.valueOf(account.getId())) : null ;
        if(accountEntity == null){
            accountEntity = accountRepository.save(toAccountEntity(account));
        }
        return accountEntity;
    }

    public boolean exists(Account account){
        return  (StringUtils.hasText(account.getId()) && accountRepository.findOne(Long.valueOf(account.getId())) != null)
                ||
                (StringUtils.hasText(account.getName()) && accountRepository.findByName(account.getName()) != null);
    }

    public AccountEntity toAccountEntity(Account account){
        return new AccountEntity(account.getId() != null ? Long.valueOf(account.getId()) : null,account.getName(),account.getAttributes());
    }

    public Account toAccount(AccountEntity accountEntity){
        return new Account().withId(accountEntity.getId().toString()).withName(accountEntity.getName()).withAttributes(accountEntity.getAttributes());
    }

    public AccountEntity save(AccountEntity entity) {
        return accountRepository.save(entity);
    }
}
