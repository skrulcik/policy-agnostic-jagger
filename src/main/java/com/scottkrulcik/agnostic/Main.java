package com.scottkrulcik.agnostic;

import com.scottkrulcik.agnostic.examples.guests.GuestListDemo;
import com.scottkrulcik.agnostic.examples.history.AdComponent;
import com.scottkrulcik.agnostic.examples.history.DaggerAdComponent;

/**
 * Tests that injection is working properly.
 */
public class Main {

    public static void main(String[] args) {
        AdComponent ads = DaggerAdComponent.create();
        GuestListDemo.main(new String[0]);
        System.out.println("complete!!");
    }
}
