package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.shell.core.StyledText;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Represents a parse command sent to Spoofax.
 */
public class ParseCommand extends SpoofaxCommand {
    private static final String DESCRIPTION = "Parse an expression.";

    @Inject
    private ISpoofaxUnitService unitService;
    @Inject
    private ISpoofaxSyntaxService syntaxService;

    /**
     * Instantiate a {@link ParseCommand}.
     * @param onSuccess Called upon success by the created {@link SpoofaxCommand}.
     * @param onError   Called upon an error by the created {@link SpoofaxCommand}.
     */
    @Inject
    public ParseCommand(@Named("onSuccess") final Consumer<StyledText> onSuccess,
                        @Named("onError") final Consumer<StyledText> onError) {
        super(onSuccess, onError);
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    /**
     * Parses a program using the {@link ISpoofaxSyntaxService}.
     * @param source the source of the program
     * @param sourceFile the file containing the source of the program
     * @return an {@link ISpoofaxParseUnit}
     * @throws MetaborgException when parsing fails
     * @throws IOException when creating a temp file fails
     */
    public ISpoofaxParseUnit parse(String source, FileObject sourceFile)
            throws IOException, MetaborgException {
        OutputStream os = sourceFile.getContent().getOutputStream();
        os.write(source.getBytes(Charset.forName("UTF-8")));
        os.close();

        final ISpoofaxInputUnit inputUnit = unitService.inputUnit(sourceFile, source,
                this.context.language(), null);

        ISpoofaxParseUnit parseUnit = this.syntaxService.parse(inputUnit);
        if (!parseUnit.valid()) {
            throw new MetaborgException("Resulting parse unit invalid.");
        }
        return parseUnit;
    }

    @Override
    public void execute(final String... args) {
        try {
            FileObject tempFile = this.context.location().resolveFile("tmp.src");
            IStrategoTerm term = this.parse(args[0], tempFile).ast();

            this.onSuccess.accept(new StyledText(common.toString(term)));
        } catch (IOException | MetaborgException e) {
            this.onError.accept(new StyledText(e.getMessage()));
        }
    }
}
