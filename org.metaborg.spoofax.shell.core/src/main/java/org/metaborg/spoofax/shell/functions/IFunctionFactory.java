package org.metaborg.spoofax.shell.functions;

import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;

public interface IFunctionFactory {
    ParseFunction createParseFunction(IProject project, ILanguageImpl lang);
    AnalyzeFunction createAnalyzeFunction(IProject project, ILanguageImpl lang);
    PTransformFunction createPTransformFunction(IProject project, ILanguageImpl lang, ITransformAction action);
    ATransformFunction createATransformFunction(IProject project, ILanguageImpl lang, ITransformAction action);
}
