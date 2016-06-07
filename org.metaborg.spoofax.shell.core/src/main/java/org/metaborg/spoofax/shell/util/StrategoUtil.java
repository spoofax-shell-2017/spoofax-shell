package org.metaborg.spoofax.shell.util;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

/**
 * Contains utility functions for dealing with {@link IStrategoTerm}s.
 */
public final class StrategoUtil {

    /* private due to checkstyle. */
    private StrategoUtil() {
    }

    /**
     * Sets the sort for a given term.
     *
     * @param term The term.
     * @param sort The sort that the term should be set to.
     */
    public static void setSortForTerm(IStrategoTerm term, String sort) {
        ImploderAttachment.putImploderAttachment(term, false,
                                                 sort, null, null);
    }

    /**
     * Gets the sort for a given term, or null if it cannot be retrieved.
     *
     * @param term
     *            The term.
     * @return The sort of the term, or null if it cannot be retrieved.
     */
    public static @Nullable String getSortForTerm(IStrategoTerm term) {
        ImploderAttachment termAttachment = term.getAttachment(ImploderAttachment.TYPE);
        if (termAttachment == null) {
            return null;
        }
        return termAttachment.getElementSort();
    }

}
