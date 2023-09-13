package gr.hua.dit.gkasios.ehealth.permissions.api;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.jwt.JsonWebToken;

import gr.hua.dit.gkasios.ehealth.permissions.api.dto.Permission;
import gr.hua.dit.gkasios.ehealth.permissions.api.dto.PermissionRequest;
import gr.hua.dit.gkasios.ehealth.permissions.entity.PermissionEntity;
import io.quarkiverse.dapr.core.SyncDaprClient;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

@Path("/permission")
@RequestScoped
public class PermissionsResource {

    @Inject
    SyncDaprClient dapr;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken tokenMicroProfile;

    // Get All Permissions Given
    @GET
    @Path("/given")
    public Uni<List<Permission>> getPermissionsGiven(@QueryParam("status") List<Permission.Status> statuses) {
        return ((!statuses.isEmpty()) ? PermissionEntity.find("{'patientAfm': ?1, 'status': {$in: [?2]} }", getAfm(), statuses)
                : PermissionEntity.find("{'patientAfm': ?1}", getAfm()))
                .stream().map(PermissionsResource::convert).collect().asList();
    }

    // Update Permission
    @PUT
    @Path("/{id}")
    public Uni<Permission> updatePermission(@PathParam("id") final String id, @FormParam("status") Permission.Status status) {
        return PermissionEntity
                .updatePermissionState(id, getAfm(), status)
                .map(PermissionsResource::convert)
                .call(permission -> {
                    dapr.publishEvent("permission", permission);
                    return Uni.createFrom().item(permission);
                });
    }

    // Get Permissions Requested
    @GET
    @Path("/request")
    public Uni<List<Permission>> getPermissionsRequest(@QueryParam("status") List<Permission.Status> statuses) {
        return ((!statuses.isEmpty()) ? PermissionEntity.find("{'userAfm': ?1, 'status': {$in: [?2]} }", getAfm(), statuses)
                : PermissionEntity.find("{'userAfm': ?1}", getAfm()))
                .stream().map(permission -> convert((PermissionEntity) permission)).collect().asList();
    }

    // Request Permission
    @POST
    @Path("/request")
    public Uni<Permission> createPermissionsRequest(PermissionRequest permissionRequest) {
        return PermissionEntity
                .requestPermission(permissionRequest.getPatientAfm(), getAfm())
                .persistOrUpdate()
                .map(PermissionsResource::convert)
                .call(permission -> {
                    dapr.publishEvent("permission", permission);
                    return Uni.createFrom().item(permission);
                });
    }

    // Has Permission
    @POST
    @Path("/exists")
    public Uni<Boolean> hasPatientsPermissions(Permission permission) {
        return PermissionEntity
                .find("{'patientAfm': ?1, 'userAfm': ?2, 'status': ?3 }",
                        permission.getPatientAfm(), permission.getUserAfm(), Permission.Status.ACCEPTED)
                .singleResultOptional().map(Optional::isPresent);
    }

    private String getAfm() {
        return tokenMicroProfile.getClaim("afm");
    }

    private static Permission convert(final ReactivePanacheMongoEntityBase permissionEntity) {
        return convert((PermissionEntity) permissionEntity);
    }

    private static Permission convert(final PermissionEntity permissionEntity) {
        if (permissionEntity == null)
            return new Permission();
        return new Permission(
                permissionEntity.id.toHexString(),
                permissionEntity.patientAfm,
                permissionEntity.userAfm,
                permissionEntity.status);
    }

}
