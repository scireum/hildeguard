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

@Register
public class SetupCommand implements ShellCommand {

    @Nonnull
    @Override
    public String getName() {
        return "setup";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Provides a quick installation tutorial for clients";
    }

    @Override
    public void invoke(ServerSession session, Values params, Command.Output output) {
        output.line("Client setup");
        output.blankLine();
        output.line("1. Register fingerprint: ssh HOST register < ~/.ssh/id_rsa.pub");
        output.line("2. Sign key: ssh HOST sign < ~/.ssh/id_rsa.pub > ~/.ssh/id_rsa-cert.pub");
        output.line("3. Verify certificate: ssh-keygen -Lf ~/.ssh/id_rsa-cert.pub");
        output.blankLine();
    }
}
