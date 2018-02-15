/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package hildeguard;

import hildeguard.shell.InteractiveShell;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import sirius.kernel.Lifecycle;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.ConfigValue;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;
import sirius.kernel.health.Log;

import java.io.File;
import java.io.IOException;

@Register
public class SSHServer implements Lifecycle {

    private SshServer sshd;

    @ConfigValue("sshd.port")
    private int port;

    @ConfigValue("sshd.keyFile")
    private String keyFile;

    @Part
    private GlobalContext context;

    public static final Log LOG = Log.get("ssh");

    @Override
    public void started() {
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(keyFile)));
        sshd.setPasswordAuthenticator(new LDAPPasswordAuthenticator());
        sshd.setPublickeyAuthenticator(new LDAPPublicKeyAuthenticator());
        sshd.setCommandFactory(this::commandFactory);
        sshd.setShellFactory(this::createShell);

        try {
            sshd.start();
        } catch (IOException e) {
            Exceptions.handle(e);
        }
    }

    private Command createShell() {
        return new InteractiveShell();
    }

    private Command commandFactory(String command) {
        CommandFactory factory = context.getPart(command, CommandFactory.class);
        if (factory == null) {
            return null;
        }

        return factory.createCommand(command);
    }

    @Override
    public void stopped() {
        try {
            sshd.stop(true);
        } catch (IOException e) {
            Exceptions.handle(e);
        }
    }

    @Override
    public void awaitTermination() {
        // Nothing to wait for...
    }

    @Override
    public String getName() {
        return "sshd";
    }
}
