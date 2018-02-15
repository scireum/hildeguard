/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package hildeguard;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;

import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import java.security.PublicKey;

public class LDAPPublicKeyAuthenticator implements PublickeyAuthenticator {

    @Part
    private static LDAPAccess ldap;

    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) {
        try {
            DirContext ctx = ldap.open();
            SearchResult sr = ldap.searchUser(ctx, username).orElse(null);
            if (sr == null) {
                return false;
            }

            return KeyUtils.getFingerPrint(key).equals(sr.getAttributes().get("sshFingerprint").get());
        } catch (Exception e) {
            Exceptions.handle(e);
            return false;
        }
    }
}
