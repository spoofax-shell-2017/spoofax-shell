package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.shell.client.IHook;
import org.metaborg.spoofax.shell.output.IResultFactory;

import com.google.inject.Inject;

/**
 * Command for processing a String as an expression in some language.
 */
public abstract class SpoofaxCommand implements IReplCommand {
    protected IResultFactory resultFactory;
    protected IProject project;
    protected ILanguageImpl lang;

    /**
     * Instantiate a {@link SpoofaxCommand}.
     *
     * @param resultFactory
     *            The {@link ResulFactory}.
     * @param project
     *            The project in which this command should operate.
     * @param lang
     *            The language to which this command applies.
     */
    @Inject
    SpoofaxCommand(IResultFactory resultFactory, IProject project, ILanguageImpl lang) {
        this.resultFactory = resultFactory;
        this.project = project;
        this.lang = lang;
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

    @Override
    public abstract IHook execute(String... args) throws MetaborgException;
}
