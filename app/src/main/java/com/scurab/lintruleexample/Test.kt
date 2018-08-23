package com.scurab.lintruleexample

abstract class Base {
    abstract fun inject()
}

open class SubClass : Base() {
    override fun inject() {

    }
}

class MoreSubclass : SubClass() {

    override fun inject() {
        super.inject()
    }
}

open class A

class Test : A() {
    init {
        val d = object : Dagger {
            override fun inject(a: A) {}
        }
        d.inject(this)
    }
}

interface Dagger {
    fun inject(a: A)
}