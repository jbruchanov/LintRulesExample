package com.example.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.*
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS

val ISSUE_INJECTION_CALLING_SUPER = Issue.create("InjectionCallingSuper",
        "Calling super.inject() is resources wasteful",
        "Avoid injection on multiple levels of inheritance, it can create a lot of objects" +
                "which will be again reassigned with latest injection",
        CORRECTNESS,
        5,
        Severity.ERROR,
        Implementation(InjectionCallingSuperDetector::class.java, Scope.JAVA_FILE_SCOPE)
)

val ISSUE_INJECTING_SUPER_TYPE = Issue.create("InjectingSuperType",
        "Invalid type injection.",
        "Used method for injection takes super type as an argument. Dagger then injects only" +
                "fields known for this type and own parents.",
        CORRECTNESS,
        5,
        Severity.ERROR,
        Implementation(InjectingSuperTypeDetector::class.java, Scope.JAVA_FILE_SCOPE)
)


class CustomIssueRegistry : IssueRegistry() {
    override val issues: List<Issue>
        get() = listOf(ISSUE_INJECTION_CALLING_SUPER, ISSUE_INJECTING_SUPER_TYPE)

    override val api: Int
        get() = CURRENT_API
}