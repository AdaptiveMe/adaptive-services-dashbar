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
 */ge me.adaptive.core.data.domain;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created by panthro on 04/06/15.
 */
@Entity
@Table(name = "account")
public class AccountEntity extends BaseEntity {

    @NotNull
    @Column(name = "name", length = 100)
    @Max(value = 100, message = "Account name can't have more than 100 characters")
    @Min(value = 3, message = "Account name need to be have at least 3 characters")
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "account_attributes", joinColumns = @JoinColumn(name = "account_id"))
    private Map<String, String> attributes;

    public AccountEntity() {

    }

    public AccountEntity(Long id, String name, Map<String, String> attributes) {
        super(id);
        this.name = name;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "AccountEntity{" +
                "name='" + name + '\'' +
                '}';
    }
}
