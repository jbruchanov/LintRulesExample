package com.example.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.PsiModifier
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.java.JavaUMethod
import org.jetbrains.uast.kotlin.declarations.KotlinUMethod


class InjectingLocationDetector : Detector(), Detector.UastScanner {

    private val injectMethodName = "inject"
    private val allowedClassSuffixes = listOf("Activity", "Fragment")

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf(UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {

            override fun visitCallExpression(node: UCallExpression) {
                if (injectMethodName == node.methodName && node.valueArgumentCount == 1) {
                    node.findParent(UClass::class.java)?.name
                            ?.takeIf { className -> allowedClassSuffixes.any { suffix -> className.endsWith(suffix) } }
                            ?.let { node.findParent(UMethod::class.java) }
                            ?.let { method ->
                                method.isOverridden() && method.name == injectMethodName
                            }
                            ?.let { isValid ->
                                if (!isValid) {
                                    context.report(ISSUE_INJECTING_LOCATION, node,
                                            context.getCallLocation(node, false, true),
                                            "Injection of object is allowed only in inject() method!")
                                }
                            }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : UElement> UElement.findParent(type: Class<T>): T? {
        return when {
            type.isAssignableFrom(this::class.java) -> this as T
            else -> this.uastParent?.findParent(type)
        }
    }

    private fun UMethod.isOverridden(): Boolean {
        return (this as? JavaUMethod)?.hasAnnotation("java.lang.Override") ?: false
                || ((this as? KotlinUMethod)?.javaPsi)?.kotlinOrigin?.hasModifier(KtTokens.OVERRIDE_KEYWORD) ?: false
    }
}