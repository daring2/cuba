/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 02.12.2008 13:14:32
 *
 * $Id$
 */
package com.haulmont.cuba.security.global;

import com.haulmont.cuba.core.global.MessageProvider;

import java.util.UUID;

public class NoUserSessionException extends RuntimeException
{
    private static final long serialVersionUID = 4820628023682230319L;

    public NoUserSessionException(UUID sessionId) {
        super(String.format(
                MessageProvider.getMessage(NoUserSessionException.class, "NoUserSessionException"),
                sessionId.toString()));
    }
}
