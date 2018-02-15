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
 * Output the current user name.
 * <p>
 * This is mainly used for debugging purposes.
 */
@Register
public class WhoAmICommand implements ShellCommand {

    @Nonnull
    @Override
    public String getName() {
        return "whoami";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Returns the name of the current user.";
    }

    @Override
    public void invoke(ServerSession session, Values params, Command.Output output) {
        output.line(session.getUsername());
    }
}
