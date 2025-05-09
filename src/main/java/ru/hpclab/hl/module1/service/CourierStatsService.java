package ru.hpclab.hl.module1.service;

import org.springframework.stereotype.Service;
import ru.hpclab.hl.module1.client.CourierClient;
import ru.hpclab.hl.module1.client.DeliveryClient;
import ru.hpclab.hl.module1.client.ParcelClient;
import ru.hpclab.hl.module1.dto.*;
import ru.hpclab.hl.module1.service.cache.CourierCache;
import ru.hpclab.hl.module1.service.cache.CourierRedisCache;

import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourierStatsService {

    private final CourierClient courierClient;
    private final DeliveryClient deliveryClient;
    private final ParcelClient parcelClient;
    private final CourierRedisCache courierRedisCache;

    public CourierStatsService(
            CourierClient courierClient,
            DeliveryClient deliveryClient,
            ParcelClient parcelClient,
            CourierRedisCache courierRedisCache
    ) {
        this.courierClient = courierClient;
        this.deliveryClient = deliveryClient;
        this.parcelClient = parcelClient;
        this.courierRedisCache = courierRedisCache;
    }

    public List<CourierStatsDTO> getStatsForAllCouriers() {
        List<DeliveryDTO> deliveries = Optional.ofNullable(deliveryClient.getAllDeliveries())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());

        Map<Long, String> courierNamesById = new HashMap<>();
        Map<String, Map<Month, Double>> statsByCourier = new HashMap<>();

        for (DeliveryDTO delivery : deliveries) {
            if (!"DELIVERED".equalsIgnoreCase(delivery.getStatus())) continue;

            ParcelDTO parcel = parcelClient.getById(delivery.getParcelId());
            if (parcel == null) continue;

            Long courierId = delivery.getCourierId();
            CourierDTO courier = courierNamesById.containsKey(courierId)
                    ? null
                    : getCourierById(courierId);

            if (courier != null) {
                courierNamesById.putIfAbsent(courierId, courier.getFullName());
            }

            String courierName = courierNamesById.get(courierId);
            if (courierName == null) continue;

            Month month = delivery.getDeliveryDate().getMonth();
            double weight = parcel.getWeight();

            statsByCourier
                    .computeIfAbsent(courierName, k -> new HashMap<>())
                    .merge(month, weight, Double::sum);
        }

        return statsByCourier.entrySet().stream()
                .map(e -> new CourierStatsDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private CourierDTO getCourierById(Long id) {
        if (!courierRedisCache.contains(id)) {
            CourierDTO courier = courierClient.getCourierById(id);
            courierRedisCache.put(courier);
            return courier;
        }
        return courierRedisCache.get(id).orElseThrow();
    }
}
