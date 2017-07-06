package org.metaborg.spoofax.shell.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to suppress <a href="http://findbugs.sourceforge.net">FindBugs</a> warnings.
 */
@Retention(RetentionPolicy.CLASS)
public @interface SuppressFBWarnings {

    /**
     * The set of FindBugs warnings that are to be suppressed in
     * annotated element. The value can be a bug category, kind or pattern.
     *
     *  @return warnings to suppress
     */
    String[] value() default {};

    /**
     * Optional documentation of the reason why the warning is suppressed.
     *
     * @return explaination
     */
    String justification() default "";
}
