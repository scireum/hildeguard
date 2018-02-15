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
import sirius.kernel.di.std.Register;

import java.io.FileInputStream;

public class ObtainPublicKeyCommand extends BasicCommand {

    @Register(name = "ca", classes = CommandFactory.class)
    public static class Factory implements CommandFactory {

        @Override
        public Command createCommand(String command) {
            return new ObtainPublicKeyCommand();
        }
    }

    @Override
    protected void execute() throws Exception {
        try (FileInputStream fin = new FileInputStream("data/ca.pub")) {
            ByteStreams.copy(fin, out);
            out.flush();
        }
    }
}
