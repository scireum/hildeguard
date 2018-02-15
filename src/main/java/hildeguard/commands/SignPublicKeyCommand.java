/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package hildeguard.commands;

import com.google.common.io.ByteStreams;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import sirius.kernel.commons.Exec;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;

import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

public class SignPublicKeyCommand extends BasicCommand {

    @Register(name = "sign", classes = CommandFactory.class)
    public static class Factory implements CommandFactory {

        @Override
        public Command createCommand(String command) {
            return new SignPublicKeyCommand();
        }
    }

    @Override
    protected void execute() throws Exception {
        DirContext ctx = ldap.open();
        SearchResult sr = findCurrentUserInLDAP(ctx);

        File pubKeyFile = readInputIntoFile(".pub");
        try {
            String roles = ldap.readRoles(sr).filter(role -> role.startsWith("ssh-")).collect(Collectors.joining(","));
            if (Strings.isEmpty(roles)) {
                throw Exceptions.createHandled().withSystemErrorMessage("No 'ssh-' permissions available...").handle();
            }
            printError("Roles: " + roles);
            printError("Signing Key....");
            signKey(pubKeyFile, roles);
            printError("Sending Result...");
            transmitCertificate(pubKeyFile);
            printError("Completed successfully!");
        } finally {
            deleteFile(pubKeyFile);
        }
    }

    private void transmitCertificate(File pubKeyFile) throws IOException {
        File signedPubKeyFile = new File(pubKeyFile.getParent(),
                                         pubKeyFile.getName().substring(0, pubKeyFile.getName().length() - 4)
                                         + "-cert.pub");
        if (!signedPubKeyFile.exists()) {
            throw Exceptions.createHandled()
                            .withSystemErrorMessage("ssh-keygen didn't create a ceritficate...")
                            .handle();
        }

        try (InputStream fin = new FileInputStream(signedPubKeyFile)) {
            ByteStreams.copy(fin, out);
            out.flush();
        } finally {
            deleteFile(signedPubKeyFile);
        }
    }

    private void signKey(File pubKeyFile, String roles) {
        try {
            Exec.exec("ssh-keygen -s data/ca -I "
                      + session.getUsername()
                      + " -n "
                      + roles
                      + " -V +1w -z 1 "
                      + pubKeyFile.getAbsolutePath());
        } catch (Exec.ExecException e) {
            throw Exceptions.createHandled()
                            .withSystemErrorMessage("ssh-keygen was unable to sign the given key: " + e.getLog())
                            .handle();
        }
    }
}
