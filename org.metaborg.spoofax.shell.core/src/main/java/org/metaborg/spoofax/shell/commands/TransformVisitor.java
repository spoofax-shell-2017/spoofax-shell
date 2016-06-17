package org.metaborg.spoofax.shell.commands;

import java.util.Map;

import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.menu.IMenu;
import org.metaborg.core.menu.IMenuAction;
import org.metaborg.core.menu.IMenuItem;
import org.metaborg.core.menu.IMenuItemVisitor;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.core.menu.Separator;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * An {@link IMenuItemVisitor} implementation that traverses over all {@link IMenuItem}s.
 * This class then creates a map of all defined {@link ITransformAction}s.
 */
public class TransformVisitor implements IMenuItemVisitor {
    private final IMenuService menuService;
    private Map<String, ITransformAction> actions;

    /**
     * Instantiates a new {@link TransformVisitor}.
     *
     * @param menuService
     *            The {@link IMenuService} to visit.
     */
    @Inject
    public TransformVisitor(IMenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * Retrieve all actions from the {@link IMenuService} that are defined for the given
     * {@link ILanguageImpl language}.
     *
     * @param lang
     *            The {@link ILanguageImpl} whose menu items to visit.
     * @return All {@link ITransformAction}s belonging to the passed {@link ILanguageImpl}.
     */
    public Map<String, ITransformAction> getActions(ILanguageImpl lang) {
        actions = Maps.newHashMap();
        menuService.menuItems(lang).forEach(e -> e.accept(this));
        return actions;
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
