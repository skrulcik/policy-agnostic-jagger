package com.scottkrulcik.agnostic.examples.coursemanager.view;

import com.scottkrulcik.agnostic.examples.coursemanager.model.Assignment;
import com.scottkrulcik.agnostic.examples.coursemanager.model.Submission;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import javax.inject.Inject;

public final class AssignmentResultsView {
    private DateFormat SUBMITTED_TIME_FORMAT = new SimpleDateFormat("MM/dd 'at' HH:mm");

    private final Assignment assignment;
    private final Set<Submission> submissions;

    @Inject
    AssignmentResultsView(
        Assignment assignment,
        Set<Submission> submissions) {

        this.assignment = assignment;
        this.submissions = submissions;
    }

    String toTableRow(Submission submission) {
        StringBuilder html = new StringBuilder("<tr><td>");
        html.append(SUBMITTED_TIME_FORMAT.format(submission.submissionDate()));
        html.append("</td><td>");
        html.append(submission.author().name());
        html.append("</td><td>");
        html.append(submission.grade());
        html.append("</td></tr>");
        return html.toString();
    }

    public String html() {
        StringBuilder html = new StringBuilder("<head><title>");
        html.append(assignment.name());
        html.append("</title></head><body><h1>");
        html.append(assignment.name());
        html.append("</h1><table>");
        for (Submission submission: submissions) {
            html.append(toTableRow(submission));
        }
        html.append("</table></body>");
        return html.toString();
    }
}
