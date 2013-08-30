/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.gradle.service.resolve;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiManager;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrReferenceExpressionImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GrImplicitVariableImpl;

import java.util.List;

/**
 * @author Vladislav.Soroka
 * @since 8/29/13
 */
public class GradleConfigurationsContributor implements GradleMethodContextContributor {

  private static final String CONFIGURATIONS = "configurations";

  @Override
  public void process(@NotNull List<String> methodCallInfo,
                      @NotNull PsiScopeProcessor processor,
                      @NotNull ResolveState state,
                      @NotNull PsiElement place) {
    if (methodCallInfo.isEmpty()) {
      return;
    }
    final String methodCall = methodCallInfo.get(0);

    PsiClass contributorClass = null;
    GroovyPsiManager psiManager = GroovyPsiManager.getInstance(place.getProject());
    if (methodCallInfo.size() == 1) {
      if (methodCall.startsWith(CONFIGURATIONS + '.')) {
        contributorClass = psiManager.findClassWithCache(Configuration.class.getName(), place.getResolveScope());
      }
      else if (CONFIGURATIONS.equals(methodCall)) {
        contributorClass = psiManager.findClassWithCache(ConfigurationContainer.class.getName(), place.getResolveScope());
        if (place instanceof GrReferenceExpressionImpl) {
          addImplicitConfiguration(processor, state, (GrReferenceExpressionImpl)place);
          return;
        }
      }
    }
    else if (methodCallInfo.size() == 2) {
      if (CONFIGURATIONS.equals(methodCallInfo.get(1))) {
        contributorClass = psiManager.findClassWithCache(ConfigurationContainer.class.getName(), place.getResolveScope());
        if (place instanceof GrReferenceExpressionImpl) {
          addImplicitConfiguration(processor, state, (GrReferenceExpressionImpl)place);
          return;
        }
      }
    }
    if (contributorClass != null) {
      contributorClass.processDeclarations(processor, state, null, place);
    }
  }

  @SuppressWarnings("MethodMayBeStatic")
  private void addImplicitConfiguration(@NotNull PsiScopeProcessor processor,
                                        @NotNull ResolveState state,
                                        @NotNull GrReferenceExpressionImpl expression) {
    String expr = expression.getCanonicalText();
    PsiVariable myPsi = new GrImplicitVariableImpl(expression.getManager(), expr, Configuration.class.getName(), expression);
    processor.execute(myPsi, state);
  }
}
