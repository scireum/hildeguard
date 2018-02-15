/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package hildeguard.commands;

import com.google.common.io.ByteStreams;
import hildeguard.LDAPAccess;
import hildeguard.SSHServer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BasicCommand implements Command, SessionAware, Runnable {

    protected InputStream in;
    protected OutputStream out;
    protected OutputStream err;
    protected ExitCallback callback;
    protected ServerSession session;

    @Part
    protected static LDAPAccess ldap;

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    protected SearchResult findCurrentUserInLDAP(DirContext ctx) throws NamingException {
        SearchResult sr = ldap.searchUser(ctx, session.getUsername()).orElse(null);
        if (sr == null) {
            throw Exceptions.createHandled().withSystemErrorMessage("Unknown user: " + session.getUsername()).handle();
        }

        printError("Found user in LDAP: " + sr.getName());

        return sr;
    }

    @Override
    public void start(Environment env) throws IOException {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            execute();
            callback.onExit(0);
        } catch (Exception e) {
            printError(Exceptions.handle(SSHServer.LOG, e).getMessage());
            callback.onExit(-1);
        }
    }

    protected void printError(String error) {
        try {
            err.write((error + "\n").getBytes());
            err.flush();
        } catch (IOException e) {
            Exceptions.ignore(e);
        }
    }

    protected void printOut(String msg) {
        try {
            out.write((msg + "\n").getBytes());
            out.flush();
        } catch (IOException e) {
            Exceptions.ignore(e);
        }
    }

    protected File readInputIntoFile(String suffix) throws IOException {
        File file = File.createTempFile(session.getUsername(), suffix);
        try {
            try (OutputStream fos = new FileOutputStream(file)) {
                ByteStreams.copy(in, fos);
            }

            return file;
        } catch (IOException e) {
            deleteFile(file);
            throw e;
        }
    }

    protected void deleteFile(File fileToDelete) {
        if (fileToDelete == null || !fileToDelete.exists()) {
            return;
        }

        if (!fileToDelete.delete()) {
            SSHServer.LOG.WARN("Cannot delete: " + fileToDelete.getAbsolutePath());
        }
    }

    protected abstract void execute() throws Exception;

    @Override
    public void destroy() {
        // Noop by default
    }

    @Override
    public void setSession(ServerSession session) {
        this.session = session;
    }
}
