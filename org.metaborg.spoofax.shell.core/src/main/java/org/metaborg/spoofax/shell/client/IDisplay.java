package org.metaborg.spoofax.shell.client;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.metaborg.core.messages.IMessage;
import org.metaborg.core.source.ISourceRegion;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.core.style.IStyle;
import org.metaborg.core.style.Style;
import org.metaborg.spoofax.shell.output.FailResult;
import org.metaborg.spoofax.shell.output.IResultVisitor;
import org.metaborg.spoofax.shell.output.ISpoofaxResult;
import org.metaborg.spoofax.shell.output.StyledText;

/**
 * Adapter {@link IResultVisitor} interface for displaying results and errors. An implementation of
 * {@link IDisplay} knows how to interpret the style information of a {@link StyledText} and display
 * it appropriately.
 */
public interface IDisplay extends IResultVisitor {

    /**
     * Display the given {@link StyledText}. How the style information is interpreted depends on the
     * client.
     *
     * @param text
     *            The {@link StyledText} to display.
     */
    void displayStyledText(StyledText text);

    @Override
    default void visitMessage(StyledText message) {
        displayStyledText(message);
    }

    @Override
    default void visitResult(ISpoofaxResult<?> result) {
        visitMessage(result.styled());
    }

    @Override
    default void visitFailure(FailResult errorResult) {
        ISpoofaxResult<?> cause = errorResult.getCause();
        String sourceText = cause.sourceText();
        List<IMessage> messages = cause.messages();
        StyledText styled = highlightMessagesInSource(sourceText, messages);

        String concat =
            messages.stream().map(message -> message.message()).collect(Collectors.joining("\n"));
        visitMessage(styled.append("\n").append(Color.RED, concat));
    }

    /**
     * Highlights the {@link SourceRegion}s of the given {@link IMessage}s in the given source text
     * with a red color and bold style.
     *
     * @param sourceText
     *            The source text that caused the failure.
     * @param messages
     *            The error messages.
     * @return The highlighted {@link StyledText}
     */
    default StyledText highlightMessagesInSource(String sourceText, List<IMessage> messages) {
        List<ISourceRegion> regions = messages.stream().map(message -> message.region())
            .filter(Objects::nonNull).collect(Collectors.toList());
        StyledText styled = new StyledText();
        IStyle style = new Style(Color.RED, null, true, false, false);
        styled.append(regions, style, sourceText);
        return styled;
    }

    @Override
    default void visitException(Throwable thrown) {
        visitMessage(new StyledText(Color.RED, thrown.getMessage()));
    }

}
