// Exceptions/ErrorResponse.java
package com.example.demo.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Setter
@Getter
@NoArgsConstructor  // ✅ allows new WorkerSchema()
@AllArgsConstructor // ✅ required by @Builder when NoArgs is added
public class WorkerSchema {
    private String ServerId;
    private String image_url;
    private Long image_id;
    private Long Uer_id;
    private String target_lang;
}