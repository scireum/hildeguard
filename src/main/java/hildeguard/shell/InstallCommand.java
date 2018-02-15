/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package hildeguard.shell;

import org.apache.sshd.server.session.ServerSession;
import sirius.kernel.commons.Values;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.console.Command;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides a short info on how to use HildeGUARD as a server.
 * <p>
 * Acutally this is just a short explanation of configuring trusted CA certs in sshd.
 */
@Register
public class InstallCommand implements ShellCommand {

    @Nonnull
    @Override
    public String getName() {
        return "install";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Provides a quick installation tutorial for servers";
    }

    @Override
    public void invoke(ServerSession session, Values params, Command.Output output) {
        output.line("Server setup");
        output.blankLine();
        output.line("1. Obtain ca certificate: ssh HOST ca");
        output.line("2. Store in /etc/ssh/ca.pub");
        output.blankLine();
        output.line("3. Add to /etc/ssh/sshd_config:");
        output.line("TrustedUserCAKeys /etc/ssh/ca.pub");
        output.line("AuthorizedPrincipalsFile /etc/ssh/auth_principals/%u");
        output.blankLine();
        output.line("4. Select principals:");
        output.line("mkdir /etc/ssh/auth_principals");
        output.line("echo -e 'ssh-principal-1\\nssh-principal-2' > /etc/ssh/auth_principals/root");
    }
}
