package info.kgeorgiy.ja.koton.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Implementor implements Impler {
    public static void main(String[] args) {
        if (args == null || args.length < 1 || args[0] == null) {
            System.err.println("Usage: java Implementor <class>");
            return;
        }

        try {
            for (String line : (Iterable<String>) new CodeGenerator().generate(Class.forName(args[0]))::iterator) {
                System.out.print(line);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Such class doesn't exist");
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token.isPrimitive() || token == Enum.class || Modifier.isPrivate(token.getModifiers()) || Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Cannot extend nor implement " + token.getSimpleName());
        }
        if (!token.isInterface() && Arrays.stream(token.getDeclaredConstructors()).allMatch(c -> Modifier.isPrivate(c.getModifiers()))) {
            throw new ImplerException("Won't extend an utility class");
        }

        Path dir = root.resolve(token.getPackageName().replace('.', File.separatorChar));
        try {
            Files.createDirectories(dir);
            try (var writer = Files.newBufferedWriter(dir.resolve(getClassName(token) + ".java"))) {
                for (String line : (Iterable<String>) new CodeGenerator().generate(token)::iterator) {
                    writer.write(line);
                }
            }
        } catch (IOException e) {
            throw new ImplerException(e);
        }
    }

    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    private static class CodeGenerator {
        public Stream<String> generate(Class<?> token) {
            return concat(generateHeader(token), NEW_LINE, generateClass(token));
        }

        //

        private Stream<String> generateHeader(Class<?> token) {
            return concat("package ", token.getPackageName(), ";", NEW_LINE);
        }

        private Stream<String> generateClass(Class<?> token) {
            return concat(
                String.format("public class %s %s %s {",
                    getClassName(token),
                    token.isInterface() ? "implements" : "extends",
                    token.getCanonicalName()
                ),
                NEW_LINE,
                generateMethods(token),
                "}"
            );
        }

        private Stream<String> generateMethods(Class<?> token) {
            return concat(
                Arrays.stream(token.getDeclaredConstructors()).flatMap(this::generateConstructor),
                Stream.concat(
                        Arrays.stream(token.getMethods()),
                        Stream.<Class<?>>iterate(token, Objects::nonNull, Class::getSuperclass)
                            .flatMap(clazz -> Arrays.stream(clazz.getDeclaredMethods()))
                    )
                    .map(MethodWrapper::new)
                    .distinct()
                    .map(MethodWrapper::method)
                    .filter(m -> Modifier.isAbstract(m.getModifiers()))
                    .flatMap(this::generateMethod)
            );
        }

        private Stream<String> generateConstructor(Constructor<?> ctor) {
            return concat(
                tab(1),
                String.format("%s %s%s%s {",
                    getModifiers(ctor),
                    getClassName(ctor.getDeclaringClass()),
                    getParameters(ctor),
                    getThrow(ctor)
                ),
                NEW_LINE,
                tab(2) + Arrays.stream(ctor.getParameters())
                    .map(Parameter::getName)
                    .collect(Collectors.joining(",", "super(", ");")),
                NEW_LINE,
                tab(1) + "}",
                NEW_LINE
            );
        }

        private Stream<String> generateMethod(Method method) {
            return concat(
                tab(1),
                String.format("%s %s %s%s%s {",
                    getModifiers(method),
                    method.getReturnType().getCanonicalName(),
                    method.getName(),
                    getParameters(method),
                    getThrow(method)
                ),
                NEW_LINE,
                Optional.of(method.getReturnType())
                    .filter(t -> t != void.class)
                    .map(t -> tab(2) + String.format("return %s;%n", getDefaultValue(t)))
                    .stream(),
                tab(1) + "}",
                NEW_LINE
            );
        }

        private String getModifiers(Executable exec) {
            return Modifier.toString(exec.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT);
        }

        private String getParameters(Executable exec) {
            return Arrays.stream(exec.getParameters())
                .map(p -> p.getType().getCanonicalName() + " " + p.getName())
                .collect(Collectors.joining(",", "(", ")"));
        }

        private String getThrow(Executable exec) {
            if (exec.getExceptionTypes().length == 0) {
                return "";
            }

            return Arrays.stream(exec.getExceptionTypes())
                .map(Class::getName)
                .collect(Collectors.joining(",", " throws ", ""));
        }

        private String getDefaultValue(Class<?> token) {
            if (!token.isPrimitive()) {
                return "null";
            }
            if (token == boolean.class) {
                return "false";
            }
            return "0";
        }

        //

        private static final String NEW_LINE = System.lineSeparator();

        @SuppressWarnings("unchecked")
        private static Stream<String> concat(Object... objects) {
            return Arrays.stream(objects).flatMap(obj -> obj instanceof String s ? Stream.of(s) : (Stream<String>) obj);
        }

        private String tab(int levels) {
            return " ".repeat(levels * 4);
        }

        private record MethodWrapper(Method method) {
            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                MethodWrapper that = (MethodWrapper) o;
                return method.getName().equals(that.method.getName()) && Arrays.equals(method.getParameterTypes(), that.method.getParameterTypes());
            }

            @Override
            public int hashCode() {
                return method.getName().hashCode() + 103 * Arrays.hashCode(method.getParameterTypes());
            }
        }
    }
}
