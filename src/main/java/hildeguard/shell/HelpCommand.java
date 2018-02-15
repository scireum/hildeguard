/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package hildeguard.shell;

import org.apache.sshd.server.session.ServerSession;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Values;
import sirius.kernel.di.std.Parts;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.console.Command;
import sirius.kernel.info.Product;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

@Register
public class HelpCommand implements ShellCommand {

    @Parts(ShellCommand.class)
    private Collection<ShellCommand> commands;

    @Nonnull
    @Override
    public String getName() {
        return "help";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void invoke(ServerSession session, Values params, Command.Output output) {
        output.line(Product.getProduct().toString());
        output.separator();
        commands.stream().filter(cmd -> Strings.isFilled(cmd.getDescription())).forEach(cmd -> {
            output.apply("%-20s %s", cmd.getName(), cmd.getDescription());
        });
    }
}
