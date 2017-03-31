package org.metaborg.spoofax.shell.client.eclipse.commands;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.metaborg.spoofax.shell.client.eclipse.ReplView;
import org.metaborg.spoofax.shell.commands.IReplCommand;
import org.metaborg.spoofax.shell.output.IResult;

/**
 * Exit the REPL.
 */
public class ExitCommand implements IReplCommand {

    @Override
    public String description() {
        return "Exit the REPL session.";
    }

    @Override
    public IResult execute(String... args) {
        return (visitor) -> {
            IWorkbenchPage page =
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IViewPart replView = page.findView(ReplView.ID);
            page.hideView(replView);
        };
    }
}
