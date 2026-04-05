package yowyob.comops.api.kernel.adapter.out.search;

import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "iwm-organization-search-v1", createIndex = true)
public record OrganizationSearchDocument(
        @Id UUID id,
        @Field(type = FieldType.Keyword) UUID tenantId,
        @Field(type = FieldType.Keyword) UUID businessActorId,
        @Field(type = FieldType.Keyword) String code,
        @Field(type = FieldType.Text) String legalName,
        @Field(type = FieldType.Text) String displayName,
        @Field(type = FieldType.Keyword) String organizationType) {
}
