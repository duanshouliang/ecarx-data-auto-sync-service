package com.ecarx.cloud.elasticsearch.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchEvent {
    private String topic;
    private Map<String, Object> conditions;
}
