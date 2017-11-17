package com.scottkrulcik.agnostic.examples.coursemanager.view;

import com.scottkrulcik.agnostic.examples.coursemanager.model.Submission;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public final class SubmissionView {
        private final Submission submission;
        private DateFormat SUBMITTED_TIME_FORMAT = new SimpleDateFormat("MM/dd 'at' HH:mm");

        SubmissionView(Submission submission) {
            this.submission = submission;
        }

        String toTableRow() {
            StringBuilder html = new StringBuilder("<tr><td>");
            html.append(SUBMITTED_TIME_FORMAT.format(submission.submissionDate()));
            html.append("</td><td>");
            html.append(submission.author());
            html.append("</td><td>");
            html.append(submission.grade());
            html.append("</td></tr>");
            return html.toString();
        }
    }
