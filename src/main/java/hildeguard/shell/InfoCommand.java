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
import sirius.kernel.info.Module;
import sirius.kernel.info.Product;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Register
public class InfoCommand implements ShellCommand {

    @Nonnull
    @Override
    public String getName() {
        return "info";
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Returns all installed modules.";
    }

    @Override
    public void invoke(ServerSession session, Values params, Command.Output output) {
        output.line(Product.getProduct().toString());
        output.separator();
        for (Module module : Product.getModules()) {
            output.line(module.toString());
        }
    }
}
