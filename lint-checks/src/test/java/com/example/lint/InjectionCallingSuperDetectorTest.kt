package com.example.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

class InjectionCallingSuperDetectorTest {

    @Test
    fun validation_notCallingSuperInject_isOk() {
        val code = """
        package test;

        abstract class Base {
            abstract void inject();
        }

        class SubClass extends Base {
            @override void inject() {
            }
        }

        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.JavaTestFile.create(code))
                .issues(ISSUE_INJECTION_CALLING_SUPER)
                .run()
                .expectClean()
    }

    @Test
    fun validation_callingSuperInject_reportsError() {
        val code = """
         package test;

        abstract class Base {
            abstract void inject();
        }

        class SubClass extends Base {
            @override void inject() {
                super.inject();
            }
        }

        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.JavaTestFile.create(code))
                .issues(ISSUE_INJECTION_CALLING_SUPER)
                .run()
                .expectErrorCount(1)
    }

    @Test
    fun validationKt_notCallingSuperInject_isOk() {
        val code = """
        package test

        abstract class Base {
            abstract fun inject()
        }

        open class SubClass : Base() {
            override fun inject() {
            }
        }

        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.KotlinTestFile.create(code))
                .issues(ISSUE_INJECTION_CALLING_SUPER)
                .run()
                .expectClean()
    }

    @Test
    fun validationKt_callingSuperInject_reportsError() {
        val code = """
        package test

        abstract class Base {
            abstract fun inject()
        }

        open class SubClass : Base() {
            override fun inject() {
                super.inject()
            }
        }

        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.KotlinTestFile.create(code))
                .issues(ISSUE_INJECTION_CALLING_SUPER)
                .run()
                .expectErrorCount(1)
    }
}