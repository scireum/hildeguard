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
import sirius.kernel.di.std.Named;
import sirius.kernel.health.console.Command;

import javax.annotation.Nullable;

/**
 * Describes a command for the interactive shell.
 */
public interface ShellCommand extends Named {

    @Nullable
    String getDescription();

    /**
     * Invokes the command.
     *
     * @param session the current SSH session
     * @param params  the parameters given for the command
     * @param output  the output to write the response to
     * @throws Exception in case of any error
     */
    void invoke(ServerSession session, Values params, Command.Output output) throws Exception;
}
