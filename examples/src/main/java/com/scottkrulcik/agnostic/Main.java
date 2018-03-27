package com.scottkrulcik.agnostic;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.scottkrulcik.agnostic.annotations.Raw;
import com.scottkrulcik.agnostic.examples.guests.GuestListDemo;
import com.scottkrulcik.agnostic.examples.history.SearchHistoryDemo;
import com.scottkrulcik.agnostic.examples.medical.MedicalExample;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.util.Collection;

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
        System.out.println("Medical records demo");
        MedicalExample.main(new String[0]);

        System.out.println("----------------------------------------");
        System.out.println("Data Filter demo");
        Data rawData = new Data(11);
        Data d = com.scottkrulcik.agnostic.DaggerMain_TestComponent.builder()
            .rawData(rawData)
            .build()
            .data();
        System.out.println("data=" + d);


        System.out.println("----------------------------------------");
        System.out.println("Map experiment");
        Multimap<String, String> deps = ArrayListMultimap.create();
        deps.put("A", "B");
        deps.put("B", "C");
        deps.put("C", "A");

        MutableGraph<String> g = GraphBuilder.directed()
            .allowsSelfLoops(true)
            .build();
        for (String key : deps.keySet()) {
            Collection<String> dependencies = deps.get(key);
            // TODO(skrulcik): Self-loop optimization
            for (String dependency : dependencies) {
                g.putEdge(key, dependency);
            }
        }

        Graph<String> gTC = Graphs.transitiveClosure(g);
        System.out.println(gTC);


        // Exit prevents production executors from running after main() returns
        System.out.println("----------------------------------------");
        System.out.println("complete!!\n");
        System.exit(0);
    }
}
