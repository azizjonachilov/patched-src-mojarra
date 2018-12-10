/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package javax.faces.view.facelets;

import javax.faces.FacesWrapper;

/**
 * <p class="changed_added_2_0"><span class="changed_modified_2_2 changed_modified_2_3">Abstract</span>
 * factory for creating instances of {@link TagHandlerDelegate}.</p>
 *
 * <p class="changed_added_2_3">Usage: extend this class and push the implementation being wrapped to the
 * constructor and use {@link #getWrapped} to access the instance being wrapped.</p>
 *
 * @since 2.0
 */
public abstract class TagHandlerDelegateFactory implements FacesWrapper<TagHandlerDelegateFactory> {

    private TagHandlerDelegateFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public TagHandlerDelegateFactory() {

    }

    /**
     * <p class="changed_added_2_3">If this factory has been decorated,
     * the implementation doing the decorating should push the implementation being wrapped to this constructor.
     * The {@link #getWrapped()} will then return the implementation being wrapped.</p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public TagHandlerDelegateFactory(TagHandlerDelegateFactory wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * <p class="changed_modified_2_3">If this factory has been decorated, the
     * implementation doing the decorating may override this method to provide
     * access to the implementation being wrapped.</p>
     *
     * @since 2.2
     */
    @Override
    public TagHandlerDelegateFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p class="changed_added_2_0">Create and return a {@link
     * TagHandlerDelegate} instance designed for use with {@link
     * ComponentHandler}.</p>
     *
     * @param owner the <code>ComponentHandler</code> instance being
     * helped by this helper instance.
     *
     * @return the newly created instance
     *
     * @since 2.0
     */
    public abstract TagHandlerDelegate createComponentHandlerDelegate(ComponentHandler owner);

    /**
     * <p class="changed_added_2_0">Create and return a {@link
     * TagHandlerDelegate} instance designed for use with {@link
     * ValidatorHandler}.</p>
     *
     * @param owner the <code>ValidatorHandler</code> instance being
     * helped by this helper instance.
     *
     * @return the newly created instance
     *
     * @since 2.0
     */
    public abstract TagHandlerDelegate createValidatorHandlerDelegate(ValidatorHandler owner);

    /**
     * <p class="changed_added_2_0">Create and return a {@link
     * TagHandlerDelegate} instance designed for use with {@link
     * ConverterHandler}.</p>
     *
     * @param owner the <code>ValidatorHandler</code> instance being
     * helped by this helper instance.
     *
     * @return the newly created instance
     *
     * @since 2.0
     */
    public abstract TagHandlerDelegate createConverterHandlerDelegate(ConverterHandler owner);

    /**
     * <p class="changed_added_2_0">Create and return a {@link
     * TagHandlerDelegate} instance designed for use with {@link
     * BehaviorHandler}.</p>
     *
     * @param owner the <code>ValidatorHandler</code> instance being
     * helped by this helper instance.
     *
     * @return the newly created instance
     *
     * @since 2.0
     */
    public abstract TagHandlerDelegate createBehaviorHandlerDelegate(BehaviorHandler owner);

}