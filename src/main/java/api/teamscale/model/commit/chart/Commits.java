package api.teamscale.model.commit.chart;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "revision",
        "commit",
        "message",
        "author",
        "repositoryIdentifier",
        "commitTypes",
        "mail",
        "numAddedFiles",
        "numChangedFiles",
        "numDeletedFiles"
})
@Generated("jsonschema2pojo")
public class Commits {

    @JsonProperty("revision")
    private String revision;
    @JsonProperty("commit")
    private CommitInfo commit;
    @JsonProperty("message")
    private String message;
    @JsonProperty("author")
    private String author;
    @JsonProperty("repositoryIdentifier")
    private String repositoryIdentifier;
    @JsonProperty("commitTypes")
    private List<String> commitTypes;
    @JsonProperty("mail")
    private String mail;
    @JsonProperty("numAddedFiles")
    private Integer numAddedFiles;
    @JsonProperty("numChangedFiles")
    private Integer numChangedFiles;
    @JsonProperty("numDeletedFiles")
    private Integer numDeletedFiles;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("revision")
    public String getRevision() {
        return revision;
    }

    @JsonProperty("revision")
    public void setRevision(String revision) {
        this.revision = revision;
    }

    @JsonProperty("commit")
    public CommitInfo getCommit() {
        return commit;
    }

    @JsonProperty("commit")
    public void setCommit(CommitInfo commit) {
        this.commit = commit;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("author")
    public String getAuthor() {
        return author;
    }

    @JsonProperty("author")
    public void setAuthor(String author) {
        this.author = author;
    }

    @JsonProperty("repositoryIdentifier")
    public String getRepositoryIdentifier() {
        return repositoryIdentifier;
    }

    @JsonProperty("repositoryIdentifier")
    public void setRepositoryIdentifier(String repositoryIdentifier) {
        this.repositoryIdentifier = repositoryIdentifier;
    }

    @JsonProperty("commitTypes")
    public List<String> getCommitTypes() {
        return commitTypes;
    }

    @JsonProperty("commitTypes")
    public void setCommitTypes(List<String> commitTypes) {
        this.commitTypes = commitTypes;
    }

    @JsonProperty("mail")
    public String getMail() {
        return mail;
    }

    @JsonProperty("mail")
    public void setMail(String mail) {
        this.mail = mail;
    }

    @JsonProperty("numAddedFiles")
    public Integer getNumAddedFiles() {
        return numAddedFiles;
    }

    @JsonProperty("numAddedFiles")
    public void setNumAddedFiles(Integer numAddedFiles) {
        this.numAddedFiles = numAddedFiles;
    }

    @JsonProperty("numChangedFiles")
    public Integer getNumChangedFiles() {
        return numChangedFiles;
    }

    @JsonProperty("numChangedFiles")
    public void setNumChangedFiles(Integer numChangedFiles) {
        this.numChangedFiles = numChangedFiles;
    }

    @JsonProperty("numDeletedFiles")
    public Integer getNumDeletedFiles() {
        return numDeletedFiles;
    }

    @JsonProperty("numDeletedFiles")
    public void setNumDeletedFiles(Integer numDeletedFiles) {
        this.numDeletedFiles = numDeletedFiles;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
