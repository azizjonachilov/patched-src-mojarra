/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.faces.facelets.tag.jsf;

import com.sun.faces.facelets.Facelet;
import com.sun.faces.facelets.FaceletFactory;
import com.sun.faces.facelets.util.ReflectionUtil;
import com.sun.faces.facelets.el.VariableMapperWrapper;
import com.sun.faces.facelets.tag.jsf.ComponentTagHandlerDelegateImpl.CreateComponentDelegate;
import com.sun.faces.facelets.tag.MetaRulesetImpl;
import com.sun.faces.facelets.tag.MetadataTargetImpl;
import com.sun.faces.util.RequestStateManager;
import com.sun.faces.util.Util;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.el.ELContext;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.ActionSource;
import javax.faces.component.ValueHolder;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UISelectOne;
import javax.faces.component.UISelectMany;
import javax.faces.context.FacesContext;
import javax.faces.view.AttachedObjectHandler;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.Metadata;
import javax.faces.view.facelets.MetadataTarget;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.MetaRule;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.faces.FactoryFinder;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;

/**
 * <p>
 * Facelet handler responsible for, building the component tree representation
 * of a composite component based on the metadata contained in the composite
 * interface and implementation sections of the composite component template.
 * </p>
 */
public class CompositeComponentTagHandler extends ComponentHandler implements CreateComponentDelegate {

    
    CompositeComponentTagHandler(Resource ccResource, ComponentConfig config) {
        super(config);
        this.ccResource = ccResource;
        ((ComponentTagHandlerDelegateImpl)this.getTagHandlerDelegate()).setCreateCompositeComponentDelegate(this);
    }


    private Resource ccResource;
    private UIComponent cc;
    


    public UIComponent createComponent(FaceletContext ctx) {
        FacesContext context = ctx.getFacesContext();
        UIComponent result = context.getApplication().createComponent(context, ccResource);
        this.cc = result;
        return result;
    }
    
    @Override
    public void applyNextHandler(FaceletContext ctx, UIComponent c) throws IOException, FacesException, ELException {
        // Allow any nested elements that reside inside the markup element
        // for this tag to get applied
        super.applyNextHandler(ctx, c);

        // Apply the facelet for this composite component
        applyCompositeComponent(ctx, c);

        // Allow any PDL declared attached objects to be retargeted
        if (ComponentHandler.isNew(c)) {
            FacesContext context = ctx.getFacesContext();
            String viewId = context.getViewRoot().getViewId();
            // PENDING(rlubke): performance
            ViewDeclarationLanguageFactory factory = (ViewDeclarationLanguageFactory)
                    FactoryFinder.getFactory(FactoryFinder.VIEW_DECLARATION_LANGUAGE_FACTORY);

            ViewDeclarationLanguage vdl = factory.getViewDeclarationLanguage(viewId);
            vdl.retargetAttachedObjects(context, c,
                    getAttachedObjectHandlers(c, false));
            vdl.retargetMethodExpressions(context, c);

            // RELEASE_PENDING This is *ugly*.  See my comments in
            // ComponentTagHandlerDelegateImpl at the end of the apply()
            // method
            if (Boolean.TRUE.equals(ctx.getFacesContext().getAttributes().get("partialStateSaving"))) {
                markInitialState(c);
            }


        }

    }

    
    private void applyCompositeComponent(FaceletContext ctx, UIComponent c)
    throws IOException {

        FacesContext facesContext = ctx.getFacesContext();
        FaceletFactory factory = (FaceletFactory)
              RequestStateManager.get(facesContext, RequestStateManager.FACELET_FACTORY);
        VariableMapper orig = ctx.getVariableMapper();
        
        UIPanel facetComponent;
        if (ComponentHandler.isNew(c)) {
            facetComponent = (UIPanel)
            facesContext.getApplication().createComponent("javax.faces.Panel");
            facetComponent.setRendererType("javax.faces.Group");
            c.getFacets().put(UIComponent.COMPOSITE_FACET_NAME, facetComponent);
        }                                                                                 
        else {
            facetComponent = (UIPanel) 
                    c.getFacets().get(UIComponent.COMPOSITE_FACET_NAME);
        }
        assert(null != facetComponent);
        
        try {
            Facelet f = factory.getFacelet(ccResource.getURL());
            setAttributes(ctx, c);
            VariableMapper wrapper = new VariableMapperWrapper(orig) {

                @Override
                public ValueExpression resolveVariable(String variable) {
                    return super.resolveVariable(variable);
                }
                
            };
            ctx.setVariableMapper(wrapper);
            f.apply(facesContext, facetComponent);
        } finally {
            ctx.setVariableMapper(orig);
        }

    }


