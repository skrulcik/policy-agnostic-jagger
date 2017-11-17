package com.scottkrulcik.agnostic.examples.coursemanager;

import com.google.common.util.concurrent.ListenableFuture;
import com.scottkrulcik.agnostic.ProductionExecutorModule;
import com.scottkrulcik.agnostic.examples.coursemanager.model.Assignment;
import com.scottkrulcik.agnostic.examples.coursemanager.model.Context;
import com.scottkrulcik.agnostic.examples.coursemanager.model.Teacher;
import com.scottkrulcik.agnostic.examples.coursemanager.view.AssignmentResultsView;
import com.scottkrulcik.agnostic.examples.coursemanager.view.SubmissionView;
import dagger.Module;
import dagger.Provides;
import dagger.producers.ProductionComponent;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CourseManagerDemo {

    @ProductionComponent(
        modules = {ProductionExecutorModule.class, ViewModule.class},
        dependencies = { Context.class, }
    )
    interface AssignmentResultsViewGraph {
        ListenableFuture<AssignmentResultsView> resultsView();
    }

    @Module
    public static final class ViewModule {
        @Provides
        public Assignment provideAssignment(Context context) {
            // TODO use request data
            return Assignment.create("A name!", Teacher.create("Ms. Yang"));
        }

        @Provides
        public List<SubmissionView> provideSubmissionViews() {
            return Collections.emptyList();
        }
    }


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Context noContext = new Context();

        AssignmentResultsViewGraph graph = com.scottkrulcik.agnostic.examples.coursemanager
            .DaggerCourseManagerDemo_AssignmentResultsViewGraph
            .builder().context(noContext).viewModule(new ViewModule()).build();

        System.out.println(graph.resultsView().get().html());
    }
}
