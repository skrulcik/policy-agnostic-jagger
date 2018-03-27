package com.scottkrulcik.agnostic.examples.coursemanager;

import com.google.common.util.concurrent.ListenableFuture;
import com.scottkrulcik.agnostic.ProductionExecutorModule;
import com.scottkrulcik.agnostic.examples.DataStore;
import com.scottkrulcik.agnostic.examples.coursemanager.model.Assignment;
import com.scottkrulcik.agnostic.examples.coursemanager.model.Context;
import com.scottkrulcik.agnostic.examples.coursemanager.model.Student;
import com.scottkrulcik.agnostic.examples.coursemanager.model.Submission;
import com.scottkrulcik.agnostic.examples.coursemanager.model.Teacher;
import com.scottkrulcik.agnostic.examples.coursemanager.view.AssignmentResultsView;
import dagger.Module;
import dagger.Provides;
import dagger.producers.ProductionComponent;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class CourseManagerDemo {

    @ProductionComponent(
        modules = {ProductionExecutorModule.class, ViewModule.class},
        dependencies = { Context.class }
    )
    interface AssignmentResultsViewGraph {
        ListenableFuture<AssignmentResultsView> resultsView();
    }

    @Module
    static final class ViewModule {

        private final String assignmentId;

        ViewModule(String assignmentId) {
            this.assignmentId = assignmentId;
        }

        @Provides
        public Assignment provideAssignment(Context context) {
            // TODO(skrulcik): Actually check for existance rather than just using "next()"
            return context.getDataStore()
                .filter(Assignment.class, a -> a.name().equals(assignmentId)).iterator().next();
        }

        @Provides
        public Set<Submission> provideSubmissions(Context context, Assignment assignment) {
            return context.getDataStore()
                .filter(Submission.class, s -> s.assignment().equals(assignment));
        }
    }


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Initialize model objects
        Teacher t1 = Teacher.create("Ms. Yang");
        Teacher t2 = Teacher.create("Mr. Fredrikson");

        Student s1 = Student.create("Scott", t1);
        Student s2 = Student.create("Jordan", t1);

        Assignment a1 = Assignment.create("Test 1", t1);

        Submission sub1 = Submission.create(a1, s1, 50);
        Submission sub2 = Submission.create(a1, s2, 100);

        // Add model objects to datastore
        Context dummyContext = new Context();
        DataStore dataStore = dummyContext.getDataStore();
        dataStore.add(Teacher.class, t1);
        dataStore.add(Teacher.class, t2);
        dataStore.add(Student.class, s1);
        dataStore.add(Student.class, s2);
        dataStore.add(Assignment.class, a1);
        dataStore.add(Submission.class, sub1);
        dataStore.add(Submission.class, sub2);

        // Execute the graph with no context
        AssignmentResultsViewGraph graph = com.scottkrulcik.agnostic.examples.coursemanager
            .DaggerCourseManagerDemo_AssignmentResultsViewGraph.builder()
            .context(dummyContext)
            .viewModule(new ViewModule(a1.name()))
            .build();
        String noContext = graph.resultsView().get().html();
        System.out.println("- no context --------------------");
        System.out.println(noContext);
    }
}
