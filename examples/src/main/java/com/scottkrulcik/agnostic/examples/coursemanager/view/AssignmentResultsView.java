package com.scottkrulcik.agnostic.examples.coursemanager.view;

import com.scottkrulcik.agnostic.examples.coursemanager.model.Assignment;
import java.util.List;
import javax.inject.Inject;

public final class AssignmentResultsView {
        private final Assignment assignment;
        private final List<SubmissionView> submissionViews;

        @Inject
        AssignmentResultsView(
            Assignment assignment,
            List<SubmissionView> submissionViews) {

            this.assignment = assignment;
            this.submissionViews = submissionViews;
        }

        public String html() {
            StringBuilder html = new StringBuilder("<head><title>");
            html.append(assignment.name());
            html.append("</title></head><body><h1>");
            html.append(assignment.name());
            html.append("</h1><table>");
            for (SubmissionView submissionView : submissionViews) {
                html.append(submissionView);
            }
            html.append("</table></body>");
            return html.toString();
        }
    }