    /**
     * Specialized implementation to prevent caching of the MetaRuleset when
     * ProjectStage is Development.
     */
    @Override
    public void setAttributes(FaceletContext ctx, Object instance) {

        if (instance != null) {
            if (ctx.getFacesContext().isProjectStage(ProjectStage.Development)) {
                Metadata meta = createMetaRuleset(instance.getClass()).finish();
                meta.applyMetadata(ctx, instance);
            } else {
                super.setAttributes(ctx, instance);
            }
        }

    }


    /**
     * This is basically a copy of what's define in ComponentTagHandlerDelegateImpl
     * except for the MetaRuleset implementation that's being used.
     *
     * This also allows us to treat composite component's backed by custom component
     * implementation classes based on their type.
     *
     * @param type the <code>Class</code> for which the
     * <code>MetaRuleset</code> must be created.
     *
     */
    @Override
    protected MetaRuleset createMetaRuleset(Class type) {

        Util.notNull("type", type);
        MetaRuleset m = new CompositeComponentMetaRuleset(getTag(), type, (BeanInfo) cc.getAttributes().get(UIComponent.BEANINFO_KEY));

        // ignore standard component attributes
        m.ignore("binding").ignore("id");

        m.addRule(CompositeComponentRule.Instance);

        // if it's an ActionSource
        if (ActionSource.class.isAssignableFrom(type)) {
            m.addRule(ActionSourceRule.Instance);
        }

        // if it's a ValueHolder
        if (ValueHolder.class.isAssignableFrom(type)) {
            m.addRule(ValueHolderRule.Instance);

            // if it's an EditableValueHolder
            if (EditableValueHolder.class.isAssignableFrom(type)) {
                m.ignore("submittedValue");
                m.ignore("valid");
                m.addRule(EditableValueHolderRule.Instance);
            }
        }

        // if it's a selectone or selectmany
        if (UISelectOne.class.isAssignableFrom(type) || UISelectMany.class.isAssignableFrom(type)) {
            m.addRule(RenderPropertyRule.Instance);
        }

        return m;

    }

