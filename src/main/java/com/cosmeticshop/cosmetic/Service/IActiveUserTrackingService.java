package com.cosmeticshop.cosmetic.Service;

import com.cosmeticshop.cosmetic.Dto.ActiveUserTrafficResponse;

public interface IActiveUserTrackingService {
    void markUserAsActive(String username);

    ActiveUserTrafficResponse getActiveUserTraffic();
}
