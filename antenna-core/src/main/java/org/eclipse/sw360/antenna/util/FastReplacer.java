/*
 * Copyright (c) Bosch Software Innovations GmbH 2016-2017.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.sw360.antenna.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FastReplacer allows to replace multiple different matches by parsing the
 * given String once. For example replacing all "A" with "1" and all "B" with
 * "2" in "ABABABABABABA" the common way is<br>
 * 
 * <pre>
 * String target = &quot;ABABABABABABA&quot;;
 * target = target.replaceAll(&quot;A&quot;, &quot;1&quot;);
 * target = target.replaceAll(&quot;B&quot;, &quot;2&quot;);
 * </pre>
 * 
 * FastReplacer does the replacement by passing the target String once through
 * the matcher.
 */
public class FastReplacer {
    private Pattern regexPattern;
    private IMatchHandler handler;

    private FastReplacer() {
    }

    /**
     * 
     * @param regexPattern
     *            Sets the regex Pattern which describes the matching pattern.
     * @param handler
     *            Sets the handler.
     */
    public FastReplacer(Pattern regexPattern, IMatchHandler handler) {
        this.regexPattern = regexPattern;
        this.handler = handler;
    }

    /**
     * Applies this FastReplacer on the given CharSequence.
     * 
     * @param operand
     *            the operand which shall be resolved
     * @return the result CharSequence.
     */
    public CharSequence applyOn(CharSequence operand) {
        int length = operand.length();
        StringBuilder destination = new StringBuilder(length);
        Matcher matcher = this.regexPattern.matcher(operand);
        int lastEnd = 0;
        while (matcher.find()) {
            int start = matcher.start();
            CharSequence betweenMatches = operand.subSequence(lastEnd, start);
            destination.append(betweenMatches);
            String replacement = this.handler.handleMatch(matcher);
            destination.append(replacement);
            lastEnd = matcher.end();
        }
        CharSequence tail = operand.subSequence(lastEnd, length);
        destination.append(tail);
        return destination;
    }

    /**
     * Applies this FastReplacer on the given String.
     *
     * @param operand
     *            the String which shall be resolved
     * @return the result String
     */
    public String applyOn(String operand) {
        return this.applyOn((CharSequence) operand).toString();
    }

    public static On apply(Pattern regexPattern) {
        FastReplacer replacer = new FastReplacer();
        replacer.regexPattern = regexPattern;
        return new On(replacer);
    }

    public static final class On {
        private FastReplacer replacer;

        private On(FastReplacer replacer) {
            this.replacer = replacer;
        }

        public Using on(CharSequence operand) {
            return new Using(this.replacer, operand);
        }
    };

    public static final class Using {
        private FastReplacer replacer;
        private CharSequence operand;

        private Using(FastReplacer replacer, CharSequence operand) {
            this.replacer = replacer;
            this.operand = operand;
        }

        public CharSequence using(IMatchHandler handler) {
            this.replacer.handler = handler;
            return this.replacer.applyOn(operand);
        }
    }
}
