package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

import javax.validation.ValidationException;
import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl,
                             RestTemplateBuilder restTemplateBuilder) {
        super(restTemplateBuilder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build()
        );
    }

    public ResponseEntity<Object> addNew(ItemRequestDto itemRequestDto, Long userId) {
        return post("", userId, itemRequestDto);
    }

    public ResponseEntity<Object> getAllOwn(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAllByPages(Long userId, Integer from, Integer size) {
        if (from % size != 0) {
            throw new ValidationException("Element index and page size mismatch!");
        }
        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректный ввод данных");
        }
        Map<String, Object> parameters = Map.of(
                "size", size,
                "from", from);
        return get("/all?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getById(Long requestId, Long userId) {
        String path = "/" + requestId;
        return get(path, userId);
    }
}