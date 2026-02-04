package com.inventrik.digitalestore.service.certificate;

import java.util.Optional;

public interface ChallengeService {
    String createChallenge(String userId);
    Optional<ChallengeData> getChallenge(String challengeId);
    boolean markChallengeUsed(String challengeId);
    void deleteChallenge(String challengeId);
    int cleanupExpiredChallenges();

    class ChallengeData {
        private final String userId;
        private final long createdAt;
        private boolean used;

        public ChallengeData(String userId, long createdAt) {
            this.userId = userId;
            this.createdAt = createdAt;
            this.used = false;
        }

        public String getUserId() {
            return userId;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public boolean isUsed() {
            return used;
        }

        public void setUsed(boolean used) {
            this.used = used;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > 10000;
        }

        public boolean isValid() {
            return !used && !isExpired();
        }
    }
}
