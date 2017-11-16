package com.scottkrulcik.agnostic.examples.guests;


import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.scottkrulcik.agnostic.Restrictable;
import java.util.List;

final class Event implements Restrictable<Event> {
    private final String name;
    private final Person owner;
    private final ImmutableList<Person> guestList;

    Event(String name, Person owner, List<Person> guestList) {
        this.name = name;
        this.owner = owner;
        this.guestList = ImmutableList.copyOf(guestList);
    }

    public ImmutableList<Person> guestList() {
        return guestList;
    }

    public String name() {
        return name;
    }

    public Person owner() {
        return owner;
    }

    @Override
    public Event defaultValue() {
        return new Event(name, owner, ImmutableList.of());
    }

    private static final Event WORKAROUND = new Event(null, null, ImmutableList.of());

    public static TypeToken<Event> staticToken() {
        return WORKAROUND.token();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Event event = (Event) o;

        if (!name.equals(event.name)) {
            return false;
        }
        if (!owner.equals(event.owner)) {
            return false;
        }
        return guestList.equals(event.guestList);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + owner.hashCode();
        result = 31 * result + guestList.hashCode();
        return result;
    }
}
