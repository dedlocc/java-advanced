package info.kgeorgiy.ja.koton.walk;

import info.kgeorgiy.ja.koton.walk.visitor.HashFileVisitor;

public class RecursiveWalk {
    public static void main(String[] args) {
        new Walker(HashFileVisitor::new).walk(args);
    }
}
