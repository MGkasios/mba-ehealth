package gr.hua.dit.gkasios.ehealth.permissions.entity;

import java.time.LocalDateTime;

import jakarta.ws.rs.NotFoundException;

import org.bson.types.ObjectId;

import gr.hua.dit.gkasios.ehealth.permissions.api.dto.Permission;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@MongoEntity(collection = "Permissions")
public class PermissionEntity extends ReactivePanacheMongoEntity {

    public String patientAfm;
    public String userAfm;
    public Permission.Status status;
    public LocalDateTime created;
    public LocalDateTime updated;

    public PermissionEntity() {
    }

    public PermissionEntity(
            String patientAfm,
            String userAfm,
            Permission.Status status,
            LocalDateTime created,
            LocalDateTime updated) {
        this.patientAfm = patientAfm;
        this.userAfm = userAfm;
        this.status = status;
        this.created = created;
        this.updated = updated;
    }

    public static PermissionEntity requestPermission(final String patientAfm, final String userAfm) {
        return new PermissionEntity(
                patientAfm,
                userAfm,
                Permission.Status.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    public static Uni<PermissionEntity> findByAfm(final String afm) {
        return PermissionEntity.findById(afm);
    }

    public static Uni<PermissionEntity> updatePermissionState(final String id, final String afm,
            final Permission.Status status) {
        Uni<PermissionEntity> permissionUni = PermissionEntity.find("{'_id': ?1,'patientAfm': ?2}", new ObjectId(id), afm)
                .firstResult();
        System.out.println("test");
        return permissionUni
                .onItem().transform(permission -> {
                    if (permission == null)
                        throw new NotFoundException();
                    permission.status = status;
                    permission.updated = LocalDateTime.now();
                    return permission;
                }).call(post -> post.persistOrUpdate());
    }

    public static Multi<PermissionEntity> streamAllNotificationConfig() {
        return streamAll();
    }

}
