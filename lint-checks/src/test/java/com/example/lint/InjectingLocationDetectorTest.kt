package com.example.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.detector.api.Detector
import org.junit.Test

class   InjectingLocationDetectorTest : Detector(), Detector.UastScanner {

    @Test
    fun validation_withInvalidLocationButNotAllowedClass_notifiesError() {
        val code = """
            package test;

            public class ParentClass {
                public void test() {
                    Component component = null;
                    component.inject(this);
                }

                interface Component {
                    void inject(ParentClass o);
                }
            }
        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.JavaTestFile.create(code))
                .issues(ISSUE_INJECTING_LOCATION)
                .run()
                .expectClean()
    }

    @Test
    fun validation_withInvalidLocationWithCheckingAllowedClass_notifiesError() {
        val code = """
            package test;

            public class ParentActivity {
                public void test() {
                    Component component = null;
                    component.inject(this);
                }

                interface Component {
                    void inject(ParentClass o);
                }
            }
        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.JavaTestFile.create(code))
                .issues(ISSUE_INJECTING_LOCATION)
                .run()
                .expectErrorCount(1)
    }

    @Test
    fun validation_withValidLocationButNoOverrideButNotAllowedClass_notifiesError() {
        val code = """
            package test;

            public class ParentClass {
                //no override is wrong, but this is not Activity/Fragment
                public void inject() {
                    Component component = null;
                    component.inject(this);
                }

                interface Component {
                    void inject(ParentClass o);
                }
            }
        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.JavaTestFile.create(code))
                .issues(ISSUE_INJECTING_LOCATION)
                .run()
                .expectClean()
    }

    @Test
    fun validation_withValidLocationButNoOverrideCheckingAllowedClass_notifiesError() {
        val code = """
            package test;

            public class ParentFragment {
                //no override is wrong
                public void inject() {
                    Component component = null;
                    component.inject(this);
                }

                interface Component {
                    void inject(ParentClass o);
                }
            }
        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.JavaTestFile.create(code))
                .issues(ISSUE_INJECTING_LOCATION)
                .run()
                .expectErrorCount(1)
    }

    @Test
    fun validation_withValidLocationInActivity_isOk() {
        val code = """
            package test;

            public class ParentActivity {

                @Override
                public void inject() {
                    Component component = null;
                    component.inject(this);
                }

                interface Component {
                    void inject(ParentClass o);
                }
            }
        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.JavaTestFile.create(code))
                .issues(ISSUE_INJECTING_LOCATION)
                .run()
                .expectClean()
    }

    @Test
    fun validationKt_withValidLocationInActivity_isOk() {
        val code = """
            package test

            class ParentActivity {

                override fun inject() {
                    val component:Component? = null
                    component?.inject(this)
                }

                interface Component {
                    fun inject(o: ParentClass)
                }
            }
        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.KotlinTestFile.create(code))
                .issues(ISSUE_INJECTING_LOCATION)
                .run()
                .expectClean()
    }

    @Test
    fun validation_withValidLocationInFragment_isOk() {
        val code = """
            package test;

            public class ParenFragment {

                @Override
                public void inject() {
                    Component component = null;
                    component.inject(this);
                }

                interface Component {
                    void inject(ParentClass o);
                }
            }
        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.JavaTestFile.create(code))
                .issues(ISSUE_INJECTING_LOCATION)
                .run()
                .expectClean()
    }
}