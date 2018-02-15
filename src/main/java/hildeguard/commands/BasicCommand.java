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

/**
 * Provides a basic implementation for commands which can be invoked via ssh.
 * <p>
 * These are not commands for the built-in console but ones which are invoked
 * via <tt>ssh HOST COMMAND</tt>. Therefore these can read from a given input stream
 * and write to the given output stream.
 */
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

    /**
     * Prints a text message to stderr.
     * <p>
     * This can be used for debugging or error reporting. Also this can be used
     * to provide additional console output if stdout is used to out processed data.
     *
     * @param msg the message to write to stderr
     */
    protected void printError(String msg) {
        try {
            err.write((msg + "\n").getBytes());
            err.flush();
        } catch (IOException e) {
            Exceptions.ignore(e);
        }
    }

    /**
     * Prints the text message to stdout.
     *
     * @param msg the message to print
     */
    protected void printOut(String msg) {
        try {
            out.write((msg + "\n").getBytes());
            out.flush();
        } catch (IOException e) {
            Exceptions.ignore(e);
        }
    }

    /**
     * Reads stdin into a temporary file.
     *
     * @param suffix the file usffix to use.
     * @return the temporary file (which should be deleted).
     * @throws IOException in case of an IO error
     */
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

    /**
     * Deletes the given file if it isn't <tt>null</tt> or non-existent.
     *
     * @param fileToDelete the file to delete
     */
    protected void deleteFile(File fileToDelete) {
        if (fileToDelete == null || !fileToDelete.exists()) {
            return;
        }

        if (!fileToDelete.delete()) {
            SSHServer.LOG.WARN("Cannot delete: " + fileToDelete.getAbsolutePath());
        }
    }

    /**
     * Actually executes the command.
     *
     * @throws Exception in case of any error during processing
     */
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
