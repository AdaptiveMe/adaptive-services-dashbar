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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by panthro on 08/06/15.
 */
@Entity
@Table(name = "workspace")
public class WorkspaceEntity extends BaseEntity {

    private boolean temporary;

    @NotNull
    @Max(value = 100, message = "Workspace id can't have more than 100 characters")
    @Min(value = 3, message = "Workspace id need to be have at least 3 characters")
    @Column(name = "workspace_id", unique = true)
    private String workspaceId;

    @NotNull
    @Pattern(regexp = "[\\w][\\w\\.\\-]{1,18}[\\w]")
    private String name;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @NotNull
    private AccountEntity account;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "workspace_attributes", joinColumns = @JoinColumn(name = "workspace_id"))
    private Map<String, String> attributes = new HashMap<String, String>();

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkspaceEntity)) return false;
        if (!super.equals(o)) return false;

        WorkspaceEntity entity = (WorkspaceEntity) o;

        if (isTemporary() != entity.isTemporary()) return false;
        if (getWorkspaceId() != null ? !getWorkspaceId().equals(entity.getWorkspaceId()) : entity.getWorkspaceId() != null)
            return false;
        if (getName() != null ? !getName().equals(entity.getName()) : entity.getName() != null) return false;
        if (getAccount() != null ? !getAccount().equals(entity.getAccount()) : entity.getAccount() != null)
            return false;
        return !(getAttributes() != null ? !getAttributes().equals(entity.getAttributes()) : entity.getAttributes() != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isTemporary() ? 1 : 0);
        result = 31 * result + (getWorkspaceId() != null ? getWorkspaceId().hashCode() : 0);
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getAccount() != null ? getAccount().hashCode() : 0);
        result = 31 * result + (getAttributes() != null ? getAttributes().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WorkspaceEntity{" +
                "temporary=" + temporary +
                ", workspaceId='" + workspaceId + '\'' +
                ", name='" + name + '\'' +
                ", account=" + account +
                ", attributes=" + attributes +
                '}';
    }
}
