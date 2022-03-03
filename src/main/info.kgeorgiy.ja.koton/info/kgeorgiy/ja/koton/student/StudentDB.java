package info.kgeorgiy.ja.koton.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements GroupQuery {
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return map(students, Student::getFirstName).toList();
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return map(students, Student::getLastName).toList();
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return map(students, Student::getGroup).toList();
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return map(students, StudentDB::getFullName).toList();
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return map(students, Student::getFirstName).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().max(Comparator.naturalOrder()).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream().sorted().toList();
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream().sorted(STUDENTS_BY_NAME).toList();
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filter(students, Student::getFirstName, name).toList();
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filter(students, Student::getLastName, name).toList();
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return filterByGroup(students, group).toList();
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return filterByGroup(students, group).collect(Collectors.toMap(
            Student::getLastName,
            Student::getFirstName,
            BinaryOperator.minBy(Comparator.naturalOrder())
        ));
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getSortedGroups(students.stream().sorted(STUDENTS_BY_NAME)).toList();
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getSortedGroups(students.stream().sorted()).toList();
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getLargestGroup(students, List::size, false);
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroup(students, l -> getDistinctFirstNames(l).size(), true);
    }

    // private static

    private static final Comparator<Student> STUDENTS_BY_NAME = Comparator
        .comparing(Student::getLastName)
        .thenComparing(Student::getFirstName)
        .reversed()
        .thenComparingInt(Student::getId);

    private static final Comparator<Group> GROUPS_BY_NAME = Comparator.comparing(Group::getName);

    private static String getFullName(Student student) {
        return String.format("%s %s", student.getFirstName(), student.getLastName());
    }

    // private

    private <T> Stream<T> map(Collection<Student> students, Function<Student, T> map) {
        return students.stream().map(map);
    }

    private <T> Stream<Student> filter(Collection<Student> students, Function<Student, T> extractor, T value) {
        return students.stream().filter(s -> extractor.apply(s).equals(value)).sorted(STUDENTS_BY_NAME);
    }

    private Stream<Student> filterByGroup(Collection<Student> students, GroupName group) {
        return filter(students, Student::getGroup, group);
    }

    private Stream<Group> getGroups(Stream<Student> students) {
        return students
            .collect(Collectors.groupingBy(Student::getGroup))
            .entrySet()
            .stream()
            .map(e -> new Group(e.getKey(), e.getValue()));
    }

    private Stream<Group> getSortedGroups(Stream<Student> students) {
        return getGroups(students).sorted(GROUPS_BY_NAME);
    }

    private GroupName getLargestGroup(Collection<Student> students, Comparator<Group> comp) {
        return getGroups(students.stream())
            .max(comp)
            .map(Group::getName)
            .orElse(null);
    }

    private GroupName getLargestGroup(Collection<Student> students, ToIntFunction<List<Student>> toInt, boolean reversed) {
        return getLargestGroup(
            students,
            Comparator.comparing((Group g) -> toInt.applyAsInt(g.getStudents()))
                .thenComparing(reversed ? GROUPS_BY_NAME.reversed() : GROUPS_BY_NAME)
        );
    }
}
