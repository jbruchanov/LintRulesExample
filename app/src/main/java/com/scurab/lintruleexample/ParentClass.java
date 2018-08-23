package com.scurab.lintruleexample;

import javax.inject.Inject;

public abstract class ParentClass {

    public static class SubClass extends ParentClass {

        @Inject
        Object ojd;

        public SubClass() {
            Component component = getComponent();
            component.inject(this);
        }

        Component getComponent() {
            return new Component() {
                @Override
                public void inject(ParentClass o) {

                }
            };
        }
    }

    interface Component {
        void inject(ParentClass o);
    }
}