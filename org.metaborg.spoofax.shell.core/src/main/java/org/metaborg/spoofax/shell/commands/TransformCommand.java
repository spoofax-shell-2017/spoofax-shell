package org.metaborg.spoofax.shell.commands;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.menu.IMenu;
import org.metaborg.core.menu.IMenuAction;
import org.metaborg.core.menu.IMenuItem;
import org.metaborg.core.menu.IMenuItemVisitor;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.core.menu.Separator;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.menu.MenuService;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.shell.client.IHook;
import org.metaborg.spoofax.shell.invoker.ICommandFactory;
import org.metaborg.spoofax.shell.output.AnalyzeResult;
import org.metaborg.spoofax.shell.output.IResultFactory;
import org.metaborg.spoofax.shell.output.InputResult;
import org.metaborg.spoofax.shell.output.ParseResult;
import org.metaborg.spoofax.shell.output.TransformResult;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Represents an evaluate command sent to Spoofax.
 */
public class TransformCommand extends SpoofaxCommand implements IMenuItemVisitor {
    private static final String DESCRIPTION = "Transform an expression:";

    private IContextService contextService;
    private ISpoofaxTransformService transformService;

    private AnalyzeCommand analyzeCommand;
    private ParseCommand parseCommand;
    private Strategy strategy;

    private Map<String, ITransformAction> actions;

    /**
     */
    private interface Strategy {
        TransformResult transform(IContext context, ParseResult unit, ITransformGoal goal)
                throws MetaborgException;
    }

    /**
     */
    private class Parsed implements Strategy {
        @Override
        public TransformResult transform(IContext context, ParseResult unit, ITransformGoal goal)
                throws MetaborgException {
            Collection<ISpoofaxTransformUnit<ISpoofaxParseUnit>> transform =
                    transformService.transform(unit.unit(), context, goal);
            return resultFactory.createTransformResult(transform.iterator().next());
        }
    }

    /**
     */
    private class Analyzed implements Strategy {
        @Override
        public TransformResult transform(IContext context, ParseResult unit, ITransformGoal goal)
                throws MetaborgException {
            AnalyzeResult analyze = analyzeCommand.analyze(unit);
            Collection<ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> transform =
                    transformService.transform(analyze.unit(), context, goal);
            return resultFactory.createTransformResult(transform.iterator().next());
        }
    }

    /**
     * Instantiate an {@link EvaluateCommand}.
     *
     * @param contextService
     *            The {@link IContextService}.
     * @param transformService
     *            The {@link ISpoofaxTransformService}.
     * @param menuService
     *            The {@link MenuService} used to retrieve actions.
     * @param commandFactory
     *            The {@link CommandFactory} to create {@link SpoofaxCommand}s.
     * @param resultFactory
     *            The {@link ResultFactory}.
     * @param project
     *            The project in which this command should operate.
     * @param lang
     *            The language to which this command applies.
     * @param analyzed
     *            Whether this language should be analyzed or not.
     */
    @Inject
    // CHECKSTYLE.OFF: ParameterNumber
    public TransformCommand(IContextService contextService,
                            ISpoofaxTransformService transformService,
                            IMenuService menuService,
                            ICommandFactory commandFactory,
                            IResultFactory resultFactory,
                            @Assisted IProject project,
                            @Assisted ILanguageImpl lang,
                            @Assisted boolean analyzed) {
        // CHECKSTYLE:ON: ParameterNumber
        super(resultFactory, project, lang);
        this.contextService = contextService;
        this.transformService = transformService;
        this.strategy = analyzed ? new Analyzed() : new Parsed();

        this.parseCommand = commandFactory.createParse(project, lang);
        this.analyzeCommand = commandFactory.createAnalyze(project, lang);

        actions = Maps.newConcurrentMap();
        menuService.menuItems(lang).forEach(e -> e.accept(this));
    }

    @Override
    public String description() {
        return Stream.concat(Stream.of(DESCRIPTION), actions.keySet().stream())
                     .collect(Collectors.joining("\n- "));
    }

    private TransformResult transform(Strategy strat, ParseResult unit, ITransformGoal goal)
            throws MetaborgException {
        IContext context = unit.context().orElse(contextService.get(unit.source(), project, lang));
        TransformResult result = strat.transform(context, unit, goal);

        // TODO: pass the result to the client instead of throwing an exception -- The client needs
        // the result in order to do fancy stuff.
        if (!result.valid()) {
            throw new MetaborgException("Invalid transform result!");
        }
        return result;
    }

    @Override
    public IHook execute(String... args) throws MetaborgException {
        String[] split = args[0].split("\\s+", 2);
        if (split.length < 1) {
            throw new MetaborgException("No transform goal specified.");
        }
        String goalname = split[0];

        if (!actions.containsKey(goalname)) {
            String message = String.format("Transform goal \"%s\" does not exist.", goalname);
            throw new MetaborgException(message);
        }
        ITransformGoal goal = actions.get(goalname).goal();

        if (split.length < 2) {
            throw new MetaborgException("No source text specified");
        }
        String source = split[1];

        try {
            InputResult input = resultFactory.createInputResult(lang, write(source), source);
            ParseResult parse = parseCommand.parse(input);
            TransformResult result = transform(strategy, parse, goal);
            return (display) -> display.displayResult(result);
        } catch (IOException e) {
            throw new MetaborgException("Cannot write to temporary source file.");
        }
    }

    @Override
    public void visitSeparator(Separator separator) {
        // No actions defined for separators.
    }

    @Override
    public void visitMenuItem(IMenuItem item) {
        // No actions defined for generic menu items.
    }

    @Override
    public void visitMenu(IMenu menu) {
        menu.items().forEach(e -> e.accept(this));
    }

    @Override
    public void visitAction(IMenuAction action) {
        actions.put(action.name().toLowerCase().replace(' ', '_'), action.action());
    }

}
