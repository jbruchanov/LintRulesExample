package com.example.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.TypeEvaluator
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement


class InjectingSuperTypeDetector : Detector(), Detector.UastScanner {

    private val injectMethodName = "inject"
    private val injectAnnotationName = "Inject"

    override fun getApplicableUastTypes(): List<Class<out UElement>>? {
        return listOf(UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {

            override fun visitCallExpression(node: UCallExpression) {
                try {
                    check(node)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            private fun check(node: UCallExpression) {
                if (injectMethodName == node.methodName && node.valueArgumentCount == 1) {
                    val injectedClass = TypeEvaluator.evaluate(node.valueArguments[0])
                    var clazz = ((TypeEvaluator.evaluate(node.receiver)) as PsiClassType).resolve()
                    while (clazz is PsiAnonymousClass) {
                        clazz = clazz.superTypes.last().resolve()
                    }
                    val notFoundActualTypeInjection = clazz
                            ?.allMethods
                            ?.filter { it.name == injectMethodName && it.parameters.count() == 1 }
                            ?.none {
                                val param = it.parameters.first()
                                param.type == injectedClass
                            } ?: true

                    val hasInjections =
                            PsiTypesUtil.getPsiClass(injectedClass)
                                    .takeIf { notFoundActualTypeInjection }
                                    ?.fields
                                    ?.filter { it.annotations.isNotEmpty() }
                                    ?.any { field ->
                                        field.annotations.any { annotation ->
                                            annotation?.qualifiedName
                                                    ?.endsWith(injectAnnotationName) ?: false
                                        }
                                    } ?: false

                    if (notFoundActualTypeInjection && hasInjections) {
                        val componentName = (node.receiverType as PsiClassType).name
                        val expectedType = PsiTypesUtil.getPsiClass(injectedClass)?.name
                                ?: injectedClass.toString()

                        val varName = expectedType[0].toLowerCase() + expectedType.substring(1)
                        context.report(ISSUE_INJECTING_SUPER_TYPE, node,
                                context.getCallLocation(node, false, true),
                                "Missing method inject($expectedType $varName) in $componentName!")
                    }
                }
            }
        }
    }
}