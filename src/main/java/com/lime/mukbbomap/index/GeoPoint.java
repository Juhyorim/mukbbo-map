package com.lime.mukbbomap.index;

/**
 * 좌표가 붙은 값 하나.
 *
 * <p>쿼드트리를 특정 엔티티에 묶지 않으려고 도입한 래퍼다. 트리는 위경도만 알면 되고,
 * 실제 담기는 값({@code value})이 {@link com.lime.mukbbomap.domain.Restaurant}든
 * 다른 무엇이든 신경 쓰지 않는다. 덕분에 엔티티를 수정하지 않아도 되고 재사용도 쉽다.
 *
 * @param <T> 담기는 값의 타입
 */
public record GeoPoint<T>(double lat, double lng, T value) {
}
