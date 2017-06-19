package org.metaborg.spoofax.shell.services;

import org.metaborg.spoofax.shell.functions.FunctionComposer;

public interface IServicesStrategyFactory {

	IEditorServicesStrategy createUnloadedStrategy();

	IEditorServicesStrategy createLoadedStrategy(FunctionComposer composer);

}
