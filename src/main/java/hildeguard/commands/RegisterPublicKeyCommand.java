/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package hildeguard.commands;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import sirius.kernel.commons.Exec;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.ConfigValue;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import java.io.File;

/**
 * Registers the public key for the current used by storing its fingerprint in the fingerprint attribute.
 */
public class RegisterPublicKeyCommand extends BasicCommand {

    @ConfigValue("ldap.fingerprintAttribute")
    private static String fingerprintAttribute;

    @Register(name = "register", classes = CommandFactory.class)
    public static class Factory implements CommandFactory {

        @Override
        public Command createCommand(String command) {
            return new RegisterPublicKeyCommand();
        }
    }

    @Override
    protected void execute() throws Exception {
        DirContext ctx = ldap.open();
        SearchResult sr = findCurrentUserInLDAP(ctx);

        File pubKeyFile = readInputIntoFile(".pub");
        try {
            String fingerprint = obtainFingerprint(pubKeyFile);
            printOut(Strings.apply("Storing fingerprint %s for user %s in LDAP...",
                                   fingerprint,
                                   session.getUsername()));
            ctx.modifyAttributes(sr.getNameInNamespace(),
                                 new ModificationItem[]{new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                                                             new BasicAttribute(fingerprintAttribute,
                                                                                                fingerprint))});
            printOut("LDAP successfully updated...");
        } finally {
            deleteFile(pubKeyFile);
        }
    }

    private String obtainFingerprint(File pubKeyFile) {
        try {
            String rawFingerprint = Exec.exec("ssh-keygen -lf " + pubKeyFile.getAbsolutePath());
            String[] fingerprintParts = rawFingerprint.split(" ");
            if (fingerprintParts.length < 2) {
                throw Exceptions.handle()
                                .withSystemErrorMessage("Invalid fingerprint result: %s (%s)", rawFingerprint)
                                .handle();
            }

            return fingerprintParts[1];
        } catch (Exec.ExecException e) {
            throw Exceptions.createHandled()
                            .withSystemErrorMessage("ssh-keygen was unable to obtain a fingerprint: " + e.getLog())
                            .handle();
        }
    }
}
