package org.nandayo.dmentions.service.message;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.*;

/**
 * @since 1.8.3
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiStatus.Internal
public @interface Parameterized {

    /**
     * @return Parameters
     * @since 1.8.3
     */
    String[] params();
}
