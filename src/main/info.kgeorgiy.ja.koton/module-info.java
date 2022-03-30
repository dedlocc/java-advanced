module info.kgeorgiy.ja.koton {
    requires java.compiler;
    requires junit;

    requires info.kgeorgiy.java.advanced.walk;
    requires info.kgeorgiy.java.advanced.arrayset;
    requires info.kgeorgiy.java.advanced.student;
    requires info.kgeorgiy.java.advanced.implementor;
    requires info.kgeorgiy.java.advanced.concurrent;

    exports info.kgeorgiy.ja.koton.walk;
    exports info.kgeorgiy.ja.koton.walk.visitor;
    exports info.kgeorgiy.ja.koton.arrayset;
    exports info.kgeorgiy.ja.koton.student;
    exports info.kgeorgiy.ja.koton.implementor;
}