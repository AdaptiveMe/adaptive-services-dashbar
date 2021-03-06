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

package me.adaptive.che.infrastructure.dao;

import me.adaptive.core.data.api.AccountEntityService;
import me.adaptive.core.data.api.AccountMemberEntityService;
import me.adaptive.core.data.api.UserEntityService;
import me.adaptive.core.data.domain.AccountEntity;
import me.adaptive.core.data.domain.AccountMemberEntity;
import me.adaptive.core.data.domain.UserEntity;
import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.eclipse.che.api.account.server.dao.Member;
import org.eclipse.che.api.account.server.dao.Subscription;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Eugene Voevodin
 */
@Service("AccountDao")
public class AdaptiveAccountDao implements AccountDao {


    @Autowired
    private AccountEntityService accountEntityService;
    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private  WorkspaceDao workspaceDao;

    @Autowired
    private AccountMemberEntityService accountMemberEntityService;

    

    @Override
    public void create(Account account) throws ConflictException {
        if (accountEntityService.findByAccountId(account.getId()).isPresent()) {
            throw new ConflictException(String.format("Account with id %s already exists.", account.getId()));
        }
        accountEntityService.createAccountEntity(account);
    }

    @Override
    public Account getById(String id) throws NotFoundException {
        Optional<AccountEntity> accountEntity = accountEntityService.findByAccountId(id);
        if (!accountEntity.isPresent()) {
            throw new NotFoundException(String.format("Not found account %s", id));
        }else {
            return accountEntityService.toAccount(accountEntity.get());
        }
    }

    @Override
    public Account getByName(String name) throws NotFoundException {
        Optional<AccountEntity> accountEntity = accountEntityService.findByName(name);
        if (!accountEntity.isPresent()) {
            throw new NotFoundException(String.format("Not found account %s", name));
        }else{
            return accountEntityService.toAccount(accountEntity.get());
        }
    }

    @Override
    public List<Account> getByOwner(String ownerEmail) {
        Optional<UserEntity> userEntity = userEntityService.findByEmail(ownerEmail);
        if (!userEntity.isPresent()) {
            return Collections.EMPTY_LIST;
        }
        final List<Account> accounts = accountMemberEntityService.findByUserEmailAndRolesContains(ownerEmail, "account/owner").stream().map(adaptiveMember -> accountEntityService.toAccount(adaptiveMember.getAccount())).collect(Collectors.toList());
        return accounts;
    }

    @Override
    public List<Member> getByMember(String userId) {
        return accountMemberEntityService.toMemberList(accountMemberEntityService.findByUserEmail(userId));
    }

    @Override
    public void update(Account account) throws NotFoundException {
        Optional<AccountEntity> accountEntity = account.getId() != null ? accountEntityService.findByAccountId(account.getId()) : Optional.<AccountEntity>empty();
        if (!accountEntity.isPresent() && account.getName() != null) {
            accountEntity = accountEntityService.findByName(account.getName());
        }
        if (!accountEntity.isPresent()) {
            throw new NotFoundException(String.format("Not found account %s", account.getId()));
        }
        accountEntityService.save(accountEntityService.toAccountEntity(account, accountEntity));
    }

    @Override
    public void remove(String id) throws NotFoundException, ServerException, ConflictException {

        Optional<AccountEntity> accountEntity = accountEntityService.findByAccountId(id);

        if (!accountEntity.isPresent()) {
            throw new NotFoundException(String.format("Not found account %s", id));
        }

        //TODO check later if workspaces is a child of account
        if (!workspaceDao.getByAccount(id).isEmpty()) {
            throw new ConflictException("It is not possible to remove account that has associated workspaces");
        }
        accountMemberEntityService.delete(accountMemberEntityService.findByAccount(accountEntity.get()));
        accountEntityService.delete(accountEntity.get());
    }

    @Override
    public void addMember(Member member) throws NotFoundException, ConflictException {


        if (!accountEntityService.findByAccountId(member.getAccountId()).isPresent()) {
            throw new NotFoundException(String.format("Not found account %s", member.getAccountId()));
        }

        if (accountMemberEntityService.findByUserIdAndAccountId(member.getUserId(), member.getAccountId()).isPresent()) {
            throw new ConflictException(String.format("Membership of user %s in account %s already exists.",
                    member.getUserId(), member.getAccountId())
            );
        }
        accountMemberEntityService.create(member);
    }

    @Override
    public List<Member> getMembers(String accountId) {
        return accountMemberEntityService.toMemberList(accountMemberEntityService.findByAccountId(accountId));
    }

