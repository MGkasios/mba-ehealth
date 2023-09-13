package gr.hua.dit.gkasios.ehealth.exams.api;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import gr.hua.dit.gkasios.ehealth.exams.api.dto.Exam;
import gr.hua.dit.gkasios.ehealth.exams.api.dto.ExamMeta;
import gr.hua.dit.gkasios.ehealth.exams.api.dto.FileMeta;
import gr.hua.dit.gkasios.ehealth.exams.entity.ExamEntity;
import gr.hua.dit.gkasios.ehealth.permissions.api.dto.Permission;
import io.dapr.client.domain.HttpExtension;
import io.dapr.client.domain.InvokeMethodRequest;
import io.dapr.utils.TypeRef;
import io.quarkiverse.dapr.core.SyncDaprClient;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
@Path("/exam")
@RequestScoped
public class ExamResource extends CommonResource {

    @Inject
    SyncDaprClient dapr;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken tokenMicroProfile;

    @Inject
    S3Client s3;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Uni<Exam> uploadCurrentUserExam(
            @RestForm("file") FileUpload file,
            @RestForm @PartType(MediaType.APPLICATION_JSON) ExamMeta meta) {
        meta.setPatientAfm(getAfm());
        meta.setUploaderAfm(getAfm());
        return ExamEntity
                .create(meta, new FileMeta(file.fileName(), file.contentType(), file.size(), LocalDateTime.now()))
                .persist()
                .map(exam -> {
                    s3.putObject(buildPutRequest((ExamEntity) exam), RequestBody.fromFile(file.uploadedFile()));
                    return (ExamEntity) exam;
                })
                .map(ExamResource::convert);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Uni<Response> fetchCurrentUserExamFile(@PathParam("id") final String id) {
        return ExamEntity
                .find("{'_id': ?1, 'examMeta.patientAfm': ?2}", new ObjectId(id), getAfm())
                .firstResult()
                .map(ExamResource::convert)
                .chain(exam -> {
                    if (exam == null)
                        throw new NotFoundException();
                    ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(buildGetRequest(id));
                    Response.ResponseBuilder response = Response.ok(objectBytes.asByteArray());
                    response.header("Content-Disposition", "attachment;filename=" + id);
                    response.header("Content-Type", objectBytes.response().contentType());
                    return Uni.createFrom().item(response.build());
                });

    }

    @GET
    @Path("{id}/meta")
    public Uni<Exam> fetchCurrentUserExamFileMeta(@PathParam("id") String id) {
        return ExamEntity
                .find("{'_id': ?1, 'examMeta.patientAfm': ?2}", new ObjectId(id), getAfm())
                .firstResult()
                .map(ExamResource::convert);
    }

    @GET
    @Path("search")
    public Uni<List<Exam>> searchCurrentUserExams(@QueryParam("type") List<ExamMeta.Type> types) {
        return ((!types.isEmpty())
                ? ExamEntity.find("{'examMeta.patientAfm': ?1, 'examMeta.type': {$in: [?2]}}", getAfm(), types)
                : ExamEntity.find("{'examMeta.patientAfm': ?1}", getAfm()))
                .stream()
                .map(ExamResource::convert).collect().asList();
    }

    @POST
    @Path("user/{afm}")
    public Uni<Exam> uploadUserExam(
            @PathParam("afm") String afm,
            @RestForm("file") FileUpload file,
            @RestForm @PartType(MediaType.APPLICATION_JSON) ExamMeta meta) {
        if (!tokenMicroProfile.getGroups().contains("lab"))
            throw new UnauthorizedException();
        meta.setPatientAfm(afm);
        meta.setUploaderAfm(getAfm());
        return ExamEntity
                .create(meta, new FileMeta(file.fileName(), file.contentType(), file.size(), LocalDateTime.now()))
                .persist()
                .map(exam -> {
                    s3.putObject(buildPutRequest((ExamEntity) exam), RequestBody.fromFile(file.uploadedFile()));
                    return (ExamEntity) exam;
                })
                .map(ExamResource::convert)
                .call(exam -> {
                    dapr.publishEvent("exam", exam);
                    return Uni.createFrom().item(exam);
                });
    }

    @GET
    @Path("user/{afm}/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Uni<Response> fetchUserExamFile(
            @PathParam("afm") final String afm,
            @PathParam("id") final String id) {
        hasPermissions(afm);
        return ExamEntity
                .find("{'_id': ?1, 'examMeta.patientAfm': ?2}", new ObjectId(id), afm)
                .firstResult()
                .map(ExamResource::convert)
                .chain(exam -> {
                    if (exam == null)
                        throw new NotFoundException();
                    ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(buildGetRequest(id));
                    Response.ResponseBuilder response = Response.ok(objectBytes.asByteArray());
                    response.header("Content-Disposition", "attachment;filename=" + id);
                    response.header("Content-Type", objectBytes.response().contentType());
                    return Uni.createFrom().item(response.build());
                });

    }

    @GET
    @Path("user/{afm}/{id}/meta")
    public Uni<Exam> fetchUserExamFileMeta(@PathParam("afm") String afm, @PathParam("id") String id) {
        hasPermissions(afm);
        return ExamEntity
                .find("{'_id': ?1, 'examMeta.patientAfm': ?2}", new ObjectId(id), afm)
                .firstResult()
                .map(ExamResource::convert);
    }

    @GET
    @Path("user/{afm}/search")
    public Uni<List<Exam>> searchUserExams(
            @PathParam("afm") String afm,
            @QueryParam("type") List<ExamMeta.Type> types) {
        hasPermissions(afm);
        return ((!types.isEmpty()) ? ExamEntity.find("{'examMeta.patientAfm': ?1, 'examMeta.type': {$in: [?2]}}", afm, types)
                : ExamEntity.find("{'examMeta.patientAfm': ?1}", afm))
                .stream()
                .map(ExamResource::convert).collect().asList();
    }

    private String getAfm() {
        return tokenMicroProfile.getClaim("afm");
    }

    private void hasPermissions(final String patientAfm) {
        InvokeMethodRequest request = new InvokeMethodRequest("permissions", "permission/exists");
        request.setHttpExtension(HttpExtension.POST);
        request.setBody(new Permission(patientAfm, getAfm()));
        Boolean hasPermission = dapr.invokeMethod(request, new TypeRef<Boolean>() {
        });
        if (!hasPermission)
            throw new UnauthorizedException();
    }

    private static Exam convert(final ReactivePanacheMongoEntityBase examEntity) {
        return convert((ExamEntity) examEntity);
    }

    private static Exam convert(final ExamEntity examEntity) {
        if (examEntity == null)
            return null;
        return new Exam(
                examEntity.id.toHexString(),
                examEntity.examMeta,
                examEntity.fileMeta);
    }
}
