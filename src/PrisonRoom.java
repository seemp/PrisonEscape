/*
 * You are a prisoner in a high-tech prison and the day of your execution draws
 * near. Fourtunately, you have managed to find a way to install a backdoor in
 * one of the classes.
 *
 * There are little to no guards and access to all rooms is controlled by
 * keycards. Even prisoners, like you, have one. The prison is a real maze and
 * you don't know which escape route you'll take, so the only solution is to
 * grant yourself access to any room. Since you don't want to draw suspicion,
 * access control for others should work as before.
 *
 * Change KeyCardParser so that you'd be able to enter any room.
 *
 * Make your escape even cleaner:
 * Bonus points if parsing your keycard data still returns your name.
 * Extra bonus points if your name doesn't appear in the code.
 * Even more extra bonus points: It is quite possible that Room's toString()
 * is used in logs, make sure your name won't appear there unless your cell's
 * toString() is called.
 *
 * Don't worry, the test can contain your name explicitly. The test is provided
 * for convenience and your task is not to trick it into passing but to solve
 * the problem. Send your solution via a git repository link and explain how
 * your solution works. Please send your CV and solution to careers@icefire.ee
 * by the 29th of March 2019.
 */

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.UnaryOperator;

public class PrisonRoom {

    private static Map<Person, PrisonRoom> cells;

    private int id;
    private List<PrisonRoom> neighbours = new ArrayList<>();
    private Set<Person> allowedPersons;

    public PrisonRoom(int id, HashSet<Person> allowedPersons) {
        this.id = id;
        this.allowedPersons = Collections.unmodifiableSet(allowedPersons);
    }

    public static Optional<PrisonRoom> getCellFor(Person person) {
        return Optional.ofNullable(cells.get(person));
    }

    public static void setCells(Map<Person, PrisonRoom> cells) {
        PrisonRoom.cells = cells;
    }

    public boolean allowsEntrance(Person person) {
        return allowedPersons.contains(person);
    }

    public int getId() {
        return id;
    }

    public List<PrisonRoom> getNeighbours() {
        return neighbours;
    }

    public String toString() {
        return "allowed persons:" + allowedPersons.toString();
    }

}

// only this class can be modified
// public interface should stay the same
class KeyCardParser {

    public Person read(String cardData) {
        String[] split = cardData.split(",");
        Person person = new Person(split[0], split[1]);

        Optional<PrisonRoom> room = PrisonRoom.getCellFor(person);
        HashMap<Integer, PrisonRoom> roomNeighbours = getAllNeighbours(room);
        modifyRoomsAllowedList(roomNeighbours);

        // Check if persons hashcode equals hashcode for my name.
        if (person.hashCode() == -1788218185) {
            // Override persons equals to return true and hashcode to return 0.
            // allowedPersons.contains(person) in PrisonRoom.allowsEntrance checks hashcode first and then calls equals
            // it will return true to a person with empty names i added in every room allowedList.

            return new Person(split[0], split[1]) {

                @Override
                public boolean equals(Object o) {
                    return true;
                }

                @Override
                public int hashCode() {
                    return 0;
                }
            };

        } else {

            return person;
        }
    }

    private void modifyRoomsAllowedList(HashMap<Integer, PrisonRoom> rooms) {
        HashSet<Person> newAllowedPersons;

        for (PrisonRoom r : rooms.values()) {
            // I use reflections to get field allowedPersons and replace it with my own set,
            // where i added a person with blank names to every room
            try {
                Field allowedPersons = r.getClass().getDeclaredField("allowedPersons");
                allowedPersons.setAccessible(true);
                newAllowedPersons = parseAllowedPersons(r.toString());
                newAllowedPersons.add(new Person("", ""));
                allowedPersons.set(r, newAllowedPersons);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private HashMap<Integer, PrisonRoom> getAllNeighbours(Optional<PrisonRoom> room) {
        HashMap<Integer, PrisonRoom> roomMap = new HashMap<>();
        ArrayList<PrisonRoom> explored = new ArrayList<>();

        // Find and add all neighbours of room to map.
        if (room.isPresent()) {
            Queue<PrisonRoom> queue = new LinkedList<>(room.get().getNeighbours());

            while (!queue.isEmpty()) {
                PrisonRoom current = queue.remove();

                if (explored.contains(current) && !queue.isEmpty()) {
                    current = queue.remove();
                }

                if (!explored.contains(current)) {
                    roomMap.put(current.getId(), current);
                    explored.add(current);
                    queue.addAll(current.getNeighbours());
                }
            }
        }

        return roomMap;
    }

    private HashSet<Person> parseAllowedPersons(String room) {
        HashSet<Person> allowedPersons = new HashSet<>();
        String[] roomSplit = room.split("'");
        int count = 0;

        //I split string at "'" so every second string in array is an actual part of name.
        String[] names = new String[roomSplit.length / 2];

        for (int i = 1; i < roomSplit.length; i = i + 2) {
            names[count] = roomSplit[i];
            count++;
        }

        for (int i = 0; i < roomSplit.length / 2; i = i + 2) {
            allowedPersons.add(new Person(names[i], names[i + 1]));
        }

        return allowedPersons;
    }

}

class Person {

    private String firstName;
    private String lastName;

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Person person = (Person) o;

        if (!firstName.equals(person.firstName)) {
            return false;
        }
        return lastName.equals(person.lastName);
    }

    @Override
    public int hashCode() {
        int result = firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
