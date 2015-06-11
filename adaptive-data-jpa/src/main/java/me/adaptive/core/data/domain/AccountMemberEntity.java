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

package me.adaptive.core.data.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by panthro on 08/06/15.
 */
@Entity
@Table(name = "account_member")
public class AccountMemberEntity extends BaseEntity {


    @ManyToOne
    @NotNull
    private UserEntity user;

    @ManyToOne
    @NotNull
    private AccountEntity account;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "account_member_roles", joinColumns = @JoinColumn(name = "account_id", referencedColumnName = "id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<String>();

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountMemberEntity)) return false;
        if (!super.equals(o)) return false;

        AccountMemberEntity that = (AccountMemberEntity) o;

        if (getUser() != null ? !getUser().equals(that.getUser()) : that.getUser() != null) return false;
        if (getAccount() != null ? !getAccount().equals(that.getAccount()) : that.getAccount() != null) return false;
        return !(getRoles() != null ? !getRoles().equals(that.getRoles()) : that.getRoles() != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getAccount() != null ? getAccount().hashCode() : 0);
        result = 31 * result + (getRoles() != null ? getRoles().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AccountMemberEntity{" +
                "user=" + user +
                ", account=" + account +
                ", roles=" + roles +
                '}';
    }
}
