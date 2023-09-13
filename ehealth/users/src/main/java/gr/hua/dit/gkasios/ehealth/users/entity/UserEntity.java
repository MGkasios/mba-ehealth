package gr.hua.dit.gkasios.ehealth.users.entity;

import java.time.LocalDateTime;

import org.bson.codecs.pojo.annotations.BsonId;

import gr.hua.dit.gkasios.ehealth.users.api.dto.User;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@MongoEntity(collection = "Users")
public class UserEntity extends ReactivePanacheMongoEntityBase {

    @BsonId
    public String afm;
    public String name;
    public String lastname;
    public String email;
    public LocalDateTime created;
    public LocalDateTime updated;

    public UserEntity() {
    }

    public UserEntity(String afm, String name, String lastname, String email, LocalDateTime created, LocalDateTime updated) {
        this.afm = afm;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.created = created;
        this.updated = updated;
    }

    public static Uni<UserEntity> findByAfm(final String afm) {
        return UserEntity.findById(afm);
    }

    public static Uni<UserEntity> createUser(final User createUser) {
        return new UserEntity(
                createUser.getAfm(),
                createUser.getName(),
                createUser.getLastname(),
                createUser.getEmail(),
                LocalDateTime.now(),
                LocalDateTime.now()).persist();
    }

    public static Uni<UserEntity> updateUser(final String afm, final User updateUser) {
        Uni<UserEntity> postUni = UserEntity.findById(afm);
        return postUni
                .onItem().transform(user -> {
                    user.name = updateUser.getName();
                    user.lastname = updateUser.getLastname();
                    user.email = updateUser.getEmail();
                    user.updated = LocalDateTime.now();
                    return user;
                }).call(user -> user.persistOrUpdate());
    }

    public static Multi<UserEntity> streamAllUser() {
        return streamAll();
    }

}
