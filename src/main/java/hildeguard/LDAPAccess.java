/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package hildeguard;

import sirius.kernel.commons.Explain;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.ConfigValue;
import sirius.kernel.di.std.Register;
import sirius.kernel.nls.Formatter;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Provides a helper for querying LDAP.
 */
@Register(classes = LDAPAccess.class)
public class LDAPAccess {

    private static final String LDAP_ATTR_MEMBER_OF = "memberOf";
    private static final String PARAM_USER = "user";

    @ConfigValue("ldap.server")
    private String server;

    @ConfigValue("ldap.ssl")
    private boolean useSSL;

    @ConfigValue("ldap.user")
    private String ldapUser;

    @ConfigValue("ldap.password")
    private String ldapPassword;

    @ConfigValue("ldap.userSuffix")
    private String userSuffix;

    @ConfigValue("ldap.searchFilter")
    private String searchFilter;

    @ConfigValue("ldap.searchBase")
    private String searchBase;

    /**
     * Creates a directory context for the given user and password.
     *
     * @param user     the username to use
     * @param password the password to use
     * @return an initialized context to access the LDAP directory
     * @throws NamingException in case of an LDAP error
     */
    @SuppressWarnings("squid:S1149")
    @Explain("Legacy collections are required here as InitialDirContext requires them.")
    public DirContext openForUser(String user, String password) throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, server);
        if (useSSL) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        if (Strings.isFilled(userSuffix) && !user.endsWith(userSuffix)) {
            env.put(Context.SECURITY_PRINCIPAL, user + userSuffix);
        } else {
            env.put(Context.SECURITY_PRINCIPAL, user);
        }
        env.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialDirContext(env);
    }

    /**
     * Provides LDAP access using the admin user.
     *
     * @return an initialized context to access the LDAP directory
     * @throws NamingException in case of an LDAP error
     */
    public DirContext open() throws NamingException {
        return openForUser(ldapUser, ldapPassword);
    }

    /**
     * Tries to find the user with the given name.
     *
     * @param ctx      the LDAP context.
     * @param username the user to find
     * @return the user wrapped as optional or an empty optional if the user doesn't exist
     * @throws NamingException in case of an LDAP error
     */
    public Optional<SearchResult> searchUser(DirContext ctx, String username) throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(null);
        NamingEnumeration<SearchResult> answer =
                ctx.search(searchBase, Formatter.create(searchFilter).set(PARAM_USER, username).format(), searchCtls);
        if (!answer.hasMoreElements()) {
            return Optional.empty();
        }

        return Optional.of(answer.next());
    }

    /**
     * Determines all roles of the given user.
     *
     * @param user the user to extract roles from
     * @return a stream of all roles
     * @throws NamingException in case of an LDAP error
     */
    public Stream<String> readRoles(SearchResult user) throws NamingException {
        Attribute memberOf = user.getAttributes().get(LDAP_ATTR_MEMBER_OF);
        if (memberOf == null) {
            return Stream.empty();
        }

        Set<String> roles = new TreeSet<>();
        NamingEnumeration<?> e = memberOf.getAll();
        while (e.hasMore()) {
            String value = String.valueOf(e.next());
            LdapName name = new LdapName(value);
            String rdn = String.valueOf(name.getRdn(name.size() - 1).getValue());
            roles.add(rdn);
        }

        return roles.stream();
    }
}
