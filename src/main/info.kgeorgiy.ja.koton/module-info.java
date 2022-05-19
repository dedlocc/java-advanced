module info.kgeorgiy.ja.koton {
    requires java.compiler;
    requires java.rmi;
    requires jdk.httpserver;
    requires junit;

    requires info.kgeorgiy.java.advanced.walk;
    requires info.kgeorgiy.java.advanced.arrayset;
    requires info.kgeorgiy.java.advanced.student;
    requires info.kgeorgiy.java.advanced.implementor;
    requires info.kgeorgiy.java.advanced.concurrent;
    requires info.kgeorgiy.java.advanced.mapper;
    requires info.kgeorgiy.java.advanced.crawler;
    requires info.kgeorgiy.java.advanced.hello;

    opens info.kgeorgiy.ja.koton.bank;
    opens info.kgeorgiy.ja.koton.bank.account;
    opens info.kgeorgiy.ja.koton.bank.bank;
    opens info.kgeorgiy.ja.koton.bank.person;
}
