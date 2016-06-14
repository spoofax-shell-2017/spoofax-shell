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
 * An {@link IMenuItemVisitor} implementation to create one {@link TransformCommand} per menu item.
 */
public class TransformVisitor implements IMenuItemVisitor {
    private Map<String, ITransformAction> actions;
    private IMenuService menuService;

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
     * Retrieve all the actions from the {@link IMenuService} that belong to the given
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
