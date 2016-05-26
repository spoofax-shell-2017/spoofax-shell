package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.shell.core.StyledText;

import com.google.inject.Inject;

/**
 * Command for processing a String as an expression in some language.
 */
public abstract class SpoofaxCommand implements IReplCommand {
    private static final String DESCRIPTION = "Process an expression in some language";

    protected IStrategoCommon common;

    protected Consumer<StyledText> onSuccess;
    protected Consumer<StyledText> onError;

    protected IProject project;
    protected ILanguageImpl lang;

    /**
     * Instantiate a {@link SpoofaxCommand}.
     * @param common    The {@link IStrategoCommon} service.
     * @param onSuccess Called upon success by the created {@link SpoofaxCommand}.
     * @param onError   Called upon an error by the created {@link SpoofaxCommand}.
     * @param project   The project in which this command should operate.
     * @param lang      The language to which this command applies.
     */
    @Inject
    SpoofaxCommand(IStrategoCommon common,
                   Consumer<StyledText> onSuccess,
                   Consumer<StyledText> onError,
                   IProject project,
                   ILanguageImpl lang) {
        this.common = common;
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.project = project;
        this.lang = lang;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Write source to a temporary file.
     * @param source the source code
     * @return a {@link FileObject}
     * @throws IOException when writing to the file fails
     */
    protected FileObject write(String source) throws IOException {
        // FIXME: hardcoded file path
        FileObject sourceFile = this.project.location().resolveFile("tmp.src");
        try (OutputStream os = sourceFile.getContent().getOutputStream()) {
            os.write(source.getBytes(Charset.forName("UTF-8")));
        }

        return sourceFile;
    }

    /**
     * Executes a command.
     *
     * @param args
     *            The command's arguments.
     */
    @Override
    public abstract void execute(String... args);
}
