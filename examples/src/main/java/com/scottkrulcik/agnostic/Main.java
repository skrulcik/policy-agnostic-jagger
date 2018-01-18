package com.scottkrulcik.agnostic;

import com.scottkrulcik.agnostic.annotations.Raw;
import com.scottkrulcik.agnostic.examples.guests.GuestListDemo;
import com.scottkrulcik.agnostic.examples.history.SearchHistoryDemo;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

/**
 * Tests that injection is working properly.
 */
public class Main {

    static class Data {
        private final int x;

        Data(int x) {
            this.x = x;
        }

        @Override
        public String toString() {
            return Integer.toString(x);
        }
    }

    @Module
    static class SanitizerModule {
        @Provides
        Data provideData(@Raw Data rawData) {
            return new Data(rawData.x * 2);
        }
    }

    @Component(modules = SanitizerModule.class)
    interface TestComponent {
        Data data();

        @Component.Builder
        interface Builder {
            @BindsInstance Builder rawData(@Raw Data data);
            TestComponent build();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("----------------------------------------");
        System.out.println("Search history demo");
        SearchHistoryDemo.main(new String[0]);

        System.out.println("----------------------------------------");
        System.out.println("Guest list demo");
        GuestListDemo.main(new String[0]);

        System.out.println("----------------------------------------");
        Data rawData = new Data(11);
        Data d = com.scottkrulcik.agnostic.DaggerMain_TestComponent.builder()
            .rawData(rawData)
            .build()
            .data();
        System.out.println("data=" + d);

        System.out.println("----------------------------------------");
        System.out.println("complete!!\n");

        // Exit prevents production executors from running after main() returns
        System.exit(0);
    }
}
