package com.example.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestLintTask
import org.junit.Test

class InjectingSuperTypeDetectorTest {

    @Test
    fun validation_withMissingConcreteTypeAndHavingInjectedField_notifiesError() {
        val code = """
            package test;

            public abstract class ParentClass {

                public static class SubClass extends ParentClass {

                    @Inject Object someInjection;

                    public void test() {
                        Component component = new Component() {
                            @Override
                            public void inject(ParentClass o) {
                            }
                        };
                        component.inject(this);
                    }
                }

                interface Component {
                    void inject(ParentClass o);
                }
            }
        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.JavaTestFile.create(code))
                .issues(ISSUE_INJECTING_SUPER_TYPE)
                .run()
                .expectErrorCount(1)
    }

    @Test
    fun validation_withMissingConcreteTypeAndNoInjectedField_isOk() {
        val code = """
            package test;

            public abstract class ParentClass {

                public static class SubClass extends ParentClass {

                    Object someInjection;

                    public void test() {
                        Component component = new Component() {
                            @Override
                            public void inject(ParentClass o) {
                            }
                        };
                        component.inject(this);
                    }
                }

                interface Component {
                    void inject(ParentClass o);
                }
            }
        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.JavaTestFile.create(code))
                .issues(ISSUE_INJECTING_SUPER_TYPE)
                .run()
                .expectClean()
    }

    @Test
    fun validation_withDefinedConcreteType_isOk() {
        val code = """
            package test;

            public abstract class ParentClass {

                public static class SubClass extends ParentClass {

                    public void test() {
                        Component component = new Component() {
                            @Override
                            public void inject(SubClass o) {
                            }
                        };
                        component.inject(this);
                    }
                }

                interface Component {
                    void inject(SubClass o);
                }
            }
        """.trimIndent()

        TestLintTask.lint()
                .files(TestFile.JavaTestFile.create(code))
                .issues(ISSUE_INJECTING_SUPER_TYPE)
                .run()
                .expectClean()
    }
}