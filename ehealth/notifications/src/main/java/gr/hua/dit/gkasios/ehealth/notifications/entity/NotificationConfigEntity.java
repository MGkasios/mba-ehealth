package gr.hua.dit.gkasios.ehealth.notifications.entity;

import java.time.LocalDateTime;

import org.bson.codecs.pojo.annotations.BsonId;

import gr.hua.dit.gkasios.ehealth.notifications.api.dto.NotificationConfig;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@MongoEntity(collection = "NotificationConfig")
public class NotificationConfigEntity extends ReactivePanacheMongoEntityBase {

    @BsonId
    public String afm;
    public Boolean email;
    public LocalDateTime updated;

    public NotificationConfigEntity() {
    }

    public NotificationConfigEntity(final NotificationConfig config) {
        email = config.getEmail();
        updated = LocalDateTime.now();
    }

    public static Uni<NotificationConfigEntity> findByAfm(final String afm) {
        return NotificationConfigEntity.findById(afm);
    }

    public static Uni<NotificationConfigEntity> persistOrUpdate(final String afm, final NotificationConfig config) {
        NotificationConfigEntity configEntity = new NotificationConfigEntity(config);
        configEntity.afm = afm;
        return configEntity.persistOrUpdate();
    }

    public static Multi<NotificationConfigEntity> streamAllNotificationConfig() {
        return streamAll();
    }

}
