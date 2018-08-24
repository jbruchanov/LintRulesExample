package com.example.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod


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
                            ?.let { method -> method.isOverride && method.name == injectMethodName }
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
}