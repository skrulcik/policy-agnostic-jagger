package com.scottkrulcik.agnostic.examples.medical;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.scottkrulcik.agnostic.examples.medical.Model.Person;
import com.scottkrulcik.agnostic.examples.medical.sampledata.GeneratedDataSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.alice;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.bob;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.docC;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.docD;
import static com.scottkrulcik.agnostic.examples.medical.sampledata.DataSet1_TestAccess.docE;
import static java.lang.String.join;
import static java.util.Arrays.stream;

/**
 * Tightly coupled with {@code generate_tests.py}, consider changes carefully.
 */
public final class EvaluationTimer {
    private static final int NUM_RUNS = 200;

    private static Set<Person> patients = GeneratedDataSet.INSTANCE.people().stream().filter(p -> p.name().contains("p")).collect(Collectors.toSet());
    private static Set<Person> doctors = GeneratedDataSet.INSTANCE.people().stream().filter(p -> p.name().contains("d")).collect(Collectors.toSet());
    private static Set<String> conditions = GeneratedDataSet.INSTANCE.records().stream().map(r -> r.condition()).collect(Collectors.toSet());
    // p0 is guaranteed not to have pysch records
    private static Person NO_PSYCH = Person.create("p0");
    private static Person DOC = doctors.iterator().next();

    private static final Path TRIAL_DIR;
    static {
        String trialId = new SimpleDateFormat("HH-mm-ss").format(new Date());
        TRIAL_DIR = Paths.get(System.getProperty("user.home"), "tmp", trialId);
    }

    // Test specifications to run
    private static final ImmutableMap<String, Consumer<HealthService>> specifications =
        ImmutableMap.<String,Consumer<HealthService>>builder()
            .put("no_psych", service -> {
                service.medicalHistory(docC, NO_PSYCH);
            })
            .put("consent_author", service -> {
                service.medicalHistory(docD, alice).history();
            })
            .put("consent_form", service -> {
                service.medicalHistory(docC, bob).history();
            })
            .put("no_consent", service -> {
                // Cannot check state because of the naive implementation
                service.medicalHistory(docE, alice).history();
            })
            .put("simple_search", service -> {
                service.patientsWithCondition(docC, "c1").patients();
            })
            .put("psych_search", service -> {
                service.patientsWithCondition(DOC, "pc1").patients();
            }
        ).build();

    private static final ImmutableList<ServiceDef> services;
    static {
        DataStoreModule generatedData = new DataStoreModule(GeneratedDataSet.INSTANCE);
        services =
            ImmutableList.of(
                new ServiceDef("adhoc", DaggerRecordService_AdHoc.builder()
                    .dataStoreModule(generatedData)
                    .build().healthService()),
                new ServiceDef("jagger", DaggerRecordService_Jagger.builder()
                    .dataStoreModule(generatedData)
                    .build().healthService()),
                new ServiceDef("naive", DaggerRecordService_Naive.builder()
                    .dataStoreModule(generatedData)
                    .build().healthService())
            );
    }


    public static void main(String[] args) throws IOException {
        // Dear Reader,
        // Sorry
        // - Scott
        Files.createDirectory(TRIAL_DIR);
        for (String specName : specifications.keySet()) {
            System.out.printf("%20s........", specName);
            String[][] results = new String[NUM_RUNS][services.size()];
            for (int run = 0; run < NUM_RUNS; run++) {
                for (int i = 0; i < services.size(); i++) {
                    final int snapshot = i;
                    results[run][i] = Long.toString(time(() -> specifications.get(specName).accept(services.get(snapshot).service)));
                }
            }
            List<String> lines = new ArrayList<>();
            lines.add(join(",", services.stream().map(s -> s.name).collect(Collectors.toList())));
            lines.addAll(stream(results).map(run -> join(",", Arrays.asList(run))).collect(Collectors.toList()));

            Path filePath = TRIAL_DIR.resolve(specName + ".csv");
            Files.write(filePath, lines);
            System.out.println("DONE");
        }
    }

    private static long time(Runnable r) {
        long before = System.nanoTime();
        r.run();
        long after = System.nanoTime();
        return after - before;
    }

    private static final class ServiceDef {
        final String name;
        final HealthService service;

        private ServiceDef(String name, HealthService service) {
            this.name = name;
            this.service = service;
        }
    }

}
