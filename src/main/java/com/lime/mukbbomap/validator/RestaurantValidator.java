package com.lime.mukbbomap.validator;

import com.lime.mukbbomap.dto.RestaurantDto.CreateRequest;
import com.lime.mukbbomap.dto.RestaurantDto.SearchRequest;
import com.lime.mukbbomap.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class RestaurantValidator {
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{2,3}-\\d{3,4}-\\d{4}$");

    /**
     * CreateRequest 검증
     */
    public void validateCreateRequest(CreateRequest request) {
        List<String> errors = new ArrayList<>();

        // 이름 검증
        validateName(request.name(), errors);

        // 카테고리 검증
        validateCategory(request.category(), errors);

        // 설명 검증
        validateDescription(request.description(), errors);

        // 주소 검증
        validateAddress(request.address(), errors);

        // 좌표 검증
        validateCoordinates(request.latitude(), request.longitude(), errors);

        // 전화번호 검증
        validatePhoneNumber(request.phoneNumber(), errors);

        // 평점 검증
        validateRating(request.rating(), errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(", ", errors));
        }
    }

    /**
     * SearchRequest 검증
     */
    public void validateSearchRequest(SearchRequest request) {
        List<String> errors = new ArrayList<>();

        // 좌표 검증
        validateCoordinates(request.latitude(), request.longitude(), errors);

        // 반경 검증
        validateRadius(request.radiusInMeters(), errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(", ", errors));
        }
    }

    private void validateName(String name, List<String> errors) {
        if (isBlank(name)) {
            errors.add("맛집 이름은 필수입니다");
            return;
        }
        if (name.length() > 100) {
            errors.add("맛집 이름은 100자 이내여야 합니다");
        }
    }

    private void validateCategory(String category, List<String> errors) {
        if (isBlank(category)) {
            errors.add("카테고리는 필수입니다");
            return;
        }
        if (category.length() > 50) {
            errors.add("카테고리는 50자 이내여야 합니다");
        }
    }

    private void validateDescription(String description, List<String> errors) {
        if (description != null && description.length() > 500) {
            errors.add("설명은 500자 이내여야 합니다");
        }
    }

    private void validateAddress(String address, List<String> errors) {
        if (isBlank(address)) {
            errors.add("주소는 필수입니다");
            return;
        }
        if (address.length() > 200) {
            errors.add("주소는 200자 이내여야 합니다");
        }
    }

    private void validateCoordinates(Double latitude, Double longitude, List<String> errors) {
        if (latitude == null) {
            errors.add("위도는 필수입니다");
        } else if (latitude < -90.0 || latitude > 90.0) {
            errors.add("위도는 -90 이상 90 이하여야 합니다");
        }

        if (longitude == null) {
            errors.add("경도는 필수입니다");
        } else if (longitude < -180.0 || longitude > 180.0) {
            errors.add("경도는 -180 이상 180 이하여야 합니다");
        }
    }

    private void validatePhoneNumber(String phoneNumber, List<String> errors) {
        if (phoneNumber != null && !PHONE_PATTERN.matcher(phoneNumber).matches()) {
            errors.add("전화번호 형식이 올바르지 않습니다 (예: 02-1234-5678)");
        }
    }

    private void validateRating(Double rating, List<String> errors) {
        if (rating != null && (rating < 0.0 || rating > 5.0)) {
            errors.add("평점은 0.0 이상 5.0 이하여야 합니다");
        }
    }

    private void validateRadius(Integer radiusInMeters, List<String> errors) {
        if (radiusInMeters == null) {
            errors.add("반경은 필수입니다");
            return;
        }
        if (radiusInMeters < 100) {
            errors.add("반경은 최소 100m 이상이어야 합니다");
        }
        if (radiusInMeters > 10000) {
            errors.add("반경은 최대 10km 이하여야 합니다");
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}