/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package hildeguard;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;

import javax.naming.directory.DirContext;

/**
 * Authenticates a user and password by checking the credentials against LDAP.
 */
public class LDAPPasswordAuthenticator implements PasswordAuthenticator {

    @Part
    private static LDAPAccess ldap;

    @Override
    public boolean authenticate(String username, String password, ServerSession session) {
        try {
            DirContext ctx = ldap.openForUser(username, password);
            return ldap.searchUser(ctx, username).isPresent();
        } catch (Exception e) {
            Exceptions.handle(e);
            return false;
        }
    }
}
