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

public interface ShellCommand extends Named {

    @Nullable
    String getDescription();

    void invoke(ServerSession session, Values params, Command.Output output) throws Exception;
}
