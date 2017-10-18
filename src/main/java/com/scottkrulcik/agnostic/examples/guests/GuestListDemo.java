package com.scottkrulcik.agnostic.examples.guests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.scottkrulcik.agnostic.Policy;
import com.scottkrulcik.agnostic.ViewingContext;
import java.util.List;

public final class GuestListDemo {

    public static void main(String[] args) {
        // Create people and events
        Person scott = Person.create("Scott");
        Person serena = Person.create("Serena");
        Person jordan = Person.create("Jordan");
        Person jean = Person.create("Jean");

        ImmutableList<Person> conspirators = ImmutableList.of(serena, jordan);
        Event cookieMaking = new Event("Cookie Making", scott, conspirators);

        // Define the policy
        Policy policy = new Policy();
        policy.addRestriction(Event.staticToken(), (context, event) -> {
            Person viewer = context.get(Person.class);
            return event.owner().equals(viewer) || event.guestList().contains(viewer);
        });

        for (Person person : ImmutableList.of(scott, serena, jordan, jean)) {
            printVisibility(policy, person, cookieMaking);
        }
    }

    private static void printVisibility(Policy policy, Person person, Event event) {
        ViewingContext userContext = new ViewingContext(ImmutableMap.of(Person.class, person));
        List<Person> visibleList = policy.concretize(userContext, event).guestList();
        System.out.println(person.name() + " can see guests " + visibleList);
    }
}
