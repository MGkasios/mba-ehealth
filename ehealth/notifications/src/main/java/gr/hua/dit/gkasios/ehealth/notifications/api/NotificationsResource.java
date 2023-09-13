package gr.hua.dit.gkasios.ehealth.notifications.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;

import gr.hua.dit.gkasios.ehealth.exams.api.dto.Exam;
import gr.hua.dit.gkasios.ehealth.exams.api.dto.ExamMeta;
import gr.hua.dit.gkasios.ehealth.notifications.api.dto.Notification;
import gr.hua.dit.gkasios.ehealth.notifications.api.dto.NotificationConfig;
import gr.hua.dit.gkasios.ehealth.notifications.entity.NotificationConfigEntity;
import gr.hua.dit.gkasios.ehealth.permissions.api.dto.Permission;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import io.dapr.client.domain.HttpExtension;
import io.dapr.client.domain.InvokeMethodRequest;
import io.dapr.utils.TypeRef;
import io.quarkiverse.dapr.core.SyncDaprClient;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

@Path("/notifications")
@RequestScoped
public class NotificationsResource {

    @Inject
    SyncDaprClient dapr;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken tokenMicroProfile;

    @POST
    @Path("/config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<NotificationConfig> addOrUpdateConfig(final NotificationConfig config) {
        return NotificationConfigEntity.persistOrUpdate(getAfm(), config).map(NotificationsResource::convert);
    }

    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<NotificationConfig> getConfig() {
        return NotificationConfigEntity.findByAfm(getAfm()).map(NotificationsResource::convert);
    }

    @POST
    @Path("/exam")
    @Topic(name = "exam")
    public void examEvent(CloudEvent<Exam> event) {
        final Exam exam = event.getData();
        final ExamMeta meta = exam.getExamMeta();
        sendNotification(
                meta.getUploaderAfm(),
                meta.getPatientAfm(),
                Notification.Type.EXAM_UPLOADED);
    }

    @POST
    @Path("/permission")
    @Topic(name = "permission")
    public void permissionsEvent(CloudEvent<Permission> event) {
        Permission data = event.getData();

        if (Permission.Status.PENDING.equals(data.getStatus())) {
            sendNotification(
                    data.getUserAfm(),
                    data.getPatientAfm(),
                    Notification.Type.PERMISSIONS_REQUEST);
        } else {
            sendNotification(
                    data.getPatientAfm(),
                    data.getUserAfm(),
                    Notification.Type.PERMISSIONS_REPLY);
        }
    }

    private void sendNotification(String from, String to, Notification.Type type) {
        NotificationConfigEntity
                .findByAfm(to)
                .subscribe()
                .with(config -> {
                    if (config != null && config.email) {
                        InvokeMethodRequest request = new InvokeMethodRequest("notifications-email", "email/notification");
                        request.setHttpExtension(HttpExtension.POST);
                        request.setBody(new Notification(from, to, type));
                        dapr.invokeMethod(request, new TypeRef<Boolean>() {
                        });
                    }
                });
    }

    private String getAfm() {
        return tokenMicroProfile.getClaim("afm");
    }

    private static NotificationConfig convert(NotificationConfigEntity notificationConfig) {
        if (notificationConfig == null)
            return new NotificationConfig();
        return new NotificationConfig(notificationConfig.email);
    }

}
