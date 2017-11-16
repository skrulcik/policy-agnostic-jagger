package com.scottkrulcik.agnostic.examples.history;

public class SearchHistoryDemo {

    public static void main(String[] args) throws Exception {
        User scott = User.create("Scott");
        scott.history().recordSearch("dagger producers");
        scott.history().recordSearch("cooking turkey");
        scott.history().recordSearch("being a dad");
        User jean = User.create("Jean");
        jean.history().recordSearch("flight to Seattle");

        AdComponent jeanAds = DaggerAdComponent.builder().user(jean).build();
        System.out.println("Jean: " + jeanAds.history().get());

        AdComponent scottAds = DaggerAdComponent.builder().user(scott).build();
        System.out.println("Scott: " + scottAds.history().get());
    }
}
