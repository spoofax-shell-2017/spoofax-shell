package org.metaborg.spoofax.shell.client.eclipse.impl;

import java.util.List;
import java.util.Observer;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.metaborg.spoofax.shell.client.IEditor;
import org.metaborg.spoofax.shell.client.IInputHistory;
import org.metaborg.spoofax.shell.client.eclipse.impl.history.EclipseInputHistory;

import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import rx.Observable;
import rx.Subscriber;

/**
 * An Eclipse-based implementation of {@link IEditor}, with a {@link Sourceviewer} backend. It
 * attaches itself as a {@link KeyListener} to listen for certain keypresses. When an Enter (e.g.
 * linefeed or carriage return) is pressed, the {@link Observer}s are notified with the text typed
 * so far.
 *
 * History is automatically maintained through {@link EclipseInputHistory}. The regular Eclipse
 * keybindings apply in the {@link SourceViewer#getTextWidget()} widget.
 *
 * Note that this class should always be run in and accessed from the UI thread!
 */
// FIXME: Make IEditor?
public class EclipseEditor extends KeyAdapter implements ModifyListener {
    private final IInputHistory history;
    private final SourceViewer input;
    // TODO: Use ReplDocument to provide custom partitioning? Perhaps more something for the output
    // as opposed to input. Should be relatively easy for output to at least partition different
    // input/output combinations.
    private final IDocument document;
    private final List<Subscriber<? super String>> observers;

    /**
     * Instantiates a new EclipseEditor.
     *
     * @param history
     *            An {@link IInputHistory} implementation to provide history to this editor.
     * @param parent
     *            A {@link Composite} control which will be the parent of this EclipseEditor.
     *            (cannot be {@code null}).
     */
    @AssistedInject
    public EclipseEditor(IInputHistory history, @Assisted Composite parent) {
        this.history = history;
        this.document = new Document();
        this.input = new SourceViewer(parent, null, SWT.BORDER | SWT.MULTI);
        this.input.setDocument(document);
        this.input.getTextWidget().addKeyListener(this);
        this.observers = Lists.newArrayList();
    }

    /**
     * Give focus to this EclipseEditor's input editor.
     */
    public void setFocus() {
        this.input.getTextWidget().setFocus();
    }

    /**
     * Creates a new {@link Observable} from this editor. The subscriber will be notified via the
     * {@link KeyListener} functions when some notable key presses (e.g. Enter to submit input)
     * occur.
     *
     * @return A new {@link Observable} from this editor.
     */
    public Observable<String> asObservable() {
        return Observable.create(s -> {
            EclipseEditor.this.observers.add(s);
        });
    }

    /**
     * Remove the passed {@link Subscriber} from this editor's observers.
     *
     * @param observer
     *            The {@link Subscriber} to remove.
     */
    public void removeObserver(Subscriber<? super String> observer) {
        this.observers.remove(observer);
    }

    private void enterPressed() {
        String text = document.get();
        this.observers.forEach(o -> o.onNext(text));
        if (text.length() > 0) {
            this.history.append(text);
        }
        this.history.reset();
        this.document.set("");
    }

    private void offerCompletions() {
    }

    private void setTextFromHistory(String text) {
        document.set(text);
        input.setSelectedRange(text.length(), 0);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        switch (event.keyCode) {
        case SWT.LF: // Fallthrough.
        case SWT.CR:
            if ((event.stateMask & (SWT.CTRL | SWT.SHIFT)) == 0) {
                enterPressed();
            }
            break;
        case SWT.SPACE:
            if ((event.stateMask & SWT.CTRL) == SWT.CTRL) {
                offerCompletions();
            }
            break;
        case SWT.PAGE_DOWN:
            if ((event.stateMask & (SWT.CTRL | SWT.SHIFT)) == 0) {
                setTextFromHistory(history.getNext());
            }
            break;
        case SWT.PAGE_UP:
            if ((event.stateMask & (SWT.CTRL | SWT.SHIFT)) == 0) {
                setTextFromHistory(history.getPrevious());
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void modifyText(ModifyEvent event) {
        // TODO: text has been modified, send it to get syntax highlighting.
    }

}
