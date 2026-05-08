package com.yu.search.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(indexName = "#{@searchProperties.indexName}")
public class SearchItemDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String subTitle;

    @Field(type = FieldType.Keyword)
    private String image;

    @Field(type = FieldType.Long)
    private Long price;

    @Field(type = FieldType.Long)
    private Long originalPrice;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Long)
    private Long shopId;

    @Field(type = FieldType.Keyword)
    private String shopName;

    @Field(type = FieldType.Integer)
    private Integer isSelf;

    @Field(type = FieldType.Keyword)
    private String shippingType;

    @Field(type = FieldType.Integer)
    private Integer shippingFee;

    @Field(type = FieldType.Integer)
    private Integer freeShippingThreshold;

    @Field(type = FieldType.Keyword)
    private String shippingDesc;

    @Field(type = FieldType.Integer)
    private Integer sold;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Integer)
    private Integer positiveRate;

    @Field(type = FieldType.Double)
    private BigDecimal avgScore;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;
}
