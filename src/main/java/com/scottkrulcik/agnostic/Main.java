package com.scottkrulcik.agnostic;

import com.scottkrulcik.agnostic.examples.history.AdComponent;
import com.scottkrulcik.agnostic.examples.history.DaggerAdComponent;

/**
 * Tests that injection is working properly.
 */
public class Main {

    public static void main(String[] args) {
        AdComponent ads = DaggerAdComponent.create();
        System.out.println("hello world");
    }
}
