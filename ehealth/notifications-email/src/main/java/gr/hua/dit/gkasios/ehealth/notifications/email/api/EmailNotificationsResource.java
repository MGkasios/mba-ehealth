package gr.hua.dit.gkasios.ehealth.notifications.email.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import gr.hua.dit.gkasios.ehealth.notifications.api.dto.Notification;
import gr.hua.dit.gkasios.ehealth.users.api.dto.User;
import io.dapr.client.domain.HttpExtension;
import io.dapr.client.domain.InvokeMethodRequest;
import io.dapr.utils.TypeRef;
import io.quarkiverse.dapr.core.SyncDaprClient;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;

@Path("/email")
@ApplicationScoped
public class EmailNotificationsResource {

    @Inject
    SyncDaprClient dapr;

    @Inject
    ReactiveMailer reactiveMailer;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/notification")
    public Uni<Boolean> sendNotification(final Notification notification) {
        final User fromUser = getUser(notification.getFrom());
        final User toUser = getUser(notification.getTo());

        return reactiveMailer.send(
                Mail.withText(
                        toUser.getEmail(),
                        getSubject(notification.getType()),
                        getBody(notification.getType(), fromUser, toUser)))
                .map(t -> true);
    }

    private User getUser(final String afm) {
        InvokeMethodRequest request = new InvokeMethodRequest("users", "user/" + afm);
        request.setHttpExtension(HttpExtension.GET);
        return dapr.invokeMethod(request, new TypeRef<User>() {
        });
    }

    // Presentation Only. This can become dynamic with Qute Templating Engine and be fetched from a db
    private String getSubject(final Notification.Type type) {
        switch (type) {
            case EXAM_UPLOADED:
                return "Νέες Εξετάσεις";
            case PERMISSIONS_REQUEST:
                return "Αίτημα Πρόσβασεις στον ιατρικό σας φάκελο";
            case PERMISSIONS_REPLY:
                return "Απάντηση στο αίτημα πρόσβασης";
        }
        return "";
    }

    private static final String EXAM_UPLOADED_BODY = "Το διαγνωστικό εργαστήριο %s (%s) ανέβασε τις εξετάσεις σας.";

    private static final String PERMISSIONS_REQUEST_BODY = "Ο χρήστης %s %s (%s) ζήτησε πρόσβαση στον ιατρικό σας φάκελο.";

    private static final String PERMISSIONS_REPLY_BODY = "Ο χρήστης %s %s (%s) απάντησε στο αίτημα πρόσβαση σας στον ιατρικό του φάκελο.";

    private String getBody(final Notification.Type type, final User fromUser, final User toUser) {
        switch (type) {
            case EXAM_UPLOADED:
                return String.format(EXAM_UPLOADED_BODY, fromUser.getName(), fromUser.getAfm());
            case PERMISSIONS_REQUEST:
                return String.format(PERMISSIONS_REQUEST_BODY,
                        fromUser.getName(), fromUser.getLastname(), fromUser.getAfm());
            case PERMISSIONS_REPLY:
                return String.format(PERMISSIONS_REPLY_BODY,
                        fromUser.getName(), fromUser.getLastname(), fromUser.getAfm());
        }
        return "";
    }

}