    @Override
    public void removeMember(Member member) throws NotFoundException {
        Optional<AccountMemberEntity> adaptiveMembers = accountMemberEntityService.findByUserIdAndAccountId(member.getUserId(), member.getAccountId());
        if (!adaptiveMembers.isPresent()) {
            throw new NotFoundException(String.format("User with id %s hasn't any account membership", member.getUserId()));
        }
        accountMemberEntityService.delete(adaptiveMembers.get());
    }

    @Override
    public void addSubscription(Subscription subscription) throws NotFoundException {
        throw new UnsupportedOperationException("Not Implemented");
//        lock.writeLock().lock();
//        try {
//            Account myAccount = null;
//            for (int i = 0, size = accounts.size(); i < size && myAccount == null; i++) {
//                if (accounts.get(i).getId().equals(subscription.getAccountId())) {
//                    myAccount = accounts.get(i);
//                }
//            }
//            if (myAccount == null) {
//                throw new NotFoundException(String.format("Not found account %s", subscription.getAccountId()));
//            }
//            subscriptions.add(new Subscription(subscription));
//        } finally {
//            lock.writeLock().unlock();
//        }
    }

    @Override
    public void removeSubscription(String subscriptionId) throws NotFoundException {
        throw new UnsupportedOperationException("Not Implemented");
//        lock.writeLock().lock();
//        try {
//            Subscription subscription = null;
//            for (int i = 0, size = subscriptions.size(); i < size && subscription == null; i++) {
//                if (subscriptions.get(i).getId().equals(subscriptionId)) {
//                    subscription = subscriptions.get(i);
//                }
//            }
//            if (subscription == null) {
//                throw new NotFoundException(String.format("Not found subscription %s", subscriptionId));
//            }
//            subscriptions.remove(subscription);
//        } finally {
//            lock.writeLock().unlock();
//        }
    }

    @Override
    public Subscription getSubscriptionById(String subscriptionId) throws NotFoundException {
        Subscription s = new Subscription();
        s.setId(subscriptionId);
        return s;
//        lock.readLock().lock();
//        try {
//            Subscription subscription = null;
//            for (int i = 0, size = subscriptions.size(); i < size && subscription == null; i++) {
//                if (subscriptions.get(i).getId().equals(subscriptionId)) {
//                    subscription = subscriptions.get(i);
//                }
//            }
//            if (subscription == null) {
//                throw new NotFoundException(String.format("Not found subscription %s", subscriptionId));
//            }
//            return new Subscription(subscription);
//        } finally {
//            lock.readLock().unlock();
//        }
    }

    @Override
    public List<Subscription> getActiveSubscriptions(String accountId) {
        return Collections.EMPTY_LIST;
//        final List<Subscription> result = new LinkedList<>();
//        lock.readLock().lock();
//        try {
//            for (Subscription subscription : subscriptions) {
//                if (accountId.equals(subscription.getAccountId()) && ACTIVE.equals(subscription.getState())) {
//                    result.add(new Subscription(subscription));
//                }
//            }
//        } finally {
//            lock.readLock().unlock();
//        }
//        return result;
    }

    @Override
    public Subscription getActiveSubscription(String accountId, String serviceId) {
        return new Subscription().withAccountId(accountId).withServiceId(serviceId);
//        lock.readLock().lock();
//        try {
//            for (Subscription subscription : subscriptions) {
//                if (accountId.equals(subscription.getAccountId()) && serviceId.equals(subscription.getServiceId())
//                    && ACTIVE.equals(subscription.getState())) {
//                    return new Subscription(subscription);
//                }
//            }
//        } finally {
//            lock.readLock().unlock();
//        }
//        return null;
    }

    @Override
    public void updateSubscription(Subscription subscription) throws NotFoundException, ServerException {
        throw new UnsupportedOperationException("Not Implemented");
//        lock.writeLock().lock();
//        try {
//            int i = 0;
//            Subscription mySubscription = null;
//            for (int size = subscriptions.size(); i < size && mySubscription == null; i++) {
//                if (subscriptions.get(i).getId().equals(subscription.getId())) {
//                    mySubscription = subscriptions.get(i);
//                }
//            }
//            if (mySubscription == null) {
//                throw new NotFoundException(String.format("Not found subscription %s", subscription.getId()));
//            }
//            subscriptions.remove(i);
//            subscriptions.add(i, new Subscription(subscription));
//        } finally {
//            lock.writeLock().unlock();
//        }
    }
}
