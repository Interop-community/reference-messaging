/*
 * Copyright 2005-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hspconsortium.platform.messaging.service.ldap;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.hspconsortium.platform.messaging.model.ldap.DirectoryType;
import org.hspconsortium.platform.messaging.model.ldap.User;
import org.hspconsortium.platform.messaging.model.ldap.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.List;
import java.util.Set;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Component
public class UserService implements BaseLdapNameAware {
    private final UserRepo userRepo;
    private LdapName baseLdapPath;
    private DirectoryType directoryType = DirectoryType.NORMAL;

    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public void setDirectoryType(DirectoryType directoryType) {
        this.directoryType = directoryType;
    }

    @Override
    public void setBaseLdapPath(LdapName baseLdapPath) {
        this.baseLdapPath = baseLdapPath;
    }

    public Iterable<User> findAll(String base) {
        LdapQuery ldapQuery = query().base(base).where("objectclass").is("pwmUser");
        return userRepo.findAll(ldapQuery);
    }

    public User findUser(String userId) {
        return userRepo.findOne(LdapUtils.newLdapName(userId));
    }

    public User findUser(LdapName userId) {
        return userRepo.findOne(userId);
    }

    public LdapName toAbsoluteDn(Name relativeName) {
        return LdapNameBuilder.newInstance(baseLdapPath)
                .add(relativeName)
                .build();
    }

    /**
     * This method expects absolute DNs of group members. In order to find the actual users
     * the DNs need to have the base LDAP path removed.
     *
     * @param absoluteIds
     * @return
     */
    public Set<User> findAllMembers(Iterable<Name> absoluteIds) {
        return Sets.newLinkedHashSet(userRepo.findAll(toRelativeIds(absoluteIds)));
    }

    public Iterable<Name> toRelativeIds(Iterable<Name> absoluteIds) {
        return Iterables.transform(absoluteIds, new Function<Name, Name>() {
            @Override
            public Name apply(Name input) {
                return LdapUtils.removeFirst(input, baseLdapPath);
            }
        });
    }

    public User updateUser(String userId, User user) {
        LdapName originalId = LdapUtils.newLdapName(userId);
        User existingUser = userRepo.findOne(originalId);

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setUserPwmId(user.getUserPwmId());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setTitle(user.getTitle());
        existingUser.setEmployeeNumber(user.getEmployeeNumber());
        existingUser.setOrganizationName(user.getOrganizationName());
        existingUser.setProfileUri(user.getProfileUri());

        if (directoryType == DirectoryType.NORMAL) {
            return userRepo.save(existingUser);
        }
        return user;
    }

    public List<User> searchByUserName(String userName) {
        return userRepo.findByUserName(userName);
    }

    public LdapName getBaseLdapPath() {
        return baseLdapPath;
    }
}
