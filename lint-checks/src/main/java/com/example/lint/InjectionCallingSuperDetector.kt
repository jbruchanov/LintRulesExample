package com.example.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.LintUtils.skipParentheses
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.*
import org.jetbrains.uast.visitor.AbstractUastVisitor
import java.awt.SystemColor.text

/**
 * Check if there is any inject() method and warn dev if it's calling super
 * Mostly copied CallSuper detector
 * https://android.googlesource.com/platform/tools/base/+/studio-master-dev/lint/libs/lint-checks/src/main/java/com/android/tools/lint/checks/CallSuperDetector.kt
 */
class InjectionCallingSuperDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf(UMethod::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return InjectionCheckHandler(context)
    }

    class InjectionCheckHandler(private val context: JavaContext) : UElementHandler() {

        private val injectMethodName = "inject"

        override fun visitMethod(method: UMethod) {
            if (injectMethodName == method.name) {
                val superMethod = context.evaluator.getSuperMethod(method)
                superMethod
                        ?.let { callsSuper(method, superMethod) }
                        ?.takeIf { it.first }
                        ?.let {
                            it.second
                                    ?: throw NullPointerException("Calling super, but the node is null!")
                        }
                        ?.let { node ->
                            method.children
                            context.report(ISSUE_INJECTION_CALLING_SUPER, node,
                                    context.getLocation(node),
                                    "We don't want to call super for injection, actual Component has to be able inject all dependencies",
                                    lintFix())
                        }
            } else {
                //check super.inject() from another methods
            }
        }

        private fun callsSuper(method: UMethod,
                               superMethod: PsiMethod): Pair<Boolean, UElement?> {
            val visitor = SuperCallVisitor(superMethod)
            method.accept(visitor)
            return Pair(visitor.callsSuper, visitor.callNode)
        }

        private fun lintFix(): LintFix {
            return LintFix.create()
                    .replace()
                    .name("Remove it...")
                    .text("super.inject()")
                    .with("")
                    .build()
        }
    }

    /** Visits a method and determines whether the method calls its super method  */
    private class SuperCallVisitor constructor(private val targetMethod: PsiMethod) : AbstractUastVisitor() {

        var callsSuper: Boolean = false
        var callNode: UElement? = null

        override fun visitCallExpression(node: UCallExpression): Boolean {
            if (callsSuper && callNode == null) {
                //this should be done better way, as it expected to be called right after
                //visitSuperExpression setting callsSuper = true
                callNode = node
            }
            return super.visitCallExpression(node)
        }

        override fun visitSuperExpression(node: USuperExpression): Boolean {
            val parent = skipParentheses(node.uastParent)
            if (parent is UReferenceExpression) {
                val resolved = parent.resolve()
                if (targetMethod == resolved
                        // Avoid false positives when there are type resolution problems
                        || resolved == null) {
                    callsSuper = true
                }
            }
            return super.visitSuperExpression(node)
        }
    }
}