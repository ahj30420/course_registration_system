package com.project.course_registration_system.common.util;

import java.net.URI;
import lombok.NoArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

@NoArgsConstructor
public class UrlCreator {

    public static URI createUri(String defaultUrl, Long resourceId) {
        return UriComponentsBuilder.newInstance()
                .path(defaultUrl + "/{resource-id}")
                .buildAndExpand(resourceId)
                .toUri();
    }

}
