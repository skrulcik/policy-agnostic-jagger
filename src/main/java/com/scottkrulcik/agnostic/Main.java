package com.scottkrulcik.agnostic;

import com.scottkrulcik.agnostic.examples.guests.GuestListDemo;
import com.scottkrulcik.agnostic.examples.history.SearchHistoryDemo;

/**
 * Tests that injection is working properly.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("----------------------------------------");
        System.out.println("Search history demo");
        SearchHistoryDemo.main(new String[0]);

        System.out.println("----------------------------------------");
        System.out.println("Guest list demo");
        GuestListDemo.main(new String[0]);
        System.out.println("----------------------------------------");
        System.out.println("complete!!\n");
    }
}