    public static List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent component) {
        return getAttachedObjectHandlers(component, true);
    }
    
    @SuppressWarnings({"unchecked"})
    public static List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent component,
            boolean create) {
        Map<String, Object> attrs = component.getAttributes();
        List<AttachedObjectHandler> result = (List<AttachedObjectHandler>)
                attrs.get("javax.faces.RetargetableHandlers");
        
        if (null == result) {
            if (create) {
                result = new ArrayList<AttachedObjectHandler>();
                attrs.put("javax.faces.RetargetableHandlers", result);
            }
            else {
                result = Collections.EMPTY_LIST;
            }
        }
        return result;
    }



    private void markInitialState(UIComponent c) {
        c.markInitialState();
        for (Iterator<UIComponent> i = c.getFacetsAndChildren(); i.hasNext(); ) {
            markInitialState(i.next());
        }
    }


    // ---------------------------------------------------------- Nested Classes


    /**
     * Specialized MetaRulesetImpl to return CompositeMetadataTarget for component
     * attribute handling.
     */
    private static final class CompositeComponentMetaRuleset extends MetaRulesetImpl {

        private BeanInfo compBeanInfo;
        private Class<?> type;

        public CompositeComponentMetaRuleset(Tag tag,
                                             Class<?> type,
                                             BeanInfo compBeanInfo) {

            super(tag, type);
            this.compBeanInfo = compBeanInfo;
            this.type = type;

        }

        @Override
        protected MetadataTarget getMetadataTarget() {
            try {
                return new CompositeMetadataTarget(type, compBeanInfo);
            } catch (IntrospectionException ie) {
                throw new FacesException(ie);
            }
        }


        // ------------------------------------------------------ Nested Classes


        /**
         * This class is responsible for creating ValueExpression instances with
         * the expected type based off the following:
         *
         *  - if the composite:attribute metadata is present, then use the type
         *    if specified by the author, or default to Object.class
         *  - if no composite:attribute is specified, then attempt to return the
         *    type based off the bean info for this component
         */
        private static final class CompositeMetadataTarget extends MetadataTargetImpl {

            private BeanInfo compBeanInfo;


            // ---------------------------------------------------- Construcrors


            public CompositeMetadataTarget(Class<?> type, BeanInfo compBeanInfo)
            throws IntrospectionException {

                super(type);
                this.compBeanInfo = compBeanInfo;

            }


            // --------------------------------- Methods from MetadataTargetImpl


            @Override
            public Class getPropertyType(String name) {
                PropertyDescriptor compDescriptor = findDescriptor(name);
                if (compDescriptor != null) {
                    // composite:attribute declaration...
                    ValueExpression typeVE = (ValueExpression) compDescriptor.getValue("type");
                    if (typeVE == null) {
                        return Object.class;
                    } else {
                        String className = (String) typeVE.getValue(FacesContext.getCurrentInstance().getELContext());
                        if (className != null) {
                            className = prefix(className);
                            try {
                                return ReflectionUtil.forName(className);
                            } catch (ClassNotFoundException cnfe) {
                                throw new FacesException(cnfe);
                            }
                        } else {
                            return Object.class;
                        }
                    }
                } else {
                    // defer to the default processing which will inspect the
                    // PropertyDescriptor of the UIComponent type
                    return super.getPropertyType(name);
                }
            }


            // ------------------------------------------------- Private Methods


            private PropertyDescriptor findDescriptor(String name) {

                for (PropertyDescriptor pd : compBeanInfo.getPropertyDescriptors()) {

                    if (pd.getName().equals(name)) {
                        return pd;
                    }

                }
                return null;

            }


            private String prefix(String className) {

                if (className.indexOf('.') == -1
                    && Character.isUpperCase(className.charAt(0))) {
                    return ("java.lang." + className);
                } else {
                    return className;
                }

            }
        }

    } // END CompositeComponentMetaRuleset


    /**
     * <code>MetaRule</code> for populating the ValueExpression map of a
     * composite component.
     */
    private static class CompositeComponentRule extends MetaRule {

        private static final CompositeComponentRule Instance = new CompositeComponentRule();


        // ------------------------------------------ Methods from ComponentRule


        public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta) {

            if (meta.isTargetInstanceOf(UIComponent.class)) {
                Class type = meta.getPropertyType(name);
                if (type == null) {
                    type = Object.class;
                }

                if (!attribute.isLiteral()) {
                    return new CompositeExpressionMetadata(name, type, attribute);
                } else {
                    return new LiteralAttributeMetadata(name, type, attribute);
                }
            }
            return null;

        }


        // ------------------------------------------------------ Nested Classes


        /**
         * For literal expressions, coerce the literal value to the type
         * as provided to the constructor prior to setting the value into
         * the component's attribute map.
         */
        private static final class LiteralAttributeMetadata extends Metadata {

            private String name;
            private Class<?> type;
            private TagAttribute attribute;


            // ---------------------------------------------------- Constructors


            public LiteralAttributeMetadata(String name,
                                            Class<?> type,
                                            TagAttribute attribute) {

                this.name = name;
                this.type = type;
                this.attribute = attribute;
                
            }


            // ------------------------------------------- Methods from Metadata


            public void applyMetadata(FaceletContext ctx, Object instance) {

                UIComponent c = (UIComponent) instance;
                c.getAttributes().put(name, attribute.getObject(ctx, type));

            }

        } // END LiteralAttributeMetadata


        /**
         * CompositeExpressionMetadata sets up specialized wrapper ValueExpression
         * instances around the source ValueExpression that, when evaluated,
         * will cause the parent composite component of the currently available
         * composite component to be pushed onto a stack that the
         * ImplicitObjectELResolver will check for.
         */
        private static final class CompositeExpressionMetadata extends Metadata {

            private String name;
            private Class<?> type;
            private TagAttribute attr;


            // ---------------------------------------------------- Constructors


            public CompositeExpressionMetadata(String name,
                                               Class<?> type,
                                               TagAttribute attr) {
                this.name = name;
                this.type = type;
                this.attr = attr;


            }

            // ------------------------------------------- Methods from Metadata


            public void applyMetadata(FaceletContext ctx, Object instance) {

                ValueExpression ve = attr.getValueExpression(ctx, type);
                UIComponent cc = (UIComponent) instance;
                assert (UIComponent.isCompositeComponent(cc));
                cc.setValueExpression(name, new ContextualCompositeExpression(ve));

            }


        } // END CompositeExpressionMetadata


        private static final class ContextualCompositeExpression extends ValueExpression {

            private ValueExpression originalVE;
       

            // ---------------------------------------------------- Constructors


            /* For serialization purposes */
            public ContextualCompositeExpression() { }


            public ContextualCompositeExpression(ValueExpression originalVE) {

                this.originalVE = originalVE;

            }


            // ------------------------------------ Methods from ValueExpression


            public Object getValue(ELContext elContext) {

                FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
                boolean pushed = pushCompositeComponent(ctx);
                try {
                    return originalVE.getValue(elContext);
                } finally {
                    if (pushed) {
                        popCompositeComponent(ctx);
                    }
                }

            }

            public void setValue(ELContext elContext, Object o) {

                FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
                 boolean pushed = pushCompositeComponent(ctx);
                try {
                    originalVE.setValue(elContext, o);
                } finally {
                    if (pushed) {
                        popCompositeComponent(ctx);
                    }
                }

            }

            public boolean isReadOnly(ELContext elContext) {

                FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
                boolean pushed = pushCompositeComponent(ctx);
                try {
                    return originalVE.isReadOnly(elContext);
                } finally {
                    if (pushed) {
                        popCompositeComponent(ctx);
                    }
                }

            }

            public Class<?> getType(ELContext elContext) {

                FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
                boolean pushed = pushCompositeComponent(ctx);
                try {
                    return originalVE.getType(elContext);
                } finally {
                    if (pushed) {
                        popCompositeComponent(ctx);
                    }
                }

            }

            public Class<?> getExpectedType() {
                
                FacesContext ctx = FacesContext.getCurrentInstance();
                boolean pushed = pushCompositeComponent(ctx);
                try {
                    return originalVE.getExpectedType();
                } finally {
                    if (pushed) {
                        popCompositeComponent(ctx);
                    }
                }

            }


            // ----------------------------------------- Methods from Expression


            public String getExpressionString() {
                return originalVE.getExpressionString();
            }

            @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
            public boolean equals(Object o) {
                return originalVE.equals(o);
            }

            public int hashCode() {
                return originalVE.hashCode();
            }

            public boolean isLiteralText() {
                return originalVE.isLiteralText();
            }


            // ------------------------------------------------- Private Methods


            private boolean pushCompositeComponent(FacesContext ctx) {

                Stack<UIComponent> tstack = getTreeCreationStack(ctx);
                Stack<UIComponent> stack = getStack(ctx, false);
                UIComponent ccp = null;
                if (tstack != null) {
                    // We have access to the stack of composite components
                    // the tree creation process has made available.
                    // Since we can' reliably access the parent composite component
                    // of the current composite component, use the index of the
                    // current composite component within the stack to locate the
                    // parent.
                    UIComponent currentComp;
                    if (stack == null || stack.isEmpty()) {
                        currentComp = UIComponent.getCurrentComponent(ctx);
                    } else {
                        currentComp = stack.peek();

                    }
                    if (currentComp != null) {
                        int idx = tstack.indexOf(currentComp);
                        if (idx > 0) {
                            ccp = tstack.get(idx - 1);
                        }
                    }
                } else {
                    // no tree creation stack available, so use the runtime stack.
                    // If the current stack isn't empty, then use the component
                    // on the stack as the current composite component.
                    stack = getStack(ctx, false);
                    if (stack != null && !stack.isEmpty()) {
                        ccp = UIComponent.getCompositeComponentParent(stack.peek());
                    } else {
                        ccp = UIComponent.getCompositeComponentParent(UIComponent.getCurrentCompositeComponent(ctx));
                    }
                }

                if (ccp != null) {
                    if (stack == null) {
                        stack = getStack(ctx, true);
                    }
                    stack.push(ccp);
                    return true;
                }
                return false;

            }


            private void popCompositeComponent(FacesContext ctx) {

                Stack<UIComponent> stack = getStack(ctx, false);
                if (stack == null) {
                    return;
                }
                if (!stack.isEmpty()) {
                    stack.pop();
                }

            }


            @SuppressWarnings({"unchecked"})
            private Stack<UIComponent> getStack(FacesContext ctx, boolean create) {

                Stack<UIComponent> stack = (Stack<UIComponent>)
                      RequestStateManager.get(ctx, RequestStateManager.COMPCOMP_STACK);
                if (stack == null && create) {
                    stack = new Stack<UIComponent>();
                    RequestStateManager.set(ctx, RequestStateManager.COMPCOMP_STACK, stack);
                }
                return stack;

            }


            @SuppressWarnings({"unchecked"})
            private Stack<UIComponent> getTreeCreationStack(FacesContext ctx) {

                return (Stack<UIComponent>)
                      RequestStateManager.get(ctx, RequestStateManager.COMPCOMP_STACK_TREE_CREATION);

            }


        } // END ContextualCompositeExpression

    } // END CompositeComponentRule
    
    
}
