/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package hildeguard.shell;

import com.google.common.base.Charsets;
import org.apache.sshd.common.io.IoInputStream;
import org.apache.sshd.common.io.IoOutputStream;
import org.apache.sshd.common.io.IoReadFuture;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.apache.sshd.server.AsyncCommand;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import sirius.kernel.async.CallContext;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.commons.Values;
import sirius.kernel.di.GlobalContext;
import sirius.kernel.di.std.ConfigValue;
import sirius.kernel.di.std.Part;
import sirius.kernel.health.Exceptions;
import sirius.kernel.health.console.Command;
import sirius.kernel.info.Product;
import sirius.kernel.nls.Formatter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/**
 * Provides a minimal shell.
 */
public class InteractiveShell implements AsyncCommand, Command.Output, SessionAware {

    public static final String COMMAND_EXIT = "exit";

    private ExitCallback callback;
    private IoInputStream in;
    private IoOutputStream out;
    private ByteArrayOutputStream inputBuffer = new ByteArrayOutputStream();

    @ConfigValue("prompt")
    private static String prompt;

    @Part
    private static GlobalContext ctx;
    private ServerSession session;

    @Override
    public void setInputStream(InputStream in) {
        // ignored
    }

    @Override
    public void setOutputStream(OutputStream out) {
        // ignored
    }

    @Override
    public void setErrorStream(OutputStream err) {
        // ignored
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(Environment env) {
        outputPrompt();
        loop();
    }

    private void loop() {
        Buffer buffer = new ByteArrayBuffer(1);
        in.read(buffer).addListener(this::processInput);
    }

    private void processInput(IoReadFuture future) {
        if (future.getRead() == 0) {
            return;
        }

        byte data = future.getBuffer().getByte();
        if (data == 0x03) {
            callback.onExit(0);
            return;
        }

        if (data == '\r') {
            processNewLine();
            return;
        }

        out.write(new ByteArrayBuffer(new byte[]{data}));
        inputBuffer.write(data);
        loop();
    }

    private void processNewLine() {
        out.write(new ByteArrayBuffer(new byte[]{'\r', '\n'}));

        String command = new String(inputBuffer.toByteArray(), Charsets.UTF_8);
        if (COMMAND_EXIT.equals(command)) {
            callback.onExit(0);
            return;
        }

        invoke(command);

        outputPrompt();
        inputBuffer = new ByteArrayOutputStream();
        loop();
    }

    private void outputPrompt() {
        out.write(new ByteArrayBuffer(Formatter.create(prompt)
                                               .set("user", session.getUsername())
                                               .set("node", CallContext.getNodeName())
                                               .set("product", Product.getProduct().getName())
                                               .format()
                                               .getBytes(Charsets.UTF_8)));
    }

    private void invoke(String commandLine) {
        try {
            Tuple<String, Values> commandAndParameters = parseCommandLine(commandLine);
            ShellCommand cmd = ctx.findPart(commandAndParameters.getFirst(), ShellCommand.class);
            cmd.invoke(session, commandAndParameters.getSecond(), this);
        } catch (NoSuchElementException e) {
            apply("Unknown command: %s (Use 'help' to obtain a list of known commands.)", commandLine);
            Exceptions.ignore(e);
        } catch (Exception e) {
            line(Exceptions.handle(e).getMessage());
        }
    }

    private Tuple<String, Values> parseCommandLine(String commandLine) {
        PrimitiveIterator.OfInt input = commandLine.chars().iterator();
        String command = readToken(input);

        List<String> params = new ArrayList<>();
        String token = readToken(input);
        while (token != null) {
            params.add(token);
            token = readToken(input);
        }

        return Tuple.create(command, Values.of(params));
    }

    private String readToken(PrimitiveIterator.OfInt input) {
        if (!input.hasNext()) {
            return null;
        }

        int firstCharacter = consumeWhitespaces(input);
        if (firstCharacter == '\0') {
            return null;
        }
        if (firstCharacter == '"') {
            return readEscapedToken(input);
        }

        return readPlainToken(firstCharacter, input);
    }

    private String readEscapedToken(PrimitiveIterator.OfInt input) {
        StringBuilder result = new StringBuilder();

        while (input.hasNext()) {
            int next = input.next();
            if (next == '\\' && input.hasNext()) {
                next = input.next();
                result.append((char) next);
            } else if (next == '"') {
                return result.toString();
            } else {
                result.append((char) next);
            }
        }

        return result.toString();
    }

    private String readPlainToken(int firstCharacter, PrimitiveIterator.OfInt input) {
        StringBuilder result = new StringBuilder();
        result.append((char) firstCharacter);

        while (input.hasNext()) {
            int next = input.next();
            if (next == '\\' && input.hasNext()) {
                next = input.next();
                result.append((char) next);
            } else if (Character.isWhitespace(next)) {
                return result.toString();
            } else {
                result.append((char) next);
            }
        }

        return result.toString();
    }

    private int consumeWhitespaces(PrimitiveIterator.OfInt input) {
        while (input.hasNext()) {
            int next = input.next();
            if (!Character.isWhitespace(next)) {
                return next;
            }
        }

        return '\0';
    }

    @Override
    public void destroy() {
        // ignored
    }

    @Override
    public void setIoInputStream(IoInputStream in) {
        this.in = in;
    }

    @Override
    public void setIoOutputStream(IoOutputStream out) {
        this.out = out;
    }

    @Override
    public void setIoErrorStream(IoOutputStream err) {
        // ignored
    }

    @Override
    public PrintWriter getWriter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Command.Output blankLine() {
        return line("");
    }

    @Override
    public Command.Output line(String line) {
        out.write(new ByteArrayBuffer((line + "\r\n").getBytes(Charsets.UTF_8)));
        return this;
    }

    @Override
    public Command.Output separator() {
        return line("-------------------------------------------------------------------------------");
    }

    @Override
    public Command.Output apply(String s, Object... objects) {
        return line(Strings.apply(s, objects));
    }

    @Override
    public void setSession(ServerSession session) {
        this.session = session;
    }
}
