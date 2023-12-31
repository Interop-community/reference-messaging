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

import org.hspconsortium.platform.messaging.model.ldap.DirectoryType;
import org.hspconsortium.platform.messaging.model.ldap.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.TimeLimitExceededException;
import javax.naming.directory.*;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * https://docs.oracle.com/javase/tutorial/jndi/ops/index.html
 * http://docs.oracle.com/javase/jndi/tutorial/ldap/connect/config.html
 * http://stackoverflow.com/questions/30984699/how-to-fix-java-net-socketexception-connection-reset
 */
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private Hashtable<String, Object> environment;

    public UserService(Hashtable<String, Object> contextEnv) {
        this.environment = contextEnv;
    }

    public void setDirectoryType(DirectoryType directoryType) {
        DirectoryType directoryType1 = directoryType;
    }

    public Iterable<User> findAll(String base) {
        List<User> resultList = new ArrayList<>();
        SearchControls searchControl = new SearchControls();

        String filter = "(&(objectClass=inetOrgPerson))";

        DirContext ldapContext = null;
        NamingEnumeration resultNamingEnumeration = null;
        try {
            ldapContext = new InitialDirContext(environment);
            resultNamingEnumeration = ldapContext.search(base, filter, searchControl);

            while (resultNamingEnumeration.hasMore()) {
                SearchResult entry = (SearchResult) resultNamingEnumeration.next();
                resultList.add(toUser(entry));
            }
        } catch (NameNotFoundException e) {
            throw new RuntimeException("The base context was not found.", e);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (resultNamingEnumeration != null) {
                try {
                    resultNamingEnumeration.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
            if (ldapContext != null) {
                try {
                    ldapContext.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }

        return resultList;
    }

    public User findUser(Attributes matchAttributes, String ldapBase) {
        User[] users = findUser(matchAttributes, ldapBase, 1);
        if (users.length > 0)
            return users[0];
        else
            return null;
    }

    public User[] findUser(String ldapBase, String filter, int total) {
        List<User> users = new ArrayList();
        DirContext ldapContext = null;
        NamingEnumeration results = null;
        try {
            if (ldapBase == null) {
                ldapBase = "";
            }

            ldapContext = new InitialDirContext(environment);
            results = ldapContext.search(ldapBase, filter, null);
            int i = 0;
            if (results != null) {
                while (results.hasMore() && i <= total) {
                    SearchResult entry = (SearchResult) results.next();
                    users.add(toUser(entry));
                    i++;
                }
            } else {
                logger.info("No user found for attributes: " + filter);
            }
            return users.toArray(new User[0]);
        } catch (NameNotFoundException e) {
            throw new RuntimeException("The base context was not found.", e);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
            if (ldapContext != null) {
                try {
                    ldapContext.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
    }

    /**
     * Specify the attributes to match.
     * Ask for objects with the surname ("uid") attribute with the value "noman.rahman@imail.org" and the "cn" attribute.
     * Attributes matchAttributes = new BasicAttributes(true); // ignore case
     * matchAttributes.put(new BasicAttribute("uid", "noman.rahman@imail.org"));
     * matchAttributes.put(new BasicAttribute("cn"));
     *
     * @param matchAttributes query attributes
     * @param ldapBase        ldap base
     * @return first matched attribute of the selected path
     */
    public User[] findUser(Attributes matchAttributes, String ldapBase, int total) {
        List<User> users = new ArrayList();
        // Search for objects that have those matching attributes
        DirContext ldapContext = null;
        NamingEnumeration results = null;
        try {
            if (ldapBase == null) {
                ldapBase = "";
            }

//            logger.info("Searching context: " + environment);
            ldapContext = new InitialDirContext(environment);
            results = ldapContext.search(ldapBase, matchAttributes);
            int i = 0;
            if (results != null) {
                while (results.hasMore() && i <= total) {
                    SearchResult entry = (SearchResult) results.next();
                    users.add(toUser(entry));
                    i++;
                }
            } else {
                logger.info("No user found for attributes: " + matchAttributes);
            }
            return users.toArray(new User[users.size()]);
        } catch (NameNotFoundException e) {
            throw new RuntimeException("The base context was not found.", e);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
            if (ldapContext != null) {
                try {
                    ldapContext.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
    }

    private User toUser(SearchResult entry) {
        User user = new User(entry.getName());

        Field[] fields = user.getClass().getDeclaredFields();

        final Attributes attributes = entry.getAttributes();


        for (Field field : fields) {
            // Grab the @Attribute annotation
            org.hspconsortium.platform.messaging.model.ldap.annotations.Attribute
                    attribute = field.getAnnotation(org.hspconsortium.platform.messaging.model.ldap.annotations.Attribute.class);

            // Did we find the annotation?
            if (attribute != null) {
                // Pull attribute name, syntax and whether attribute is binary
                // from the annotation
                String localAttributeName = attribute.name();
                //donot readout password
                if ("userPassword".equals(localAttributeName))
                    continue;

                if (!localAttributeName.isEmpty()) {
                    Attribute ldapAttribute = attributes.get(localAttributeName);
                    PropertyDescriptor pd = null;
                    if (ldapAttribute != null) {
                        try {
                            pd = new PropertyDescriptor(field.getName(), user.getClass());
                            pd.getWriteMethod().invoke(user, ldapAttribute.get());
                        } catch (Exception e) {
                            try {
                                throw new RuntimeException(String.format("%s needs getter/setter (%s) for annotated (attribute=%s) field %s"
                                        , user.getClass().getCanonicalName()
                                        , pd != null ? pd.getWriteMethod().getName() : ""
                                        , ldapAttribute.get()
                                        , field.getName()
                                ));
                            } catch (NamingException e1) {
                                throw new RuntimeException(e1);
                            }
                        }
                    }
                }
            }
        }
        return user;
    }

    public User updateUser(User user) {
        DirContext ldapContext = null;
        try {
            List<ModificationItem> modificationItemList = new ArrayList<>();
            ldapContext = new InitialDirContext(environment);
            Attributes attributes = ldapContext.getAttributes(user.getLdapEntityName());

            final Field[] fields = user.getClass().getDeclaredFields();

            for (Field field : fields) {
                // Grab the @Attribute annotation
                org.hspconsortium.platform.messaging.model.ldap.annotations.Attribute fieldAttribute
                        = field.getAnnotation(org.hspconsortium.platform.messaging.model.ldap.annotations.Attribute.class);

                // Did we find the annotation?
                if (fieldAttribute != null) {
                    Attribute ldapAttribute = attributes.get(fieldAttribute.name());

                    PropertyDescriptor pd = null;
                    Object fieldValue;
                    try {
                        pd = new PropertyDescriptor(field.getName(), user.getClass());
                        fieldValue = pd.getReadMethod().invoke(user);
                        if (fieldValue == null) {
                            continue;
                        }
                    } catch (Exception e) {
                        try {
                            throw new RuntimeException(String.format("%s needs getter/setter (%s) for annotated (attribute = %s) field %s"
                                    , user.getClass().getCanonicalName()
                                    , pd != null ? pd.getReadMethod().getName() : ""
                                    , ldapAttribute.get()
                                    , field.getName()
                            ));
                        } catch (NamingException e1) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (ldapAttribute != null) {
                        if (!fieldValue.equals(ldapAttribute.get())) {
                            modificationItemList.add(
                                    new ModificationItem(DirContext.REPLACE_ATTRIBUTE
                                            , new BasicAttribute(ldapAttribute.getID(), fieldValue)));
                        }
                    } else {
                        modificationItemList.add(
                                new ModificationItem(DirContext.REPLACE_ATTRIBUTE
                                        , new BasicAttribute(fieldAttribute.name(), fieldValue)));
                    }

                }
            }

            if (modificationItemList.size() > 0) {
                //ldapContext = new InitialDirContext(environment);
                ldapContext.modifyAttributes(user.getLdapEntityName(), modificationItemList.toArray(new ModificationItem[0]));
            }
        } catch (NameNotFoundException e) {
            throw new RuntimeException("The base context was not found.", e);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (ldapContext != null) {
                try {
                    ldapContext.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
        return user;
    }

    public User createUser(User user) {
        DirContext ldapContext = null;
        Attribute ldapAttribute;
        try {
            Attributes attributes = new BasicAttributes(true); //case-ignore
            ldapAttribute = new BasicAttribute("objectclass");
            ldapAttribute.add("top");
            ldapAttribute.add("inetOrgPerson");
            ldapAttribute.add("person");
            ldapAttribute.add("organizationalPerson");
            attributes.put(ldapAttribute);

            final Field[] fields = user.getClass().getDeclaredFields();

            for (Field field : fields) {
                // Grab the @Attribute annotation
                org.hspconsortium.platform.messaging.model.ldap.annotations.Attribute fieldAttribute
                        = field.getAnnotation(org.hspconsortium.platform.messaging.model.ldap.annotations.Attribute.class);

                // Did we find the annotation?
                if (fieldAttribute != null) {
                    PropertyDescriptor pd = null;
                    Object fieldValue;
                    try {
                        pd = new PropertyDescriptor(field.getName(), user.getClass());
                        fieldValue = pd.getReadMethod().invoke(user);
                        if (fieldValue != null) {
                            ldapAttribute = new BasicAttribute(fieldAttribute.name(), fieldValue);
                            attributes.put(ldapAttribute);
                        }
                    } catch (Exception e) {
                        try {
                            throw new RuntimeException(String.format("%s needs getter/setter (%s) for annotated (attribute = %s) field %s"
                                    , user.getClass().getCanonicalName()
                                    , pd != null ? pd.getReadMethod().getName() : "Property descriptor not found."
                                    , ldapAttribute.get()
                                    , field.getName()
                            ));
                        } catch (NamingException e1) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            if (attributes.size() > 0) {
                ldapContext = new InitialDirContext(environment);
                ldapContext.createSubcontext(user.getLdapEntityName(), attributes);
            }
        } catch (NameNotFoundException e) {
            throw new RuntimeException("The base context was not found.", e);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (ldapContext != null) {
                try {
                    ldapContext.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
        return null;
    }

    public Iterable<User> searchByDistinctName(LdapName dn) {
        Attributes matchAttributes = new BasicAttributes(true); // ignore case
        List<User> resultList = new ArrayList<>();
        DirContext ldapContext = null;
        NamingEnumeration results = null;
        try {
            ldapContext = new InitialDirContext(environment);
            final List<Rdn> contextRdns = new LdapName(ldapContext.getNameInNamespace()).getRdns();
            if (dn.startsWith(contextRdns)) {
                final Object remove = dn.remove(contextRdns.size());
                dn = new LdapName(remove.toString());

            }

            for (Rdn rdn : dn.getRdns()) {
                Attributes attributes = rdn.toAttributes();
                NamingEnumeration<String> ids = attributes.getIDs();
                while (ids.hasMore()) {
                    matchAttributes.put(attributes.get(ids.next()));
                }
            }

            results = ldapContext.search("", matchAttributes);

            while (results.hasMore()) {
                SearchResult entry = (SearchResult) results.next();
                resultList.add(toUser(entry));
            }
        } catch (TimeLimitExceededException te) {
            return resultList;
        } catch (NameNotFoundException e) {
            throw new RuntimeException("The base context was not found.", e);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
            if (ldapContext != null) {
                try {
                    ldapContext.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
        return resultList;
    }

    public User deleteUser(User user) {
        DirContext ldapContext = null;
        try {
            ldapContext = new InitialDirContext(environment);
            ldapContext.destroySubcontext(user.getLdapEntityName());
        } catch (NameNotFoundException e) {
            throw new RuntimeException("The base context was not found.", e);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        } finally {
            if (ldapContext != null) {
                try {
                    ldapContext.close();
                } catch (Exception e) {
                    // Never mind this.
                }
            }
        }
        return user;
    }
}
