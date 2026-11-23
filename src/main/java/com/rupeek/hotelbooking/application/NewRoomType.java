package com.rupeek.hotelbooking.application;

import com.rupeek.hotelbooking.domain.common.Money;

public record NewRoomType(String name, int maxOccupancy, int totalUnits, Money pricePerNight) {
}
