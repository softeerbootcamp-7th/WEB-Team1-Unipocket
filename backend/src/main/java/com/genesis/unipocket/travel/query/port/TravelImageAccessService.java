package com.genesis.unipocket.travel.query.port;

import java.time.Duration;

public interface TravelImageAccessService {

	boolean isTravelImageKey(String imageKey);

	boolean exists(String imageKey);

	String issueGetPath(String imageKey, Duration expiration);
}
