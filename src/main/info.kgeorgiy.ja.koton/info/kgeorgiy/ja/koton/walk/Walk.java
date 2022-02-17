package info.kgeorgiy.ja.koton.walk;

import info.kgeorgiy.ja.koton.walk.visitor.NoDirsHashFileVisitor;

public class Walk {
    public static void main(String[] args) {
        new Walker(NoDirsHashFileVisitor::new).walk(args);
    }
}
