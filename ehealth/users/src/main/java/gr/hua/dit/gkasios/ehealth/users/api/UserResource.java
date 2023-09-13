package gr.hua.dit.gkasios.ehealth.users.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.jwt.JsonWebToken;

import gr.hua.dit.gkasios.ehealth.users.api.dto.User;
import gr.hua.dit.gkasios.ehealth.users.entity.UserEntity;
import io.quarkiverse.dapr.core.SyncDaprClient;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/user")
@RequestScoped
public class UserResource {

    @Inject
    SyncDaprClient dapr;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken tokenMicroProfile;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<User> addCurrentUser(User user) {
        user.setAfm(getAfm());
        return UserEntity.createUser(user).map(UserResource::convert);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<User> getCurrentUser() {
        return UserEntity.findByAfm(getAfm()).map(UserResource::convert);
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<User> getAllUsers() {
        return UserEntity.streamAllUser().map(UserResource::convert);
    }

    @GET
    @Path("/{afm}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<User> getUser(@PathParam("afm") String afm) {
        return UserEntity.findByAfm(afm).map(UserResource::convert);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<User> updateCurrentUser(User user) {
        final String afm = getAfm();
        user.setAfm(afm);
        return UserEntity.updateUser(afm, user).map(UserResource::convert);
    }

    private String getAfm() {
        return tokenMicroProfile.getClaim("afm");
    }

    private static User convert(UserEntity userEntity) {
        if (userEntity == null)
            return new User();
        return new User(userEntity.afm, userEntity.name, userEntity.lastname, userEntity.email);
    }

}
